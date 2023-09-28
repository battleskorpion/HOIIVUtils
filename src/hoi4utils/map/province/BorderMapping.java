package hoi4utils.map.province;

import hoi4utils.map.SeedsSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BorderMapping<P extends MapPoint> {
    /** Integer can be int representation of color */
    private final HashMap<Integer, SeedsSet<P>> borderMapSeeds;      // consider allowing non-hashset instead, for sake of lower-memory use

//    /* cache */
//    private final Map<String, Set<P>> seedsSetCache = new HashMap<>();
//    private final Map<String, List<P>> seedsListCache = new HashMap<>();

    public BorderMapping() {
        borderMapSeeds = new HashMap<>();
    }

    public void addSeed(int stateMapColor, P mapPoint) {
        borderMapSeeds.putIfAbsent(stateMapColor, new SeedsSet<>());

        borderMapSeeds.get(stateMapColor).add(mapPoint);
    }

    public void addSet(int borderArea, SeedsSet<P> mapPoint) {
        if (!borderMapSeeds.containsKey(borderArea)) {
            borderMapSeeds.put(borderArea, mapPoint);
        } else {
            borderMapSeeds.get(borderArea).addAll(mapPoint);
        }
    }

    public boolean containsState(int borderAreaValue) {
        return borderMapSeeds.containsKey(borderAreaValue);
    }

    public SeedsSet<P> seedsSet(int borderArea) {
        return borderMapSeeds.get(borderArea);
    }

    /*
     * BAD IMPL: leads to 321GB of memory use
     */
//    public Set<MapPoint> seedsList(int state, int type) {
//        Set<MapPoint> seeds = new HashSet<>();
//        for (MapPoint seed : seedsList(state)) {
//            if (seed.type == type) {
//                seeds.add(seed);
//            }
//        }
//
//        if (seeds.isEmpty()) {
//            SeedsSet<MapPoint> seedsList = seedsList(state);
//            if (seedsList.isEmpty()) {
//                System.err.println("No seeds of type for state int: " + state);
//            }
//            return seedsList;
//        }
//
//        return seeds;
//    }

    /**
     * Returns a set of seeds corresponding to the borderArea.
     * @param borderArea
     * @param type
     * @return null if the border area did not exist, or
     */
    public Set<P> seedsSet(int borderArea, int type) {
        SeedsSet<P> seeds = borderMapSeeds.get(borderArea);
        if (seeds == null) return null;

        Set<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toSet());
        return seedsOfType;
    }

    // TODO cache?
    /**
     * Returns a list of seeds corresponding to the borderArea, best intended for iteration.
     * @param borderArea
     * @param type
     * @return null if the border area did not exist, or
     */
    public List<P> seedsList(int borderArea, int type) {
        SeedsSet<P> seeds = borderMapSeeds.get(borderArea);
        if (seeds == null) return null;

        List<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toList());
        return seedsOfType;
    }
}
