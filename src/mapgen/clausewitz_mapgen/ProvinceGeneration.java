package mapgen.clausewitz_mapgen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

//import static mapgen.values.stateBorderMap;

public class ProvinceGeneration {
    private static BufferedImage provinceMap;
    private static ProvinceMapPointsList points;
    private static ProvinceMapSeedsList seeds;
    private static StateMapList stateMapList;

    public static void main(String[] args) {
        /* values */

        provinceGeneration();

        try {
            ImageIO.write(provinceMap, "bmp", new File("output.bmp"));
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }

        //System.out.println(points.get(1, 1).rgb);
    }

    public static void provinceGeneration() {
        /* create new image (map) */
        provinceMap = new BufferedImage(MapgenValues.imageWidth, MapgenValues.imageHeight,
                BufferedImage.TYPE_INT_RGB);

        /* initialize points list */
        points = new ProvinceMapPointsList(MapgenValues.imageWidth, MapgenValues.imageHeight);

        /* initialize mapping of seeds to states (regions for purposes of province generation) */
        // TODO: optimization may be possible
        stateMapList = new StateMapList();

        /* white canvas (kinda unnecessary) */
        for (int y = 0; y < MapgenValues.imageHeight; y++) {
            for (int x = 0; x < MapgenValues.imageWidth; x++) {
                // set color at pixel cords
//                provinceMap.setRGB(x, y, MapgenValues.rgb_white);
            }
        }

        /*
         * values - load bmp images
         */

//        try {
//             MapgenValues.heightmap = ImageIO.read(new File(MapgenValues.heightmapName));    		// loadBMPImage("heightmap.bmp");
//            MapgenValues.stateBorderMap = ImageIO.read(new File(MapgenValues.stateBordersName));    // loadBMPImage("state_borders.bmp");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
        //TODO make below workie
        //MapgenValues.imageWidth = MapgenValues.heightmap.getWidth();
        //MapgenValues.imageHeight = MapgenValues.heightmap.getHeight(); 	// may break things but good idea

        /* seeds generation */
        if(MapgenValues.generationType == ProvinceGenerationType.GRID_SEED) {
            gridSeedGeneration();
        }
        else if(MapgenValues.generationType == ProvinceGenerationType.DYNAMIC) {
            dynamicSeedGeneration();
        }
        else {
            System.out.println("HELP");
        }
    }

    /**
     * Generates seeds in a grid-like manner, but with a random offset which adds some variance
     * while keeping provinces sizes/distances/density roughly even while still looking
     * semi-random/natural.
     */
    private static void gridSeedGeneration() {
        Random random = new Random(); 		// Random number generator for generating seed locations
        seeds = new ProvinceMapSeedsList();

//        for(int y = MapgenValues.imageHeight / MapgenValues.numSeedsY / 2 - 1; y < MapgenValues.imageHeight;
//            y += MapgenValues.imageHeight / MapgenValues.numSeedsY) {
//            for(int x = MapgenValues.imageWidth / MapgenValues.numSeedsX / 2 - 1; x < MapgenValues.imageWidth;
//                x += MapgenValues.imageWidth / MapgenValues.numSeedsX) {
//                int xOffset = random.nextInt(MapgenValues.imageWidth  / MapgenValues.numSeedsX - 1)
//                        - (MapgenValues.imageWidth  / MapgenValues.numSeedsX / 2 - 1); 	// -3 to 3		// should make variables	// int xOffset = random.nextInt(numSeedsX - 1) - (numSeedsX / 2 - 1);
//                int yOffset = random.nextInt(MapgenValues.imageHeight / MapgenValues.numSeedsY - 1)
//                        - (MapgenValues.imageHeight / MapgenValues.numSeedsY / 2 - 1); 	// -3 to 3		// should make variables	// int yOffset = random.nextInt(numSeedsY - 1) - (numSeedsY / 2 - 1);
//                int seedX = x + xOffset;		// x-value of seed
//                int seedY = y + yOffset; 		// y-value of seed
//
//                /* heightmap color stuff */
//                // heightmap is in grayscale meaning only need to find red value to get height value
//                // at point.
//                int heightmapHeight = (MapgenValues.heightmap.getRGB(seedX, seedY) >> 16) & 0xFF;
//                int rgb = provinceColorGeneration(seedX, seedY, heightmapHeight); 			// rgb color int value
//                int stateMapColor = stateBorderMap.getRGB(seedX, seedY);
//
//                /* add point to points array */
//                ProvinceMapPoint provinceMapPoint;
//                /* calculate sea or land prov. */
//                int type = 0; // 0: land
//                if (heightmapHeight < MapgenValues.HEIGHTMAP_SEA_LEVEL) {
//                    type = 1; // 1: sea
//                }
//
//                provinceMapPoint = new ProvinceMapPoint(seedX, seedY, true, type);
//                points.set(provinceMapPoint);
//
//                /* add point to seeds array */
//                // x and y needed
////				Integer rgbInteger = rgb;
//                Point point = new Point(seedX, seedY);
//                seeds.add(provinceMapPoint, rgb);		//TODO rgb? now in map point idk hmmmmmm
//                provinceMapPoint.rgb = rgb;
////				stateSeedsMap.put(provinceMapPoint, stateMapColor);
//                stateMapList.addSeed(stateMapColor, provinceMapPoint);
//
//                // TODO no not here
//                // TODO don't need thiS???? #redundant
//                // set color at pixel cords
//                try {
//                    provinceMap.setRGB(seedX, seedY, rgb);
//
//                }
//                catch (ArrayIndexOutOfBoundsException exc) {
//                    exc.printStackTrace();
//                    System.out.println("x: " + (seedX));
//                    System.out.println("y: " + (seedY));
//                    return;
//                    //continue; 	// skip rest of iteration
//                }
//            }
//        }
    }

    /**
     * Generates seeds dynamically, until there is a set number of seeds. Seed density of sea can be set lower
     * than land.
     */
    private static void dynamicSeedGeneration()	{
//        Random random = new Random(); 		// Random number generator for generating seed locations
//        seeds = new ProvinceMapSeedsList();
//
//		/*
//		create regions/islands
//		 */
//        long seed = random.nextLong();
//        int avg = 0;
//
//        for(int y = 0; y < MapgenValues.imageWidth; y++) {
//            for (int x = 0; x < MapgenValues.imageWidth; x++) {
//                avg += OpenSimplex2.noise2(seed, x, y);
//            }
//        }
//
//        avg /= MapgenValues.numPoints;
//        System.out.println(avg);
//
    }
}
