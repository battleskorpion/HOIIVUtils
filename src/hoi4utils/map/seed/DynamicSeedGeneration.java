package hoi4utils.map.seed;

import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;
import hoi4utils.map.values;

import java.awt.*;
import java.util.*;

public class DynamicSeedGeneration extends AbstractSeedGeneration<MapPoint> {
	private final SeedProbabilityMap seedProbabilityMap;

	public DynamicSeedGeneration(Heightmap heightmap) {
		super(heightmap);
		seedProbabilityMap = new SeedProbabilityMap(heightmap);
	}

	/**
	 * Generates seeds dynamically, until there is a set number of seeds. Seed density of sea
	 * can be set lower than land.
	 */
	public void generate()	{
		Random random = new Random(); 		// Random number generator for generating seed locations

		/*
		create regions/islands
		 */
		long seed = random.nextLong();
//		int avg = 0;
//
//		for(int y = 0; y < heightmap.getHeight(); y++) {
//			for (int x = 0; x < heightmap.getWidth(); x++) {
//				avg += OpenSimplex2.noise2(seed, x, y);
//			}
//		}
//
//		avg /= values.numPoints;
//		System.out.println(avg);
//		// TODO

		for (int sy = 0; sy < values.numSeedsY; sy++) {
			for (int sx = 0; sx < values.numSeedsX; sx++) {
				MapPoint p;
				do {
//					int px = random.nextInt(heightmap.getWidth());
//					int py = random.nextInt(heightmap.getHeight());
					Point dynamicPoint = weightedRandomPoint(random);
					int heightmapHeight = (heightmap.getRGB(px, py) >> 16) & 0xFF;
					int type = provinceType(heightmapHeight);
					p = new MapPoint(px, py, type);
				} while (seeds.contains(p));
				seeds.add(p);
			}
		}
	}

	/**
	 *
	 * @param random Pseudorandom number generator for generating probabilities
	 * @return
	 */
	private Point weightedRandomPoint(Random random) {
		/*
		double p = random.nextDouble();
		double cumlativeProbability = 0.0
		for (Item item : items) {
			cumlativeProbability += item->probability;
			if (p <= cumlativeProbability) {
			return item;
			} // idk???
		 */
	}

}
