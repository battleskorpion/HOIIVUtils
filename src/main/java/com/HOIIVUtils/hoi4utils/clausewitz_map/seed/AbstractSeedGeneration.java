package com.HOIIVUtils.hoi4utils.clausewitz_map.seed;

import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.AbstractMapGeneration;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;

public abstract class AbstractSeedGeneration<P extends MapPoint> extends AbstractMapGeneration implements SeedGeneration<P> {
	protected Heightmap heightmap; // should always need a heightmap, simple way differentiate water and land.
	protected SeedsSet<P> seeds;

	protected AbstractSeedGeneration(Heightmap heightmap) {
		this.heightmap = heightmap;
		seeds = new SeedsSet<>();
	}

	@Override
	public void generate(BorderMapping<P> stateMapList, BorderMap borderMap) {
		generate();
		for (P seed : seeds) {
			int stateMapColor = borderMap.getRGB(seed.x, seed.y);
			stateMapList.addSeed(stateMapColor, seed);
		}
	}

//	protected AbstractSeedGeneration() {
//		heightmap = null;
//	}
}
