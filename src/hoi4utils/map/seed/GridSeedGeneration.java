package hoi4utils.map.seed;

import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;
import hoi4utils.map.values;

import java.util.Random;

public class GridSeedGeneration extends AbstractSeedGeneration<MapPoint> {
	public GridSeedGeneration(Heightmap heightmap) {
		super(heightmap);
	}
	/**
	 * Generates seeds in a grid-like manner, but with a random offset which adds some variance
	 * while keeping provinces sizes/distances/density roughly even while still looking
	 * semi-random/natural.
	 */
	@Override
	public void generate() {
		Random random = new Random(); 		// Random number generator for generating seed locations
		
		for (int y = heightmap.xyHeight() / values.numSeedsY / 2 - 1; y < heightmap.xyHeight();
		     y += heightmap.xyHeight() / values.numSeedsY) {
			for (int x = heightmap.width() / values.numSeedsX / 2 - 1; x < heightmap.width();
			    x += heightmap.width() / values.numSeedsX) {
				int xOffset = random.nextInt(heightmap.width()  / values.numSeedsX - 1)
						- (heightmap.width()  / values.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
				int yOffset = random.nextInt(heightmap.xyHeight() / values.numSeedsY - 1)
						- (heightmap.xyHeight() / values.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
				int seedX = x + xOffset;		// x-value of seed
				int seedY = y + yOffset; 		// y-value of seed

				/* heightmap color stuff */
				byte heightmapHeight = heightmap.xyHeight(seedX, seedY);
				int rgb = mapPointColorGeneration(seedX, seedY, heightmapHeight); 			// rgb color int value

				/* add point to points array */
				MapPoint mapPoint;
				int type = provinceType(heightmapHeight); 	// 0: land
															// 1: sea
				mapPoint = new MapPoint(seedX, seedY, type);

				/* add point to seeds array */
				mapPoint.setRGB(rgb);
				seeds.add(mapPoint);
//				stateSeedsMap.put(provinceMapPoint, stateMapColor);
			}
		}
	}

}
