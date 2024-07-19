//package main.java.com.HOIIVUtils.hoi4utils.map.province;
//
//import main.java.com.HOIIVUtils.hoi4utils.SettingsManager;
//import main.java.com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Expression;
//import main.java.com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Parser;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Random;
//
//import static main.java.com.HOIIVUtils.hoi4utils.Settings.MOD_PATH;
//
///*
// * ColorGenerator/Type/Order
// */
//public class ColorGenerator {
//	public enum ColorGenOrder {
//		DEFAULT,					// as generated
//		CHROMANUMERICALLY,		  // order of int representing color
//		REVERSE_CHROMANUMERICALLY, HUE,  // reverse order of int representing color
//	}
//	public enum ColorGenType {
//		EQUAL_DISTRIBUTION,	 // r, g, b generated separately
//		UNIFORM_DISTRIBUTION,   // r, g, b generated from one value
//		TEMP_1
//	}
//	private static BufferedImage colorMap;
//	private static HashSet<Color> existingColors;
//	private static HashSet<Color> colors;
//	private static int rMin = 0;
//	private static int rMax = 255;
//	private static int gMin = 0;
//	private static int gMax = 255;
//	private static int bMin = 0;
//	private static int bMax = 255;
//	private ColorGenType COLOR_GEN_TYPE = ColorGenType.UNIFORM_DISTRIBUTION; // ColorGenType.TEMP_1;
//	// private ColorGenOrder COLOR_GEN_ORDER = ColorGenOrder.DEFAULT;
//	// //ColorGenOrder.CHROMANUMERICALLY;
//	private ColorGenOrder COLOR_GEN_ORDER = ColorGenOrder.HUE;
//
//	// rules:
//	// ! max < min
//	// ! min > max
//	public ColorGenerator() {
//		/* max < min */
//		if (rMax < rMin) {
//			System.err.println("Error: color gen color max < min value. Switching values.");
//			int temp = rMax;
//			rMax = rMin;
//			rMin = temp;
//		}
//		if (gMax < gMin) {
//			System.err.println("Error: color gen color max < min value. Switching values.");
//			int temp = gMax;
//			gMax = gMin;
//			gMin = temp;
//		}
//		if (bMax < bMin) {
//			System.err.println("Error: color gen color max < min value. Switching values.");
//			int temp = bMax;
//			bMax = bMin;
//			bMin = temp;
//		}
//		/* min > max */
//		if (bMax < bMin) {
//			System.err.println("Error: color gen color min > max value. Switching values.");
//			int temp = rMax;
//			rMax = rMin;
//			rMin = temp;
//		}
//		if (bMax < bMin) {
//			System.err.println("Error: color gen color min > max value. Switching values.");
//			int temp = gMax;
//			gMax = gMin;
//			gMin = temp;
//		}
//		if (bMax < bMin) {
//			System.err.println("Error: color gen color min > max value. Switching values.");
//			int temp = bMax;
//			bMax = bMin;
//			bMin = temp;
//		}
//		existingColors = new HashSet<>(4096); // most going to have at least 4k colors
//	}
//
//	public static void main(String[] args) throws IOException {
//		ColorGenerator colorGenerator = new ColorGenerator();
//		// HOI4Fixes.hoi4utils.settings = new LocalizerSettings();
//		colorGenerator.generateColors(100);
//	}
//
//	private static void findExistingColors() {
//		Parser definitionParser = new Parser(SettingsManager.get(MOD_PATH)
//				+ "//map//definition.csv");
//		Expression[] exps = definitionParser.expression().getAll();
//
//		if (!exps[0].getText().trim().startsWith("0;0;0;0;")) {
//			System.err.println("Definition file invalid");
//			return;
//		}
//
//		for (Expression exp : exps) {
//			String s = exp.toString();
//			s = s.trim();
//
//			/* data -> colors (parse csv line) */
//			String[] data = s.split(";");
////			Province province = new Province(Integer.parseInt(data[0]));
//			int r = Integer.parseInt(data[1]);
//			int g = Integer.parseInt(data[2]);
//			int b = Integer.parseInt(data[3]);
//			Color color = new Color(r, g, b);
//
//			existingColors.add(color);
//		}
//	}
//
//	public static void setRedMin(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//		rMin = value;
//	}
//
//	public static void setRedMax(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//		rMax = value;
//	}
//
//	public static void setGreenMin(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//
//		gMin = value;
//	}
//
//	public static void setGreenMax(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//
//		gMax = value;
//	}
//
//	public static void setBlueMin(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//
//		bMin = value;
//	}
//
//	public static void setBlueMax(int value) {
//		if (value < 0) {
//			value = 0;
//		} else if (value > 255) {
//			value = 255;
//		}
//
//		bMax = value;
//	}
//
//	public void generateColors(int numColors) {
//		generateColors(numColors, null);
//	}
//
//	public boolean generateColors(int numColors, JProgressBar progressBar) {
//		/* create colors bmp */
//		int imageWidth;
//		int imageHeight;
//		int numPixels;
//		int maxColors;
//		boolean progressUpdates;
//
//		progressUpdates = progressBar != null;
//
//		/* find existing colors */
//		findExistingColors();
//
//		// 2^24 = 16,777,216
//		// maxColors will equal 2^24 - 1 if min is 0 and max is 255
//		// if max == min the value is still usable
//		maxColors = ((rMax - rMin + 1) * (gMax - gMin + 1) * (bMax - bMin + 1) - 1);
//		maxColors -= existingColors.size();
//		if (numColors > maxColors) {
//			numColors = maxColors;
//			System.err.println(
//					"Error: color generation attempting to generate more unique colors than is possible. Generating max possible "
//							+ "[" + numColors + "]" + " instead.");
//		}
//
//		/* image */
//		imageWidth = (int) Math.ceil(Math.sqrt(numColors));
//		imageHeight = (int) Math.ceil(Math.sqrt(numColors));
//		numPixels = imageWidth * imageHeight;
//		colorMap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
//
//		/* colors list */
//		colors = new HashSet<>(numPixels);
//
//		Random random = new Random();
//		int rDiff = rMax - rMin;
//		int gDiff = gMax - gMin;
//		int bDiff = bMax - bMin;
//		for (int i = 0; i < numColors; i++) {
//			/* gen new color */
//			Color color;
//			do {
//				if (COLOR_GEN_TYPE == ColorGenType.EQUAL_DISTRIBUTION) {
//					color = new Color(random.nextInt(rMin, rMax), random.nextInt(gMin, gMax),
//							random.nextInt(bMin, bMax)); // 0-255
//				} else if (COLOR_GEN_TYPE == ColorGenType.UNIFORM_DISTRIBUTION) {
//					// todo very difficult math
//					int max = (rDiff + 1) * (gDiff + 1) * (bDiff + 1) - 1; // starts from 0, so max of 7 -> 8 possible
//																		   // values.
//					int colorInt = random.nextInt(max);
//					int r = (colorInt >> 16) & 0xFF + rMin; // number at least rMin ( >> 16 -> at most 255 - rMax)
//					int g = (colorInt >> 8) & 0xFF;
//					int b = (colorInt) & 0xFF;
//					color = new Color(r, g, b);
//				} else if (COLOR_GEN_TYPE == ColorGenType.TEMP_1) {
//					int r = random.nextInt(0, rDiff);
//					if (rDiff < 255 && random.nextInt(255) > rDiff) {
//						r = Math.floorDiv(r, random.nextInt(255 - rDiff) + 1);
//					}
//					int g = random.nextInt(0, gDiff);
//					if (gDiff < 255 && random.nextInt(255) > gDiff) {
//						g = Math.floorDiv(g, random.nextInt(255 - gDiff) + 1);
//					}
//					int b = random.nextInt(0, bDiff);
//					if (bDiff < 255 && random.nextInt(255) > bDiff) {
//						b = Math.floorDiv(b, random.nextInt(255 - bDiff) + 1);
//					}
//					color = new Color(r, g, b);
//				} else {
//					return false;
//				}
//			} while (existingColors.contains(color) || colors.contains(color));
//
//			colors.add(color);
//			if (COLOR_GEN_ORDER == ColorGenOrder.DEFAULT) {
//				colorMap.setRGB(i / imageWidth, i % imageWidth, color.getRGB());
//				if (progressUpdates) {
//					SwingUtilities.invokeLater(() -> {
//						progressBar.setValue(progressBar.getValue() + 1);
//					});
//				}
//			}
//		}
//
//		if (COLOR_GEN_ORDER == ColorGenOrder.CHROMANUMERICALLY) {
//			int i = 0;
//			for (Iterator<Color> it = colors.stream().sorted(new colorIntComparator()).iterator(); it.hasNext(); i++) {
//				Color color = it.next();
//				colorMap.setRGB(i / imageWidth, i % imageWidth, color.getRGB());
//				if (progressUpdates) {
//					SwingUtilities.invokeLater(() -> {
//						progressBar.setValue(progressBar.getValue() + 1);
//					});
//				}
//			}
//		} else if (COLOR_GEN_ORDER == ColorGenOrder.HUE) {
//			int i = 0;
//			for (Iterator<Color> it = colors.stream().sorted(new colorHueComparator()).iterator(); it.hasNext(); i++) {
//				Color color = it.next();
//				colorMap.setRGB(i / imageWidth, i % imageWidth, color.getRGB());
//				if (progressUpdates) {
//					SwingUtilities.invokeLater(() -> {
//						progressBar.setValue(progressBar.getValue() + 1);
//					});
//				}
//			}
//		}
//
//		try {
//			ImageIO.write(colorMap, "bmp", new File("colors.bmp"));
//		} catch (IOException exc) {
//			throw new RuntimeException(exc);
//		}
//
//		return true;
//	}
//
//	private static class colorIntComparator implements Comparator<Color> {
//		@Override
//		public int compare(Color o1, Color o2) {
//			return Integer.compare(o1.getRGB(), o2.getRGB());
//		}
//	}
//
//	private static class colorHueComparator implements Comparator<Color> {
//		@Override
//		public int compare(Color o1, Color o2) {
//			float[] hsb1 = Color.RGBtoHSB(o1.getRed(), o1.getGreen(), o1.getBlue(), null);
//			float[] hsb2 = Color.RGBtoHSB(o2.getRed(), o2.getGreen(), o2.getBlue(), null);
//
//			return Float.compare(hsb1[0], hsb2[0]);
//		}
//	}
//
//}
