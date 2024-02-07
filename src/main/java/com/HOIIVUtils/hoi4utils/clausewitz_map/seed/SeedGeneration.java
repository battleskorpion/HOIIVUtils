package com.HOIIVUtils.hoi4utils.clausewitz_map.seed;

import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
