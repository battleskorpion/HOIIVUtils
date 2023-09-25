package hoi4utils.map;

import hoi4utils.map.province.Heightmap;
import opensimplex2.OpenSimplex2;

import java.util.Random;

public class DynamicSeedGeneration extends AbstractSeedGeneration {
	public DynamicSeedGeneration(Heightmap heightmap) {
		super(heightmap);
	}

	/**
	 * Generates seeds dynamically, until there is a set number of seeds. Seed density of sea can be set lower
	 * than land.
	 */
	public void generate()	{
		Random random = new Random(); 		// Random number generator for generating seed locations
		seeds = new SeedsList();

		/*
		create regions/islands
		 */
		long seed = random.nextLong();
		int avg = 0;

		for(int y = 0; y < heightmap.getHeight(); y++) {
			for (int x = 0; x < heightmap.getWidth(); x++) {
				avg += OpenSimplex2.noise2(seed, x, y);
			}
		}

		avg /= values.numPoints;
		System.out.println(avg);
		// TODO
	}
}
