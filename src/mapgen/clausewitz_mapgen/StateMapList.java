package mapgen.clausewitz_mapgen;

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

    public ArrayList<ProvinceMapPoint> seedsList(Integer state) {
        return stateMapSeeds.get(state);
    }
}
