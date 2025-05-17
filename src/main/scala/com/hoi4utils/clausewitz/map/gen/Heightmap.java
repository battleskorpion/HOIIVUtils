package com.hoi4utils.clausewitz.map.gen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

//public class Heightmap extends BufferedImage {
public class Heightmap {
	public static final String DEFAULT = "/map/heightmap.bmp";
	byte[][] heightmap; // y, x
	int width;
	int height;

	public Heightmap(BufferedImage bufferedImage) {
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
		heightmap = new byte[height][width];
		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				heightmap[y][x] = (byte) (bufferedImage.getRGB(x, y) & 0xFF);
			}
		}
	}

	public Heightmap(File file) throws IOException, IllegalArgumentException {
		this(ImageIO.read(file));
	}

	public Heightmap(InputStream inputStream) throws IOException, IllegalArgumentException {
		this(ImageIO.read(inputStream));
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

	/**
	 * Returns a snapshot of the heightmap. Why is this snapshot, and not just clone? I don't know!!
	 * @return
	 */
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
}
