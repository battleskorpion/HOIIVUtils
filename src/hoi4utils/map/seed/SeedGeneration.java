package hoi4utils.map.seed;

import hoi4utils.map.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
