package com.hoi4utils.hoi4mod.map.seed;

import com.hoi4utils.hoi4mod.map.gen.Heightmap;
import com.hoi4utils.hoi4mod.map.gen.MapPoint;
import com.hoi4utils.hoi4mod.map.province.ProvinceGenConfig;

import java.util.Random;

public class HexGridSeedGeneration extends AbstractSeedGeneration<MapPoint> {
    private final double gridHeight;
    private final double gridWidth;
    private final int gridHeightFloor;
    private final int gridWidthFloor;
    private final double gridCenterY;
    private final double gridCenterX;
    private final int gridCenterYFloor;
    private final int gridCenterXFloor;

    public HexGridSeedGeneration(ProvinceGenConfig properties, Heightmap heightmap) {
        super(heightmap, properties);
        gridHeight = (double) heightmap.height() / properties.numSeedsY();
        gridWidth = (double) heightmap.width() / properties.numSeedsX();
        gridHeightFloor = (int) Math.floor(gridHeight);
        gridWidthFloor = (int) Math.floor(gridWidth);
        gridCenterY = gridHeight / 2.0;
        gridCenterX = gridWidth / 2.0;
        gridCenterYFloor = (int) Math.floor(gridCenterY);
        gridCenterXFloor = (int) Math.floor(gridCenterX);
    }

    /**
     * Generates seeds in a grid-like manner, but with a random offset which adds some variance
     * while keeping provinces sizes/distances/density roughly even while still looking
     * semi-random/natural.
     */
    @Override
    public void generate() {
        Random random = new Random();

        // hex cell dimensions
        double hexW = gridWidth;
        double hexH = gridHeight;
        // vertical distance between rows so that they interlock (75% of full cell height)
        double vertSpacing = hexH * 0.75;
        
        for (int row = 0; row < properties.numSeedsY(); row++) {
            double y = gridCenterY + row * vertSpacing;
            // alternate rows shifted right by half a cell
            double rowOffset = (row % 2 == 1) ? hexW / 2.0 : 0;
            for (int col = 0; col < properties.numSeedsX(); col++) {
                double x = gridCenterX + col * hexW + rowOffset;
                
                int xOffset = random.nextInt(gridWidthFloor)  - gridCenterXFloor;
                int yOffset = random.nextInt(gridHeightFloor) - gridCenterYFloor;
                int seedX = (int) Math.floor(x) + xOffset;
                int seedY = (int) Math.floor(y) + yOffset;
                // clamp within bounds 
                seedX = Math.max(0, Math.min(seedX, heightmap.width() - 1));    
                seedY = Math.max(0, Math.min(seedY, heightmap.height() - 1));
                
                int heightmapHeight = heightmap.height_xy(seedX, seedY);
                int rgb = mapPointColorGeneration(seedX, seedY, heightmapHeight, properties.seaLevel());
                int type = provinceType(heightmapHeight, properties.seaLevel());
                
                MapPoint p = new MapPoint(seedX, seedY, type);
                p.setRGB(rgb);
                seeds.add(p);
            }
        }
    }

}
