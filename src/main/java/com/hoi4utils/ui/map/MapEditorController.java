package com.hoi4utils.ui.map;

import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.clausewitz.map.province.DefinitionCSV;
import com.hoi4utils.clausewitz.map.province.Province;
import com.hoi4utils.clausewitz.map.province.ProvinceDefinition;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Implements the state view using the definitions CSV.
     * For each pixel in the province map, we:
     *   1. Look up its RGB value in the definitions mapping to determine the province id.
     *   2. Look up the province id in the state mapping (built from loaded states) to get a state color.
     *   3. Recolor the pixel with the state color (if available), or leave it unchanged otherwise.
     */
    @FXML
    void onViewByState() {
        LOGGER.info("Switching view mode to State.");
        if (mapImage == null) {
            LOGGER.warn("No province map image loaded.");
            return;
        }

        // Load definitions CSV. Assume HOIIVFiles.Mod.definitions_file exists.
        File defFile = HOIIVFiles.Mod.definition_csv_file;
        if (!defFile.exists()) {
            LOGGER.warn("Definitions file not found: " + defFile.getAbsolutePath());
            return;
        }

        // Load the province definitions from CSV (Scala object)
        scala.collection.immutable.Map<Object, ProvinceDefinition> scalaDefs = DefinitionCSV.load(defFile);
        // Convert Scala Map to Java Map
        Map<Object, ProvinceDefinition> defs = CollectionConverters.asJava(scalaDefs); 

        // Build a mapping from province RGB to province id.
        Map<Integer, Integer> provinceColorToId = new HashMap<>();
        for (ProvinceDefinition def : defs.values()) {
            // Combine RGB components into a single int (ignoring alpha).
            int rgb = (def.red() << 16) | (def.green() << 8) | def.blue();
            provinceColorToId.put(rgb, def.id());
        }

        // Build a mapping from province id to a state color.
        Map<Integer, Color> provinceIdToStateColor = new HashMap<>();
        // Retrieve all states (assuming State.observeStates() returns an ObservableList<State>)
        ObservableList<State> states = State.observeStates();
        for (State state : states) {
            // Assign a random color for each state.
            Color stateColor = Color.hsb(Math.random() * 360, 0.5, 0.9);
            // Assume state.provinces() returns an Iterable<Province> and each Province has a getId() method.
            for (Province province : CollectionConverters.asJava(state.provinces().toList())) {
                var id = (Integer) province.id().get(); 
                provinceIdToStateColor.put(id, stateColor);
            }
        }

        // Create a new WritableImage and recolor it.
        int width = (int) mapImage.getWidth();
        int height = (int) mapImage.getHeight();
        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = mapImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color origColor = reader.getColor(x, y);
                int r = (int) (origColor.getRed() * 255);
                int g = (int) (origColor.getGreen() * 255);
                int b = (int) (origColor.getBlue() * 255);
                int pixelRgb = (r << 16) | (g << 8) | b;
                Integer provinceId = provinceColorToId.get(pixelRgb);
                if (provinceId != null) {
                    Color stateColor = provinceIdToStateColor.get(provinceId);
                    if (stateColor != null) {
                        writer.setColor(x, y, stateColor);
                    } else {
                        writer.setColor(x, y, origColor);
                    }
                } else {
                    writer.setColor(x, y, origColor);
                }
            }
        }
        // Update the map image and redraw.
        mapImage = newImage;
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
