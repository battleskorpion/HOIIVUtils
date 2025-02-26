package com.hoi4utils.clausewitz.map.seed;

import com.hoi4utils.clausewitz.map.ProvinceGenConfig;
import com.hoi4utils.clausewitz.map.gen.Heightmap;
import com.hoi4utils.clausewitz.map.gen.MapPoint;

import java.util.Random;

public class RandomSeedGeneration extends AbstractSeedGeneration<MapPoint> {
    public RandomSeedGeneration(ProvinceGenConfig properties, Heightmap heightmap) {
        super(heightmap, properties);
    }

    @Override
    public void generate() {
        Random random = new Random();

        for (int i = 0; i < properties.numSeeds(); i++) {
            boolean repeat;
            do {
                repeat = true;
                int seedX = random.nextInt(heightmap.width());
                int seedY = random.nextInt(heightmap.height());

                /* heightmap color stuff */
                int heightmapHeight = heightmap.height_xy(seedX, seedY);
                int rgb = mapPointColorGeneration(seedX, seedY, heightmapHeight,
                        properties.seaLevel());            // rgb color int value

                /* add point to points array */
                MapPoint mapPoint;
                int type = provinceType(heightmapHeight, properties.seaLevel());
                mapPoint = new MapPoint(seedX, seedY, type);

                /* add point to seeds array */
                if (!seeds.contains(mapPoint)) {
                    mapPoint.setRGB(rgb);
                    seeds.add(mapPoint);
                    repeat = false;
                }
            } while (repeat);
        }
    }
}
