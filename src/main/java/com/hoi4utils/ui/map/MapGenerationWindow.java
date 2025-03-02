package com.hoi4utils.ui.map;

import com.hoi4utils.clausewitz.HOIIVUtilsFiles;
import com.hoi4utils.clausewitz.map.ProvinceGenConfig;
import com.hoi4utils.clausewitz.map.gen.Heightmap;
import com.hoi4utils.clausewitz.map.province.ProvinceGeneration;
import com.hoi4utils.clausewitz.map.province.ProvinceMap;
import com.hoi4utils.ui.FXWindow;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;

public class MapGenerationWindow extends HOIIVUtilsWindow {

	private static final String DEFAULT_HEIGHTMAP_PATH = "src\\main\\resources\\map\\heightmap.bmp";
	// Heightmap heightmap = null;
	@FXML
	Canvas heightmapCanvas;
	@FXML
	Canvas provinceCanvas;
	@FXML
	TextField heightmapTextField;
	@FXML
	Button browseHeightmapButton;
	@FXML
	Button provinceGenerationButton;
	@FXML
	GridPane provinceGridPane;
	@FXML
	AnchorPane provinceAnchorPane; 
	@FXML
	ProgressBar provinceGenerationProgressBar;
	Heightmap heightmap;
	public ProvinceGeneration provinceGeneration;
	public ProvinceGenConfig properties;

	/* static initialization */
	{ // todo temp
		try {
			heightmap = new Heightmap(new File(DEFAULT_HEIGHTMAP_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MapGenerationWindow() {
		/* window */
		setFxmlResource("MapGenerationWindow.fxml");
		setTitle("Map Generation");

		properties = new ProvinceGenConfig(45, heightmap.width(), heightmap.height(), 4000, 0); // sea level 4? for
																									// china?
		provinceGeneration = new ProvinceGeneration(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	void initialize() {
		heightmapCanvas.setWidth(heightmap.width() / 4.0);
		heightmapCanvas.setHeight(heightmap.height() / 4.0);
		provinceCanvas.setWidth(heightmap.width() / 4.0);
		provinceCanvas.setHeight(heightmap.height() / 4.0);

		drawHeightmap();
		// if default heightmap loaded display its filepath
		if (heightmap != null) {
			heightmapTextField.setText(DEFAULT_HEIGHTMAP_PATH);
		}
	}

	private void drawHeightmap() {
		drawImageOnCanvas(heightmapCanvas, heightmap.width(), heightmap.height(), heightmap::height_xy_INT_RGB);
	}

	private void drawImageOnCanvas(Canvas canvas, int imgWidth, int imgHeight,
			BiFunction<Integer, Integer, Integer> rbg_supplier) {
		drawImageOnCanvas(canvas, imgWidth, imgHeight, rbg_supplier, 4);
	}

	private void drawImageOnCanvas(Canvas canvas, int imgWidth, int imgHeight,
			BiFunction<Integer, Integer, Integer> rbg_supplier, int zoom) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		WritableImage wImage = new WritableImage(imgWidth / zoom, imgHeight / zoom);
		PixelWriter pixelWriter = wImage.getPixelWriter();
		for (int y = 0; y < wImage.getHeight(); y++) {
			for (int x = 0; x < wImage.getWidth(); x++) {
				int px = rbg_supplier.apply(x * zoom, y * zoom);
				Color c = new Color(px);
				pixelWriter.setArgb(x, y, c.getRGB());
			}
		}
		gc.drawImage(wImage, 0, 0);
	}

	void drawProvinceMap() {
		ProvinceMap map = this.provinceGeneration.getProvinceMap();
		Canvas var10001 = this.provinceCanvas;
		int var10002 = map.width();
		int var10003 = map.height();
		Objects.requireNonNull(map);
		this.drawImageOnCanvas(var10001, var10002, var10003, map::getRGB);
	}

	@FXML
	void OnBrowseHeightmap() {
		File f;
		try {
			f = FXWindow.openChooser(browseHeightmapButton, HOIIVUtilsFiles.mod_folder, false);
			heightmap = new Heightmap(f);
		} catch (IOException exc) {
			heightmap = null; // deselect any heightmap
			return;
		} catch (IllegalArgumentException exc) {
			// thrown when file selected is null
			return;
		}
		// draw heightmap etc.
		heightmapTextField.setText(f.getPath()); // todo- relative path maybe?
		drawHeightmap();
	}

	@FXML
	void onEnterHeightmap() {
		var p = Path.of(heightmapTextField.getText());
		if (!Files.exists(p)) {
			return;
		}

		try {
			heightmap = new Heightmap(p);
		} catch (IOException e) {
			System.out.println("Error: heightmap could not be loaded. Filepath selected: " + p);
			return;
		} catch (IllegalArgumentException exc) {
			// thrown when file selected is null
			return;
		}
		drawHeightmap();
	}

	@FXML
	void onGenerateProvinces() {
		provinceGenerationButton.setVisible(false);
		provinceGenerationProgressBar.setVisible(true);

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() throws Exception {
				updateProgress(5, 100);
				provinceGeneration.generate(heightmap);

				updateProgress(99, 100);
				provinceGeneration.writeProvinceMap();
				drawProvinceMap();
				updateProgress(100, 100);
				return null;
			}
		};

		task.setOnSucceeded(e -> {
			provinceGenerationProgressBar.setVisible(false);
			provinceGenerationButton.setVisible(true);
//			provinceGenerationButton.setText("Generate");
		});

		provinceGenerationProgressBar.progressProperty().bind(task.progressProperty());
		new Thread(task).start();
	}

	@FXML
	void onOpenProvinceGenSettingsWindow() {
		new MapGenerationSettingsWindow().open(properties);
	}
}
