package ui.map;

import hoi4utils.map.province.Heightmap;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import ui.HOIUtilsWindow;

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
		heightmapCanvas.setWidth(heightmap.getWidth() / 4);
		heightmapCanvas.setHeight(heightmap.getHeight() / 4);

		GraphicsContext gc = heightmapCanvas.getGraphicsContext2D();

		WritableImage wImage = new WritableImage(heightmap.getWidth() / 4, heightmap.getHeight() / 4);
		PixelWriter pixelWriter = wImage.getPixelWriter();
		for (int y = 0; y < wImage.getHeight(); y++) {
			for (int x = 0; x < wImage.getWidth(); x++) {
				int px = heightmap.getRGB(x * 4, y * 4);
				pixelWriter.setArgb(x, y, px);
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
