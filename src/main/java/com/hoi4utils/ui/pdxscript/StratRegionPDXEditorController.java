package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.map.StrategicRegion;
import com.hoi4utils.clausewitz.map.province.Province;
import com.hoi4utils.clausewitz.script.DoublePDX;
import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.List;

public class StratRegionPDXEditorController extends HOIIVUtilsAbstractController {

    @FXML
    public TableView<StrategicRegion> strategicRegionTable;
    @FXML
    public TableColumn<StrategicRegion, Integer> idColumn;
    @FXML
    public TableColumn<StrategicRegion, String> nameColumn;
    @FXML
    private TextField nameField;
    @FXML
    private ListView<Province> provincesListView;
    @FXML
    private Accordion weatherAccordion;

    private final List<PDXScript<?>> pdxScripts = new ArrayList<>();


    public StratRegionPDXEditorController() {
        setFxmlResource("StratRegionPDXEditor.fxml");
        setTitle("HOIIVUtils PDX Editor");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #StratRegionPDXEditorController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScripts
     */
    @SuppressWarnings("unused")
    public StratRegionPDXEditorController(List<StrategicRegion> pdxScripts) {
        this.pdxScripts.addAll(pdxScripts);
    }

    @FXML
    private void initialize() {
        strategicRegionTable.setItems(StrategicRegion.observeStratRegions());

        idColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Set cell value factories.
        // Adjust the getter methods based on your StrategicRegion class.
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().id().getOrElse(0)).asObject());
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name().getOrElse("")));

        // Bind the table to the observable list of StrategicRegions.
        strategicRegionTable.setItems(StrategicRegion.observeStratRegions());

        strategicRegionTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newRegion) -> {
            if (newRegion != null) {
                nameField.setText(newRegion.name().getOrElse(""));

                // Populate weatherListView.
                if(newRegion.weather() != null && newRegion.weather().period().nonEmpty()) {
                    setWeatherPeriods(CollectionConverters.asJava(newRegion.weather().period().toList()));
                }

                // Populate provincesListView similarly:
                if(newRegion.provinces() != null) {
                    provincesListView.setItems(FXCollections.observableList(CollectionConverters.asJava(newRegion.provinces().toList())));
                }
            }
        });


    }

    /**
     * Called by the parent controller or main UI to pass in the WeatherPeriods we want to edit.
     */
    public void setWeatherPeriods(List<StrategicRegion.Weather.WeatherPeriod> periods) {
        weatherAccordion.getPanes().clear(); // remove old data if any

        if (periods == null || periods.isEmpty()) {
            // Possibly show a "No weather periods" message
            return;
        }

        // For each WeatherPeriod, create a TitledPane with a GridPane of fields
        for (int i = 0; i < periods.size(); i++) {
            StrategicRegion.Weather.WeatherPeriod period = periods.get(i);

            TitledPane pane = new TitledPane();
            pane.setText("Period " + (i + 1)); // Or show date range, etc.

            GridPane grid = createWeatherPeriodGrid(period);
            pane.setContent(grid);

            weatherAccordion.getPanes().add(pane);
        }
    }

    /**
     * Builds a GridPane with sliders/fields for the key DoublePDX fields in WeatherPeriod.
     */
    private GridPane createWeatherPeriodGrid(StrategicRegion.Weather.WeatherPeriod period) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        // Example row 0: "Between Start"
        Label lblBetweenStart = new Label("Between Start:");
        // todo custom picker for date
        Slider sldBetweenStart;
        if (!period.between().isEmpty()) {
            sldBetweenStart = createDoubleSlider(
                    period.between().head().getOrElse(0.0),
                    period.between().head()
            );
        } else {
            sldBetweenStart = createDoubleSlider(0.0, null); // No value to bind
        }
        grid.add(lblBetweenStart, 0, 0);
        grid.add(sldBetweenStart, 1, 0);

        // Example row 1: "Between End"
        Label lblBetweenEnd = new Label("Between End:");
        Slider sldBetweenEnd;
        if (period.between().size() >= 2) {
            sldBetweenEnd = createDoubleSlider(
                    period.between().apply(1).getOrElse(0.0),
                    period.between().size() > 1
                            ? period.between().apply(1)
                            : null
            );
        } else {
            sldBetweenEnd = createDoubleSlider(0.0, null); // No value to bind
        }
        grid.add(lblBetweenEnd, 0, 1);
        grid.add(sldBetweenEnd, 1, 1);

        // Example row 2: "Rain Heavy"
        Label lblRainHeavy = new Label("Rain Heavy:");
        Slider sldRainHeavy = createDoubleSlider(period.rain_heavy().getOrElse(0.0), period.rain_heavy());
        grid.add(lblRainHeavy, 0, 2);
        grid.add(sldRainHeavy, 1, 2);

        // Add as many rows as you like for other fields: snow, blizzard, etc.

        return grid;
    }

    /**
     * Creates a Slider [0..1] as an example, and binds it to the underlying DoublePDX value.
     * You could expand or parameterize for different ranges or different fields.
     */
    private Slider createDoubleSlider(double initialValue, DoublePDX field) {
        Slider slider = new Slider(0, 1, initialValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.1);
        slider.setBlockIncrement(0.01);

        // When user moves slider, update the DoublePDX field
        if (field != null) {
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double d = newVal.doubleValue();
                // Call field.setNode(...) or however your DoublePDX updates the underlying Node
                field.set(d);
                LOGGER.info("Updated field {} to: {}", field.pdxIdentifier(), d);
            });
        }

        // Optionally convert the slider tooltip or label to show the numeric value
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.format("%.2f", value);
            }
            @Override
            public Double fromString(String s) {
                return Double.valueOf(s);
            }
        });

        return slider;
    }

}
