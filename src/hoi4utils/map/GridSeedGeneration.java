package hoi4utils.map;

import hoi4utils.map.province.Heightmap;
import hoi4utils.map.province.MapPoint;
import hoi4utils.map.province.ProvinceMapSeedsList;
import hoi4utils.map.province.values;

import java.awt.*;
import java.util.Random;

import static hoi4utils.map.province.values.stateBorderMap;

public class GridSeedGeneration extends AbstractSeedGeneration {
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
		seeds = new ProvinceMapSeedsList();

		for(int y = values.imageHeight / values.numSeedsY / 2 - 1; y < values.imageHeight;
		    y += values.imageHeight / values.numSeedsY) {
			for(int x = values.imageWidth / values.numSeedsX / 2 - 1; x < values.imageWidth;
			    x += values.imageWidth / values.numSeedsX) {
				int xOffset = random.nextInt(values.imageWidth  / values.numSeedsX - 1)
						- (values.imageWidth  / values.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
				int yOffset = random.nextInt(values.imageHeight / values.numSeedsY - 1)
						- (values.imageHeight / values.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
				int seedX = x + xOffset;		// x-value of seed
				int seedY = y + yOffset; 		// y-value of seed

				/* heightmap color stuff */
				// heightmap is in grayscale meaning only need to find red value to get height value
				// at point.
				int heightmapHeight = (values.heightmap.getRGB(seedX, seedY) >> 16) & 0xFF;
				int rgb = provinceColorGeneration(seedX, seedY, heightmapHeight); 			// rgb color int value
				int stateMapColor = stateBorderMap.getRGB(seedX, seedY);

				/* add point to points array */
				MapPoint mapPoint;
				/* calculate sea or land prov. */
				int type = provinceType(heightmapHeight); 	// 0: land
				// 1: sea
				mapPoint = new MapPoint(seedX, seedY, true, type);
				points.set(mapPoint);

				/* add point to seeds array */
				// x and y needed
//				Integer rgbInteger = rgb;
				Point point = new Point(seedX, seedY);
				seeds.add(mapPoint, rgb);		//TODO rgb? now in map point idk hmmmmmm
				mapPoint.rgb = rgb;
//				stateSeedsMap.put(provinceMapPoint, stateMapColor);
				stateMapList.addSeed(stateMapColor, mapPoint);

				// TODO no not here
				// TODO don't need thiS???? #redundant
				// set color at pixel cords
				try {
					provinceMap.setRGB(seedX, seedY, rgb);
				}
				catch (ArrayIndexOutOfBoundsException exc) {
					exc.printStackTrace();
					System.out.println("x: " + (seedX));
					System.out.println("y: " + (seedY));
					return;
					//continue; 	// skip rest of iteration
				}
			}
		}
	}
}
