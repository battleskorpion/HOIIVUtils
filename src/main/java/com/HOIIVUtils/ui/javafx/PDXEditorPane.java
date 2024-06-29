package com.HOIIVUtils.ui.javafx;

import com.HOIIVUtils.clauzewitz.script.PDXScript;
import com.HOIIVUtils.clauzewitz.script.StructuredPDX;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class PDXEditorPane extends AnchorPane {
    private final PDXScript<?> pdxScript;
    private final GridPane gridPane;

    public PDXEditorPane(PDXScript<?> pdxScript) {
        this.pdxScript = pdxScript;
        this.gridPane = new GridPane();
        this.getChildren().add(gridPane);

        // Anchor the grid pane to all sides of the PDXEditorPane
        AnchorPane.setTopAnchor(gridPane, 0.0);
        AnchorPane.setBottomAnchor(gridPane, 0.0);
        AnchorPane.setLeftAnchor(gridPane, 0.0);
        AnchorPane.setRightAnchor(gridPane, 0.0);

        // Set padding and spacing for the grid pane
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Initialize the editor with the properties of the PDXScript
        initializeEditor();
    }

    private void initializeEditor() {
        // Iterate through the properties of the PDXScript and add them to the grid pane
        int row = 0;
        if (pdxScript instanceof StructuredPDX pdx) {
            for (var property : pdx.pdxProperties()) {
                Label label = new Label(property.getPDXIdentifier() + ":");
                var script = property.toScript();
                if (script == null) continue;

                TextField textField = new TextField(script);
//                // Add a listener to update the PDXScript when the text field value changes
//                textField.textProperty().addListener((observable, oldValue, newValue) -> {
//                    property.set(newValue);
//                });
                gridPane.add(label, 0, row);
                gridPane.add(textField, 1, row);
                row++;
            }
        }
    }
}
