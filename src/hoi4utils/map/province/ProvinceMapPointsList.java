package hoi4utils.map.province;

import java.util.ArrayList;

public class ProvinceMapPointsList {
	/**
	 * y, x order
	 */
	private ArrayList<ArrayList<MapPoint>> mapPointsList;

	public ProvinceMapPointsList(int x, int y) {
		/*
		 * initialize arrayList of ArrayList of ProvinceMapPoints length y, x
		 */
		mapPointsList = new ArrayList<>(y);
		for(int i = 0; i < y; i++) {
			mapPointsList.add(new ArrayList<MapPoint>(x));
			for(int j = 0; j < x; j++) {
				mapPointsList.get(i).add(new MapPoint(x, y));
			}
		}
	}

	/**
	 * add point to points array list
	 * @param point point
	 */
	public void add(MapPoint point) {
		mapPointsList.get(point.y).add(point.x, point);
	}

	public MapPoint get(int x, int y) {
		return mapPointsList.get(y).get(x);
	}

	public void set(MapPoint point) {
		mapPointsList.get(point.y).set(point.x, point);
	}

	/**
	 * @deprecated
	 * @param point point
	 * @return
	 */
	public MapPoint remove(MapPoint point) {
		return mapPointsList.get(point.y).remove(point.x);
	}

	/**
	 * @deprecated
	 * @param x x-coord
	 * @param y y-coord
	 * @return
	 */
	public MapPoint remove(int x, int y) {
		return null;
	}

	public void setRGB(int x, int y, int rgb) {
		get(x, y).rgb = rgb;
	}
}
