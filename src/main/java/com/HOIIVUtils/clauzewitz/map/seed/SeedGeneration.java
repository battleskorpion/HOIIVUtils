package com.HOIIVUtils.clauzewitz.map.seed;

import com.HOIIVUtils.clauzewitz.map.gen.MapPoint;

public interface SeedGeneration<P extends MapPoint> {
	void generate();
	void generate(BorderMapping<P> stateMapList, BorderMap borderMap);
}
