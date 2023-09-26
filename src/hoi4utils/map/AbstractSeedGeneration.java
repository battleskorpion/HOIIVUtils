package hoi4utils.map;

import hoi4utils.map.province.Heightmap;

public abstract class AbstractSeedGeneration extends AbstractMapGeneration implements SeedGeneration {
	protected Heightmap heightmap; // should always need a heightmap, simple way differentiate water and land.
	protected SeedsList seeds;

	protected AbstractSeedGeneration(Heightmap heightmap) {
		this.heightmap = heightmap;
		seeds = new SeedsList();
	}

//	protected AbstractSeedGeneration() {
//		heightmap = null;
//	}
}
