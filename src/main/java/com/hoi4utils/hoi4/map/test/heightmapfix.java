package com.hoi4utils.hoi4.map.test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class heightmapfix {

    private static BufferedImage heightmap;
    public static final int MAX = 255;
    public static final int SEA_LEVEL = 95;     //45;

    public static void main(String[] args) throws IOException {
        heightmap = ImageIO.read(new File("resources/map/heightmap_balkan_ex.bmp"));

        for (int x = 0; x < heightmap.getWidth(); x++) {
            for (int y = 0; y < heightmap.getHeight(); y++) {
                int rgb = heightmap.getRGB(x, y);

                Color color = new Color(rgb);
                int r = color.getRed();
                if (r == 0) {
                    r = 40;     //r = 40;   // r = 90;
                } else {
                    double theta = 1 - ((double) r / MAX);
                    r += (46 * theta);      //r += (46 * theta);    // r += (96 * theta)
                }

                color = new Color(r, r, r);
                rgb = colorToInt(color);

                heightmap.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(heightmap, "bmp", new File("resources/map/heightmap.bmp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int colorToInt(Color color) {
        int rgb;
        // Color -> int
        rgb = color.getRed();
        rgb = (rgb << 8) + color.getGreen();
        rgb = (rgb << 8) + color.getBlue();
        return rgb;
    }
}
