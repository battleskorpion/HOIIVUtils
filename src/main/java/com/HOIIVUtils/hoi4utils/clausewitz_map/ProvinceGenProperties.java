package com.HOIIVUtils.hoi4utils.clausewitz_map;

import com.HOIIVUtils.hoi4utils.clausewitz_map.province.ProvinceGenerationType;

import java.awt.*;

public class ProvinceGenProperties {
    protected ProvinceGenerationType generationType = ProvinceGenerationType.GRID_SEED;	//dynamic, GRID_SEED, PROBABILISTIC
    private byte HEIGHTMAP_SEA_LEVEL = 45;       //95;
    //public static final int SEA_LEVEL_INT_RGB = ((SEA_LEVEL_RGB.getRed() << 8) + SEA_LEVEL_RGB.getGreen()) << 8 + SEA_LEVEL_RGB.getBlue();
    protected int imageWidth = 4608; 	// 1024, 512, 256 works	// 5632 - default	// 4608 nad
    protected int imageHeight = 2816;	// 1024, 512, 256 works	// 2048 - default	// 2816 nad
    protected int numSeedsY = 64;
    protected int numSeedsX = 80;

    public static int rgb_white;
    static {
        rgb_white = Color.white.getRed();
        rgb_white = (rgb_white << 8) + Color.white.getGreen();
        rgb_white = (rgb_white << 8) + Color.white.getBlue();
    }

    public ProvinceGenProperties() {
    }

    public ProvinceGenProperties(ProvinceGenerationType generationType, byte seaLevel, int imageWidth, int imageHeight, int numSeedsX, int numSeedsY) {
        this.HEIGHTMAP_SEA_LEVEL = seaLevel;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.numSeedsX = numSeedsX;
        this.numSeedsY = numSeedsY;
        this.generationType = generationType;
    }

    public ProvinceGenProperties(byte seaLevel, int imageWidth, int imageHeight, int numSeedsX, int numSeedsY) {
        this(ProvinceGenerationType.GRID_SEED, seaLevel, imageWidth, imageHeight, numSeedsX, numSeedsY);
    }

    public ProvinceGenProperties(byte seaLevel, int imageWidth, int imageHeight, int numSeeds) {
        /* math
        numSeeds = numSeedsX * numSeedsY
        numSeedsX = numSeeds / numSeedsY
        want to keep x and y proportional
        lets say
        width (x) = 2000
        height (y) = 1600
         */
        this(seaLevel, imageWidth, imageHeight, numSeeds * (), numSeedsY);
    }

    public ProvinceGenProperties(byte sealevel, int imageWidth, int imageHeight) {
        this(sealevel, imageWidth, imageHeight, 64, 80);
    }


    public ProvinceGenerationType generationType() {
        return generationType;
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

    public void setGenerationType(ProvinceGenerationType generationType) {
        this.generationType = generationType;
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

    public void setSeaLevel(int seaLevel) {
        this.HEIGHTMAP_SEA_LEVEL = (byte) seaLevel;
    }

    public void setSeaLevel(Color seaLevel) {
        this.HEIGHTMAP_SEA_LEVEL = (byte) seaLevel.getRed();
    }

}

