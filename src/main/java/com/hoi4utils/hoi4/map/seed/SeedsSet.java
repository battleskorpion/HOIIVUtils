package com.hoi4utils.hoi4.map.seed;

import com.hoi4utils.hoi4.map.gen.MapPoint;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SeedsSet<P extends MapPoint> extends AbstractSet<P> implements Iterable<P>, Set<P> {
	public static final int SEEDS_LIST_SIZE = 3;
	private final ArrayList<Set<P>> seedsList;    // note: more mem-efficient to use one set, with tradeoffs, but then would really be a more generic MapPoint set
//	private HashSet<MapPoint> seedsRGBList;

	public SeedsSet() {
		seedsList = new ArrayList<>(SEEDS_LIST_SIZE);     // land, sea, lake
		for(int i = 0; i < 3; i++) {
			seedsList.add(new HashSet<>());
		}
	}

	public SeedsSet(List<P> collect) {
		this();
		this.addAll(collect);
	}

	public boolean add(P point) {
		return seedsList.get(point.type()).add(point);
	}

	public MapPoint get(int x, int y) { return get(new Point(x, y)); }

	public int getRGB(int x, int y) {
		return getRGB(get(x, y));
	}

	public int getRGB(Point point) {
		for (Set<P> set : seedsList) {
			for (MapPoint mp : set) {
				if (mp.equals(point)) {
					return mp.rgb();
				}
			}
		}

		System.out.println("No rgb match in + " + this.getClass());
		return -1;
	}

	public MapPoint get(Point point) {
		for (Set<P> mapSet : seedsList) {
			for (P mapPoint : mapSet) {
				if (mapPoint.equals(point)) {
					return mapPoint;
				}
			}
		}
		return null;
	}

	public Iterator<P> iterator() {
		// todo !
		return new Iterator<>() {
			private int listIndex = 0;
			private Iterator<P> setIterator = seedsList.get(0).iterator(); // init with the first set

			@Override
			public boolean hasNext() {
				// Check if there are more elements in the set
				if (setIterator.hasNext()) {
					return true;
				}

				// Check if there are more sets in the seeds list to iterate over
				while (listIndex < SEEDS_LIST_SIZE - 1) {
					listIndex++;
					setIterator = seedsList.get(listIndex).iterator();
					if (setIterator.hasNext()) {
						return true;
					}
				}

				return false;
			}

			@Override
			public P next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return setIterator.next();
			}
		};
	}

	@Override
	public int size() {
		int size = 0;
		for (Set<P> set : seedsList) {
			size += set.size();
		}
		return size;
	}
}
