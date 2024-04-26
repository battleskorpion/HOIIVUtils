package com.HOIIVUtils.hoi4utils.clausewitz_map.seed;

import com.HOIIVUtils.hoi4utils.clausewitz_map.ProvinceGenProperties;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;

import java.util.Random;

public class GridSeedGeneration extends AbstractSeedGeneration<MapPoint> {
	private final double gridHeight;
	private final double gridWidth;
	private final int gridHeightFloor;
	private final int gridWidthFloor;
	private final double gridCenterY;
	private final double gridCenterX;
	private final int gridCenterYFloor;
	private final int gridCenterXFloor;

	public GridSeedGeneration(ProvinceGenProperties properties, Heightmap heightmap) {
		super(heightmap, properties);
		gridHeight = (double) heightmap.height() / properties.numSeedsY();
		gridWidth = (double) heightmap.width() / properties.numSeedsX();
		gridHeightFloor = (int) Math.floor(gridHeight);
		gridWidthFloor = (int) Math.floor(gridWidth);
		gridCenterY = gridHeight / 2.0;
		gridCenterX = gridWidth / 2.0;
		gridCenterYFloor = (int) Math.floor(gridCenterY);
		gridCenterXFloor = (int) Math.floor(gridCenterX);
	}
	/**
	 * Generates seeds in a grid-like manner, but with a random offset which adds some variance
	 * while keeping provinces sizes/distances/density roughly even while still looking
	 * semi-random/natural.
	 */
	@Override
	public void generate() {
		Random random = new Random(); 		// Random number generator for generating seed locations

		for (double y = gridCenterY; y < heightmap.height(); y += gridHeight) {
			for (double x = gridCenterX; x < heightmap.width(); x += gridWidth) {
				//int xOffset = random.nextInt(heightmap.width()  / values.numSeedsX - 1)
				//		- (heightmap.width()  / values.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
				//int yOffset = random.nextInt(heightmap.height() / values.numSeedsY - 1)
				//		- (heightmap.height() / values.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
				int xOffset = random.nextInt(gridWidthFloor) - gridCenterXFloor;
				int yOffset = random.nextInt(gridHeightFloor) - gridCenterYFloor;
				int seedX = (int) Math.floor(x) + xOffset;		// x-value of seed
				int seedY = (int) Math.floor(y) + yOffset; 		// y-value of seed

				/* heightmap color stuff */
				int heightmapHeight = heightmap.height_xy(seedX, seedY);
				int rgb = mapPointColorGeneration(seedX, seedY, heightmapHeight, properties.seaLevel()); 			// rgb color int value

				/* add point to points array */
				MapPoint mapPoint;
				int type = provinceType(heightmapHeight, properties.seaLevel()); 	// 0: land
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
