package com.HOIIVUtils.ui.map;

import com.HOIIVUtils.hoi4utils.clausewitz_map.ProvinceGenProperties;
import com.HOIIVUtils.hoi4utils.clausewitz_map.province.ProvinceDeterminationType;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.SeedGenType;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

// todo rename to province gen properties window or whatever
public class MapGenerationSettingsWindow extends HOIIVUtilsStageLoader {

    @FXML
    TextField seaLevelTextField;
    @FXML
    TextField numSeedsTextField;
    @FXML
    ChoiceBox<SeedGenType> seedGenChoiceBox;
    ChoiceBox<ProvinceDeterminationType> provinceDeterminationChoiceBox;

    private ProvinceGenProperties properties = null;

    public MapGenerationSettingsWindow() {
        setFxmlResource("MapGenerationSettingsWindow.fxml");
        setTitle("Map Generation Settings");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #MapGenerationSettingsWindow()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param properties
     */
    public MapGenerationSettingsWindow(ProvinceGenProperties properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @FXML
    void initialize() {
        seaLevelTextField.setText(String.valueOf(properties.seaLevel()));
        numSeedsTextField.setText(String.valueOf(properties.numSeeds()));
        // default should be grid_seed
        seedGenChoiceBox.getItems().addAll(SeedGenType.values());
        seedGenChoiceBox.setValue(properties.generationType());
        provinceDeterminationChoiceBox.getItems().addAll(ProvinceDeterminationType.values());
        provinceDeterminationChoiceBox.setValue(properties.determinationType());
    }

    @FXML
    private void onSetSeaLevel() {
        // dont actually change properties until apply, but update any preview
    }

    @FXML
    private void onSetNumSeeds() {
        // dont actually change properties until apply, but update any preview
    }

    @FXML
    private void onApplyChanges() {
        int seaLevel = Integer.parseInt(seaLevelTextField.getText());
        int numSeeds = Integer.parseInt(numSeedsTextField.getText());
        SeedGenType generationType = seedGenChoiceBox.getValue();
        ProvinceDeterminationType determinationType = provinceDeterminationChoiceBox.getValue();
        System.out.println("prev. seaLevel: " + properties.seaLevel() + ", prev. numSeeds: " + properties.numSeeds());
        properties.setSeaLevel(seaLevel);
        properties.setNumSeeds(numSeeds);
        properties.setGenerationType(generationType);
        properties.setDeterminationType(determinationType);
        System.out.println("updated seaLevel: " + properties.seaLevel() + ", updated numSeeds: " + properties.numSeeds());
    }
}
