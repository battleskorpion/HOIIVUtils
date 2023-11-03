package ui.map;

import hoi4utils.map.province.Heightmap;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.HOIUtilsWindow;

import java.io.File;
import java.io.IOException;

public class MapGenerationWindow extends HOIUtilsWindow {

	//Heightmap heightmap = null;
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
