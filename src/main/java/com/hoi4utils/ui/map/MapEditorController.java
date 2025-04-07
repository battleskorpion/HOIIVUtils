package com.hoi4utils.ui.map;

import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class MapEditorController extends HOIIVUtilsAbstractController {
    public static final Logger LOGGER = LogManager.getLogger(MapEditorController.class);

    @FXML
    private Canvas mapCanvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private ScrollPane mapScrollPane;

    private Image mapImage;
    private double zoomFactor = 1.0;


    public MapEditorController() {
        setFxmlResource("MapEditor.fxml");
        setTitle("Map Editor");
    }

    @FXML
    void initialize() {
        // Try loading a default province map image (adjust the path as needed)
        try {
            File file = HOIIVFiles.Mod.province_map_file;
            if (file.exists()) {
                mapImage = new Image(file.toURI().toString());
                LOGGER.info("Default province map loaded: " + file.getAbsolutePath());
            } else {
                LOGGER.warn("Default province map not found at " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Error loading default province map image", e);
        }

        zoomSlider.setValue(1.0);
        drawMap();
    }

    private void drawMap() {
        if (mapImage == null) {
            return;
        }
        double width = mapImage.getWidth() * zoomFactor;
        double height = mapImage.getHeight() * zoomFactor;
        mapCanvas.setWidth(width);
        mapCanvas.setHeight(height);
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.drawImage(mapImage, 0, 0, width, height);
    }

    @FXML
    void onLoadProvinceMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Province Map");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bitmap Images", "*.bmp", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(mapCanvas.getScene().getWindow());
        if (file != null) {
            try {
                mapImage = new Image(file.toURI().toString());
                LOGGER.info("Loaded province map: " + file.getAbsolutePath());
                drawMap();
            } catch (Exception e) {
                LOGGER.error("Failed to load image", e);
            }
        }
    }

    @FXML
    void onViewByProvince() {
        // TODO: Implement the province view rendering.
        LOGGER.info("Switching view mode to Province.");
        drawMap();
    }

    @FXML
    void onViewByState() {
        // TODO: Implement state-based view rendering.
        LOGGER.info("Switching view mode to State.");
        drawMap();
    }

    @FXML
    void onViewByStrategicRegion() {
        // TODO: Implement strategic region view rendering.
        LOGGER.info("Switching view mode to Strategic Region.");
        drawMap();
    }

    @FXML
    void onZoomIn() {
        zoomFactor *= 1.2;
        zoomSlider.setValue(zoomFactor);
        drawMap();
    }

    @FXML
    void onZoomOut() {
        zoomFactor /= 1.2;
        zoomSlider.setValue(zoomFactor);
        drawMap();
    }

    @FXML
    void onResetZoom() {
        zoomFactor = 1.0;
        zoomSlider.setValue(zoomFactor);
        drawMap();
    }

    @FXML
    void onZoomSliderReleased(MouseEvent event) {
        zoomFactor = zoomSlider.getValue();
        drawMap();
    }

}
