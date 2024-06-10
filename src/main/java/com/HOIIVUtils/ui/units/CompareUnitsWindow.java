package com.HOIIVUtils.ui.units;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.data.units.SubUnit;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import com.HOIIVUtils.ui.javafx.DiffViewPane;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompareUnitsWindow extends HOIIVUtilsStageLoader {

//    @FXML GridPane CustomUnitDetailsPane = new GridPane();
//    @FXML GridPane BaseUnitDetailsPane = new GridPane();
    @FXML
    AnchorPane rootAnchorPane = new AnchorPane();
    private DiffViewPane unitsDiffViewPane;
    private boolean skipNullProperties = true;

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
        List<SubUnit> customUnits = SubUnit.read(HOIIVFile.mod_units_folder);
        List<SubUnit> baseUnits = SubUnit.read(HOIIVFile.hoi4_units_folder);

        unitsDiffViewPane = new DiffViewPane("Base Unit Details", "Custom Unit Details");
        rootAnchorPane.getChildren().add(unitsDiffViewPane);
        // set anchors
        AnchorPane.setTopAnchor(unitsDiffViewPane, 30.0);
        AnchorPane.setBottomAnchor(unitsDiffViewPane, 0.0);
        AnchorPane.setLeftAnchor(unitsDiffViewPane, 0.0);
        AnchorPane.setRightAnchor(unitsDiffViewPane, 0.0);

        /* add data */
        // custom unit
        List<String> customUnitText = new ArrayList<>();
        for (int i = 0; i < customUnits.size(); i++) {
            SubUnit unit = customUnits.get(i);
            appendUnitDetails(customUnitText, unit);
            customUnitText.add("");
        }
        if (customUnits.isEmpty()) customUnitText.add("No custom units found");
        // base unit
        List<String> baseUnitText = new ArrayList<>();
        for (int i = 0; i < baseUnits.size(); i++) {
            SubUnit unit = baseUnits.get(i);
            appendUnitDetails(baseUnitText, unit);
            baseUnitText.add("");
        }
        if (baseUnits.isEmpty()) baseUnitText.add("No base units found");
        // append
        unitsDiffViewPane.setData(baseUnitText, customUnitText);
    }

    private void appendUnitDetails(Collection<String> unitText, SubUnit unit) {
        var df = SubUnit.getDataFunctions();
        var dfl = SubUnit.getDataLabels();
        int maxLabelWidth = dfl.stream().mapToInt(String::length).max().orElse(0);
        for (int i = 0; i < df.size(); i++) {
            var data = df.get(i).apply(unit);
            if (skipNullProperties && data == null) continue;
            var dataLabel = dfl.get(i);
            var spacing = " ".repeat(maxLabelWidth - dataLabel.length());
            var str = dataLabel + ": " + spacing + (data == null ? "[null]" : data + "");
            unitText.add(str);
            //System.out.println(str + " [" + str.length() + "]");
        }
    }
}
