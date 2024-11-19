package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.script.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.List;

/**
 * A Pane that displays an editor for a PDXScript.
 */
public class PDXEditorPane extends AnchorPane {
    private final PDXScript<?> pdxScript;
    private final VBox rootVBox;
    private final List<PDXScript<?>> nullProperties = new ArrayList<>();
    private final List<Node> nullPropertyNodes = new ArrayList<>();
    private boolean displayNullProperties = false;
    private Runnable onUpdate = null;

    public PDXEditorPane(PDXScript<?> pdxScript) {
        this.pdxScript = pdxScript;
        this.rootVBox = new VBox();
        this.getChildren().add(rootVBox);

        // Anchor the vbox to all sides of the PDXEditorPane
        AnchorPane.setTopAnchor(rootVBox, 0.0);
        AnchorPane.setBottomAnchor(rootVBox, 0.0);
        AnchorPane.setLeftAnchor(rootVBox, 0.0);
        AnchorPane.setRightAnchor(rootVBox, 0.0);

        // Set padding and spacing for the vbox
        rootVBox.setPadding(new Insets(10));
        rootVBox.setSpacing(10);

        // Initialize the editor with the properties of the PDXScript
        drawEditor(pdxScript, rootVBox);
    }

    public PDXEditorPane(PDXScript<?> pdxScript, Runnable onUpdate) {
        this(pdxScript);
        this.onUpdate = onUpdate;
    }

    private void drawEditor(PDXScript<?> pdxScript, VBox vbox) {
        vbox.getChildren().clear(); // Clear existing children to reset the editor

        if (pdxScript instanceof StructuredPDX pdx) {
            for (var property : CollectionConverters.asJavaCollection(pdx.pdxProperties())) {
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                Label label = new Label(property.getPDXIdentifier() + " =");
                label.setFont(Font.font("Monospaced"));
                label.setMinWidth(10);
                label.setPrefHeight(25);

                Node editorNode = createEditorNode(property, false, false);
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
            // initial
            if (displayNullProperties) {
                showNullProperties();
            }
        }
    }

    private Node createEditorNode(PDXScript<?> property, boolean allowNull, boolean withLabel) {
        switch (property) {
            case StructuredPDX pdx -> {
                return visualizeStructuredPDX(pdx);
            }
            case StringPDX pdx -> {
                if (pdx.get() == null && !allowNull) return null;
                return visualizeStringPDX(property, withLabel, pdx);
            }
            case BooleanPDX pdx -> {
                return visualizeBooleanPDX(pdx, withLabel);
            }
            case IntPDX pdx -> {
                if (pdx.get() == null && !allowNull) return null;
                return visualizeIntPDX(pdx, withLabel);
            }
            case DoublePDX pdx -> {
                if (pdx.get() == null && !allowNull) return null;
                return visualizeDoublePDX(pdx, withLabel);
            }
            case ReferencePDX<?> pdx -> {
                if (pdx.get() == null && !allowNull) return null;
                return visualizeReferencePDX(pdx);
            }
            case MultiReferencePDX<?> pdx -> {
                if (pdx.isUndefined() && !allowNull) return null;
                return visualizeMultiReferencePDX(pdx);
            }
            case MultiPDX<?> pdx -> {
                if (pdx.isUndefined() && !allowNull) return null;
                return visualizeMultiPDX(pdx, allowNull);
            }
            case CollectionPDX<?> pdx -> {
                if (pdx.isUndefined() && !allowNull) return null;
                return visualizeCollectionPDX(pdx, allowNull);
            }
            case null, default ->
                    System.out.println("Ui node unknown for property type: " + (property == null ? "[null]" : property.getClass()));
        }
        return null;
    }

    private @Nullable Node visualizeMultiPDX(MultiPDX<?> pdx, boolean allowNull) {
        VBox subVBox = new VBox();
        subVBox.setSpacing(10);
        if (!pdx.isEmpty()) {
            pdx.foreach(pdxScript -> {
                var subNode = createSubNode(allowNull, (PDXScript<?>) pdxScript);
                if (subNode != null) subVBox.getChildren().add(subNode);
                return null;
            });
            return subVBox;
        } else if (allowNull) {
            var newPDX = pdx.applySomeSupplier();
            return createEditorNode((PDXScript<?>) newPDX, allowNull, false);
        } else {
            return null;
        }
    }

    private @NotNull VBox visualizeCollectionPDX(CollectionPDX<?> pdx, boolean allowNull) {
        VBox subVBox = new VBox();
        subVBox.setSpacing(10);
        pdx.foreach(pdxScript -> {
            var subNode = createEditorNode((PDXScript<?>) pdxScript, allowNull, true);
            if (subNode != null) subVBox.getChildren().add(subNode);
            return null;
        });
        return subVBox;
    }

    private @NotNull VBox visualizeStructuredPDX(StructuredPDX pdx) {
        VBox subVBox = new VBox();
        subVBox.setPadding(new Insets(10));
        subVBox.setSpacing(10);
        Label subLabel = new Label(pdx.getPDXIdentifier() + " Sub-Properties:");
        subLabel.setFont(Font.font("Monospaced"));
        subVBox.getChildren().add(subLabel);
        drawEditor(pdx, subVBox);
        return subVBox;
    }

    private @NotNull HBox visualizeStringPDX(PDXScript<?> property, boolean withLabel, StringPDX pdx) {
        HBox hbox = new HBox();
        TextField textField = new TextField(pdx.getOrElse(""));
        textField.setPrefWidth(200);
        textField.setPrefHeight(25);
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            pdx.setNode(newValue);
            if (!newValue.isEmpty() && nullProperties.contains(property)) {
                reloadEditor();
            }
            else onPropertyUpdate();
        });
        if (withLabel) addLabelToHBox(pdx, hbox);
        hbox.getChildren().add(textField);
        return hbox;
    }

    private @NotNull ComboBox<String> visualizeReferencePDX(ReferencePDX<?> pdx) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(200);
        comboBox.setPrefHeight(25);
        comboBox.getSelectionModel().select(pdx.getReferenceName());
        comboBox.setItems(
                FXCollections.observableArrayList(CollectionConverters.asJavaCollection(pdx.getReferenceCollectionNames())));
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            pdx.setReferenceName(newValue);
            if (nullProperties.contains(pdx)) {
                reloadEditor();
            }
            else onPropertyUpdate();
        });
        return comboBox;
    }

    private @NotNull VBox visualizeMultiReferencePDX(MultiReferencePDX<?> pdx) {
        VBox subVBox = new MultiReferencePDXVBox(pdx, this::reloadEditor);
        subVBox.setSpacing(2);
        return subVBox;
    }

    private @NotNull HBox visualizeBooleanPDX(BooleanPDX pdx, boolean withLabel) {
        HBox hbox = new HBox();
        Label customCheckBox = new Label();
        customCheckBox.setText(pdx.$() ? "yes" : "no");
        customCheckBox.setFont(Font.font("Monospaced"));
        customCheckBox.setPrefHeight(25);
        customCheckBox.getStyleClass().add("custom-check-box");
        customCheckBox.setOnMouseClicked(event -> {
            pdx.invert();
            customCheckBox.setText(pdx.$() ? "yes" : "no");
            if (nullProperties.contains(pdx)) {
                reloadEditor();
            }
            else onPropertyUpdate();
        });
        if (withLabel) addLabelToHBox(pdx, hbox);
        hbox.getChildren().add(customCheckBox);
        return hbox;
    }

    /**
     * Perform some onUpdate action when the properties may have been updated
     */
    private void onPropertyUpdate() {
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private static void addLabelToHBox(PDXScript<?> pdx, HBox hbox) {
        Label label = new Label(pdx.getPDXIdentifier() + " =");
        label.setFont(Font.font("Monospaced"));
        label.setMinWidth(10);
        label.setPrefHeight(25);
        hbox.getChildren().add(label);
    }

    private Node createSubNode(boolean allowNull, PDXScript<?> pdxScript) {
        return createEditorNode(pdxScript, allowNull, false);
    }

    /**
     * Clears the null properties list and nodes, and redraws the editor
     */
    private void reloadEditor() {
        nullProperties.clear();
        nullPropertyNodes.clear();
        onPropertyUpdate();     // Properties may have been updated
        drawEditor(pdxScript, rootVBox);
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

            Node editorNode = createEditorNode(property, true, false);
            if (editorNode != null) {
                hbox.getChildren().addAll(label, editorNode);
                rootVBox.getChildren().add(rootVBox.getChildren().size() - 1, hbox); // Add before the add button
                nullPropertyNodes.add(hbox);
            }
        }
    }

    private void hideNullProperties() {
        for (var node : nullPropertyNodes) {
            rootVBox.getChildren().remove(node);
        }
        nullPropertyNodes.clear();
    }

    private <T> @NotNull HBox newSpinnerHBox(RangedPDXScript<?> pdx, boolean withLabel, Spinner<T> spinner) {
        HBox hbox = new HBox();
        spinner.setPrefHeight(25);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            pdx.setNode(newValue);
            if (nullProperties.contains(pdx)) {
                reloadEditor();
            }
            else onPropertyUpdate();
        });
        if (withLabel) addLabelToHBox(pdx, hbox);
        hbox.getChildren().add(spinner);
        return hbox;
    }

    private HBox visualizeDoublePDX(DoublePDX pdx, boolean withLabel) {
        double minValue = pdx.isDefaultRange() ? pdx.minValue() : pdx.minValueNonInfinite();    // todo simplify?
        double maxValue = pdx.isDefaultRange() ? pdx.maxValue() : pdx.maxValueNonInfinite();
        double value = pdx.getOrElse(pdx.defaultValue());
        Spinner<Double> spinner = new Spinner<>(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, value, 1));
        return newSpinnerHBox(pdx, withLabel, spinner);
    }

    private  HBox visualizeIntPDX(IntPDX pdx, boolean withLabel) {
        int minValue = pdx.isDefaultRange() ? Integer.MIN_VALUE : pdx.minValue();
        int maxValue = pdx.isDefaultRange() ? Integer.MAX_VALUE : pdx.maxValue();
        Spinner<Integer> spinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue));
        // DO NOT GET RID OF 'REDUNDANT' CAST, COMPILER moment
        spinner.getValueFactory().setValue((Integer) pdx.getOrElse(0));

        return newSpinnerHBox(pdx, withLabel, spinner);
    }
}
