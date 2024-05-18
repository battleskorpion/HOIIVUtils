package com.HOIIVUtils.hoi4utils.clausewitz_map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_map.ProvinceGenProperties;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.AbstractMapGeneration;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMapping;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMapping_CPU;
import com.opensimplex2.OpenSimplex2;

import java.io.Serial;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RejectedExecutionException;

import static com.HOIIVUtils.hoi4utils.clausewitz_map.ProvinceGenProperties.rgb_white;

public class DistanceMTDetermination<P extends MapPoint> extends AbstractMapGeneration implements ProvinceDetermination<P>{
    public static final float OFFSET_NOISE_MODIFIER = 1.0f;
    public static final double NOISE_POLLING_FACTOR = 0.025;     // 0.005        // 0.025

    private final Heightmap heightmap;
    private final ProvinceMap provinceMap;
    private ProvinceMapPointsList points;
    private final int threadLimit;
    private final ProvinceGenProperties properties;
    private boolean adjProvinceByGraphConnectivity = false;
    private BorderMapping<P> stateMapList;
    private BorderMap stateBorderMap;


    public DistanceMTDetermination(Heightmap heightmap, ProvinceMap provinceMap, ProvinceGenProperties properties, int threadLimit) {
        this.heightmap = heightmap;
        this.provinceMap = provinceMap;
        this.properties = properties;
        this.threadLimit = threadLimit;
    }

    @Override
    public void generate(BorderMapping<P> stateMapList, BorderMap stateBorderMap) {
        this.stateMapList = stateMapList;
        this.stateBorderMap = stateBorderMap;
        initLists();
        executeProvinceDetermination();
    }

    private void executeProvinceDetermination() {
        ForkColorDetermination forkColorDetermination = new ForkColorDetermination(provinceMap, heightmap);
        ForkJoinPool forkJoinPool;
        if (threadLimit == 0) {
            forkJoinPool = new ForkJoinPool();
        } else {
            forkJoinPool = new ForkJoinPool(threadLimit);
        }
        try {
            forkJoinPool.invoke(forkColorDetermination);
        }
        catch(NullPointerException exc) {
            exc.printStackTrace();
        }
        catch(RejectedExecutionException exc) {
            exc.printStackTrace();
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }
        if (adjProvinceByGraphConnectivity) {
            ForkProvinceConnectivityDetermination forkProvinceConnectivityDetermination
                    = new ForkProvinceConnectivityDetermination(points, heightmap);
            forkJoinPool.invoke(forkProvinceConnectivityDetermination);
        }
    }

    private void initLists() {
        /* initialize points list */
        points = new ProvinceMapPointsList(heightmap.width(), heightmap.height());
    }

    private int offsetWithNoise(int offsetPotential, int seed, int x, int y) {
        double noise = simplexNoise2(seed, x, y, OFFSET_NOISE_MODIFIER);     //multiplier try (2.0f / offsetPotential)
//		long roundedNoise = Math.round(noise); // Round to the nearest integer        // may lead to more even distribution when int cast occurs.
        return (int) (offsetPotential * noise);
    }

    private float simplexNoise2(int seed, int x, int y) {
        return simplexNoise2(seed, x, y, 1);
    }

    private float simplexNoise2(int seed, int x, int y, float multiplier) {
        return OpenSimplex2.noise2(seed, x * NOISE_POLLING_FACTOR, y * NOISE_POLLING_FACTOR) * multiplier;
    }

    /**
     * Determines color from closest seed to point x,y.
     * <p>
     * The x- and y-offset is for adding variation to the province the point belongs to. The color will be
     * determined relative to the closest seed to the offset point, but can be assigned to the original xy point.
     * It is not checked whether the x- and y-coordinates, or the offset coordinates, are within a valid range,
     * so that variation still works near the map edges.
     * This method can be called any number of times with the same point. </p>
     * @param x x-coordiate of point
     * @param xOffset
     * @param y y-coordinate of point
     * @param yOffset
     * @param mapPoints collection of map seeds to use when determining color of this point by distance with offset.
     * @return
     */
    private int determineColor(int x, int xOffset, int y, int yOffset, Collection<P> mapPoints) {
        return determineColor(x + xOffset, y + yOffset, mapPoints);
    }

    /**
     * Determines color from closest seed to point x,y.
     * <p>
     * This method adds no offset, so if variation is needed, offset should be added to the x/y
     * coordinates before calling this method, or by using the <code>determineColor</code> with offset method.
     * It is not checked whether the x- and y-coordinates are within a valid range, so that variation
     * still works near the map edges.
     * This method can be called any number of times with the same point. </p>
     * @param x x-coordiate of point
     * @param y y-coordinate of point
     * @param seeds collection of map seeds to use when determining color of this point by distance.
     * @return
     */
    private int determineColor(int x, int y, final Collection<P> seeds) {
        // (default white)
        int nearestColor = rgb_white;     // color of nearest seed (int value)
        int dist = Integer.MAX_VALUE;            // select a big number

        // todo stream operation?
        for (MapPoint point : seeds) {
            // calculate the difference in x and y direction
            int xdiff = point.x - x;
            int ydiff = point.y - y;

            // calculate current squared Euclidean distance, for comparing only
            int cdist = xdiff * xdiff + ydiff * ydiff;

            if (cdist < dist) {
                nearestColor = point.rgb();
                dist = cdist;
            }
        }

        return nearestColor;
    }

    /**
     * Pixel color determination using {@link RecursiveAction} for multithreading efficiency.
     *
     * @see RecursiveAction
     * @see OpenSimplex2
     */
    public class ForkColorDetermination extends RecursiveAction {

        /**
         * Auto-generated serialVersionUID
         */
        @Serial
        private static final long serialVersionUID = 7925866053687723919L;
        /** float datatype is used by simplex noise, and may improve performance over double */
        protected static int splitThreshold = 16;       // was 8
        private static final int seed;

        /**
         * y-value to start at (inclusive)
         */
        private final int startY;

        /**
         * y-value to go until (exclusive)
         */
        private final int endY;

        /**
         * number of y-values to work with
         */
        private final int dy;

        /**
         * simplex noise to offset color determination
         */
        private OpenSimplex2 noise;
        private final ProvinceMap provinceMap;
        private final Heightmap heightmap;

        static {
            Random random = new Random();
            seed = random.nextInt();
        }

//		private Iterator<Map.Entry<ProvinceMapPoint, Integer>> seedsRGBMapIterator;

        /**
         * constructor (y set as 0 to imageHeight). Recommended constructor for initial initialization.
         */
        public ForkColorDetermination(ProvinceMap provinceMap, Heightmap heightmap) {
            this(provinceMap, heightmap, 0, heightmap.height());
        }

        /**
         * constructor
         * // todo pass in prev fork color determination instead of province map, heightmap?
         */
        public ForkColorDetermination(ProvinceMap provinceMap, Heightmap heightmap, int startY, int endY) {
            this.provinceMap = provinceMap;
            this.heightmap = heightmap;
            this.startY = startY;
            this.endY = endY;
            dy = endY - startY;
        }

        @Override
        protected void compute() {
            if (dy <= splitThreshold) {
                computeDirectly();
                return;
            }

            int split = dy / 2;

            invokeAll(new ForkColorDetermination(provinceMap, heightmap, startY, startY + split),
                    new ForkColorDetermination(provinceMap, heightmap, startY + split, endY));
        }

        /**
         * Determine color for each point
         */
        protected void computeDirectly() {
            final int widthPerSeed = heightmap.width()  / properties.numSeedsX();
            final int heightPerSeed = heightmap.height() / properties.numSeedsY();
            final int offsetPotential = 4;
            System.out.println("run: " + startY + ", " + endY);

            try {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < heightmap.width(); x++) {
                        int rgb;
                        int heightmapHeight = heightmap.height_xy(x, y);
                        int stateBorderValue = stateBorderMap.getRGB(x, y);
                        int type = provinceType(heightmapHeight, properties.seaLevel());

                        int xOffset = offsetWithNoise(offsetPotential, seed, x, y);    //TODO work on values
                        int yOffset = offsetWithNoise(offsetPotential, seed, x, y);
                        rgb = determineColor(x, xOffset, y, yOffset, stateMapList.seedsList(stateBorderValue, type));

                        points.setRGB(x, y, rgb);
                        provinceMap.setRGB(x, y, rgb);
                    }
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }

    }
}
