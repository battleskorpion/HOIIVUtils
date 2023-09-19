package hoi4utils.mapgen.test.mapgen;

import java.util.ArrayList;
import java.util.HashMap;

public class StateMapList {
    private HashMap<Integer, ArrayList<ProvinceMapPoint>> stateMapSeeds;

    public StateMapList() {
        stateMapSeeds = new HashMap<>();
    }

    public void addSeed(int stateMapColor, ProvinceMapPoint provinceMapPoint) {
        stateMapSeeds.putIfAbsent(stateMapColor, new ArrayList<ProvinceMapPoint>());

        stateMapSeeds.get(stateMapColor).add(provinceMapPoint);
    }

    public boolean containsState(int stateBorderValue) {
        return stateMapSeeds.containsKey(stateBorderValue);
    }

    public ArrayList<ProvinceMapPoint> seedsList(int state) {
        return stateMapSeeds.get(state);
    }

    public ArrayList<ProvinceMapPoint> seedsList(int state, int type) {
        ArrayList<ProvinceMapPoint> seeds = new ArrayList<>();
        for (ProvinceMapPoint seed : seedsList(state)) {
            if (seed.type == type) {
                seeds.add(seed);
            }
        }

        if (seeds.size() == 0) {
            ArrayList<ProvinceMapPoint> seedsList = seedsList(state);
            if (seedsList.size() == 0) {
                System.err.println("No seeds of type for state int: " + state);
            }
            return seedsList;
        }

        return seeds;
    }
}
