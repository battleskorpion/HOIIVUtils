package com.HOIIVUtils.hoi4utils.clausewitz_map.province;//package hoi4utils.map.province;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//
//public class ProvinceMapSeedsList {
//	public static final int NUM_TYPES = 3;      // land, sea, lake
//	private ArrayList<HashMap<Point, MapPoint>> seedsList;
//	private HashMap<MapPoint, Integer> seedsRGBList;
//
//	public ProvinceMapSeedsList() {
//		seedsList = new ArrayList<>(NUM_TYPES);
//		for(int i = 0; i < NUM_TYPES; i++) {
//			seedsList.add(new HashMap<>());
//		}
//		seedsRGBList = new HashMap<>();
//	}
//
//	public void add(MapPoint point) {
//		add(point, null);
//	}
//
//	public void add(MapPoint point, Integer rgb) {
//		if(!point.seed) {
//			// error
//			return;
//		}
//
//		seedsList.get(point.type).putIfAbsent(new Point(point.x, point.y), point);
//
//		if(rgb != null) {
//			seedsRGBList.put(point, rgb);
//		}
//	}
//
//	public MapPoint get(int x, int y) { return get(new Point(x, y)); }
//
//	public Integer getRGB(int x, int y) {
//		return getRGB(get(x, y));
//	}
//
//	public Integer getRGB(MapPoint point) {
//		return seedsRGBList.get(point);
//	}
//
//	public MapPoint get(Point point) {
//		for(HashMap<Point, MapPoint> map : seedsList) {
//			if(map.containsKey(point)) {
//				return map.get(point);
//			}
//		}
//		return null;
//	}
//
//	public void set(MapPoint point) {
//		if(!point.seed) {
//			// error
//			return;
//		}
//
//		seedsList.get(point.type).put(new Point(point.x, point.y), point);
//	}
//
//	public Iterator iterator() {
//		return seedsList.iterator();
//	}
//
//	public Iterator rgbIterator() {
//		return seedsRGBList.entrySet().iterator();
//	}
//
//}
