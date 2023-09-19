package hoi4utils.mapgen.test.mapgen;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProvinceMapSeedsList {
	private ArrayList<HashMap<Point, ProvinceMapPoint>> seedsList;
	private HashMap<ProvinceMapPoint, Integer> seedsRGBList;

	public ProvinceMapSeedsList() {
		seedsList = new ArrayList<>(3);     // land, sea, lake
		for(int i = 0; i < 3; i++) {
			seedsList.add(new HashMap<>());
		}
		seedsRGBList = new HashMap<>();
	}

	public void add(ProvinceMapPoint point) {
		add(point, null);
	}

	public void add(ProvinceMapPoint point, Integer rgb) {
		if(!point.seed) {
			// error
			return;
		}

		seedsList.get(point.type).putIfAbsent(new Point(point.x, point.y), point);

		if(rgb != null) {
			seedsRGBList.put(point, rgb);
		}
	}

	public ProvinceMapPoint get(int x, int y) { return get(new Point(x, y)); }

	public Integer getRGB(int x, int y) {
		return getRGB(get(x, y));
	}

	public Integer getRGB(ProvinceMapPoint point) {
		return seedsRGBList.get(point);
	}

	public ProvinceMapPoint get(Point point) {
		for(HashMap<Point, ProvinceMapPoint> map : seedsList) {
			if(map.containsKey(point)) {
				return map.get(point);
			}
		}
		return null;
	}

	public void set(ProvinceMapPoint point) {
		if(!point.seed) {
			// error
			return;
		}

		seedsList.get(point.type).put(new Point(point.x, point.y), point);
	}

	public Iterator iterator() {
		return seedsList.iterator();
	}

	public Iterator rgbIterator() {
		return seedsRGBList.entrySet().iterator();
	}

}
