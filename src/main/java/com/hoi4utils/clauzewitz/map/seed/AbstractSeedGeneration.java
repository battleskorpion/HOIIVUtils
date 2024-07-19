package main.java.com.hoi4utils.clauzewitz.map.seed;

import main.java.com.hoi4utils.clauzewitz.map.SeedGenProperties;
import main.java.com.hoi4utils.clauzewitz.map.gen.AbstractMapGeneration;
import main.java.com.hoi4utils.clauzewitz.map.gen.Heightmap;
import main.java.com.hoi4utils.clauzewitz.map.gen.MapPoint;

public abstract class AbstractSeedGeneration<P extends MapPoint> extends AbstractMapGeneration implements SeedGeneration<P> {
	protected Heightmap heightmap; // should always need a heightmap, simple way differentiate water and land.
	protected SeedsSet<P> seeds;
	protected SeedGenProperties properties;

	protected AbstractSeedGeneration(Heightmap heightmap, SeedGenProperties properties) {
		this.heightmap = heightmap;
		seeds = new SeedsSet<>();
		this.properties = properties;
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
