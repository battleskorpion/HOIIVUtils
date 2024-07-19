package main.java.com.hoi4utils.clauzewitz.map.province;

import main.java.com.hoi4utils.clauzewitz.map.gen.MapPoint;

public class ProvinceMapPointsList {
	final int width;
	final int height;

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
		width = x;
		height = y;
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

	public void setRGB(int x, int y, int rgb) {
		get(x, y).setRGB(rgb);
	}

	public int height() {
		return height;
	}

	public int width() {
		return width;
	}
}
