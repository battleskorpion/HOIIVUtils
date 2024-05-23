package com.HOIIVUtils.ui.units;

import com.HOIIVUtils.hoi4utils.HOIIVFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.units.SubUnit;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.util.List;

public class CompareUnitsWindow extends HOIIVUtilsStageLoader {

    @FXML GridPane CustomUnitDetailsPane = new GridPane();
    @FXML GridPane BaseUnitDetailsPane = new GridPane();

    public CompareUnitsWindow() {
        /* window */
        setFxmlResource("CompareUnitsWindow.fxml");
        setTitle("Compare Units");
    }

    /**
     * {@inheritDoc}
     */
    @FXML
    void initialize() {
        List<SubUnit> customUnits = SubUnit.read(HOIIVFile.units_folder);

        for (int i = 0; i < customUnits.size(); i++) {
            SubUnit unit = customUnits.get(i);
            TextArea customUnitTextArea = new TextArea();
            customUnitTextArea.appendText("Custom Unit\n");
            customUnitTextArea.appendText("details\n");
            appendUnitDetails(customUnitTextArea, unit);
            //customUnitTextArea.setPrefHeight(30);
            CustomUnitDetailsPane.addRow(i, customUnitTextArea);
        }

        TextArea baseUnitTextArea = new TextArea();
        baseUnitTextArea.appendText("Base Unit\n");
        baseUnitTextArea.appendText("details\n");
        BaseUnitDetailsPane.addRow(0, baseUnitTextArea);
    }

    private void appendUnitDetails(TextArea unitTextArea, SubUnit unit) {
        var df = SubUnit.getDataFunctions();
        for (var f : df) {
            var data = f.apply(unit);
            unitTextArea.appendText(data == null ? "[null]" : data.toString());
        }
    }
}