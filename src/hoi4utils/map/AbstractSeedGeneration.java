package hoi4utils.map;

import hoi4utils.map.province.Heightmap;
import hoi4utils.map.province.MapGeneration;
import hoi4utils.map.province.MapPoint;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractSeedGeneration extends MapGeneration implements SeedGeneration {
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
