package hoi4utils.map.seed;

import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;
import hoi4utils.map.values;

import java.util.*;

public class ProbabilisticSeedGeneration extends AbstractSeedGeneration<MapPoint> {
	private final SeedProbabilityMap seedProbabilityMap;

	public ProbabilisticSeedGeneration(Heightmap heightmap) {
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

		for (int sy = 0; sy < values.numSeedsY; sy++) {
			for (int sx = 0; sx < values.numSeedsX; sx++) {
				MapPoint dynP;
				byte heightmapHeight = 0;
				do {
//					int px = random.nextInt(heightmap.getWidth());
//					int py = random.nextInt(heightmap.getHeight());
					dynP = weightedRandomPoint(random);
					heightmapHeight = heightmap.xyHeight(dynP.x, dynP.y);
					int type = provinceType(heightmapHeight);
//					p = new MapPoint(dynP, type);
				} while (seeds.contains(dynP));
				int rgb = mapPointColorGeneration(dynP.x, dynP.y, heightmapHeight);
				dynP.setRGB(rgb);
				seeds.add(dynP);
			}
		}
	}

	/**
	 *
	 * @param random Pseudorandom number generator for generating probabilities
	 * @return
	 */
	private MapPoint weightedRandomPoint(Random random) {
		MapPoint mp = seedProbabilityMap.getPoint(random);
		return mp;
	}

}
