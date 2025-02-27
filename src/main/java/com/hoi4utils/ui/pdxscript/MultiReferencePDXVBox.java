package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.script.MultiReferencePDX;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.jetbrains.annotations.NotNull;
import scala.jdk.javaapi.CollectionConverters;

public class MultiReferencePDXVBox extends VBox {

    private final MultiReferencePDX<?> pdx;
    private final Runnable reloadAction;

    public MultiReferencePDXVBox(MultiReferencePDX<?> pdx, Runnable reloadAction) {
        super();
        this.pdx = pdx;
        this.reloadAction = reloadAction;

        generateFromPDX();
        // todo? 
        super.setBackground(Background.fill(Paint.valueOf("gray")));
    }

    private void generateFromPDX() {
        for (int i = 0; i < pdx.numReferences(); i++) {
            HBox propertyHBox = visualizeReferenceElement(i);
            this.getChildren().add(propertyHBox);
        }
        // addtl standalone plus button
        HBox plusHBox = new HBox();
        Button plusButton = new Button("+");
        plusButton.setOnAction(event -> {
            HBox newPropertyHBox = visualizeEmptyReference();
            // Add the new property before the "+" button
            this.getChildren().add(this.getChildren().size() - 1, newPropertyHBox);
            // Remove the initial "+" button
            //this.getChildren().remove(plusHBox);
        });
        plusHBox.getChildren().add(plusButton);
        this.getChildren().add(plusHBox);
        System.out.println("MultiReferencePDXVBox generated");
        System.out.println(pdx);

    }

    private @NotNull HBox visualizeReferenceElement(int i) {
        HBox propertyHBox = new HBox();
        propertyHBox.setSpacing(2);

        Button minusButton = new Button("-");
        Button plusButton = new Button("+");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(200);
        comboBox.setPrefHeight(25);
        comboBox.setItems(FXCollections.observableArrayList(CollectionConverters.asJavaCollection(
                pdx.getReferenceCollectionNames())).sorted());
        if (i >= 0) {
            comboBox.getSelectionModel().select(pdx.getReferenceName(i));
        }

        /* listeners / on actions */
        addReferenceHBoxEventHandlers(propertyHBox, minusButton, plusButton, comboBox);
        propertyHBox.getChildren().add(plusButton);
        propertyHBox.getChildren().add(comboBox);
        propertyHBox.getChildren().add(minusButton);
        return propertyHBox;
    }

    private @NotNull HBox visualizeEmptyReference() {
        return visualizeReferenceElement(-1);
    }

    /**
     * Visualize a new reference element
     *
     * @return The HBox visualizing the new reference
     */
    private @NotNull HBox visualizeNewReferenceElement() {
        HBox propertyHBox = new HBox();
        propertyHBox.setSpacing(2);

        Button minusButton = new Button("-");
        Button plusButton = new Button("+");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(200);
        comboBox.setPrefHeight(25);
        comboBox.setItems(FXCollections.observableArrayList(CollectionConverters.asJavaCollection(
                pdx.getReferenceCollectionNames())).sorted());

        /* listeners / on actions */
        addReferenceHBoxEventHandlers(propertyHBox, minusButton, plusButton, comboBox);
        propertyHBox.getChildren().add(plusButton);
        propertyHBox.getChildren().add(comboBox);
        propertyHBox.getChildren().add(minusButton);
        return propertyHBox;
    }

    private void addReferenceHBoxEventHandlers(HBox propertyHBox, Button minusButton, Button plusButton, ComboBox<String> comboBox) {
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (pdx.containsReferenceName(oldValue)) {
                pdx.changeReference(oldValue, newValue);
            }
            else {
                pdx.addReferenceName(newValue);
            }
            reloadAction.run();
        });
        minusButton.setOnAction(event -> {
            pdx.removeReference(comboBox.getValue());
            propertyHBox.getChildren().remove(minusButton);
            propertyHBox.getChildren().remove(comboBox);
            propertyHBox.getChildren().remove(plusButton);
            reloadAction.run();
        });
        plusButton.setOnAction(event -> {
            int newIndex = this.getChildren().indexOf(propertyHBox);
            HBox newPropertyHBox = visualizeNewReferenceElement();
            this.getChildren().add(newIndex, newPropertyHBox);
        });
    }

}
