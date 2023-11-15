package hoi4utils.map.seed;

import hoi4utils.map.AbstractMapGeneration;
import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SeedProbabilityMap extends AbstractMapGeneration {
	ArrayList<Map<MapPoint, Float>> seedProbabilityMap;
	final Heightmap heightmap;

	public SeedProbabilityMap(Heightmap heightmap) {
		this.heightmap = heightmap;

		seedProbabilityMap = new ArrayList<>(heightmap.getHeight());
		for (int yi = 0; yi < heightmap.getHeight(); yi++) {
			Map<MapPoint, Float> mapx = new HashMap<>();
			for (int xi = 0; xi < heightmap.getWidth(); xi++) {
				float p = 1.0f;
				int height = (heightmap.getRGB(xi, yi) >> 16) & 0xFF;
				int type = provinceType(height);

				/* probability */
				p *= type == 0 ? 1.15f : 0.85f;

				// todo NORMALIZE the probabilities !
				mapx.put(new MapPoint(xi, yi, type), p);
			}
			seedProbabilityMap.add(mapx);
		}
	}

	public void normalize() {
		AtomicReference<Double> sum = new AtomicReference<>((double) 0);
		for (var map : seedProbabilityMap) {
			map.forEach((k, v) -> sum.updateAndGet(v1 -> v1 + v));
		}
		if (sum.get() == 0) {
			return; 
		}
		for (var map : seedProbabilityMap) {
			map.replaceAll((k, v) -> (float) (v / sum.get()));
		}
	}
}
