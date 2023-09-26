package hoi4utils.map.province;

import java.util.ArrayList;
import java.util.HashMap;

public class StateMapList {
    private HashMap<Integer, ArrayList<MapPoint>> stateMapSeeds;

    public StateMapList() {
        stateMapSeeds = new HashMap<>();
    }

    public void addSeed(int stateMapColor, MapPoint mapPoint) {
        stateMapSeeds.putIfAbsent(stateMapColor, new ArrayList<MapPoint>());

        stateMapSeeds.get(stateMapColor).add(mapPoint);
    }

    public boolean containsState(int stateBorderValue) {
        return stateMapSeeds.containsKey(stateBorderValue);
    }

    public ArrayList<MapPoint> seedsList(int state) {
        return stateMapSeeds.get(state);
    }

    public ArrayList<MapPoint> seedsList(int state, int type) {
        ArrayList<MapPoint> seeds = new ArrayList<>();
        for (MapPoint seed : seedsList(state)) {
            if (seed.type == type) {
                seeds.add(seed);
            }
        }

        if (seeds.isEmpty()) {
            ArrayList<MapPoint> seedsList = seedsList(state);
            if (seedsList.isEmpty()) {
                System.err.println("No seeds of type for state int: " + state);
            }
            return seedsList;
        }

        return seeds;
    }
}
