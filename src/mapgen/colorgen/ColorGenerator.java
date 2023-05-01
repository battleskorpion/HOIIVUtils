package mapgen.colorgen;

import clausewitz_parser.Expression;
import clausewitz_parser.Parser;
import settings.LocalizerSettings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class ColorGenerator {
    private static BufferedImage colorMap;
    private static HashSet<Color> existingColors;
    private static HashSet<Color> colors;
    private static int rMin = 0;
    private static int rMax = 255;
    private static int gMin = 0;
    private static int gMax = 255;
    private static int bMin = 0;
    private static int bMax = 255;

    // rules:
    // ! max < min
    // ! min > max
    public ColorGenerator() {
        /* max < min */
        if (rMax < rMin) {
            System.err.println("Error: color gen color max < min value. Switching values.");
            int temp = rMax;
            rMax = rMin;
            rMin = temp;
        }
        if (gMax < gMin) {
            System.err.println("Error: color gen color max < min value. Switching values.");
            int temp = gMax;
            gMax = gMin;
            gMin = temp;
        }
        if (bMax < bMin) {
            System.err.println("Error: color gen color max < min value. Switching values.");
            int temp = bMax;
            bMax = bMin;
            bMin = temp;
        }
        /* min > max */
        if (bMax < bMin) {
            System.err.println("Error: color gen color min > max value. Switching values.");
            int temp = rMax;
            rMax = rMin;
            rMin = temp;
        }
        if (bMax < bMin) {
            System.err.println("Error: color gen color min > max value. Switching values.");
            int temp = gMax;
            gMax = gMin;
            gMin = temp;
        }
        if (bMax < bMin) {
            System.err.println("Error: color gen color min > max value. Switching values.");
            int temp = bMax;
            bMax = bMin;
            bMin = temp;
        }
        existingColors = new HashSet<>(4096);   // most going to have at least 4k colors
    }

    public static void main(String[] args) throws IOException {
        ColorGenerator colorGenerator = new ColorGenerator();
        //HOI4Fixes.settings = new LocalizerSettings();
        colorGenerator.generateColors(100);
    }

    private static void findExistingColors() {
        Parser definitionParser = new Parser(LocalizerSettings.get(MOD_DIRECTORY)
                + "//map//definition.csv");
        Expression[] exps = definitionParser.expression().getAll();

        if (!exps[0].getText().trim().startsWith("0;0;0;0;")) {
            System.err.println("Definition file invalid");
            return;
        }

        for (Expression exp : exps) {
            String s = exp.toString();
            s = s.trim();

            /* data -> colors (parse csv line) */
            String[] data = s.split(";");
            int province = Integer.parseInt(data[0]);
            int r = Integer.parseInt(data[1]);
            int g = Integer.parseInt(data[2]);
            int b = Integer.parseInt(data[3]);
            Color color = new Color(r, g, b);

            existingColors.add(color);
        }
    }

    public void generateColors(int numColors) {
        /* create colors bmp */
        int imageWidth;
        int imageHeight;
        int numPixels;
        int maxColors;

        /* find existing colors */
        findExistingColors();

        // 2^24 = 16,777,216
        // maxColors will equal 2^24 - 1 if min is 0 and max is 255
        // if max == min the value is still usable
        maxColors = ((rMax - rMin + 1) * (gMax - gMin + 1) * (bMax - bMin + 1) - 1);
        maxColors -= existingColors.size();
        if (numColors > maxColors) {
            numColors = maxColors;
            System.err.println("Error: color generation attempting to generate more unique colors than is possible. Generating max possible "
                    + "[" + numColors + "]" + " instead.");
        }

        /* image */
        imageWidth = (int) Math.ceil(Math.sqrt(numColors));
        imageHeight = (int) Math.ceil(Math.sqrt(numColors));
        numPixels = imageWidth * imageHeight;
        colorMap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        /* colors list */
        colors = new HashSet<>(numPixels);

        Random random = new Random();

        for (int i = 0; i < numColors; i++) {
            /* gen new color */
            Color color;
            do {
                color = new Color(random.nextInt(rMin, rMax), random.nextInt(gMin, gMax),
                        random.nextInt(bMin, bMax)); // 0-255
            } while (existingColors.contains(color) || colors.contains(color));

            colorMap.setRGB(i / imageWidth, i % imageWidth, color.getRGB());
            colors.add(color);
        }

        try {
            ImageIO.write(colorMap, "bmp", new File("colors.bmp"));
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }
}
