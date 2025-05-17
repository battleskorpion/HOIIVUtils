package com.hoi4utils.ui.map;

import com.hoi4utils.clausewitz.HOIIVFiles;
import map.DefinitionCSV;
import map.Province;
import map.ProvinceDefinition;
import map.State;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.buildings.StateTable;
import com.hoi4utils.ui.pdxscript.PDXEditorPane;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

public class MapEditorController extends HOIIVUtilsAbstractController {
    public static final Logger logger = LogManager.getLogger(MapEditorController.class);

    @FXML
    private Canvas mapCanvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private ScrollPane pdxScrollPane;
    @FXML
    private SplitPane mapEditorSplitPane;

    private Image mapImage;
    // Keep the original province map image (with definition colors) for hover lookup.
    private Image originalMapImage;

    private double zoomFactor = 1.0;
    
    private boolean showBuildingsTable = false;

    // Tooltip to display state info on hover.
    private final Tooltip stateTooltip = new Tooltip();
    private StateTable stateTable = null; 
    
    private State selectedState = null; // currently selected state
    private Province selectedProvince = null; // currently selected province

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
                originalMapImage = mapImage; // store original
                logger.info("Default province map loaded: " + file.getAbsolutePath());
            } else {
                logger.warn("Default province map not found at " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error loading default province map image", e);
        }

        zoomSlider.setValue(1.0);
        pdxScrollPane.setVisible(false);
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

    /**
     * Helper function that calculates and returns the state at the given canvas coordinates.
     *
     * @param canvasX               X coordinate on the canvas.
     * @param canvasY               Y coordinate on the canvas.
     * @param provinceColorToId     Mapping from province RGB to province id.
     * @param provinceIdToStateMap  Mapping from province id to State.
     * @return the State at the given coordinates, or null if none.
     */
    private State getStateAtCanvasCoordinates(double canvasX, double canvasY,
                                              Map<Integer, Integer> provinceColorToId,
                                              Map<Integer, State> provinceIdToStateMap) {
        if (originalMapImage == null) return null;
        // Convert canvas coordinates to original image coordinates.
        int origX = (int) (canvasX / zoomFactor);
        int origY = (int) (canvasY / zoomFactor);
        if (origX < 0 || origY < 0 || origX >= originalMapImage.getWidth() || origY >= originalMapImage.getHeight()) {
            return null;
        }
        PixelReader origReader = originalMapImage.getPixelReader();
        Color origPixelColor = origReader.getColor(origX, origY);
        int pr = (int) (origPixelColor.getRed() * 255);
        int pg = (int) (origPixelColor.getGreen() * 255);
        int pb = (int) (origPixelColor.getBlue() * 255);
        int origRgb = (pr << 16) | (pg << 8) | pb;
        Integer provinceId = provinceColorToId.get(origRgb);
        if (provinceId != null) {
            return provinceIdToStateMap.get(provinceId);
        }
        return null;
    }

    /**
     * Helper to render the map coloured by some per‐state integer metric.
     * @param metricFn    extracts the metric (e.g. civ or mil factories) from a State
     * @param colorFn     maps a normalized [0–1] value to a Color
     */
    private void viewByStateMetric(ToIntFunction<State> metricFn,
                                   java.util.function.DoubleFunction<Color> colorFn) {
        if (originalMapImage == null) return;

        // load province → ID
        File defFile = HOIIVFiles.Mod.definition_csv_file;
        if (!defFile.exists()) return;
        var scalaDefs = DefinitionCSV.load(defFile);
        var defs = CollectionConverters.asJava(scalaDefs);
        Map<Integer,Integer> provinceColorToId = new HashMap<>();
        for (ProvinceDefinition d : defs.values()) {
            int rgb = (d.red() << 16) | (d.green() << 8) | d.blue();
            provinceColorToId.put(rgb, d.id());
        }

        // load states and compute max metric
        ObservableList<State> states = State.observeStates();
        int max = states.stream()
                .mapToInt(metricFn)
                .max()
                .orElse(1);

        // build province→stateColor map
        Map<Integer,Color> provinceIdToColor = new HashMap<>();
        for (State s : states) {
            int val = metricFn.applyAsInt(s);
            double norm = val / (double) max;
            Color c = colorFn.apply(norm);
            for (Province p : CollectionConverters.asJava(s.provinces().toList())) {
                provinceIdToColor.put((Integer)p.id().get(), c);
            }
        }

        // recolour every pixel
        int w = (int) originalMapImage.getWidth(), h = (int) originalMapImage.getHeight();
        WritableImage newImg = new WritableImage(w, h);
        PixelReader rdr = originalMapImage.getPixelReader();
        PixelWriter wtr = newImg.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color oc = rdr.getColor(x, y);
                int pr = (int) (oc.getRed() * 255),
                        pg = (int) (oc.getGreen() * 255),
                        pb = (int) (oc.getBlue() * 255);
                Integer pid = provinceColorToId.get((pr << 16) | (pg << 8) | pb);
                wtr.setColor(x, y, (pid != null && provinceIdToColor.containsKey(pid))
                        ? provinceIdToColor.get(pid)
                        : oc);
            }
        }

        mapImage = newImg;
        drawMap();
        mapCanvas.setOnMouseMoved(null); // no tooltip for these views
    }

    @FXML
    void onLoadProvinceMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Province Map");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Bitmap Images", "*.bmp", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(mapCanvas.getScene().getWindow());
        if (file != null) {
            try {
                mapImage = new Image(file.toURI().toString());
                originalMapImage = mapImage; // update original as well
                logger.info("Loaded province map: " + file.getAbsolutePath());
                drawMap();
            } catch (Exception e) {
                logger.error("Failed to load image", e);
            }
        }
    }

    @FXML
    void onViewByProvince() {
        logger.info("Switching view mode to Province.");
        // Remove any existing mouse handler.
        mapCanvas.setOnMouseMoved(null);
        drawMap();
    }

    /**
     * Implements the state view using the definitions CSV.
     * For each pixel in the province map, we:
     *   1. Look up its RGB value (from the original image) in the definitions mapping to determine the province id.
     *   2. Use a mapping from province id to State (built from loaded states) to get a state color.
     *   3. Recolor the pixel with the state color (if available), or leave it unchanged otherwise.
     * Also, install a mouse moved event handler that shows a tooltip with the state name under the mouse.
     */
    @FXML
    void onViewByState() {
        logger.info("Switching view mode to State.");
        if (originalMapImage == null) {
            logger.warn("No province map image loaded.");
            return;
        }

        // Load definitions CSV. Assume HOIIVFiles.Mod.definition_csv_file exists.
        File defFile = HOIIVFiles.Mod.definition_csv_file;
        if (!defFile.exists()) {
            logger.warn("Definitions file not found: " + defFile.getAbsolutePath());
            return;
        }

        // Load the province definitions from CSV (Scala object)
        scala.collection.immutable.Map<Object, ProvinceDefinition> scalaDefs = DefinitionCSV.load(defFile);
        Map<Object, ProvinceDefinition> defs = CollectionConverters.asJava(scalaDefs);

        // Build a mapping from province RGB (as in the original image) to province id.
        final Map<Integer, Integer> provinceColorToId = new HashMap<>();
        for (ProvinceDefinition def : defs.values()) {
            int rgb = (def.red() << 16) | (def.green() << 8) | def.blue();
            provinceColorToId.put(rgb, def.id());
        }

        // Build a mapping from province id to a state color and a mapping to the state.
        final Map<Integer, Color> provinceIdToStateColor = new HashMap<>();
        final Map<Integer, State> provinceIdToStateMap = new HashMap<>();
        ObservableList<State> states = State.observeStates();
        for (State state : states) {
            // Assign a random color for each state.
            Color stateColor = Color.hsb(Math.random() * 360, 0.5, 0.9);
            // Assume state.provinces() returns an Iterable<Province> (convert Scala collection to Java)
            for (Province province : CollectionConverters.asJava(state.provinces().toList())) {
                Integer id = (Integer) province.id().get();
                provinceIdToStateColor.put(id, stateColor);
                provinceIdToStateMap.put(id, state);
            }
        }

        // Create a new WritableImage by recoloring the original image.
        int width = (int) originalMapImage.getWidth();
        int height = (int) originalMapImage.getHeight();
        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = originalMapImage.getPixelReader();
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
        // Update the displayed image.
        mapImage = newImage;
        drawMap();


        // Install a mouse moved handler to update a tooltip with the state name.
        mapCanvas.setOnMouseMoved((MouseEvent event) -> {
            State s = getStateAtCanvasCoordinates(event.getX(), event.getY(), provinceColorToId, provinceIdToStateMap);
            if (s != null) {
                stateTooltip.setText("State: " + s.toString());
                stateTooltip.show(mapCanvas, event.getScreenX() + 10, event.getScreenY() + 10);
            } else {
                stateTooltip.hide();
            }
        });
    }

    @FXML
    void onViewByStrategicRegion() {
        logger.info("Switching view mode to Strategic Region.");
        // TODO: Implement strategic region view rendering.
        // Remove tooltip handler if active.
        mapCanvas.setOnMouseMoved(null);
        drawMap();
    }

    @FXML
    void onViewByCivFactories() {
        logger.info("Switching view mode to Civilian Factories.");
        viewByStateMetric(
                State::civilianFactories,       // extract civilianFactories
                norm -> Color.hsb(120, 0.8, 0.3 + 0.7 * norm) // greenish scale, brighter = more factories
        );
    }

    @FXML
    void onViewByMilFactories() {
        logger.info("Switching view mode to Military Factories.");
        viewByStateMetric(
                State::militaryFactories,       // extract militaryFactories
                norm -> Color.hsb(0, 0.8, 0.3 + 0.7 * norm)   // reddish scale, brighter = more factories
        );
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

    @FXML
    void onToggleBuildingsTable() {
        showBuildingsTable = !showBuildingsTable;
        if (showBuildingsTable) {
            var buildingsTable = new Pane();
            var content = new StateTable(); 
            buildingsTable.getChildren().add(content); 
            buildingsTable.setId("buildingsTable");
            mapEditorSplitPane.getItems().add(buildingsTable);
            buildingsTable.setVisible(true);
            if (selectedState != null) content.setStates(selectedState); 
            else content.clearStates();
            stateTable = content;
        } else {
            var buildingsTable = mapEditorSplitPane.lookup("#buildingsTable");
            if (buildingsTable != null) {
                mapEditorSplitPane.getItems().remove(buildingsTable);
            }
            stateTable = null; 
        }
    }

    /**
     * Mouse click handler that calls getStateAtCanvasCoordinates and processes the clicked state.
     * This method is meant to be used as a right-click handler.
     *
     * @param event the mouse event.
     */
    @FXML
    void onCanvasMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            // For simplicity, we rebuild the mappings as in onViewByState().
            // In a production version, consider caching these maps.
            File defFile = HOIIVFiles.Mod.definition_csv_file;
            if (!defFile.exists()) {
                logger.warn("Definitions file not found: " + defFile.getAbsolutePath());
                return;
            }
            scala.collection.immutable.Map<Object, ProvinceDefinition> scalaDefs = DefinitionCSV.load(defFile);
            Map<Object, ProvinceDefinition> defs = CollectionConverters.asJava(scalaDefs);
            final Map<Integer, Integer> provinceColorToId = new HashMap<>();
            for (ProvinceDefinition def : defs.values()) {
                int rgb = (def.red() << 16) | (def.green() << 8) | def.blue();
                provinceColorToId.put(rgb, def.id());
            }
            final Map<Integer, State> provinceIdToStateMap = new HashMap<>();
            ObservableList<State> states = State.observeStates();
            for (State state : states) {
                for (Province province : CollectionConverters.asJava(state.provinces().toList())) {
                    Integer id = (Integer) province.id().get();
                    provinceIdToStateMap.put(id, state);
                }
            }
            State clickedState = getStateAtCanvasCoordinates(event.getX(), event.getY(), provinceColorToId, provinceIdToStateMap);
            if (clickedState != null) {
                logger.info("Right-clicked state: " + clickedState);
                // add pdxEditor to scroll pane
                var pdxEditorPane = new PDXEditorPane(clickedState);
                pdxEditorPane.showSaveButton();
                pdxScrollPane.setContent(pdxEditorPane);
                pdxScrollPane.setVisible(true);
                this.selectedState = clickedState; 
                if (stateTable != null) stateTable.setStates(clickedState); 
            } else {
                logger.info("Right-clicked on an undefined state area.");
                this.selectedState = null; 
                if (stateTable != null) stateTable.clearStates();
            }
        }
    }

}
