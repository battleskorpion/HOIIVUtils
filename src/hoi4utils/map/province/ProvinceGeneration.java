package hoi4utils.map.province;

import opensimplex2.OpenSimplex2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RejectedExecutionException;

import static hoi4utils.map.province.values.stateBorderMap;

//import static ho.values.stateBorderMap;

public class ProvinceGeneration {
	private static BufferedImage provinceMap;
	private static ProvinceMapPointsList points;
	private static ProvinceMapSeedsList seeds;

//	private static HashMap<ProvinceMapPoint, Integer> stateSeedsMap;
	private static StateMapList stateMapList;

	public static void main(String[] args) {
		provinceGeneration();

		try {
			ImageIO.write(provinceMap, "bmp", new File("output.bmp"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void provinceGeneration() {
		/* create new image (map) */
		provinceMap = new ProvinceMap(values.imageWidth, values.imageHeight,
				BufferedImage.TYPE_INT_RGB);
		initLists();

		// load heightmap, states map
		loadMaps();

		/* seeds generation */
		seedGeneration();

		provinceDetermination();
	}

	private static void initLists() {
		/* initialize points list */
		points = new ProvinceMapPointsList(values.imageWidth, values.imageHeight);

		/* initialize mapping of seeds to states (regions for purposes of province generation) */
		// TODO: optimization may be possible
//		stateSeedsMap = new HashMap<>();
		stateMapList = new StateMapList();
		System.out.println(stateMapList);
	}

	/**
	 * values - load heightmap, states map
	 */
	private static void loadMaps() {
		try {
			values.heightmap = ImageIO.read(new File(values.heightmapName));    		// loadBMPImage("heightmap.bmp");
			stateBorderMap = ImageIO.read(new File(values.stateBordersName));    // loadBMPImage("state_borders.bmp");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		values.imageWidth = values.heightmap.getWidth();
		values.imageHeight = values.heightmap.getHeight(); 	// may break things but good idea
	}

	private static void seedGeneration() {
		if(values.generationType == ProvinceGenerationType.GRID_SEED) {
			gridSeedGeneration();
		}
		else if(values.generationType == ProvinceGenerationType.DYNAMIC) {
			dynamicSeedGeneration();
		}
		else {
			System.out.println("HELP");
		}
	}

	/**
	 * Generates seeds in a grid-like manner, but with a random offset which adds some variance
	 * while keeping provinces sizes/distances/density roughly even while still looking
	 * semi-random/natural.
	 */
	private static void gridSeedGeneration() {
		Random random = new Random(); 		// Random number generator for generating seed locations
		seeds = new ProvinceMapSeedsList();

		for(int y = values.imageHeight / values.numSeedsY / 2 - 1; y < values.imageHeight;
		    y += values.imageHeight / values.numSeedsY) {
			for(int x = values.imageWidth / values.numSeedsX / 2 - 1; x < values.imageWidth;
			    x += values.imageWidth / values.numSeedsX) {
				int xOffset = random.nextInt(values.imageWidth  / values.numSeedsX - 1)
						- (values.imageWidth  / values.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
				int yOffset = random.nextInt(values.imageHeight / values.numSeedsY - 1)
						- (values.imageHeight / values.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
				int seedX = x + xOffset;		// x-value of seed
				int seedY = y + yOffset; 		// y-value of seed

				/* heightmap color stuff */
				// heightmap is in grayscale meaning only need to find red value to get height value
				// at point.
				int heightmapHeight = (values.heightmap.getRGB(seedX, seedY) >> 16) & 0xFF;
				int rgb = provinceColorGeneration(seedX, seedY, heightmapHeight); 			// rgb color int value
				int stateMapColor = stateBorderMap.getRGB(seedX, seedY);

				/* add point to points array */
				ProvinceMapPoint provinceMapPoint;
				/* calculate sea or land prov. */
				int type = provinceType(heightmapHeight); 	// 0: land
															// 1: sea
				provinceMapPoint = new ProvinceMapPoint(seedX, seedY, true, type);
				points.set(provinceMapPoint);

				/* add point to seeds array */
				// x and y needed
//				Integer rgbInteger = rgb;
				Point point = new Point(seedX, seedY);
				seeds.add(provinceMapPoint, rgb);		//TODO rgb? now in map point idk hmmmmmm
				provinceMapPoint.rgb = rgb;
//				stateSeedsMap.put(provinceMapPoint, stateMapColor);
				stateMapList.addSeed(stateMapColor, provinceMapPoint);

				// TODO no not here
				// TODO don't need thiS???? #redundant
				// set color at pixel cords
				try {
					provinceMap.setRGB(seedX, seedY, rgb);
				}
				catch (ArrayIndexOutOfBoundsException exc) {
					exc.printStackTrace();
					System.out.println("x: " + (seedX));
					System.out.println("y: " + (seedY));
					return;
					//continue; 	// skip rest of iteration
				}
			}
		}
	}

	/**
	 * Generates seeds dynamically, until there is a set number of seeds. Seed density of sea can be set lower
	 * than land.
	 */
	private static void dynamicSeedGeneration()	{
		Random random = new Random(); 		// Random number generator for generating seed locations
		seeds = new ProvinceMapSeedsList();

		/*
		create regions/islands
		 */
		long seed = random.nextLong();
		int avg = 0;

		for(int y = 0; y < values.imageWidth; y++) {
			for (int x = 0; x < values.imageWidth; x++) {
				avg += OpenSimplex2.noise2(seed, x, y);
			}
		}

		avg /= values.numPoints;
		System.out.println(avg);
		// TODO
	}

	private static int provinceColorGeneration(int seedX, int seedY, int heightmapHeight) {
		int rgb;                        // rgb color int value
		int stateMapColor;
		Color color;
		Random random = new Random();

		// state (region) point belongs to, should (if reasonable) obtain color of seed from
		// the same region (maintaining state boundaries)
		try {
			stateMapColor = stateBorderMap.getRGB(seedX, seedY);
		} catch (ArrayIndexOutOfBoundsException exc) {
			System.err.println("x: " + seedX + ", y: " + seedY);
		}

		aa:
		do {
			/* prov color */
			if (heightmapHeight < values.HEIGHTMAP_SEA_LEVEL) {
				// generate new color until unique color generated (color does not exist already)
				color = new Color(random.nextInt(64), random.nextInt(64), random.nextInt(64));

				rgb = colorToInt(color);
			}
			else {
				// generate new color until unique color generated (color does not exist already)
				color = new Color(random.nextInt(192) + 64, random.nextInt(192) + 64, random.nextInt(192) + 64);

				// Color -> int
				rgb = colorToInt(color);
			}

			/**
			 * check if color already exists
			 */
//			if (values.seedsRGBValues.containsValue(rgb)) {		// TODO should reimplement!!!!
//				continue aa;
//			}
		}
		while(false);

		return rgb;
	}

	private static int colorToInt(Color color) {
		int rgb;
		// Color -> int
		rgb = color.getRed();
		rgb = (rgb << 8) + color.getGreen();
		rgb = (rgb << 8) + color.getBlue();
		return rgb;
	}

	private static int provinceType(int heightmapHeight) {
		if (heightmapHeight < values.HEIGHTMAP_SEA_LEVEL) {
			return 1; 	// sea
		}
		return 0; 		// land
	}

	private static void provinceDetermination() {
		ForkColorDetermination forkColorDetermination = new ForkColorDetermination();
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
	public static class ForkColorDetermination extends RecursiveAction {
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

//		private Iterator<Map.Entry<ProvinceMapPoint, Integer>> seedsRGBMapIterator;

		/**
		 * constructor (y set as 0 to imageHeight). Recommended constructor for initial initialization.
		 */
		public ForkColorDetermination() {
			this(0, values.imageHeight);
		}

		/**
		 * constructor
		 */
		public ForkColorDetermination(int startY, int endY) {
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

			invokeAll(new ForkColorDetermination(startY, startY + split), new ForkColorDetermination(startY + split, endY));
		}

		/**
		 * Determine color for each point
		 */
		protected void computeDirectly() {
			final int widthPerSeed = values.imageWidth  / values.numSeedsX;
			final int heightPerSeed = values.imageHeight / values.numSeedsY;
			Random random = new Random();
			int seed = random.nextInt();

			try {
				for (int y = startY; y < endY; y++) {
					for (int x = 0; x < values.imageWidth; x++) {
						/* noise */
						double noise1 = OpenSimplex2.noise2(seed, x * 0.005, y * 0.005);
						double noise2 = OpenSimplex2.noise2(seed, x * 0.005, y * 0.005);

//						int xOffset = (int) (widthPerSeed  * ((noise.get(x * 0.005, y * 0.005, 0.0) - 1) * 0.5));		// * ((noise.getValue(x * 0.005, y * 0.005, 0.0) - 1) * 0.5)));	 good values for 32*32 seeds and 4608 * 2816 image
//						int yOffset = (int) (heightPerSeed * ((noise.get(x * 0.005, y * 0.005, 1.0) - 1) * 0.5));
						int xOffset = (int) (widthPerSeed * (noise1) * NOISE_MULTIPLIER);	//TODO work on values
						int yOffset = (int) (heightPerSeed * (noise2) * NOISE_MULTIPLIER);

						int rgb;
						//int heightmapValue = values.heightmap.getRGB(x, y);
						int heightmapHeight = (values.heightmap.getRGB(x, y) >> 16) & 0xFF;
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

							//rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue));
							rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));
						}
						else {
							rgb = 0;    // bad
						}

						points.setRGB(x, y, rgb);
						provinceMap.setRGB(x, y, rgb);
//						try {
//							if (testWidth / 2 + xOffset < testWidth && testWidth / 2 + yOffset < testWidth) {
//								testImage.setRGB(testWidth / 2 + xOffset, testWidth / 2 + yOffset, rgb);
//							}
//						}
//						catch (Exception exc) {
//
//						}
						//offset.put(new Point(x, y), new Point(xOffset, yOffset));
					}
				}
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}

	}

	private static int determineColor(int x, int xOffset, int y, int yOffset, final ArrayList<ProvinceMapPoint> seeds)
	{
		int nearestColor = values.rgb_white;            // color of nearest seed (int value)
		// (default white)
		int dist = Integer.MAX_VALUE;            // select a big number

		//Point point = new Point(x + xOffset, y + yOffset);

		//determineClosestPoint(point, seedsRGBValue);

		// iterate through each seed
		//for (int s = 0; s < seeds.size(); s++) {
//		for (Iterator<Point> pointIterator = seedsRGBValue.keySet().iterator(); pointIterator.hasNext(); ) {
		for (ProvinceMapPoint point : seeds) {
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
}


