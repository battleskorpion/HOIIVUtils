package com.hoi4utils.clausewitz.map.seed;

import com.hoi4utils.clausewitz.map.Heightmap;
import com.hoi4utils.clausewitz.map.MapPoint;
import com.hoi4utils.clausewitz.map.SeedGenProperties;

import java.util.Random;

public class ProbabilisticSeedGeneration extends AbstractSeedGeneration<MapPoint> {
	private final SeedProbabilityMap_GPU_3 seedProbabilityMap;

	public ProbabilisticSeedGeneration(Heightmap heightmap, SeedGenProperties properties) {
		super(heightmap, properties);
		seedProbabilityMap = new SeedProbabilityMap_GPU_3(heightmap, properties);
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
		MapPoint[] mapPoints = seedProbabilityMap.getPoints(random, properties.numSeeds());
		int mpCounter = 0;

		for (int sy = 0; sy < properties.numSeedsY(); sy++) {
			for (int sx = 0; sx < properties.numSeedsX() && mpCounter < properties.numSeeds(); sx++) {
				MapPoint dynP;
				int heightmapHeight = 0;
				do {
//					int px = random.nextInt(heightmap.getWidth());
//					int py = random.nextInt(heightmap.getHeight());
					// weighted random point
					dynP = mapPoints[mpCounter++];
					heightmapHeight = heightmap.height_xy(dynP.x, dynP.y);
					int type = provinceType(heightmapHeight, properties.seaLevel());
//					p = new MapPoint(dynP, type);
				} while (seeds.contains(dynP));
				int rgb = mapPointColorGeneration(dynP.x, dynP.y, heightmapHeight, properties.seaLevel());
				dynP.setRGB(rgb);
				seeds.add(dynP);
			}
		}
	}

}
