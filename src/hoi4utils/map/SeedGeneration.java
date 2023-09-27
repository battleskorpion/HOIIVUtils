package hoi4utils.map;

import hoi4utils.map.province.BorderMap;
import hoi4utils.map.province.BorderMapping;

public interface SeedGeneration {
	void generate();
	void generate(BorderMapping stateMapList, BorderMap borderMap);
}
