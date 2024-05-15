package com.HOIIVUtils.hoi4utils.clausewitz_map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_map.ProvinceGenProperties;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.AbstractMapGeneration;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMapping;
import com.aparapi.Kernel;
import com.aparapi.Range;

public class DistanceGPUDetermination<P extends MapPoint> extends AbstractMapGeneration implements ProvinceDetermination<P> {
    private Heightmap heightmap;
    private ProvinceMap provinceMap;
    private ProvinceMapPointsList points;
    private ProvinceGenProperties properties;
    private BorderMapping<P> stateMapList;
    private BorderMap stateBorderMap;

    public DistanceGPUDetermination(Heightmap heightmap, ProvinceMap provinceMap, ProvinceGenProperties properties) {
        this.heightmap = heightmap;
        this.provinceMap = provinceMap;
        this.properties = properties;
    }

    @Override
    public void generate(BorderMapping<P> stateMapList, BorderMap stateBorderMap) {
        this.stateMapList = stateMapList;
        this.stateBorderMap = stateBorderMap;
        points = new ProvinceMapPointsList(heightmap.width(), heightmap.height());

        /* gpu calculation time */
        final int offsetPotential = 4;

        final int[] rgb_values = new int[heightmap.width() * heightmap.height()];
        DistanceKernel kernel = new DistanceKernel(rgb_values);
//        DistanceKernel kernel = new DistanceKernel();
        kernel.execute(Range.create2D(heightmap.width(), heightmap.height()));
        for (int y = 0; y < heightmap.height(); y++) {
            for (int x = 0; x < heightmap.width(); x++) {
                points.setRGB(x, y, rgb_values[y * heightmap.width() + x]);
                provinceMap.setRGB(x, y, rgb_values[y * heightmap.width() + x]);
            }
        }
    }

    private class DistanceKernel extends Kernel {
        final byte[] heightmap_values;
          final int[] rgb_values;
        final int[] seedsX; // Array to hold x-coordinates of seeds
        final int[] seedsY; // Array to hold y-coordinates of seeds
        final int[] seedsRGB;
        final int[] borderAreas;
        final int[] stateBorderValues;
        final int numSeeds;
        final int heightmap_width = heightmap.width();
        //final int heightmap_height = heightmap.height();

        public DistanceKernel(final int[] rgb_values) {
            // heightmap
            byte[][] heightmap2D = heightmap.heightmap();
            int height = heightmap2D.length;
            int width = heightmap2D[0].length;
            heightmap_values = new byte[height * width];
            for (int i = 0; i < height; i++) {
                System.arraycopy(heightmap2D[i], 0, heightmap_values, i * width, width);
            }
            this.rgb_values = rgb_values;
            //final Collection<P> seeds = stateMapList.seedsList(stateBorderValue, type);
            var seeds = stateMapList.seedsList();
            numSeeds = seeds.size();
            seedsX = new int[numSeeds];
            seedsY = new int[numSeeds];
            seedsRGB = new int[numSeeds];
            int i = 0;
            for (P seed : seeds) {
                seedsX[i] = seed.x;
                seedsY[i] = seed.y;
                seedsRGB[i] = seed.rgb();
                i++;
            }
            // seed border map values
            var borderAreas = stateMapList.getSeedsCorrelatingBorderAreas(seeds);
            this.borderAreas = borderAreas.stream().mapToInt(Integer::intValue).toArray();
            // xy state border map values
            stateBorderValues = new int[heightmap.height() * heightmap.width()];
            for (int y = 0; y < heightmap.height(); y++) {
                for (int x = 0; x < heightmap.width(); x++) {
                    stateBorderValues[y * heightmap.width() + x] = stateBorderMap.getRGB(x, y);
                }
            }
        }

        @Override
        public void run() {
            final int x = getGlobalId(0);
            final int y = getGlobalId(1);
            //final int heightmapHeight = heightmap_values[y][x] & 0xFF; //todo also no 2d
            //final int stateBorderValue = stateBorderMap.getRGB(x, y);
            //final int type = provinceType(heightmapHeight, properties.seaLevel()); todo

            //int xOffset = offsetWithNoise(offsetPotential, seed, x, y);
            //int yOffset = offsetWithNoise(offsetPotential, seed, x, y);
            /*
             rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));
             */
            int xOffset = 0;
            int yOffset = 0;
            //int nearestColor = ProvinceGenProperties.rgb_white;        // does NOT compile
            int nearestColor = 16777215;
            int dist = Integer.MAX_VALUE;

            for (int i = 0; i < numSeeds; i++) {
                // calculate the difference in x and y direction
                int xdiff = seedsX[i] - x;
                int ydiff = seedsY[i] - y;

                // calculate current squared Euclidean distance, for comparing only
                int cdist = xdiff * xdiff + ydiff * ydiff;
                // only consider same state
                dist = stateBorderValues[y * heightmap_width + x] != borderAreas[i] ? Integer.MAX_VALUE : dist;

                if (cdist < dist) {
                    nearestColor = seedsRGB[i];
                    dist = cdist;
                }
            }

            rgb_values[y * heightmap_width + x] = nearestColor;
        }
    }
}
