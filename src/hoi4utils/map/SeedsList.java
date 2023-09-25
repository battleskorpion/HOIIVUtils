package hoi4utils.map;

import hoi4utils.map.province.MapPoint;

import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

public class SeedsList implements Iterable<MapPoint> {
	private ArrayList<HashSet<MapPoint>> seedsList;
//	private HashSet<MapPoint> seedsRGBList;

	public SeedsList() {
		seedsList = new ArrayList<>(3);     // land, sea, lake
		for(int i = 0; i < 3; i++) {
			seedsList.add(new HashSet<>());
		}
//		seedsRGBList = new HashMap<>();
	}

	public void add(MapPoint point) {
//		if(!point.seed()) { // todo remove
//			// error
//			return;
//		}

		//seedsList.get(point.type()).putIfAbsent(new Point(point.x, point.y), point);
		seedsList.get(point.type()).add(point);

//		if(rgb != null) {
//			seedsRGBList.put(point, rgb);
//		}
	}

	public MapPoint get(int x, int y) { return get(new Point(x, y)); }

	public int getRGB(int x, int y) {
		return getRGB(get(x, y));
	}

	public int getRGB(Point point) {
		for (Set<MapPoint> set : seedsList) {
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
		for (HashSet<MapPoint> mapSet : seedsList) {
			for (MapPoint mapPoint : mapSet) {
				if (mapPoint.equals(point)) {
					return mapPoint;
				}
			}
		}
		return null;
	}

//	public void set(Point point) {
//		if(!point.seed) {
//			// error
//			return;
//		}
//
//		seedsList.get(point.type).put(new Point(point.x, point.y), point);
//	}

//	public Iterator<HashSet<MapPoint>> iterator() {
//		return seedsList.iterator();
//	}
	public Iterator<MapPoint> iterator() {
		// todo !
		return new Iterator<MapPoint>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public MapPoint next() {
				return null;
			}
		}
	}

//	public Iterator rgbIterator() {
//		return seedsRGBList.entrySet().iterator();
//	}
}
