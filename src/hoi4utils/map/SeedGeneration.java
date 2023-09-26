package hoi4utils.map;

import hoi4utils.map.province.BorderMap;
import hoi4utils.map.province.Heightmap;
import hoi4utils.map.province.MapPoint;
import hoi4utils.map.province.StateMapList;

public interface SeedGeneration {
	void generate();
	void generate(StateMapList stateMapList, BorderMap borderMap);
}
