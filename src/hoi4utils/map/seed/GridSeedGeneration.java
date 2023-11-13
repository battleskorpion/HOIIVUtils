package hoi4utils.map.seed;

import hoi4utils.map.Heightmap;
import hoi4utils.map.MapPoint;
import hoi4utils.map.values;

import java.util.Random;

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
		
		for(int y = heightmap.getHeight() / values.numSeedsY / 2 - 1; y < heightmap.getHeight();
		    y += heightmap.getHeight() / values.numSeedsY) {
			for(int x = heightmap.getWidth() / values.numSeedsX / 2 - 1; x < heightmap.getWidth();
			    x += heightmap.getWidth() / values.numSeedsX) {
				int xOffset = random.nextInt(heightmap.getWidth()  / values.numSeedsX - 1)
						- (heightmap.getWidth()  / values.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
				int yOffset = random.nextInt(heightmap.getHeight() / values.numSeedsY - 1)
						- (heightmap.getHeight() / values.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
				int seedX = x + xOffset;		// x-value of seed
				int seedY = y + yOffset; 		// y-value of seed

				/* heightmap color stuff */
				// heightmap is in grayscale meaning only need to find red value to get height value
				// at point.
				int heightmapHeight = (heightmap.getRGB(seedX, seedY) >> 16) & 0xFF;
				int rgb = mapPointColorGeneration(seedX, seedY, heightmapHeight); 			// rgb color int value

				/* add point to points array */
				MapPoint mapPoint;
				/* calculate sea or land prov. */
				int type = provinceType(heightmapHeight); 	// 0: land
				// 1: sea
				mapPoint = new MapPoint(seedX, seedY, type);        // was true for seed (is a seed)
				// points.set(mapPoint);        // todo maybe only add to seeds list instead

				/* add point to seeds array */
				// x and y needed
//				Integer rgbInteger = rgb;
//				Point point = new Point(seedX, seedY);
				mapPoint.setRGB(rgb);
				seeds.add(mapPoint);		//TODO rgb? now in map point idk hmmmmmm
//				stateSeedsMap.put(provinceMapPoint, stateMapColor);

//				// TODO no not here
//				// todo not here
//				// TODO don't need thiS???? #redundant
//				// set color at pixel cords
//				try {
//					provinceMap.setRGB(seedX, seedY, rgb);
//				}
//				catch (ArrayIndexOutOfBoundsException exc) {
//					exc.printStackTrace();
//					System.out.println("x: " + (seedX));
//					System.out.println("y: " + (seedY));
//					return;
//					//continue; 	// skip rest of iteration
//				}
			}
		}
	}

}
