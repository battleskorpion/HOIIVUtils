package com.HOIIVUtils.hoi4utils.clausewitz_map.gen;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

//public class Heightmap extends BufferedImage {
public class Heightmap {
	byte[][] heightmap;     // y, x
	int width;
	int height;

	public Heightmap(BufferedImage temp) {
		width = temp.getWidth();
		height = temp.getHeight();
		heightmap = new byte[height][width];
		for (int y = 0; y < temp.getHeight(); y++) {
			for (int x = 0; x < temp.getWidth(); x++) {
				heightmap[y][x] = (byte) (temp.getRGB(x, y) & 0xFF);
			}
		}
	}

	public Heightmap(File file) throws IOException, IllegalArgumentException {
		this(ImageIO.read(file));
	}

	public Heightmap(Path p) throws IOException, IllegalArgumentException {
		this(p.toFile());
	}

	public int getArea() {
		return width * height;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public int height_xy(int x, int y) {
		return heightmap[y][x] & 0xFF;
	}
	public int height_yx(int y, int x) {
		return heightmap[y][x] & 0xFF;
	}

	public final byte[][] snapshot() {
		return heightmap.clone();
	}

    public int height_xy_INT_RGB(int x, int y) {
		int height_xy = heightmap[y][x] & 0xFF;
		Color heightColor = new Color(height_xy, height_xy, height_xy);
		return heightColor.getRGB();
    }

	public byte[][] heightmap() {
		return heightmap;
	}

//	/**
//	 * Constructs a {@code Heightmap} of one of the predefined
//	 * image types:
//	 * TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED.
//	 *
//	 * <p> If the image type is TYPE_BYTE_BINARY, the number of
//	 * entries in the color model is used to determine whether the
//	 * image should have 1, 2, or 4 bits per pixel.  If the color model
//	 * has 1 or 2 entries, the image will have 1 bit per pixel.  If it
//	 * has 3 or 4 entries, the image with have 2 bits per pixel.  If
//	 * it has between 5 and 16 entries, the image will have 4 bits per
//	 * pixel.  Otherwise, an IllegalArgumentException will be thrown.
//	 *
//	 * @param width     width of the created image
//	 * @param height    height of the created image
//	 * @param imageType type of the created image
//	 * @param cm        {@code IndexColorModel} of the created image
//	 * @throws IllegalArgumentException if the imageType is not
//	 *                                  TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED or if the imageType is
//	 *                                  TYPE_BYTE_BINARY and the color map has more than 16 entries.
//	 * @see #TYPE_BYTE_BINARY
//	 * @see #TYPE_BYTE_INDEXED
//	 */
//	public Heightmap(int width, int height, int imageType, IndexColorModel cm) {
//		super(width, height, imageType, cm);
//	}
}
