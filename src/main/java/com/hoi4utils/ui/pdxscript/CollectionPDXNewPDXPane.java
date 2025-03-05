package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.code.effect.Effect;
import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.script.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Pane that displays an editor for a PDXScript.
 */
public class CollectionPDXNewPDXPane extends AnchorPane {
    private final CollectionPDX<?> pdxScript;

    /* UI */
    private boolean showDebugBorders = false;    // todo

    private final VBox rootVBox;
    // UI Controls for filtering
    private TextField searchField;
    private ListView<Effect> effectListView;

    public CollectionPDXNewPDXPane(CollectionPDX<?> pdxScript) {
        this.pdxScript = pdxScript;

        rootVBox = new VBox();
        this.getChildren().add(rootVBox);

        // Anchor the vbox to all sides of the PDXEditorPane
        AnchorPane.setTopAnchor(rootVBox, 0.0);
        AnchorPane.setBottomAnchor(rootVBox, 0.0);
        AnchorPane.setLeftAnchor(rootVBox, 0.0);
        AnchorPane.setRightAnchor(rootVBox, 0.0);
        // Set padding and spacing for the vbox
        rootVBox.setPadding(new Insets(10));
        rootVBox.setSpacing(10);

        drawEditor(rootVBox);
    }

    private void drawEditor(VBox rootVBox) {
        // todo ai?
        // 2) Create the filtering controls
        searchField = new TextField();
        searchField.setPromptText("Search by name...");

        ComboBox<String> categoryCombo;
        categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Category");
        // Example categories; replace with your real ones if needed
        categoryCombo.getItems().addAll("All", "Economy", "Military", "Diplomacy");
        categoryCombo.setValue("All"); // Default

        // 3) Load your ~400 items (or however many) into an ObservableList
        //    In real code, you'd query a DB or use your own data structures.
        ObservableList<Effect> allEffects;
        allEffects = FXCollections.observableArrayList(
                CollectionConverters.asJava(EffectDatabase.effects()));
        // 4) Create a FilteredList to handle real-time filtering
        FilteredList<Effect> filteredEffects;
        filteredEffects = new FilteredList<>(allEffects, e -> true);

        // 5) Wire up the filtering logic
        searchField.textProperty().addListener((obs, oldVal, newVal) 
                -> applyFilter(categoryCombo, filteredEffects));
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) 
                -> applyFilter(categoryCombo, filteredEffects));

        // 6) Create the ListView and bind it to the filtered list
        effectListView = new ListView<>(filteredEffects);
        effectListView.setPrefHeight(300);
        effectListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(@Nullable Effect effect, boolean empty) {
                super.updateItem(effect, empty);
                if (empty || effect == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(effect.getPDXIdentifier());
                    //setFont(Font.font("Monospaced"));
                }
            }
        });

        // 7) Handle selection
        effectListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newEffect) -> {
            if (newEffect != null) {
                // For example, add the chosen item to your pdxScript (if it’s a CollectionPDX).
                // ((CollectionPDX<?>) pdxScript).add(newItem.toPDXScript());
                System.out.println("Selected: " + newEffect);
                searchField.setMaxHeight(100);
                Effect newPDX = null;
                try {
                    newPDX = (Effect) newEffect.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
                var pdxEditorPane = new PDXEditorPane(newPDX);
                rootVBox.getChildren().add(pdxEditorPane);
            }
        });

        // 8) Layout: an HBox with the search bar & combo, then the ListView below
        HBox filterBar = new HBox(10,
                new Label("Search:"), searchField,
                new Label("Category:"), categoryCombo
        );
        rootVBox.getChildren().addAll(filterBar, effectListView);
    }

    /**
     * Clears the null properties list and nodes, and redraws the editor
     */
    private void reloadEditor() {
        rootVBox.getChildren().clear();     // Clear existing children to reset the editor
//        onPropertyUpdate();     // Properties may have been updated
//        drawEditor(pdxScript, rootVBox);
    }

    /**
     * The filtering logic. Called whenever the search text or category changes.
     */
    private void applyFilter(ComboBox<String> categoryCombo, FilteredList<Effect> filteredEffects) {
        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedCategory = categoryCombo.getValue();

        filteredEffects.setPredicate(effectItem -> {
            // 1) Category filter
            if (!"All".equals(selectedCategory) && !effectItem.effectCategory().equalsIgnoreCase(selectedCategory)) {
                return false;
            }
            // 2) Search filter
            if (searchText.isEmpty()) {
                return true; // No search text => all pass
            }
            // You can filter by name, ID, or anything else in effectItem
            return effectItem.getPDXIdentifier().toLowerCase().contains(searchText);
        });
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
}
