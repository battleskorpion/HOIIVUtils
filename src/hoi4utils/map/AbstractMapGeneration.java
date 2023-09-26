package hoi4utils.map;

import hoi4utils.map.province.BorderMap;

import java.awt.*;
import java.util.Random;

public abstract class AbstractMapGeneration {
	protected static int mapPointColorGeneration(int seedX, int seedY, int heightmapHeight, BorderMap borderMap) {
		int rgb;                        // rgb color int value
		int stateMapColor;
		Color color;
		Random random = new Random();

		// state (region) point belongs to, should (if reasonable) obtain color of seed from
		// the same region (maintaining state boundaries)
		try {
			stateMapColor = borderMap.getRGB(seedX, seedY);
		} catch (ArrayIndexOutOfBoundsException exc) {
			System.err.println("x: " + seedX + ", y: " + seedY);
		}

		aa:
		do {
			/* prov color */
			if (heightmapHeight < values.HEIGHTMAP_SEA_LEVEL) {
				// generate new color until unique color generated (color does not exist already)
				color = new Color(random.nextInt(64), random.nextInt(64), random.nextInt(64));

				rgb = colorToInt(color);
			}
			else {
				// generate new color until unique color generated (color does not exist already)
				color = new Color(random.nextInt(192) + 64, random.nextInt(192) + 64, random.nextInt(192) + 64);

				// Color -> int
				rgb = colorToInt(color);
			}

			/**
			 * check if color already exists
			 */
//			if (values.seedsRGBValues.containsValue(rgb)) {		// TODO should reimplement!!!!
//				continue aa;
//			}
		}
		while(false);

		return rgb;
	}

	public static int colorToInt(Color color) {
		int rgb;
		// Color -> int
		rgb = color.getRed();
		rgb = (rgb << 8) + color.getGreen();
		rgb = (rgb << 8) + color.getBlue();
		return rgb;
	}

	protected static int provinceType(int heightmapHeight) {
		if (heightmapHeight < values.HEIGHTMAP_SEA_LEVEL) {
			return 1; 	// sea
		}
		return 0; 		// land
	}
}
