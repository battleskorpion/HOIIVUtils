package hoi4utils.mapgen.test.mapgen;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class values {
	public static String heightmapName = "src\\hoi4utils\\mapgen\\resources\\heightmap.bmp";			//"heightmap_new_england.bmp";
	public static String stateBordersName = "src\\hoi4utils\\mapgen\\resources\\state_borders_none.bmp"; //"state_borders_new_england.bmp";
	public static ProvinceGenerationType generationType = ProvinceGenerationType.GRID_SEED;	//dynamic

	public values() {
		// convert Color.white to int representation
		// Color -> int
		values.rgb_white = Color.white.getRed();
		values.rgb_white = (rgb_white << 8) + Color.white.getGreen();
		values.rgb_white = (rgb_white << 8) + Color.white.getBlue();
	}

	protected static final int HEIGHTMAP_SEA_LEVEL = 45;//95;
	public static final Color SEA_LEVEL_RGB = new Color(HEIGHTMAP_SEA_LEVEL, HEIGHTMAP_SEA_LEVEL, HEIGHTMAP_SEA_LEVEL); ;
	public static final int SEA_LEVEL_INT_RGB = ((SEA_LEVEL_RGB.getRed() << 8) + SEA_LEVEL_RGB.getGreen()) << 8 + SEA_LEVEL_RGB.getBlue();
	protected static int imageWidth = 4608; 	// 1024, 512, 256 works	// 5632 - default	// 4608 nad
	protected static int imageHeight = 2816;	// 1024, 512, 256 works	// 2048 - default	// 2816 nad
	protected static int numSeedsY = 64; 		// 64 is ok	// 64^2 = 4096
	protected static int numSeedsX = 64; 		// 64 is ok // 64^2 = 4096
	protected static int numSeeds = numSeedsX * numSeedsY;
	protected static int numPoints = imageWidth * imageHeight;
//	protected static ArrayList<HashMap<Integer, HashMap<Point, Integer>>> seedsRGBValueMaps
//			= new ArrayList<HashMap<Integer, HashMap<Point, Integer>>>(2);
	protected static int rgb_white;
	protected static BufferedImage heightmap; 			// elevation data heightmap
	protected static BufferedImage stateBorderMap; 		// heightmap of preferred borders
//	private boolean constructorCalled = false; 			// whether the constructor of this class has been called previously
}


