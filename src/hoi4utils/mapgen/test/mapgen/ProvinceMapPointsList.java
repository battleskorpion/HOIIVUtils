package hoi4utils.mapgen.test.mapgen;

import java.util.ArrayList;

public class ProvinceMapPointsList {
	/**
	 * y, x order
	 */
	private ArrayList<ArrayList<ProvinceMapPoint>> mapPointsList;

	public ProvinceMapPointsList(int x, int y) {
		/*
		 * initialize arrayList of ArrayList of ProvinceMapPoints length y, x
		 */
		mapPointsList = new ArrayList<>(y);
		for(int i = 0; i < y; i++) {
			mapPointsList.add(new ArrayList<ProvinceMapPoint>(x));
			for(int j = 0; j < x; j++) {
				mapPointsList.get(i).add(new ProvinceMapPoint(x, y));
			}
		}
	}

	/**
	 * add point to points array list
	 * @param point point
	 */
	public void add(ProvinceMapPoint point) {
		mapPointsList.get(point.y).add(point.x, point);
	}

	public ProvinceMapPoint get(int x, int y) {
		return mapPointsList.get(y).get(x);
	}

	public void set(ProvinceMapPoint point) {
		mapPointsList.get(point.y).set(point.x, point);
	}

	/**
	 * @deprecated
	 * @param point point
	 * @return
	 */
	public ProvinceMapPoint remove(ProvinceMapPoint point) {
		return mapPointsList.get(point.y).remove(point.x);
	}

	/**
	 * @deprecated
	 * @param x x-coord
	 * @param y y-coord
	 * @return
	 */
	public ProvinceMapPoint remove(int x, int y) {
		return null;
	}

	public void setRGB(int x, int y, int rgb) {
		get(x, y).rgb = rgb;
	}
}
