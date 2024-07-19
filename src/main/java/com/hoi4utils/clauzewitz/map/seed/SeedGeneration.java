package main.java.com.hoi4utils.clauzewitz.map.seed;

import main.java.com.hoi4utils.clauzewitz.map.gen.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
