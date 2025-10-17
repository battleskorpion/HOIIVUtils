package com.hoi4utils.hoi4mod.map.province;

import com.hoi4utils.hoi4mod.map.gen.MapGenProperties;
import com.hoi4utils.hoi4mod.map.gen.SeedGenProperties;
import com.hoi4utils.hoi4mod.map.seed.SeedGenType;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

public class ProvinceGenConfig implements MapGenProperties, SeedGenProperties {
    protected SeedGenType generationType; // dynamic, GRID_SEED, PROBABILISTIC
    protected ProvinceDeterminationType determinationType;
    private byte HEIGHTMAP_SEA_LEVEL; // = 45 //95;
    // public static final int SEA_LEVEL_INT_RGB = ((SEA_LEVEL_RGB.getRed() << 8) +
    // SEA_LEVEL_RGB.getGreen()) << 8 + SEA_LEVEL_RGB.getBlue();
    protected int imageWidth; // 1024, 512, 256 works // 5632 - default // 4608 nad
    protected int imageHeight; // 1024, 512, 256 works // 2048 - default // 2816 nad
    protected int numSeedsY = 64;
    protected int numSeedsX = 80;

    /**
     * threadLimit = 0: max/no limit
     */
    @Setter @Getter
    protected int threadLimit = 0;

    public static final int rgb_white;
    static {
        int rgb_white_temp = Color.white.getRed();
        rgb_white_temp = (rgb_white_temp << 8) + Color.white.getGreen();
        rgb_white = (rgb_white_temp << 8) + Color.white.getBlue();
    }

    public ProvinceGenConfig(ProvinceGenConfig properties) {
        this.asCopyOf(properties);
    }

    public ProvinceGenConfig(int seaLevel, int imageWidth, int imageHeight, int threadLimit) {
        this((byte) seaLevel, imageWidth, imageHeight, threadLimit);
    }

    public ProvinceGenConfig(byte sealevel, int imageWidth, int imageHeight, int threadLimit) {
        this(sealevel, imageWidth, imageHeight, 64, 80, threadLimit);
    }

    public ProvinceGenConfig(int seaLevel, int imageWidth, int imageHeight, int numSeedsX, int numSeedsY, int threadLimit) {
        this(SeedGenType.GRID, ProvinceDeterminationType.DISTANCE_MULTITHREADED, (byte) seaLevel, imageWidth,
                imageHeight, numSeedsX, numSeedsY, threadLimit);
    }

    public ProvinceGenConfig(int seaLevel, byte imageWidth, int imageHeight, int numSeedsX, int numSeedsY, int threadLimit) {
        this(SeedGenType.GRID, ProvinceDeterminationType.DISTANCE_MULTITHREADED, seaLevel, imageWidth, imageHeight,
                numSeedsX, numSeedsY, threadLimit);
    }

    public ProvinceGenConfig(SeedGenType generationType, ProvinceDeterminationType determinationType, int seaLevel,
                             int imageWidth, int imageHeight, int numSeedsX, int numSeedsY, int threadLimit) {
        this(generationType, determinationType, (byte) seaLevel, imageWidth, imageHeight, numSeedsX, numSeedsY, threadLimit);
    }

    public ProvinceGenConfig(SeedGenType generationType, ProvinceDeterminationType determinationType, byte seaLevel,
                             int imageWidth, int imageHeight, int numSeedsX, int numSeedsY, int threadLimit) {
        this.HEIGHTMAP_SEA_LEVEL = seaLevel;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.numSeedsX = numSeedsX;
        this.numSeedsY = numSeedsY;
        this.generationType = generationType;
        this.determinationType = determinationType;
        this.threadLimit = threadLimit;
    }

    public ProvinceGenConfig(int seaLevel, int imageWidth, int imageHeight, int numSeeds, int threadLimit) {
        this(SeedGenType.GRID, ProvinceDeterminationType.DISTANCE_MULTITHREADED, seaLevel,
                imageWidth, imageHeight, numSeeds, threadLimit);
    }

    public ProvinceGenConfig(SeedGenType generationType, ProvinceDeterminationType determinationType, int seaLevel,
                             int imageWidth, int imageHeight, int numSeeds, int threadLimit) {
        this.generationType = generationType;
        this.determinationType = determinationType;
        this.HEIGHTMAP_SEA_LEVEL = (byte) seaLevel;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.threadLimit = threadLimit;

        setNumSeeds(numSeeds);
    }

    private static int[] solveNumSeedsXY(double imageWidth, double imageHeight, int numSeeds) {
        /*
         * math
         * numSeeds [s] = numSeedsX * numSeedsY
         * numSeedsX [x] = numSeeds / numSeedsY
         * numSeedsY [y] = numSeeds / numSeedsX
         * want to keep x and y proportional
         * lets say
         * width [w] = 2000
         * height [h] = 1600
         * s = 200
         * width * height = 3,200,000 = p
         * sqrt(w * h) = ~1789
         * x / y = r
         * want:
         * x / y = w / h
         * w / h = r
         * r = 1.25
         * x = wy/h
         * y = hx/w
         * x / y = 1.25
         * x = 1.25y, 1.25 x per y
         * y * 1.25 y = s
         * 200 = 1.25y^2
         * 200/1.25 = 200/r = 160
         * 160 = y^2
         * y = 12.65
         * round y to most convenient value
         * 12? or 13?
         * y = 12:
         * y * 1.25y ~= s
         * 12 * 1.25(12) = 180
         * y = 13:
         * y * 1.25y ~= s
         * 13 * 1.25(13) = 211.25
         * closer to 200
         * squares aren't linear so you can't just round up or down y for the best
         * approximation.
         */
        double r = imageWidth / imageHeight;
        // numSeedsX = r * numSeedsY
        // numSeeds = r * (numSeedsY)^2
        double y_squared = numSeeds / r;
        double y_exact = Math.sqrt(y_squared);
        int y_floor = (int) Math.floor(y_exact);
        int y_ceil = (int) Math.ceil(y_exact);
        // x * y ~= numSeeds
        int x_1 = (int) Math.floor((double) numSeeds / y_floor);
        int x_2 = (int) Math.ceil((double) numSeeds / y_floor);
        int x_3 = (int) Math.floor((double) numSeeds / y_ceil);
        int x_4 = (int) Math.ceil((double) numSeeds / y_ceil);
        int x_1_d = Math.abs(numSeeds - x_1 * y_floor);
        int x_2_d = Math.abs(numSeeds - x_2 * y_floor);
        int x_3_d = Math.abs(numSeeds - x_3 * y_ceil);
        int x_4_d = Math.abs(numSeeds - x_4 * y_ceil);
        int[][] solution = { { y_floor, x_1, x_1_d }, { y_floor, x_2, x_2_d }, { y_ceil, x_3, x_3_d },
                { y_ceil, x_4, x_4_d } };
        Arrays.sort(solution, Comparator.comparingInt(a -> a[2]));
        return solution[0];
    }

    public SeedGenType generationType() {
        return generationType;
    }

    public void setGenerationType(SeedGenType generationType) {
        this.generationType = generationType;
    }

    public ProvinceDeterminationType determinationType() {
        return determinationType;
    }

    public void setDeterminationType(ProvinceDeterminationType determinationType) {
        this.determinationType = determinationType;
    }

    public Color seaLevel_RGB() {
        return new Color(HEIGHTMAP_SEA_LEVEL, HEIGHTMAP_SEA_LEVEL, HEIGHTMAP_SEA_LEVEL);
    }

    public int seaLevel() {
        return HEIGHTMAP_SEA_LEVEL;
    }

    public int seaLevel_INT_RGB() {
        Color SEA_LEVEL_RGB = seaLevel_RGB();
        return ((SEA_LEVEL_RGB.getRed() << 8) + SEA_LEVEL_RGB.getGreen() << 8) + SEA_LEVEL_RGB.getBlue();
    }

    public int numSeeds() {
        return numSeedsX * numSeedsY;
    }

    public int numPoints() {
        return imageWidth * imageHeight;
    }

    public int imageWidth() {
        return imageWidth;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public int numSeedsY() {
        return numSeedsY;
    }

    public int numSeedsX() {
        return numSeedsX;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setNumSeedsY(int numSeedsY) {
        this.numSeedsY = numSeedsY;
    }

    public void setNumSeedsX(int numSeedsX) {
        this.numSeedsX = numSeedsX;
    }

    public void setNumSeeds(int numSeeds) {
        int[] solution = solveNumSeedsXY(imageWidth, imageHeight, numSeeds);
        this.numSeedsY = solution[0];
        this.numSeedsX = solution[1];
    }

    public void setSeaLevel(int seaLevel) {
        this.HEIGHTMAP_SEA_LEVEL = (byte) seaLevel;
    }

    public void setSeaLevel(Color seaLevel) {
        this.HEIGHTMAP_SEA_LEVEL = (byte) seaLevel.getRed();
    }

    public void asCopyOf(ProvinceGenConfig propertiesUpdated) {
        this.generationType = propertiesUpdated.generationType;
        this.determinationType = propertiesUpdated.determinationType;
        this.HEIGHTMAP_SEA_LEVEL = propertiesUpdated.HEIGHTMAP_SEA_LEVEL;
        this.imageWidth = propertiesUpdated.imageWidth;
        this.imageHeight = propertiesUpdated.imageHeight;
        this.numSeedsX = propertiesUpdated.numSeedsX;
        this.numSeedsY = propertiesUpdated.numSeedsY;
    }

    /**
     * Use this when a thread limit of 0 is not interpreted as 'no limit' by some API.
     * @return the thread limit, or the number of processors available to the JVM if the thread limit is 0
     */
    public int getThreadLimitNonzero() {
        if (threadLimit == 0) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            return threadLimit;
        }
    }
}
