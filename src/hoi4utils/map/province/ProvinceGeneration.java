package hoi4utils.map.province;

import hoi4utils.map.*;
import opensimplex2.OpenSimplex2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

	//	private static HashMap<ProvinceMapPoint, Integer> stateSeedsMap;
	private BorderMapping<MapPoint> stateMapList;
	private Heightmap heightmap;
	private SeedGeneration seedGeneration;

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
		SeedGeneration seedGeneration;
		if (generationType == ProvinceGenerationType.GRID_SEED) {
			seedGeneration = new GridSeedGeneration(heightmap);
		} else {
			seedGeneration = new DynamicSeedGeneration(heightmap);
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
		points = new ProvinceMapPointsList(heightmap.getWidth(), heightmap.getHeight());

		/* initialize mapping of seeds to states (regions for purposes of province generation) */
		// TODO: optimization may be possible
//		stateSeedsMap = new HashMap<>();
		stateMapList = new BorderMapping<>();
	}

	/**
	 * values - load heightmap, states map
	 */
	private Heightmap loadHeightmap(String heightmapName) {
//		try {
//			values.heightmap = ImageIO.read(new File(values.heightmapName));    		// loadBMPImage("heightmap.bmp");
//			stateBorderMap = ImageIO.read(new File(values.stateBordersName));    // loadBMPImage("state_borders.bmp");
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//		values.imageWidth = values.heightmap.getWidth();
//		values.imageHeight = values.heightmap.getHeight(); 	// may break things but good idea
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
		ForkJoinPool forkJoinPool = new ForkJoinPool();
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
	}

	private int offsetWithNoise(int offsetPotential, int seed, int x, int y) {
		double noise = simplexNoise2(seed, x, y, OFFSET_NOISE_MODIFIER);     //multiplier try (2.0f / offsetPotential)
//		System.out.println(offsetPotential * noise);
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
	private static int determineColor(int x, int y, final Collection<MapPoint> seeds)
	{
		int nearestColor = values.rgb_white;            // color of nearest seed (int value)
		// (default white)
		int dist = Integer.MAX_VALUE;            // select a big number

		//Point point = new Point(x + xOffset, y + yOffset);

		//determineClosestPoint(point, seedsRGBValue);

		// iterate through each seed
		//for (int s = 0; s < seeds.size(); s++) {
//		for (Iterator<Point> pointIterator = seedsRGBValue.keySet().iterator(); pointIterator.hasNext(); ) {
		for (MapPoint point : seeds) {
			// calculate the difference in x and y direction
			int xdiff = point.x - x;
			int ydiff = point.y - y;

			// calculate current Euclidean distance, sqrt is not needed
			// because we only compare and do not need the real value
			int cdist = xdiff * xdiff + ydiff * ydiff;

			if (cdist < dist) {
				nearestColor = point.rgb();        // index 2 is rgb int value of seed // seeds.get(s).get(2)
				dist = cdist;
			}
		}

		return nearestColor;
	}

	private int determineColor(int x, int xOffset, int y, int yOffset, Collection<MapPoint> mapPoints) {
		return determineColor(x + xOffset, y + yOffset, mapPoints);
	}

	//	/**
//	 * note: can/should seed generation be used for some other stuff as well ie state gen (as in, when doing only state gen?, etc.) not sure.
//	 */
//	private class SeedGeneration {
//		// todo use enum set here, constructor etc.
//		// ? different seed generation types in different classes with interface??? and/or abstract class
////		private void seedGeneration(Heightmap heightmap) {
////			this.heightmap = heightmap;z
////			if(values.generationType == ProvinceGenerationType.GRID_SEED) {
////				gridSeedGeneration();
////			} else if(values.generationType == ProvinceGenerationType.DYNAMIC) {
////				dynamicSeedGeneration();
////			} else {
////				System.out.println("HELP");
////			}
////		}
//
//	}

	/**
	 * Pixel color determination using {@link RecursiveAction} for multithreading efficency.
	 *
	 * @see RecursiveAction
	 * @see OpenSimplex2
	 */
	public class ForkColorDetermination extends RecursiveAction {

		/**
		 * Auto-generated serialVersionUID
		 */
		private static final long serialVersionUID = 7925866053687723919L;
		public static final float OFFSET_NOISE_MODIFIER = 1.0f;        /* float datatype is used by simplex noise, and may improve performance over double */
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
			this(provinceMap, heightmap, 0, heightmap.getHeight());
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
			final int widthPerSeed = heightmap.getWidth()  / values.numSeedsX;
			final int heightPerSeed = heightmap.getHeight() / values.numSeedsY;
			final int offsetPotential = 4;
//			Random random = new Random();
//			int seed = random.nextInt();
			System.out.println("run: " + startY + ", " + endY);

//			if (widthPerSeed < heightPerSeed) {
//				offsetPotential = (int) (widthPerSeed * 0.1);
//			} else {
//				offsetPotential = (int) (heightPerSeed * 0.1);
//			}

			try {
				for (int y = startY; y < endY; y++) {
					for (int x = 0; x < heightmap.getWidth(); x++) {
						int rgb;
						int heightmapHeight = (heightmap.getRGB(x, y) >> 16) & 0xFF;
						int stateBorderValue = stateBorderMap.getRGB(x, y);
						int type = provinceType(heightmapHeight);

//						if(stateMapList.containsState(stateBorderValue)) {
						int xOffset = offsetWithNoise(offsetPotential, seed, x, y);    //TODO work on values
						int yOffset = offsetWithNoise(offsetPotential, seed, x, y);
						rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));
//						}
//						else {
//							rgb = 0;    // bad
//							System.out.println("state map list did not contain state of: " + stateBorderValue);
//						}

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
}


