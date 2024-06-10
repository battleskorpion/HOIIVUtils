package com.HOIIVUtils.clauzewitz.map.seed;

import com.HOIIVUtils.clauzewitz.map.gen.MapPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BorderMapping_GPU<P extends MapPoint> implements BorderMapping<P> {
    /** Integer can be int representation of color */
    // private final HashMap<Integer, SeedsSet<P>> borderMapSeeds; // consider
    // allowing non-hashset instead, for sake of lower-memory use
    private final List<P> seeds;
    private final List<Integer> borderColorMapping; // Maps each element in seeds to a color (border map) index

    public BorderMapping_GPU() {
        seeds = new ArrayList<>();
        borderColorMapping = new ArrayList<>();
    }

    public void addSeed(int stateMapColor, P mapPoint) {
        seeds.add(mapPoint);
        borderColorMapping.add(stateMapColor);
    }

    public void addSet(int borderArea, SeedsSet<P> mapPoint) {
        seeds.addAll(mapPoint);
        borderColorMapping.addAll(Collections.nCopies(mapPoint.size(), borderArea));
    }

    public boolean containsState(int borderAreaValue) {
        // return borderMapSeeds.containsKey(borderAreaValue);
        return borderColorMapping.contains(borderAreaValue);
    }

    public SeedsSet<P> seedsSet(int borderArea) {
        // return borderMapSeeds.get(borderArea);
        return new SeedsSet<>(seeds.stream()
                .filter(mapPoint -> borderColorMapping.get(seeds.indexOf(mapPoint)) == borderArea)
                .collect(Collectors.toList()));
    }

    /*
     * BAD IMPL: leads to 321GB of memory use
     */
    // public Set<MapPoint> seedsList(int state, int type) {
    // Set<MapPoint> seeds = new HashSet<>();
    // for (MapPoint seed : seedsList(state)) {
    // if (seed.type == type) {
    // seeds.add(seed);
    // }
    // }
    //
    // if (seeds.isEmpty()) {
    // SeedsSet<MapPoint> seedsList = seedsList(state);
    // if (seedsList.isEmpty()) {
    // System.err.println("No seeds of type for state int: " + state);
    // }
    // return seedsList;
    // }
    //
    // return seeds;
    // }

    /**
     * Returns a set of seeds corresponding to the borderArea.
     * 
     * @param borderArea
     * @param type
     * @return null if the border area did not exist, or
     */
    public Set<P> seedsSet(int borderArea, int type) {
        // Check if the seed set is cached
        if (!borderColorMapping.contains(borderArea))
            return null;

        Set<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toSet());

        return seedsOfType;
    }

    // TODO cache?
    /**
     * Returns a list of seeds corresponding to the borderArea, best intended for
     * iteration.
     * 
     * @param borderArea
     * @param type
     * @return null if the border area did not exist, or
     */
    public List<P> seedsList(int borderArea, int type) {
        if (!borderColorMapping.contains(borderArea))
            return null;

        List<P> seedsOfType = seeds.stream()
                .filter(mapPoint -> mapPoint.type() == type)
                .collect(Collectors.toList());

        return seedsOfType;
    }

    /**
     * Returns a list of seeds corresponding to all border areas and types (all
     * seeds).
     * 
     * @return null if the border area did not exist, or
     */
    public List<P> seedsList() {
        return seeds;
    }

    public List<Integer> getSeedsCorrelatingBorderAreas(List<P> seeds) {
        return borderColorMapping;
    }
}
