package main.java.com.hoi4utils.ui.javafx.image;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class JavaFXImageUtils {
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
