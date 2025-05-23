package com.hoi4utils.clausewitz.map.seed;

import com.hoi4utils.clausewitz.map.MapPoint;

import java.util.*;
import java.util.stream.Collectors;

public class BorderMapping_CPU<P extends MapPoint> implements BorderMapping<P> {
    /** Integer can be int representation of color */
    private final HashMap<Integer, SeedsSet<P>> borderMapSeeds;      // consider allowing non-hashset instead, for sake of lower-memory use

    /* cache */
    private final Map<Integer, Map<Integer, Set<P>>> seedSetCache = new HashMap<>();
    private final Map<Integer, Map<Integer, List<P>>> seedListCache = new HashMap<>();

    public BorderMapping_CPU() {
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
        // Check if the seed set is cached
        if (seedSetCache.containsKey(borderArea) && seedSetCache.get(borderArea).containsKey(type)) {
            return seedSetCache.get(borderArea).get(type);
        }

        SeedsSet<P> seeds = borderMapSeeds.get(borderArea);
        if (seeds == null) return null;

        Set<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toSet());

        addToSeedSetCache(borderArea, type, seedsOfType);
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
        // Check if the seed list is cached
        if (seedListCache.containsKey(borderArea) && seedListCache.get(borderArea).containsKey(type)) {
            return seedListCache.get(borderArea).get(type);
        }

        SeedsSet<P> seeds = borderMapSeeds.get(borderArea);
        if (seeds == null) return null;

        List<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toList());

        addToSeedListCache(borderArea, type, seedsOfType);
        return seedsOfType;
    }

    /**
     * Returns a list of seeds corresponding to all border areas and types (all seeds).
     * @return null if the border area did not exist, or
     */
    public List<P> seedsList() {
        List<P> seeds = new ArrayList<>();
        for (SeedsSet<P> seedSet : borderMapSeeds.values()) {
            seeds.addAll(seedSet);
        }
        return seeds;
    }

    public List<Integer> getSeedsCorrelatingBorderAreas(List<P> seeds) {
        List<Integer> borderAreas = new ArrayList<>();

        for (P seed : seeds) {
            for (Map.Entry<Integer, SeedsSet<P>> entry : borderMapSeeds.entrySet()) {
                if (entry.getValue().contains(seed)) {
                    borderAreas.add(entry.getKey());
                    break;
                }
            }
        }

        return borderAreas;
    }

    private void addToSeedListCache(int borderArea, int type, List<P> seedsOfType) {
        // Create map by border area if necessary
        seedListCache.computeIfAbsent(borderArea, k -> new HashMap<>());
        // todo concurrent modification exception!!!!
        // originating from (ProvinceGeneration$ForkColorDetermination.computeDirectly)

        // add seed collection to cache
        seedListCache.get(borderArea).put(type, seedsOfType);
    }

    private void addToSeedSetCache(int borderArea, int type, Set<P> seedsOfType) {
        // Create map by border area if necessary
        seedSetCache.computeIfAbsent(borderArea, k -> new HashMap<>());

        // add seed collection to cache
        seedSetCache.get(borderArea).put(type, seedsOfType);
    }
}


