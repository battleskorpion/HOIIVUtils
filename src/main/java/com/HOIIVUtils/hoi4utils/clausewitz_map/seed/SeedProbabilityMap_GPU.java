package com.HOIIVUtils.hoi4utils.clausewitz_map.seed;

import com.HOIIVUtils.hoi4utils.clausewitz_map.SeedGenProperties;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.AbstractMapGeneration;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;
import com.aparapi.Kernel;
import com.aparapi.Range;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * SeedProbabilityMap, designed for GPU efficiency.
 */
public class SeedProbabilityMap_GPU extends AbstractMapGeneration {
	double[][] seedProbabilityMap;      // y, x
	double[][] cumulativeProbabilities;   // per x, inclusive of previous maps // todo this comment makes no sense
	final Heightmap heightmap;
	final int width;
	final int height;
	int pointNumber = 0;
//	ForkJoinPool fjpool;
	double probabilitySum = 0;
	SeedGenProperties properties;

	public SeedProbabilityMap_GPU(Heightmap heightmap, SeedGenProperties properties) {
		this.heightmap = heightmap;
		this.width = heightmap.width();
		this.height = heightmap.height();
		seedProbabilityMap = new double[height][width]; // y, x
		cumulativeProbabilities = new double[height][width];
		this.properties = properties;
//		fjpool = new ForkJoinPool();

		initializeProbabilityMap();
	}

	private void initializeProbabilityMap() {
		final double[][] _seedMap = new double[height][width];
//		final double[] _cumulativeProbabilities = new double[height];
		final byte[][] _heightmap = heightmap.snapshot();
		final int seaLevel = properties.seaLevel();   // necessary as can not use interface related call in kernel
		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int x = getGlobalId(0);
				int y = getGlobalId(1);
				if (x < getGlobalSize(0) && y < getGlobalSize(1)) {
					int xyheight = _heightmap[y][x] & 0xFF;
					//int type = provinceType(xyheight, seaLevel); // is this bad here? probably.
					int type = xyheight < seaLevel ? 0 : 1;
					_seedMap[y][x] = type == 0 ? 1.15 : 0.85;   // todo magic numbers :(
					//_seedMap[y][x] = 1.0;
//					result[i] = inA[i] + inB[i];
//					if (x == 0) {
//						/* once per row */
//						_cumulativeProbabilities[y] = 0.0;
//					}
				}
			}
		};

		// The group size must always be a ‘factor’ of the global range. So globalRange % groupSize == 0
		// [prime numbers run bad with this impl, but that's fine bc we use specific multiples of 2, 4, 8 for the maps]
		Range range = Range.create2D(width, height);
		kernel.execute(range);
		kernel.dispose();
		seedProbabilityMap = _seedMap;
//		cumulativeProbabilities = _cumulativeProbabilities;

		normalize();    // will calc cumulative probabilities
	}

	public void normalize() {
		double sum;
		if (probabilitySum == 0) {
			System.out.println("t1");
			sum = mapReduce2D(seedProbabilityMap);
		} else {
			System.out.println("t2");
			sum = probabilitySum;
		}
		if (sum == 0) {
			System.out.println("t3");
			probabilitySum = sum;
			return;
		}
		else if (sum == 1) {
			System.out.println("t4");
			cumulativeProbabilities = cumulativeReduce2D(seedProbabilityMap);
			probabilitySum = sum;
			return;
		}

		double sumRecip = 1 / sum;
		System.out.println("Sum recip: " + sumRecip);
		seedProbabilityMap = multiplyAll(seedProbabilityMap, sumRecip);
		probabilitySum = 1.0;
		cumulativeProbabilities = cumulativeReduce2D(seedProbabilityMap);
	}

	/**
	 * Based on example in aparapi examples
	 * @return
	 */
	private double mapReduce2D(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		int size = rows * cols;
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
		System.out.println("map reduce array size: " + size);
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

	/**
	 * Apply cumulative reduce operation to a 2D matrix.
	 *
	 * @param matrix The input 2D matrix.
	 * @return The result of the cumulative reduce operation.
	 */
	private double[][] cumulativeReduce2D(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		int size = rows * cols;
		final double[] _sdata = new double[size];
		//final int count = 3;        // todo 3? no.

		// Flatten the matrix into a 1D array
		System.arraycopy(Stream.of(matrix)
				.parallel()
				.flatMapToDouble(Arrays::stream)
				.toArray(),
				0, _sdata, 0, size);
		System.out.println("cumulative reduce array size: " + size);
		System.out.println("arr:" + _sdata[0] + ", " + _sdata[1]);

		// Initialize totals array
		double[][] values = new double[rows][cols];

		/*
		reduce phase
		if gid % 2^(index + 1) >= 2^(index)
		add value[gid - ([gid % 2^(index)] + 1)] to value[gid]
		~12.8m array -> 24 iterations
		 */
		double[] cumulativeTotals = _sdata;
		for (int i = 0;; i++) {
			final double[] currentTotals = cumulativeTotals;
			final double[] nextTotals = currentTotals.clone();
			final int pow_2_i = 1 << i;

			Kernel reduceKernel = new Kernel() {
				@Override
				public void run() {
					int gid = getGlobalId();
					if (gid % (pow_2_i << 1) >= pow_2_i)
						nextTotals[gid] += currentTotals[gid - (gid % pow_2_i + 1)];
					else nextTotals[gid] += 0;
				}
			};
			reduceKernel.execute(Range.create(size));
			reduceKernel.dispose();
			cumulativeTotals = nextTotals;
			System.out.println("reducing iteration: " + i);
			System.out.println("reducing arr: " + cumulativeTotals[0] + ", " + cumulativeTotals[1] + ",.. " + cumulativeTotals[size - 1]);
			if (pow_2_i >= size) break;
		}

//		// Reconstruct the result matrix
		double[][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
            System.arraycopy(cumulativeTotals, i * cols, result[i], 0, cols);
		}

		// Uncomment the next line if you want to print the result
		//System.out.println(Arrays.deepToString(result));
		System.out.println("arr res:" + result[0][0] + ", " + result[0][1]);
		return result;
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
		Point cumulativeP = findCumulativeProbabilityIndex(p);
		if (cumulativeP == null) {
			System.err.println("Cumulative probability index null " + this);
			System.out.println("bad probability? " + p);
//			Arrays.stream(cumulativeProbabilities).forEach(System.out::println);
			return null;
		}

		// Find the first MapPoint where cumulative probability >= p
		System.out.println("cumulative:" + cumulativeP + ", " + cumulativeProbabilities[cumulativeP.y][cumulativeP.x]);
//		System.out.println("cumulative1:" + Arrays.deepToString(cumulativeProbabilities));
		MapPoint mp = new MapPoint(cumulativeP.x, cumulativeP.y, provinceType(heightmap.height_xy(cumulativeP.x, cumulativeP.y),
				properties.seaLevel()));
//		if (mp == null) {
//			System.out.println("bad probability? " + p);    // todo
//			return getPoint(random);
//		}
		System.out.println(++pointNumber);
		/* adjust probabilities */
		//adjustProbabilitiesInRadius(mp, 9); instead, ? ->
		setSeedProbabilityMapYX(mp.y, mp.x, 0.0);
		return mp;
	}

	private void setSeedProbabilityMapYX(int y, int x, double v) {
		// todo optimize?
		System.out.println("psum " + probabilitySum);
		probabilitySum -= seedProbabilityMap[y][x];
		seedProbabilityMap[y][x] = v;
		probabilitySum += v;
		System.out.println("psum " + probabilitySum);
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

	public Point findCumulativeProbabilityIndex(double p) {
//		Optional<Point> result = IntStream.range(0, cumulativeProbabilities.length)
//				.boxed()
//				.flatMap(i -> IntStream.range(0, cumulativeProbabilities[i].length)
//						.filter(j -> Double.compare(cumulativeProbabilities[i][j], p) >= 0)
//						.mapToObj(j -> new Point(i, j)))
//				.findFirst();
//
//		return result.orElse(null);
		for (int y = 0; y < cumulativeProbabilities.length; y++) {
			for (int x = 0; x < cumulativeProbabilities[y].length; x++) {
				if (Double.compare(cumulativeProbabilities[y][x], p) >= 0) {
					return new Point(x, y);
				}
			}
		}
		return null;
	}
}