package hoi4utils.map.province;

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

	public int type() {
		return type;
	}

	// Override the equals method to compare based on coordinates
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		MapPoint other = (MapPoint) obj;
		return this.x == other.x && this.y == other.y;
	}

	// Optionally, you might want to override hashCode as well
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public int rgb() {
		return rgb;
	}
}
