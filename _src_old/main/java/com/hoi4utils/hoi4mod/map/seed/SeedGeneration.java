package com.hoi4utils.hoi4mod.map.seed;

import com.hoi4utils.hoi4mod.map.gen.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
