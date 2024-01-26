package com.HOIIVUtils.hoi4utils.map;

import java.awt.*;
import java.util.Objects;

public class MapPoint extends Point {
//	boolean seed;       // true or false
	int type;           // 0 land 1 sea

	int rgb;            // rgb value at point

	public MapPoint(int x, int y, int type) {
		super(x, y);
//		this.seed = seed;
		this.type = type;
	}

	public MapPoint(int x, int y) {
		this(x, y, 0);
	}

	public MapPoint(Point dynP, int type) {
		super(dynP);
		this.type = type;
	}

	public int type() {
		return type;
	}

	/**
	 * similar to impl in {@code Point}
	 * @param obj an object to be compared with this {@code Point2D}
	 * @return {@code true} if the object to be compared is
	 *         an instance of {@code Point2D} and has
	 *         the same values; {@code false} otherwise.
	 *
	 * @see Point
	 */
	@Override
	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//
//		if (obj == null || getClass() != obj.getClass()) {
//			return false;
//		}
//
//		MapPoint other = (MapPoint) obj;
//		return this.x == other.x && this.y == other.y;
		if (obj instanceof MapPoint other) {
			return this.x == other.x && y == other.y;
		}
		return super.equals(obj);
	}

	// Optionally, you might want to override hashCode as well
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public int rgb() {
		return rgb;
	}

	public void setRGB(int rgb) {
		this.rgb = rgb;
	}
}
