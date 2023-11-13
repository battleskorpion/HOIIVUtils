package hoi4utils.map.seed;

public interface SeedGeneration {
	void generate();
	void generate(BorderMapping stateMapList, BorderMap borderMap);
}
