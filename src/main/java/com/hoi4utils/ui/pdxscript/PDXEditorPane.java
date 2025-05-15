package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.HOIIVUtils;
import com.hoi4utils.script.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;

import org.controlsfx.control.SearchableComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Pane that displays an editor for a PDXScript.
 */
public class PDXEditorPane extends AnchorPane {
    public static final Logger LOGGER = LogManager.getLogger(PDXEditorPane.class);
    
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

    private void drawEditor(PDXScript<?> pdxScript, Pane pane) {
        var editorPDXNode = createEditorPDXNode(pdxScript, false, true);
        
        if (editorPDXNode != null) {
            pane.getChildren().add(editorPDXNode);
        } else {
            nullProperties.add(pdxScript);
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
            pane.getChildren().add(addButton);
            // initial
            if (displayNullProperties) {
                showNullProperties();
            }
        }

        /* post ui construction */
        if (HOIIVUtils.get("debug.colors").equals("true")) applyDebugBorders(pane);
    }

    // todo remove allowNull? since now have separate
    private Node createEditorPDXNode(PDXScript<?> property, boolean allowNull, boolean withLabel) {
        Pane editorPropertyPane = switch(property) {
            case CollectionPDX<?> pdx -> {
                var vbox = new VBox();
                vbox.setSpacing(4);
                yield vbox;
            }
            case StructuredPDX pdx -> {
                var vbox = new VBox();
                vbox.setSpacing(4);
                yield vbox;
            }
            default -> {
                var hbox = new HBox();
                hbox.setSpacing(6);
                yield hbox;
            }
        };
        Label label = null;
        if (withLabel) {
            var labelText = switch (property) {
                case StructuredPDX pdx -> pdx.pdxIdentifier() + " :=";
                case CollectionPDX<?> pdx -> pdx.pdxIdentifier() + " :=";
                default -> property.pdxIdentifier() + " =";
            }; 
            label = new Label(labelText);
            label.setFont(Font.font("Monospaced"));
            label.setMinWidth(10);
            label.setPrefHeight(25);
        }

        Node editorNode = switch (property) {
            case StructuredPDX pdx ->
                visualizeStructuredPDX(pdx);
            case StringPDX pdx -> (pdx.value() == null && !allowNull)
                        ? null
                        : visualizeStringPDX(property, pdx);
            case BooleanPDX pdx ->
                    visualizeBooleanPDX(pdx);
            case IntPDX pdx -> (pdx.value() == null && !allowNull)
                            ? null
                            : visualizeIntPDX(pdx);
            case DoublePDX pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeDoublePDX(pdx);
            case ReferencePDX<?> pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeReferencePDX(pdx);
            case MultiReferencePDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeMultiReferencePDX(pdx);
            case MultiPDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeMultiPDX(pdx, allowNull);    // ignore error idk/idc rn it works 
            case CollectionPDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeCollectionPDX(pdx, allowNull);
            default -> {
                System.out.println("Ui node unknown for property type: " + (property == null ? "[null]" : property.getClass()));
                yield null;
            }
        };

        if (editorNode != null) {
            if (withLabel) editorPropertyPane.getChildren().add(label);
            editorPropertyPane.getChildren().add(editorNode);
            return editorPropertyPane;
        }
        else return null;
    }

    private Node createEditorNullPDXNode(PDXScript<?> property) {
        HBox editorNullPropertyHBox = new HBox();
        editorNullPropertyHBox.setSpacing(10);
        editorNullPropertyHBox.setPadding(new Insets(6, 6, 6, 20)); // Indent the null properties
        Label label = null;
        var allowNull = true;

        Node editorNode = switch (property) {
            case StructuredPDX pdx ->
                    visualizeStructuredPDX(pdx);
            case StringPDX pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeStringPDX(property, pdx);
            case BooleanPDX pdx ->
                    visualizeBooleanPDX(pdx);
            case IntPDX pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeIntPDX(pdx);
            case DoublePDX pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeDoublePDX(pdx);
            case ReferencePDX<?> pdx -> (pdx.value() == null && !allowNull)
                    ? null
                    : visualizeReferencePDX(pdx);
            case MultiReferencePDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeMultiReferencePDX(pdx);
            case MultiPDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeMultiPDX(pdx, allowNull);
            case CollectionPDX<?> pdx -> (pdx.isUndefined() && !allowNull)
                    ? null
                    : visualizeCollectionPDX(pdx, allowNull);
            default -> {
                System.out.println("Ui node unknown for property type: " + (property == null ? "[null]" : property.getClass()));
                yield null;
            }
        };

        if (editorNode != null) {
            editorNullPropertyHBox.getChildren().add(label);
            editorNullPropertyHBox.getChildren().add(editorNode);
            return editorNullPropertyHBox;
        }
        else return null;
    }

    private @Nullable <T extends PDXScript<?>> Node visualizeMultiPDX(MultiPDX<T> pdx, boolean allowNull) {
        VBox subVBox = new VBox();
        subVBox.setSpacing(10);
        if (!pdx.isEmpty()) {
            /* sub PDX visualization */
            pdx.foreach(pdxScript -> {
                // always allow null child to appear visually
                var subNode = createSubNode(true, (T) pdxScript);
                if (subNode != null) {
                    // Wrap the sub-node with a remove button in a container.
                    HBox container = new HBox();
                    container.setSpacing(6);
                    container.getChildren().add(subNode);

                    // Create the remove button for this sub-element.
                    Button removeButton = new Button("Remove");
                    removeButton.setOnAction(event -> {
                        // Remove this specific sub-element.
                        pdx.remove(pdxScript);
                        reloadEditor();
                    });
                    container.getChildren().add(removeButton);

                    subVBox.getChildren().add(container);
                }
                //if (subNode != null) subVBox.getChildren().add(subNode);
                return null;
            });

            /* new sub pdx button */
            Button addPDXButton = new Button("Add " + pdx.getPDXTypeName());
            addPDXButton.setPrefWidth(200);
            addPDXButton.setOnAction(event -> {
                pdx.addNewPDX();
                this.reloadEditor();
            });
            subVBox.getChildren().add(addPDXButton);

            return subVBox;
        } else if (allowNull) {
            var newPDX = pdx.applySomeSupplier();
            return createEditorPDXNode((PDXScript<?>) newPDX, allowNull, false);
        } else {
            /* modify sub pdx buttons */
            HBox modifySubPDXHBox = new HBox();
            // add sub pdx
            Button addPDXButton = new Button("Add " + pdx.getPDXTypeName());
            addPDXButton.setPrefWidth(200);
            addPDXButton.setOnAction(event -> {
                var newPDX = pdx.applySomeSupplier();
                // always allow null child to appear visually
                var newPDXNode = createEditorPDXNode((PDXScript<?>) newPDX, true, false);
                if (newPDXNode != null) {
                    subVBox.getChildren().add(subVBox.getChildren().size() - 1, newPDXNode); // Add before the add button
                }
            });
            // remove sub pdx
            Button removePDXButton = new Button("Remove");
            removePDXButton.setPrefWidth(80);
            removePDXButton.setOnAction(event -> {
                // hover over pdx (highlights), remove on click

            });
            modifySubPDXHBox.getChildren().add(addPDXButton);

            return subVBox;
        }
    }

    private @NotNull VBox visualizeCollectionPDX(CollectionPDX<?> pdx, boolean allowNull) {
        VBox subVBox = new VBox();
        subVBox.setSpacing(10);
        pdx.foreach(pdxScript -> {
            var subNode = createEditorPDXNode((PDXScript<?>) pdxScript, allowNull, true);
            if (subNode != null) subVBox.getChildren().add(subNode);
            return null;
        });
        /* Add [new collection pdx child pdx] button */
        Button addPDXButton = new Button("Add " + pdx.getPDXTypeName());
        addPDXButton.setPrefWidth(280);
        final var finalPDX = pdx;
        addPDXButton.setOnAction(event -> {
//            var newPDX = pdx.applySomeSupplier();
//            var newPDXNode = createEditorPDXNode((PDXScript<?>) newPDX, allowNull, false);
//            if (newPDXNode != null) {
//                subVBox.getChildren().add(subVBox.getChildren().size() - 1, newPDXNode); // Add before the add button
//            }
            CollectionPDXNewPDXController newPDXController = new CollectionPDXNewPDXController();
            newPDXController.open(finalPDX);
//            var newPDX =
//            finalPDX.add(newPDX)
        });
        subVBox.getChildren().add(addPDXButton);

        // indent children
        // this way, by indenting *here* we don't indent the label of the collectionPDX itself.
        subVBox.setPadding(new Insets(6, 6, 6, 20));
        return subVBox;
    }

    private @NotNull VBox visualizeStructuredPDX(StructuredPDX pdx) {
        VBox subVBox = new VBox();
        subVBox.setPadding(new Insets(6));
        subVBox.setSpacing(6);
        Collection<? extends PDXScript<?>> pdxProperties = CollectionConverters.asJavaCollection(
                pdx.pdxProperties());
        for (var property : pdxProperties) {
            Node editorPDXNode = createEditorPDXNode(property, false, true);
            if (editorPDXNode != null) {
                subVBox.getChildren().add(editorPDXNode);
            } else {
                nullProperties.add(property);
            }
        }
        // indent children
        // this way, by indenting *here* we don't indent the label of the structuredPDX itself.
        subVBox.setPadding(new Insets(6, 6, 6, 14));
        return subVBox;
    }

    private @NotNull HBox visualizeStringPDX(PDXScript<?> property,StringPDX pdx) {
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
//        if (withLabel) addLabelToHBox(pdx, hbox);
        hbox.getChildren().add(textField);
        return hbox;
    }

    private @NotNull ComboBox<String> visualizeReferencePDX(ReferencePDX<?> pdx) {
        var comboBox = new SearchableComboBox<String>();
        comboBox.setPrefWidth(200);
        comboBox.setPrefHeight(25);
        comboBox.getSelectionModel().select(pdx.getReferenceName());
//        comboBox.setItems(
//                FXCollections.observableArrayList(CollectionConverters.asJavaCollection(pdx.getReferenceCollectionNames())).sorted());
        comboBox.setItems(FXCollections.observableArrayList(CollectionConverters.asJavaCollection(pdx.getReferenceCollectionNames())));
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

    private @NotNull HBox visualizeBooleanPDX(BooleanPDX pdx) {
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
//        if (withLabel) addLabelToHBox(pdx, hbox);
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
        Label label = new Label(pdx.pdxIdentifier() + " =");
        label.setFont(Font.font("Monospaced"));
        label.setMinWidth(10);
        label.setPrefHeight(25);
        hbox.getChildren().add(label);
    }

    private Node createSubNode(boolean allowNull, PDXScript<?> pdxScript) {
        return createEditorPDXNode(pdxScript, allowNull, false);
    }

    /**
     * Clears the null properties list and nodes, and redraws the editor
     */
    private void reloadEditor() {
        nullProperties.clear();
        nullPropertyNodes.clear();
        rootVBox.getChildren().clear();     // Clear existing children to reset the editor
        onPropertyUpdate();     // Properties may have been updated
        drawEditor(pdxScript, rootVBox);
    }

    private void showNullProperties() {
        for (var property : nullProperties) {
//            HBox hbox = new HBox();
//            hbox.setSpacing(10);
//            hbox.setPadding(new Insets(0, 0, 0, 20)); // Indent the null properties
//            Label label = new Label(property.getPDXIdentifier() + " =");
//            label.setFont(Font.font("Monospaced"));
//            label.setMinWidth(10);
//            label.setPrefHeight(25);
//            label.setStyle("-fx-text-fill: grey;");

            Node editorPDXNode = createEditorNullPDXNode(property);
            if (editorPDXNode != null) {
//                hbox.getChildren().addAll(label, editorPDXNode);
                rootVBox.getChildren().add(rootVBox.getChildren().size() - 1, editorPDXNode); // Add before the add button
                nullPropertyNodes.add(editorPDXNode);
            }
        }
    }

    private void hideNullProperties() {
        for (var node : nullPropertyNodes) {
            rootVBox.getChildren().remove(node);
        }
        nullPropertyNodes.clear();
    }

    private <T> @NotNull HBox newSpinnerHBox(ValPDXScript<?> pdx, Spinner<T> spinner) {
        HBox hbox = new HBox();
        spinner.setPrefHeight(25);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            pdx.setNode(newValue);
            if (nullProperties.contains(pdx)) {
                reloadEditor();
            }
            else onPropertyUpdate();
        });
//        if (withLabel) addLabelToHBox(pdx, hbox);
        hbox.getChildren().add(spinner);
        return hbox;
    }

    private HBox visualizeDoublePDX(DoublePDX pdx) {
        double minValue = pdx.isDefaultRange() ? pdx.minValue() : pdx.minValueNonInfinite();    // todo simplify?
        double maxValue = pdx.isDefaultRange() ? pdx.maxValue() : pdx.maxValueNonInfinite();
        double value = pdx.getOrElse(pdx.defaultValue());
        Spinner<Double> spinner = new Spinner<>(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, value, 1));
        return newSpinnerHBox(pdx, spinner);
    }

    private  HBox visualizeIntPDX(IntPDX pdx) {
        int minValue = pdx.isDefaultRange() ? Integer.MIN_VALUE : pdx.minValue();
        int maxValue = pdx.isDefaultRange() ? Integer.MAX_VALUE : pdx.maxValue();
        Spinner<Integer> spinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue));
        // DO NOT GET RID OF 'REDUNDANT' CAST, COMPILER moment
        spinner.getValueFactory().setValue(pdx.getOrElse(0));

        return newSpinnerHBox(pdx, spinner);
    }

    private void applyDebugBorders(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Region region) {
                switch (node) {
                    case VBox vbox -> vbox.setBorder(Border.stroke(Paint.valueOf("blue")));
                    case HBox hbox -> hbox.setBorder(Border.stroke(Paint.valueOf("lightblue")));
                    case Label label -> label.setBorder(Border.stroke(Paint.valueOf("orange")));
                    default -> region.setBorder(Border.stroke(Paint.valueOf("green")));
                }
            }
            if (node instanceof Parent childParent) {
                applyDebugBorders(childParent); // Recursively apply to children
            }
        }
    }

    public void showSaveButton() {
        Button saveButton = new Button("Save Script");
        saveButton.setOnAction(event -> {
            LOGGER.info("Saving PDXScript...");
            switch (pdxScript) {
                case PDXFile pdxFile -> pdxFile.save();
                default -> {
                    LOGGER.warn("Cannot save PDXScript of type: " + pdxScript.getClass());
                }
            }
        });
        rootVBox.getChildren().add(saveButton);
    }
}
