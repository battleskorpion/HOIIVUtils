package com.hoi4utils.ui.map;

import com.hoi4utils.hoi4mod.map.gen.Heightmap;
import com.hoi4utils.hoi4mod.map.province.ProvinceGenConfig;
import com.hoi4utils.hoi4mod.map.province.ProvinceGeneration;
import com.hoi4utils.hoi4mod.map.province.ProvinceMap;
import com.hoi4utils.main.HOIIVUtils;
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.custom_javafx.controller.JavaFXUIManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiFunction;

public class MapGenerationController extends HOIIVUtilsAbstractController {
	public static final Logger logger = LogManager.getLogger(MapGenerationController.class);
	private ProvinceGeneration provinceGeneration;
	private ProvinceGenConfig config;
	private Heightmap heightmap;

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

	public MapGenerationController() {
		setFxmlResource("MapGeneration.fxml");
		setTitle("Map Generation");
	}

	@FXML
	void initialize() {
		try {
			InputStream heightmapStream = getClass().getResourceAsStream(Heightmap.DEFAULT);
			heightmap = new Heightmap(heightmapStream);
		} catch (IOException e) {
			logger.error("Default heightmap could not be loaded. {}", Heightmap.DEFAULT);
			throw new RuntimeException(e);
		}

		config = new ProvinceGenConfig(45, heightmap.width(), heightmap.height(), 4000, 0); // sea level 4? for
																							// china?
		provinceGeneration = new ProvinceGeneration(config);

		heightmapTextField.setText(Heightmap.DEFAULT);
		heightmapCanvas.setWidth(heightmap.width() / 4.0);
		heightmapCanvas.setHeight(heightmap.height() / 4.0);
		provinceCanvas.setWidth(heightmap.width() / 4.0);
		provinceCanvas.setHeight(heightmap.height() / 4.0);
		drawHeightmap();
	}

	private void drawHeightmap() {
		drawImageOnCanvas(heightmapCanvas, heightmap.width(), heightmap.height(), heightmap::height_xy_INT_RGB);
	}

	private void drawImageOnCanvas(Canvas canvas, 
								   int imgWidth, 
								   int imgHeight, 
								   BiFunction<Integer, Integer, Integer> rbg_supplier) {
		
		drawImageOnCanvas(canvas, imgWidth, imgHeight, rbg_supplier, 4);
	}

	private void drawImageOnCanvas(Canvas canvas, 
								   int imgWidth, 
								   int imgHeight, 
								   BiFunction<Integer, Integer, Integer> rbg_supplier, 
								   int zoom) {
		
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

	@FXML
	void OnBrowseHeightmap() {
		File file = null;
		try {
			file = JavaFXUIManager.openChooser(browseHeightmapButton, new File(HOIIVUtils.get("hDir") + File.separator + "maps"), false);
			heightmap = new Heightmap(file);
		} catch (IOException | IllegalArgumentException exc) {
			JOptionPane.showMessageDialog(null, "Bad File Path." + file.getPath(), "Error", JOptionPane.ERROR_MESSAGE);
			logger.warn("Error: heightmap could not be loaded. Filepath selected: {}", file.getPath());
			heightmap = null;
			return;
		}
		// draw heightmap etc.
		heightmapTextField.setText(file.getPath()); // todo- relative path maybe?
		drawHeightmap();
	}

	@FXML
	void onEnterHeightmap() {
		File file = null;
		try {
			file = new File(heightmapTextField.getText());
			if (file.exists()) {
				heightmap = new Heightmap(file);
			}
		} catch (IOException | IllegalArgumentException exc) {
			JOptionPane.showMessageDialog(null, "Bad File Path." + file.getPath(), "Error", JOptionPane.ERROR_MESSAGE);
			logger.warn("heightmap could not be loaded. Filepath selected: {}", file.getPath());
			heightmap = null;
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
			protected Void call() {
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
		});

		provinceGenerationProgressBar.progressProperty().bind(task.progressProperty());
		
		new Thread(task).start();
	}

	void drawProvinceMap() {
		ProvinceMap map = this.provinceGeneration.getProvinceMap();
		Canvas canvas = this.provinceCanvas;
		int width = map.width();
		int height = map.height();
		Objects.requireNonNull(map);
		this.drawImageOnCanvas(canvas, width, height, map::getRGB);
	}

	@FXML
	void onOpenProvinceGenSettingsWindow() {
		new MapGenerationSettingsController().open(config);
	}
}
