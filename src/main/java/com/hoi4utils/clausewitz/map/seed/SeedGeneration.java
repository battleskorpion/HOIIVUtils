package com.hoi4utils.clausewitz.map.seed;

import com.hoi4utils.clausewitz.map.gen.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
