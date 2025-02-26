package com.hoi4utils.ui.javafx.image;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Utility class for converting DDS pixel data into a JavaFX Image.
 */
public class JavaFXImageUtils {

	/**
	 * Converts an array of ARGB pixel data into a JavaFX {@link Image}.
	 *
	 * @param ddspixels the array of pixel data in ARGB format
	 * @param ddswidth     the width of the image
	 * @param ddsheight    the height of the image
	 * @return a {@link Image} constructed from the provided pixel data
	 */
	public static Image imageFromDDS(int[] ddspixels, int ddswidth, int ddsheight) {
		WritableImage writableImage = new WritableImage(ddswidth, ddsheight);
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int y = 0; y < ddsheight; y++) {
			for (int x = 0; x < ddswidth; x++) {
				int pixel = ddspixels[y * ddswidth + x];
				pixelWriter.setArgb(x, y, pixel);
			}
		}

		return writableImage;
	}
}
