package com.HOIIVUtils.hoi4utils.map.seed;

import com.HOIIVUtils.hoi4utils.map.AbstractMapGeneration;
import com.HOIIVUtils.hoi4utils.map.Heightmap;
import com.HOIIVUtils.hoi4utils.map.MapPoint;
import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SeedProbabilityMap_GPU extends AbstractMapGeneration {
	double[][] seedProbabilityMap;      // y, x
	double[] cumulativeProbabilities;   // per x, inclusive of previous maps
//	double[] partialSums;               // per x, sum of all x probabilities.
	final Heightmap heightmap;
	final int width;
	final int height;
	int pointNumber = 0;
	ForkJoinPool fjpool;
	double probabilitySum = 0;

	public SeedProbabilityMap_GPU(Heightmap heightmap) {
		this.heightmap = heightmap;
		this.width = heightmap.width();
		this.height = heightmap.height();
		seedProbabilityMap = new double[height][width]; // y, x
		cumulativeProbabilities = new double[height];
		fjpool = new ForkJoinPool();

		initializeProbabilityMap();
	}

	private void initializeProbabilityMap() {
		final double[][] _seedMap = new double[height][width];
		final double[] _cumulativeProbabilities = new double[height];
		final byte[][] _heightmap = heightmap.snapshot();
		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int x = getGlobalId(0);
				int y = getGlobalId(1);
				if (x < getGlobalSize(0) && y < getGlobalSize(1)) {
					int xyheight = _heightmap[y][x] & 0xFF;
					int type = provinceType(xyheight); // is this bad here? probably.
					_seedMap[y][x] = type == 0 ? 1.15 : 0.85;
//					result[i] = inA[i] + inB[i];
					if (x == 0) {
						/* once per row */
						_cumulativeProbabilities[y] = 0.0;
					}
				}
			}
		};

		// The group size must always be a ‘factor’ of the global range. So globalRange % groupSize == 0
		// [prime number run bad with this impl, but thats fine bc we use specific multiples of 2, 4, 8 for the maps]
		Range range = Range.create2D(width, height);
		kernel.execute(range);
		kernel.dispose();
		seedProbabilityMap = _seedMap;
		cumulativeProbabilities = _cumulativeProbabilities;

		normalize();
	}

	public void normalize() {
		int length = seedProbabilityMap.length;
		double sum;
		if (probabilitySum == 0) {
			sum = mapReduce2D(seedProbabilityMap);
		} else {
			sum = probabilitySum;
		}
		if (sum == 0) {
			return;
		}

		final double[] _cumulativeProbabilities = new double[height];
		final double[][] _map = seedProbabilityMap.clone();
		Kernel cumulativeKernel = new Kernel() {
			@Override
			public void run() {
				int y = getGlobalId();
				_cumulativeProbabilities[y] = 0;
				for (int i = 0; i < width; i++) {
					_cumulativeProbabilities[y] = _cumulativeProbabilities[y] + _map[y][i];
				}
			}
		};
		cumulativeKernel.execute(Range.create(_cumulativeProbabilities.length));
		cumulativeKernel.dispose();
		cumulativeProbabilities[0] = _cumulativeProbabilities[0];
		for (int i = 1; i < height; i++) {
			cumulativeProbabilities[i] += cumulativeProbabilities[i - 1] + _cumulativeProbabilities[i];
		}
		System.out.println("c: " + Arrays.toString(cumulativeProbabilities));

		double sumRecip = 1 / sum;
		seedProbabilityMap = multiplyAll(seedProbabilityMap, sumRecip);
		probabilitySum = 1.0;
	}

	/**
	 * Based on example in aparapi examples
	 * @return
	 */
	private double mapReduce2D(double[][] matrix) {
		int size = matrix.length * matrix[0].length;
		final double[] _sdata = new double[size];
		final int count = 3;
		System.out.println("map reduce array size: " + size);

        System.arraycopy(Stream.of(matrix)
		        .parallel()
		        .flatMapToDouble(DoubleStream::of)
		        .toArray(),
		        0, _sdata, 0, size);

		// this will hold our values between the phases.
		double[][] totals = new double[count][size];

		/*
		map phase
		 */
		final double[][] kernelTotals = totals;
		Kernel mapKernel = new Kernel() {
			@Override
			public void run() {
				int gid = getGlobalId();
				double value = _sdata[gid];
				for (int index = 0; index < count; index++) {
					if (value >= (double) index / count && value < (double) (index + 1) / count)
						kernelTotals[index][gid] = 1.0;
				}
			}
		};
		mapKernel.execute(Range.create(size));
		System.out.println(mapKernel.getTargetDevice());
		mapKernel.dispose();
		totals = kernelTotals;

		/*
		reduce phase
		 */
		while (size > 1) {
			int nextSize = size / 2;
			final double[][] currentTotals = totals;
			final double[][] nextTotals = new double[count][nextSize];
			Kernel reduceKernel = new Kernel() {
				@Override
				public void run() {
					int gid = getGlobalId();
					for (int index = 0; index < count; index++) {
						nextTotals[index][gid] = currentTotals[index][gid * 2] + currentTotals[index][gid * 2 + 1];
					}
				}
			};
			reduceKernel.execute(Range.create(nextSize));
			reduceKernel.dispose();

			totals = nextTotals;
			size = nextSize;
		}
		assert size == 1;

		double[] result = Arrays.stream(totals)
				.parallel()
				.mapToDouble(row -> row[0])
				.toArray();

//		System.out.println(Arrays.toString(result));
		return result[0] + result[1] + result[2];
	}

	private double[][] multiplyAll(double[][] matrix, final double value) {
		final int width = matrix.length;
		final int height = matrix[0].length;
		final double[][] in = matrix.clone();
		final double[][] out = new double[width][height];
		Kernel multKernal = new Kernel() {
			@Override
			public void run() {
				int x = getGlobalId(0);
				int y = getGlobalId(1);
				out[x][y] = in[x][y] * value;
			}
		};
		multKernal.execute(Range.create2D(width, height));
		multKernal.dispose();
		return out;
	}

	public MapPoint getPoint(Random random) {
		normalize();

		double p = random.nextDouble();
		// Find the first index where cumulative probability >= p
		int y = findCumulativeProbabilityIndex(p);
		if (y == -1) {
			System.err.println("Index -1 in " + this);
//			Arrays.stream(cumulativeProbabilities).forEach(System.out::println);
			return null;
		}

		// Find the first MapPoint where cumulative probability >= p
		double cumulative = (y == 0) ? 0.0 : cumulativeProbabilities[y - 1];
		System.out.println("cumulative:" + cumulative);
		System.out.println("cumulative1:" + cumulativeProbabilities[y]);
		MapPoint mp = null;
		for (int x = 0; x < seedProbabilityMap[y].length; x++) {
			cumulative += seedProbabilityMap[y][x];
			if (cumulative >= p) {
				mp = new MapPoint(x, y, provinceType(heightmap.height_xy(x, y)));
				break;
			}
		}
		if (mp == null) {
			System.out.println("bad probability? " + p);    // todo
			return getPoint(random);
		}
		System.out.println(++pointNumber);
		/* adjust probabilities */
		//adjustProbabilitiesInRadius(mp, 9); instead, ? ->
		setSeedProbabilityMapYX(mp.y, mp.x, 0.0);
		return mp;
	}

	private void setSeedProbabilityMapYX(int y, int x, double v) {
		// todo optimize?
		probabilitySum -= seedProbabilityMap[y][x];
		seedProbabilityMap[y][x] = v;
		probabilitySum += v;
	}

	private void adjustProbabilitiesInRadius(MapPoint mp, int r) {
		// todo parallelize
		int minY = Math.max(mp.y - r, 0);
		int maxY = Math.min(mp.y + r, seedProbabilityMap.length - 1);
		int minX = Math.max(mp.x - r, 0);
		int maxX = Math.min(mp.x + r, seedProbabilityMap[0].length - 1);
		double[] distanceModifiers = new double[r + 1]; // 0 to r
		distanceModifiers[0] = 0.0;
		for (int i = 1; i <= r; i++) {
			distanceModifiers[i] = (double) (i - 0.2) / r; // Normalize the value to be between 0 and 1
		}

		//int rSquared = r * r;
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				double distance = mp.distance(x, y);
				// Check if the distance is within the specified radius
				if (distance <= r) {
					// Adjust the probability at position (y, x)
					// todo optimize??
					setSeedProbabilityMapYX(y, x, seedProbabilityMap[y][x] *= distanceModifiers[(int) distance]);
				}
			}
		}
	}

	private int findCumulativeProbabilityIndex(double p) {
		return IntStream.range(0, cumulativeProbabilities.length)
				.filter(i -> Double.compare(cumulativeProbabilities[i], p) >= 0)
				.findFirst()
				.orElse(-1);
	}

}