package com.HOIIVUtils.ui.map;

import com.HOIIVUtils.hoi4utils.map.Heightmap;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import com.HOIIVUtils.ui.HOIUtilsWindow;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MapGenerationWindow extends HOIUtilsWindow {

	//Heightmap heightmap = null;
	@FXML
	Canvas heightmapCanvas ;
	Heightmap heightmap;
	{   // todo temp
		try {
			heightmap = new Heightmap(new File("resources\\map\\heightmap.bmp"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@FXML Button browseHeightmapButton;

	public MapGenerationWindow() {
		/* window */
		setFxmlResource("MapGenerationWindow.fxml");
		setTitle("Map Generation");
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML void initialize() {
		heightmapCanvas.setWidth(heightmap.width() / 4.0);
		heightmapCanvas.setHeight(heightmap.height() / 4.0);

		GraphicsContext gc = heightmapCanvas.getGraphicsContext2D();

		WritableImage wImage = new WritableImage(heightmap.width() / 4, heightmap.height() / 4);
		PixelWriter pixelWriter = wImage.getPixelWriter();
		for (int y = 0; y < wImage.getHeight(); y++) {
			for (int x = 0; x < wImage.getWidth(); x++) {
				int px = heightmap.height_xy(x * 4, y * 4);
				Color c = new Color(px, px, px);
				pixelWriter.setArgb(x, y, c.getRGB());
			}
		}
		gc.drawImage(wImage, 0, 0);
	}

	@FXML void OnBrowseHeightmap() {
		try {
			heightmap = new Heightmap(openChooser(browseHeightmapButton, false)); // todo init directory
		} catch (IOException e) {
			heightmap = null;       // deselect any heightmap
			throw new RuntimeException(e);
		}

		// draw heightmap etc.
	}
}
