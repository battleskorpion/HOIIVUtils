package hoi4utils.map.province;

import hoi4utils.map.*;
import opensimplex2.OpenSimplex2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RejectedExecutionException;

import static hoi4utils.map.values.generationType;
import static hoi4utils.map.values.stateBorderMap;

public class ProvinceGeneration extends AbstractMapGeneration {
	private ProvinceMap provinceMap;
	private ProvinceMapPointsList points;
	private SeedsList seeds;

	//	private static HashMap<ProvinceMapPoint, Integer> stateSeedsMap;
	private StateMapList stateMapList;
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

		executeProvinceDetermination();
	}

	private void generate(Heightmap heightmap) {
		this.heightmap = heightmap;
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
		stateMapList = new StateMapList();
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
		public static final double NOISE_MULTIPLIER = 0.05;

		protected static int splitThreshold = 8;		// split until 1 row each

		/**
		 * y-value to start at
		 */
		private int startY;

		/**
		 * y-value to go until (do not do work at this y-value, do work up to this y-value)
		 */
		private int endY;

		/**
		 * number of y-values to work with
		 */
		private int dy;

		/**
		 * simplex noise to offset color determination
		 */
		private OpenSimplex2 noise;
		private ProvinceMap provinceMap;
		private Heightmap heightmap;

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
//			noise = new OpenSimplex2();
//			noise.setNoiseQuality(NoiseQualitySimplex.SMOOTH);

//			seedsRGBMapIterator = seeds.rgbIterator(); // no usage anymore
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
			Random random = new Random();
			int seed = random.nextInt();

			try {
				for (int y = startY; y < endY; y++) {
					for (int x = 0; x < heightmap.getWidth(); x++) {
						/* noise */
						double noise1 = OpenSimplex2.noise2(seed, x * 0.005, y * 0.005);
						double noise2 = OpenSimplex2.noise2(seed, x * 0.005, y * 0.005);

//						int xOffset = (int) (widthPerSeed  * ((noise.get(x * 0.005, y * 0.005, 0.0) - 1) * 0.5));		// * ((noise.getValue(x * 0.005, y * 0.005, 0.0) - 1) * 0.5)));	 good values for 32*32 seeds and 4608 * 2816 image
//						int yOffset = (int) (heightPerSeed * ((noise.get(x * 0.005, y * 0.005, 1.0) - 1) * 0.5));
						int xOffset = (int) (widthPerSeed * (noise1) * NOISE_MULTIPLIER);	//TODO work on values
						int yOffset = (int) (heightPerSeed * (noise2) * NOISE_MULTIPLIER);

						int rgb;
						//int heightmapValue = values.heightmap.getRGB(x, y);
						int heightmapHeight = (heightmap.getRGB(x, y) >> 16) & 0xFF;
						int stateBorderValue = stateBorderMap.getRGB(x, y);
						int type = provinceType(heightmapHeight);

//						if(stateSeedsMap.containsValue(stateBorderValue)) {
						if(stateMapList.containsState(stateBorderValue)) {
//							/*
//							all seeds in this state
//							// TODO: why is this a hashmap. all values will be the same (need arrayList etc.)
//							 */
//							HashMap<ProvinceMapPoint, Integer> stateSeeds = new HashMap<>();
//							// TODO: def prob can use advanced collections methods etc for better algorithm
//							stateSeedsMap.forEach((key, value) -> {
//								if(value == stateBorderValue) {
//									stateSeeds.put(key, value);
//								}
//							});

							rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));
						}
						else {
							rgb = 0;    // bad
						}

						points.setRGB(x, y, rgb);
						provinceMap.setRGB(x, y, rgb);

						//offset.put(new Point(x, y), new Point(xOffset, yOffset));
					}
				}
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}

	}

	private static int determineColor(int x, int xOffset, int y, int yOffset, final ArrayList<MapPoint> seeds)
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
//			System.out.println(seeds.size()); // TODO: size is 683 7 or 3?
			// calculate the difference in x and y direction
			int xdiff = point.x - (x + xOffset);
			int ydiff = point.y - (y + yOffset);

			// calculate euclidean distance, sqrt is not needed
			// because we only compare and do not need the real value
			int cdist = xdiff * xdiff + ydiff * ydiff;

			// is the current distance smaller than the old distance?
			if (cdist < dist) {
				nearestColor = point.rgb;        // index 2 is rgb int value of seed // seeds.get(s).get(2)
				dist = cdist;
			}
		}

		return nearestColor;
	}

	//	/**
//	 * note: can/should seed generation be used for some other stuff as well ie state gen (as in, when doing only state gen?, etc.) not sure.
//	 */
//	private class SeedGeneration {
//		// todo use enum set here, constructor etc.
//		// ? different seed generation types in different classes with interface??? and/or abstract class
////		private void seedGeneration(Heightmap heightmap) {
////			this.heightmap = heightmap;
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
}


