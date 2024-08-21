package com.hoi4utils.clausewitz.map.gen;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class StateBorderMap extends BufferedImage {

	/**
	 * Constructs a {@code BufferedImage} of one of the predefined
	 * image types.  The {@code ColorSpace} for the image is the
	 * default sRGB space.
	 *
	 * @param width     width of the created image
	 * @param height    height of the created image
	 * @param imageType type of the created image
	 * @see #TYPE_INT_RGB
	 * @see #TYPE_INT_ARGB
	 * @see #TYPE_INT_ARGB_PRE
	 * @see #TYPE_INT_BGR
	 * @see #TYPE_3BYTE_BGR
	 * @see #TYPE_4BYTE_ABGR
	 * @see #TYPE_4BYTE_ABGR_PRE
	 * @see #TYPE_BYTE_GRAY
	 * @see #TYPE_USHORT_GRAY
	 * @see #TYPE_BYTE_BINARY
	 * @see #TYPE_BYTE_INDEXED
	 * @see #TYPE_USHORT_565_RGB
	 * @see #TYPE_USHORT_555_RGB
	 */
	public StateBorderMap(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	/**
	 * Constructs a {@code BufferedImage} of one of the predefined
	 * image types:
	 * TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED.
	 *
	 * <p> If the image type is TYPE_BYTE_BINARY, the number of
	 * entries in the color model is used to determine whether the
	 * image should have 1, 2, or 4 bits per pixel.  If the color model
	 * has 1 or 2 entries, the image will have 1 bit per pixel.  If it
	 * has 3 or 4 entries, the image with have 2 bits per pixel.  If
	 * it has between 5 and 16 entries, the image will have 4 bits per
	 * pixel.  Otherwise, an IllegalArgumentException will be thrown.
	 *
	 * @param width     width of the created image
	 * @param height    height of the created image
	 * @param imageType type of the created image
	 * @param cm        {@code IndexColorModel} of the created image
	 * @throws IllegalArgumentException if the imageType is not
	 *                                  TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED or if the imageType is
	 *                                  TYPE_BYTE_BINARY and the color map has more than 16 entries.
	 * @see #TYPE_BYTE_BINARY
	 * @see #TYPE_BYTE_INDEXED
	 */
	public StateBorderMap(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}
}
