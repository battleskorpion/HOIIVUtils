package hoi4utils.map;

import hoi4utils.map.province.BorderMap;
import hoi4utils.map.province.Heightmap;
import hoi4utils.map.province.MapPoint;
import hoi4utils.map.province.BorderMapping;

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
