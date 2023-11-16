package hoi4utils.map.seed;

import hoi4utils.map.AbstractMapGeneration;
import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class SeedProbabilityMap extends AbstractMapGeneration {
	double[][] seedProbabilityMap;
	double[] cumulativeProbabilities; // per map, inclusive of previous maps
	final Heightmap heightmap;

	public SeedProbabilityMap(Heightmap heightmap) {
		this.heightmap = heightmap;

		seedProbabilityMap = new double[heightmap.getHeight()][heightmap.getWidth()]; // y, x
		cumulativeProbabilities = new double[(heightmap.getHeight())];
		initializeProbabilityMap();
	}

	private void initializeProbabilityMap() {
		for (int yi = 0; yi < heightmap.getHeight(); yi++) {
			for (int xi = 0; xi < heightmap.getWidth(); xi++) {
				double p = 1.0;
				int height = (heightmap.getRGB(xi, yi) >> 16) & 0xFF;
				int type = provinceType(height);

				/* probability */
				p *= type == 0 ? 1.15 : 0.85;

				seedProbabilityMap[yi][xi] = p;
			}
			cumulativeProbabilities[yi] = 0.0;
		}
		normalize();
	}

	public void normalize() {
		int length = seedProbabilityMap.length;
		ForkJoinPool pool = new ForkJoinPool();
		double sum = pool.invoke(new ProbabilitySumPerXTask(0, length));
		if (sum == 0) {
			return;
		}

		pool.invoke(new CumulativeProbabilityPerXTask(0, length, sum));
		for (int i = 1; i < cumulativeProbabilities.length; i++) {
			cumulativeProbabilities[i] += cumulativeProbabilities[i - 1];
		}
	}

	public MapPoint getPoint(Random random) {
		normalize();

		double p = random.nextDouble();
		// Find the first index where cumulative probability >= p
		int y = findCumulativeProbabilityIndex(p);
		if (y == -1) {
			System.err.println("Index -1 in " + this);
			Arrays.stream(cumulativeProbabilities).forEach(System.out::println);
			return null;
		}

		// Find the first MapPoint where cumulative probability >= p
		double cumulative = (y == 0) ? 0.0 : cumulativeProbabilities[y - 1];
		System.out.println(cumulative);
		MapPoint mp = null;
		for (int x = 0; x < seedProbabilityMap[y].length; x++) {
			cumulative += seedProbabilityMap[y][x];
			if (cumulative >= p) {
				mp = new MapPoint(x, y, provinceType((heightmap.getRGB(x, y) >> 16) & 0xFF));
				break;
			}
		}
		if (mp == null) {
			//System.out.println(cumulativeProbabilities[heightmap.getHeight() - 1]);
			System.out.println("bad probability? " + p);    // todo
			return getPoint(random);
		}
		return mp;
	}

	private int findCumulativeProbabilityIndex(double p) {
		return IntStream.range(0, cumulativeProbabilities.length)
				.filter(i -> Double.compare(cumulativeProbabilities[i], p) >= 0)
				.findFirst()
				.orElse(-1);
	}

	public class CumulativeProbabilityPerXTask extends RecursiveTask<Void> {
		private static final int THRESHOLD = 8; // Adjust based on your data size
		private final int start;        // inclusive
		private final int end;          // exclusive
		private final double pSum;      // probability sum

		public CumulativeProbabilityPerXTask(int start, int end, double pSum) {
			this.start = start;
			this.end = end;
			this.pSum = pSum;
		}

		@Override
		protected Void compute() {
			if (end - start > THRESHOLD) {
				int mid = (start + end) / 2;
				invokeAll(
						new CumulativeProbabilityPerXTask(start, mid, pSum),
						new CumulativeProbabilityPerXTask(mid, end, pSum)
				);
			} else {
				cumulativeProbability(start, end);
			}
			return null;
		}

		private void cumulativeProbability(int start, int end) {
			final double recipSum = 1.0 / pSum;

			for (int i = start; i < end; i++) {
				double cumulativeProbability = 0.0;
				for (int j = 0; j < seedProbabilityMap[i].length; j++) {
					seedProbabilityMap[i][j] *= recipSum;
					cumulativeProbability += seedProbabilityMap[i][j];
				}
				cumulativeProbabilities[i] = cumulativeProbability;
			}
		}
	}

	public class ProbabilitySumPerXTask extends RecursiveTask<Double> {
		private static final int THRESHOLD = 4; // Adjust based on your data size
		private final int start;
		private final int end;

		public ProbabilitySumPerXTask(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		protected Double compute() {
			if (end - start > THRESHOLD) {
				int mid = (start + end) / 2;
				var left = new ProbabilitySumPerXTask(start, mid);
				var right = new ProbabilitySumPerXTask(mid, end);

				invokeAll(left, right);

				return left.join() + right.join();
			} else {
				return computePartialSum();
			}
		}

		private double computePartialSum() {
			double sum = 0.0;
			for (int i = start; i < end; i++) {
				for (double v : seedProbabilityMap[i]) {
					sum += v;
				}
			}
			return sum;
		}
	}

}