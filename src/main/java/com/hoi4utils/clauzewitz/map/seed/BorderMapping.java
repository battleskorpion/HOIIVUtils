package main.java.com.hoi4utils.clauzewitz.map.seed;

import main.java.com.hoi4utils.clauzewitz.map.gen.MapPoint;

import java.util.Collection;
import java.util.List;

public interface BorderMapping<P extends MapPoint> {
    void addSeed(int stateMapColor, P mapPoint);

    void addSet(int borderArea, SeedsSet<P> mapPoint);

    boolean containsState(int borderAreaValue);

    SeedsSet<P> seedsSet(int borderArea);

    List<P> seedsList();

    List<Integer> getSeedsCorrelatingBorderAreas(List<P> seeds);

    Collection<P> seedsList(int stateBorderValue, int type);
}
