package hoi4utils.map.seed;

import hoi4utils.map.AbstractMapGeneration;
import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;

public abstract class AbstractSeedGeneration extends AbstractMapGeneration implements SeedGeneration {
	protected Heightmap heightmap; // should always need a heightmap, simple way differentiate water and land.
	protected SeedsSet<MapPoint> seeds;

	protected AbstractSeedGeneration(Heightmap heightmap) {
		this.heightmap = heightmap;
		seeds = new SeedsSet<>();
	}

	@Override
	public void generate(BorderMapping stateMapList, BorderMap borderMap) {
		generate();
		for (MapPoint seed : seeds) {
			int stateMapColor = borderMap.getRGB(seed.x, seed.y);
			stateMapList.addSeed(stateMapColor, seed);
		}
	}

//	protected AbstractSeedGeneration() {
//		heightmap = null;
//	}
}
