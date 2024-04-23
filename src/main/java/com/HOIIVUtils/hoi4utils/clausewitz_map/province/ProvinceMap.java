package com.HOIIVUtils.hoi4utils.clausewitz_map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class ProvinceMap extends BufferedImage {
	/**
	 * Constructs a {@code ProvinceMap} of one of the predefined
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
	public ProvinceMap(int width, int height, int imageType) {
		super(width, height, imageType);
		initProvinceMap();
	}

	/**
	 * Constructs a {@code ProvinceMap} of one of the predefined
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
	public ProvinceMap(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
		initProvinceMap();
	}

	/**
	 * Constructs a new {@code ProvinceMap} with a specified
	 * {@code ColorModel} and {@code Raster}.  If the number and
	 * types of bands in the {@code SampleModel} of the
	 * {@code Raster} do not match the number and types required by
	 * the {@code ColorModel} to represent its color and alpha
	 * components, a {@link RasterFormatException} is thrown.  This
	 * method can multiply or divide the color {@code Raster} data by
	 * alpha to match the {@code alphaPremultiplied} state
	 * in the {@code ColorModel}.  Properties for this
	 * {@code BufferedImage} can be established by passing
	 * in a {@link Hashtable} of {@code String}/{@code Object}
	 * pairs.
	 *
	 * @param cm                    {@code ColorModel} for the new image
	 * @param raster                {@code Raster} for the image data
	 * @param isRasterPremultiplied if {@code true}, the data in
	 *                              the raster has been premultiplied with alpha.
	 * @param properties            {@code Hashtable} of
	 *                              {@code String}/{@code Object} pairs.
	 * @throws RasterFormatException    if the number and
	 *                                  types of bands in the {@code SampleModel} of the
	 *                                  {@code Raster} do not match the number and types required by
	 *                                  the {@code ColorModel} to represent its color and alpha
	 *                                  components.
	 * @throws IllegalArgumentException if
	 *                                  {@code raster} is incompatible with {@code cm}
	 * @see ColorModel
	 * @see Raster
	 * @see WritableRaster
	 */
	public ProvinceMap(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
		initProvinceMap();
	}

	/**
	 * New {@code ProvinceMap} with {@code BufferedImage.TYPE_INT_RGB}
	 * @param width     width of the created image
	 * @param height    height of the created image
	 */
	public ProvinceMap(int width, int height) {
		super(width, height, BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * New {@code ProvinceMap} with {@code BufferedImage.TYPE_INT_RGB}, uses width and height of the {@code Heightmap}
	 * @param heightmap
	 */
	public ProvinceMap(Heightmap heightmap) {
		this(heightmap.width(), heightmap.height());
	}

	private void initProvinceMap() {
		// todo necessary?
//		/* white canvas (kinda unnecessary) */
//		for (int y = 0; y < values.imageHeight; y++) {
//			for (int x = 0; x < values.imageWidth; x++) {
//				// set color at pixel cords
//				setRGB(x, y, values.rgb_white);
//			}
//		}
	}

    public void write() {
	    try {
		    ImageIO.write(this, "bmp", new File("output.bmp"));
	    } catch (IOException e) {
		    throw new RuntimeException(e);
	    }
    }

	public int width() {
		return getWidth();
	}
	public int height() {
		return getHeight();
	}
}
