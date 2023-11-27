package hoi4utils.map.province;

import hoi4utils.map.AbstractMapGeneration;
import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;
import hoi4utils.map.seed.*;
import hoi4utils.map.values;
import opensimplex2.OpenSimplex2;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RejectedExecutionException;

import static hoi4utils.map.province.ProvinceGeneration.ForkColorDetermination.OFFSET_NOISE_MODIFIER;
import static hoi4utils.map.values.generationType;

public class ProvinceGeneration extends AbstractMapGeneration {
	public static final double NOISE_POLLING_FACTOR = 0.025;     // 0.005        // 0.025
	public BorderMap stateBorderMap; 		// heightmap of preferred borders
	private ProvinceMap provinceMap;
	private ProvinceMapPointsList points;
	private SeedsSet<MapPoint> seeds;

	private BorderMapping<MapPoint> stateMapList;
	private Heightmap heightmap;
	private SeedGeneration seedGeneration;
	/** threadLimit = 0: max (use all processors/threads). */
	private int threadLimit = 0;
	boolean adjProvinceByGraphConnectivity = false;

	public static void main(String[] args) {
		ProvinceGeneration provinceGeneration = new ProvinceGeneration();
		provinceGeneration.generate(values.heightmapName);

		try {
			ImageIO.write(provinceGeneration.provinceMap, "bmp", new File("output.bmp"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void generate() {
		/* create new image (map) */
		provinceMap = new ProvinceMap(heightmap);
		stateBorderMap = loadStateBorderMap(values.stateBordersName); // ! todo temp!!
		initLists();        // todo like hmmm more classes not sure

		/* seeds generation */
		SeedGeneration<MapPoint> seedGeneration;
		if (generationType == ProvinceGenerationType.GRID_SEED) {
			seedGeneration = new GridSeedGeneration(heightmap);
		} else {
			seedGeneration = new ProbabilisticSeedGeneration(heightmap);
		}
		seedGeneration.generate(stateMapList, stateBorderMap);

		executeProvinceDetermination();
	}

	private void generate(Heightmap heightmap) {
		this.heightmap = heightmap;     // todo can we optimize Heightmap since grayscale?
		generate();
	}

	private void generate(String heightmapName) {
		heightmap = loadHeightmap(heightmapName);
		generate();
	}

	private void initLists() {
		/* initialize points list */
		points = new ProvinceMapPointsList(heightmap.width(), heightmap.height());

		/* initialize mapping of seeds to states (regions for purposes of province generation) */
		// TODO: optimization may be possible
		stateMapList = new BorderMapping<>();
	}

	/**
	 * values - load heightmap, states map
	 */
	private Heightmap loadHeightmap(String heightmapName) {
		try {
			BufferedImage temp = ImageIO.read(new File(heightmapName));
			return new Heightmap(temp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private BorderMap loadStateBorderMap(String stateBorderMapName) {
		try {
			return stateBorderMap = new BorderMap(ImageIO.read(new File(stateBorderMapName)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void executeProvinceDetermination() {
		ForkColorDetermination forkColorDetermination = new ForkColorDetermination(provinceMap, heightmap);
		ForkJoinPool forkJoinPool;
		if (threadLimit == 0) {
			forkJoinPool = new ForkJoinPool();
		} else {
			forkJoinPool = new ForkJoinPool(threadLimit);
		}
		try {
			forkJoinPool.invoke(forkColorDetermination);
		}
		catch(NullPointerException exc) {
			exc.printStackTrace();
		}
		catch(RejectedExecutionException exc) {
			exc.printStackTrace();
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
		if (adjProvinceByGraphConnectivity) {
			ForkProvinceConnectivityDetermination forkProvinceConnectivityDetermination = new ForkProvinceConnectivityDetermination(points);
			forkJoinPool.invoke(forkProvinceConnectivityDetermination);
		}
	}

	private int offsetWithNoise(int offsetPotential, int seed, int x, int y) {
		double noise = simplexNoise2(seed, x, y, OFFSET_NOISE_MODIFIER);     //multiplier try (2.0f / offsetPotential)
//		long roundedNoise = Math.round(noise); // Round to the nearest integer        // may lead to more even distribution when int cast occurs.
		return (int) (offsetPotential * noise);
	}

	private float simplexNoise2(int seed, int x, int y) {
		return simplexNoise2(seed, x, y, 1);
	}

	private float simplexNoise2(int seed, int x, int y, float multiplier) {
		return OpenSimplex2.noise2(seed, x * NOISE_POLLING_FACTOR, y * NOISE_POLLING_FACTOR) * multiplier;
	}

	/**
	 * determines color from closest seed to point x,y
	 * @param x
	 * @param y
	 * @param seeds
	 * @return
	 */
	private static int determineColor(int x, int y, final Collection<MapPoint> seeds) {
		// (default white)
		int nearestColor = values.rgb_white;     // color of nearest seed (int value)
		int dist = Integer.MAX_VALUE;            // select a big number

		// todo stream operation?
		for (MapPoint point : seeds) {
			// calculate the difference in x and y direction
			int xdiff = point.x - x;
			int ydiff = point.y - y;

			// calculate current squared Euclidean distance, for comparing only
			int cdist = xdiff * xdiff + ydiff * ydiff;

			if (cdist < dist) {
				nearestColor = point.rgb();
				dist = cdist;
			}
		}

		return nearestColor;
	}

	private int determineColor(int x, int xOffset, int y, int yOffset, Collection<MapPoint> mapPoints) {
		return determineColor(x + xOffset, y + yOffset, mapPoints);
	}

	/**
	 * Pixel color determination using {@link RecursiveAction} for multithreading efficiency.
	 *
	 * @see RecursiveAction
	 * @see OpenSimplex2
	 */
	public class ForkColorDetermination extends RecursiveAction {

		/**
		 * Auto-generated serialVersionUID
		 */
		@Serial
		private static final long serialVersionUID = 7925866053687723919L;
		/** float datatype is used by simplex noise, and may improve performance over double */
		public static final float OFFSET_NOISE_MODIFIER = 1.0f;
		protected static int splitThreshold = 16;       // was 8
		private static final int seed;

		/**
		 * y-value to start at (inclusive)
		 */
		private final int startY;

		/**
		 * y-value to go until (exclusive)
		 */
		private final int endY;

		/**
		 * number of y-values to work with
		 */
		private final int dy;

		/**
		 * simplex noise to offset color determination
		 */
		private OpenSimplex2 noise;
		private final ProvinceMap provinceMap;
		private final Heightmap heightmap;

		static {
			Random random = new Random();
			seed = random.nextInt();
		}

//		private Iterator<Map.Entry<ProvinceMapPoint, Integer>> seedsRGBMapIterator;

		/**
		 * constructor (y set as 0 to imageHeight). Recommended constructor for initial initialization.
		 */
		public ForkColorDetermination(ProvinceMap provinceMap, Heightmap heightmap) {
			this(provinceMap, heightmap, 0, heightmap.height());
		}

		/**
		 * constructor
		 * // todo pass in prev fork color determination instead of province map, heightmap?
		 */
		public ForkColorDetermination(ProvinceMap provinceMap, Heightmap heightmap, int startY, int endY) {
			this.provinceMap = provinceMap;
			this.heightmap = heightmap;
			this.startY = startY;
			this.endY = endY;
			dy = endY - startY;
		}

		@Override
		protected void compute() {
			if (dy <= splitThreshold) {
				computeDirectly();
				return;
			}

			int split = dy / 2;

			invokeAll(new ForkColorDetermination(provinceMap, heightmap, startY, startY + split),
					new ForkColorDetermination(provinceMap, heightmap, startY + split, endY));
		}

		/**
		 * Determine color for each point
		 */
		protected void computeDirectly() {
			final int widthPerSeed = heightmap.width()  / values.numSeedsX;
			final int heightPerSeed = heightmap.height() / values.numSeedsY;
			final int offsetPotential = 4;
			System.out.println("run: " + startY + ", " + endY);

			try {
				for (int y = startY; y < endY; y++) {
					for (int x = 0; x < heightmap.width(); x++) {
						int rgb;
						int heightmapHeight = heightmap.xyHeight(x, y);
						int stateBorderValue = stateBorderMap.getRGB(x, y);
						int type = provinceType(heightmapHeight);

						int xOffset = offsetWithNoise(offsetPotential, seed, x, y);    //TODO work on values
						int yOffset = offsetWithNoise(offsetPotential, seed, x, y);
						rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));

						points.setRGB(x, y, rgb);
						provinceMap.setRGB(x, y, rgb);
					}
				}
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}

	}

	/**
	 * Province connectivity determination (->graph) using {@link RecursiveAction} for multithreading efficiency.
	 *
	 * @see RecursiveAction
	 */
	public class ForkProvinceConnectivityDetermination extends RecursiveAction {

		/** Auto-generated serialVersionUID */
		@Serial
		private static final long serialVersionUID = 9171676481286895487L;
		protected static int splitThreshold = 16;       // was 8
		private final int startY;
		private final int endY;
		private final int dy;
		private final ProvinceMapPointsList mapPoints;
		private static final Graph<MapPoint, DefaultEdge> mpGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
		private static final Graph<MapPoint, DefaultEdge> sync_mpGraph = new AsSynchronizedGraph<>(mpGraph);

		/**
		 * constructor (y set as 0 to height). Recommended constructor for initial initialization.
		 */
		public ForkProvinceConnectivityDetermination(ProvinceMapPointsList mapPoints) {
			this(mapPoints, 0, mapPoints.height());
		}

		/**
		 * constructor
		 * // todo pass in prev fork color determination instead of province map, heightmap?
		 */
		public ForkProvinceConnectivityDetermination(ProvinceMapPointsList mapPoints, int startY, int endY) {
			this.mapPoints = mapPoints;
			this.startY = startY;
			this.endY = endY;
			dy = endY - startY;
		}

		@Override
		protected void compute() {
			if (dy <= splitThreshold) {
				computeDirectly();
				return;
			}

			int split = dy / 2;

			invokeAll(new ForkProvinceConnectivityDetermination(mapPoints, startY, startY + split),
					new ForkProvinceConnectivityDetermination(mapPoints, startY + split, endY));
		}

		protected void computeDirectly() {
			try {
				for (int y = startY; y < endY; y++) {
					for (int x = 0; x < heightmap.width(); x++) {
						int currentType = mapPoints.get(x, y).type();
						connectNeighbors(x, y, currentType);
					}
				}
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		private void connectNeighbors(int x, int y, int currentType) {
			// Left neighbor
			connectIfApplicable(x, y, x - 1, y, currentType);
			// Right neighbor
			connectIfApplicable(x, y,x + 1, y, currentType);
			// Up neighbor
			connectIfApplicable(x, y, x, y - 1, currentType);
			// Down neighbor
			connectIfApplicable(x, y, x, y + 1, currentType);
		}

		private void connectIfApplicable(int x, int y, int nx, int ny, int currentType) {
			if (isValidCoordinate(nx, ny) && mapPoints.get(nx, ny).type() == currentType) {
				MapPoint p = mapPoints.get(x, y);
				MapPoint np = mapPoints.get(nx, ny);
				if (!sync_mpGraph.containsVertex(p)) {
					sync_mpGraph.addVertex(mapPoints.get(x, y));
				}
				if (!sync_mpGraph.containsVertex(np)) {
					sync_mpGraph.addVertex(mapPoints.get(nx, ny));
				}
				sync_mpGraph.addEdge(p, np);
				// todo optimize prev ?
			}
		}

		private boolean isValidCoordinate(int x, int y) {
			return x >= 0 && x < mapPoints.width() && y >= 0 && y < mapPoints.height();
		}

	}
}


