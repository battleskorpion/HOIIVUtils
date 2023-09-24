package hoi4utils.map;

import hoi4utils.map.province.Heightmap;

public abstract class AbstractSeedGeneration implements SeedGeneration {
	protected Heightmap heightmap; // should always need a heightmap, simple way differentiate water and land.
	protected AbstractSeedGeneration(Heightmap heightmap) {
		this.heightmap = heightmap;
	}

//	protected AbstractSeedGeneration() {
//		heightmap = null;
//	}
}
