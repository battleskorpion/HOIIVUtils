package com.HOIIVUtils.ui.javafx;

import com.HOIIVUtils.clauzewitz.script.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * A Pane that displays an editor for a PDXScript.
 */
public class PDXEditorPane extends AnchorPane {
    private final PDXScript<?> pdxScript;
    private final VBox vbox;
    private final List<AbstractPDX<?>> nullProperties = new ArrayList<>();
    private final List<Node> nullPropertyNodes = new ArrayList<>();
    private boolean displayNullProperties = false;

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
        drawEditor(pdxScript);
    }

    private void drawEditor(PDXScript<?> pdxScript) {
        vbox.getChildren().clear(); // Clear existing children to reset the editor

        if (pdxScript instanceof StructuredPDX pdx) {
            for (var property : pdx.pdxProperties()) {
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                Label label = new Label(property.getPDXIdentifier() + " =");
                label.setFont(Font.font("Monospaced"));
                label.setMinWidth(10);
                label.setPrefHeight(25);

                Node editorNode = createEditorNode(property, false, label);
                if (editorNode != null) {
                    hbox.getChildren().addAll(label, editorNode);
                    vbox.getChildren().add(hbox);
                } else {
                    nullProperties.add(property);
                }
            }
        }

        // Add a plus sign button to add new properties
        if (!nullProperties.isEmpty()) {
            Button addButton = new Button("+");
            addButton.setOnAction(event -> {
                if (displayNullProperties) {
                    hideNullProperties();
                } else {
                    showNullProperties();
                }
                displayNullProperties = !displayNullProperties;
            });
            vbox.getChildren().add(addButton);
        }
    }

    private Node createEditorNode(AbstractPDX<?> property, boolean allowNull, Label label) {
        if (property instanceof StructuredPDX) {
            VBox subVBox = new VBox();
            subVBox.setPadding(new Insets(10));
            subVBox.setSpacing(10);

            Label subLabel = new Label(property.getPDXIdentifier() + " Sub-Properties:");
            subLabel.setFont(Font.font("Monospaced"));

            subVBox.getChildren().add(subLabel);
            drawEditor(property);

            return subVBox;
        } else if (property instanceof StringPDX pdx) {
            if (pdx.get() == null && !allowNull) return null;
            TextField textField = new TextField(pdx.toScript() != null ? pdx.toScript() : "");
            textField.setPrefWidth(200);
            textField.setPrefHeight(25);
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
                if (!newValue.isEmpty() && nullProperties.contains(property)) {
                    reloadEditor();
                }
            });
            return textField;
        } else if (property instanceof BooleanPDX pdx) {
            Label customCheckBox = new Label();
            customCheckBox.setText(pdx.get() ? "yes" : "no");
            customCheckBox.setFont(Font.font("Monospaced"));
            customCheckBox.setPrefHeight(25);
            customCheckBox.getStyleClass().add("custom-check-box");

            customCheckBox.setOnMouseClicked(event -> {
                pdx.invert();
                customCheckBox.setText(pdx.get() ? "yes" : "no");
                if (nullProperties.contains(property)) {
                    reloadEditor();
                }
            });

            return customCheckBox;
        } else if (property instanceof IntegerPDX pdx) {
            if (pdx.get() == null && !allowNull) return null;
            Spinner<Integer> spinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE));
            spinner.getValueFactory().setValue(pdx.getOrElse(0));
            spinner.setPrefHeight(25);
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
                if (nullProperties.contains(property)) {
                    reloadEditor();
                }
            });
            return spinner;
        } else if (property instanceof DoublePDX pdx) {
            if (pdx.get() == null && !allowNull) return null;
            Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE));
            spinner.getValueFactory().setValue(pdx.getOrElse(0.0));
            spinner.setPrefHeight(25);
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.set(newValue);
                if (nullProperties.contains(property)) {
                    reloadEditor();
                }
            });
            return spinner;
        } else if (property instanceof ReferencePDXScript<?> pdx) {
            if (pdx.get() == null && !allowNull) return null;
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setPrefWidth(200);
            comboBox.setPrefHeight(25);
            comboBox.getSelectionModel().select(pdx.getReferenceName());
            comboBox.setItems(FXCollections.observableArrayList(pdx.getReferenceCollectionNames()));
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                pdx.setReferenceName(newValue);
                if (nullProperties.contains(property)) {
                    reloadEditor();
                }
            });
            return comboBox;
        }
        return null;
    }

    /**
     * Clears the null properties list and nodes, and redraws the editor
     */
    private void reloadEditor() {
        nullProperties.clear();
        nullPropertyNodes.clear();
        drawEditor(pdxScript);
    }

    private void showNullProperties() {
        for (var property : nullProperties) {
            HBox hbox = new HBox();
            hbox.setSpacing(10);
            hbox.setPadding(new Insets(0, 0, 0, 20)); // Indent the null properties
            Label label = new Label(property.getPDXIdentifier() + " =");
            label.setFont(Font.font("Monospaced"));
            label.setMinWidth(10);
            label.setPrefHeight(25);
            label.setStyle("-fx-text-fill: grey;");

            Node editorNode = createEditorNode(property, true, label);
            if (editorNode != null) {
                hbox.getChildren().addAll(label, editorNode);
                vbox.getChildren().add(vbox.getChildren().size() - 1, hbox); // Add before the add button
                nullPropertyNodes.add(hbox);
            }
        }
    }

    private void hideNullProperties() {
        for (var node : nullPropertyNodes) {
            vbox.getChildren().remove(node);
        }
        nullPropertyNodes.clear();
    }
}
