package mapgen.colorgen;

import hoi4Parser.Parser;
import clausewitz_coding.HOI4Fixes;
import settings.LocalizerSettings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class ColorGenerator {
    private static BufferedImage colorMap;
    private static ArrayList<Color> existingColors;
    private static ArrayList<ArrayList<Color>> colors;

    public static void main(String[] args) throws IOException {
        ColorGenerator colorGenerator = new ColorGenerator();
        HOI4Fixes.settings = new LocalizerSettings();
        colorGenerator.generateColors(100);
    }

    private static void findExistingColors() {
        Parser definitionParser = new Parser(HOI4Fixes.settings.get(MOD_DIRECTORY)
                + "//map//definition.csv");

    }

    public void generateColors(int numColors) {
        /* create colors bmp */
        int imageWidth;
        int imageHeight;
        int numPixels;
        imageWidth = (int) Math.ceil(Math.sqrt(numColors));
        imageHeight = (int) Math.ceil(Math.sqrt(numColors));
        numPixels = imageWidth * imageHeight;
        colorMap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        /* find existing colors */
        findExistingColors();

        /* colors array list */
        colors = new ArrayList<>(imageHeight);
        for (int i = 0; i < imageHeight; i++) {
            colors.add(new ArrayList<>(imageWidth));
        }

        Random random = new Random();

        for (int i = 0; i < numColors; i++) {
            /* gen new color */
            Color color = new Color(random.nextInt(0, 256), random.nextInt(0, 256),
                    random.nextInt(0, 256)); // 0-255
            if (colors.contains(color)) {
                i--;
                continue;
            }

            colorMap.setRGB(i / imageWidth, i % imageWidth, color.getRGB());
            colors.get(i % imageWidth).add(color);
        }

        try {
            ImageIO.write(colorMap, "bmp", new File("colors.bmp"));
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }
}
