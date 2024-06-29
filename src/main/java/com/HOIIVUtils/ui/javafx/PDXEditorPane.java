package com.HOIIVUtils.ui.javafx;

import com.HOIIVUtils.clauzewitz.script.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class PDXEditorPane extends AnchorPane {
    private final PDXScript<?> pdxScript;
    private final VBox vbox;

    public PDXEditorPane(PDXScript<?> pdxScript) {
        this.pdxScript = pdxScript;
        this.vbox = new VBox();
        this.getChildren().add(vbox);

        // Anchor the vbox to all sides of the PDXEditorPane
        AnchorPane.setTopAnchor(vbox, 0.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);

        // Set padding and spacing for the vbox
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        // Initialize the editor with the properties of the PDXScript
        initializeEditor(pdxScript);
    }

    private void initializeEditor(PDXScript<?> pdxScript) {
        if (pdxScript instanceof StructuredPDX pdx) {
            for (var property : pdx.pdxProperties()) {
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                Label label = new Label(property.getPDXIdentifier() + " =");
                label.setFont(Font.font("Monospaced"));
                label.setMinWidth(150); // Set a fixed width for labels

                Node editorNode = createEditorNode(property);
                if (editorNode != null) {
                    hbox.getChildren().addAll(label, editorNode);
                    vbox.getChildren().add(hbox);
                }
            }
        }
    }

    private Node createEditorNode(AbstractPDX<?> property) {
        if (property instanceof StructuredPDX) {
            VBox subVBox = new VBox();
            subVBox.setPadding(new Insets(10));
            subVBox.setSpacing(10);

            Label subLabel = new Label(property.getPDXIdentifier() + " Sub-Properties:");
            subLabel.setFont(Font.font("Monospaced"));

            subVBox.getChildren().add(subLabel);
            initializeEditor(property);

            return subVBox;
        } else if (property instanceof StringPDX pdx) {
            TextField textField = new TextField(pdx.toScript());
            textField.setPrefWidth(200);
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
            });
            return textField;
        } else if (property instanceof BooleanPDX pdx) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(Boolean.parseBoolean(pdx.toScript()));
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
            });
            return checkBox;
        } else if (property instanceof IntegerPDX pdx) {
            Spinner<Integer> spinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE));
            spinner.getValueFactory().setValue(pdx.get());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
            });
            return spinner;
        } else if (property instanceof DoublePDX pdx) {
            Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE));
            spinner.getValueFactory().setValue(pdx.get());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
            });
            return spinner;
        } else if (property instanceof ReferencePDXScript<?> pdx) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setPrefWidth(200);
            comboBox.getSelectionModel().select(pdx.toScript());
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.setReferenceName(newValue);
            });
            return comboBox;
        }
        return null;
    }
}
