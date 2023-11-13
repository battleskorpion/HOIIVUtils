package hoi4utils.map.province;

import hoi4utils.map.MapPoint;

public class ProvinceMapPointsList {
	/**
	 * y, x order
	 */
	private final MapPoint[][] mapPointsList;

	public ProvinceMapPointsList(int x, int y) {
		/*
		 * initialize arrayList of ArrayList of ProvinceMapPoints length y, x
		 */
		mapPointsList = new MapPoint[y][x];
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				mapPointsList[i][j] = new MapPoint(j, i);
			}
		}
	}

	/**
	 * Add point to points array list
	 *
	 * @param point point
	 */
	public void add(MapPoint point) {
		mapPointsList[point.y][point.x] = point;
	}

	public MapPoint get(int x, int y) {
		return mapPointsList[y][x];
	}
	public void set(MapPoint point) {
		mapPointsList[point.y][point.x] = point;
	}

//	/**
//	 * @deprecated
//	 * @param point point
//	 * @return
//	 */
//	public MapPoint remove(MapPoint point) {
//		return mapPointsList.get(point.y).remove(point.x);
//	}
//
//	/**
//	 * @deprecated
//	 * @param x x-coord
//	 * @param y y-coord
//	 * @return
//	 */
//	public MapPoint remove(int x, int y) {
//		return null;
//	}

	public void setRGB(int x, int y, int rgb) {
		get(x, y).setRGB(rgb);
	}
}
