package clausewitz_mapgen;

public class ProvinceMapPoint {
	int x;
	int y;
	boolean seed;       // true or false
	int type;           // 0 land 1 sea

	int rgb;

	public ProvinceMapPoint(int x, int y, boolean seed, int type) {
		this.x = x;
		this.y = y;
		this.seed = seed;
		this.type = type;
	}

	public ProvinceMapPoint(int x, int y) {
		this(x, y, false, 0);
	}
}
