package com.HOIIVUtils.ui.hoi4localization;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.Settings;
import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import com.HOIIVUtils.clauzewitz.data.focus.FocusTree;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.localization.Localization;

import com.HOIIVUtils.ui.FXWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import com.HOIIVUtils.ui.javafx.table.TableViewWindow;

import java.io.File;
import java.util.Iterator;


/**
 * TODO: have to redo some methods/design to fit with the new localization system
 */
public class FocusLocalizationWindow extends HOIIVUtilsStageLoader implements TableViewWindow {

    @FXML
    private Label numLocAddedLabel;
    @FXML
    private TextField focusTreeFileTextField;
    @FXML
    private Button focusTreeFileBrowseButton;
    @FXML
    private Label focusTreeNameLabel;
    @FXML
    private TextField focusLocFileTextField;
    @FXML
    private Button focusLocFileBrowseButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button saveButton;
    @FXML
    private TableView<Focus> focusListTable;
    @FXML
    private TableColumn<Focus, String> focusIDColumn;
    @FXML
    private TableColumn<Focus, String> focusNameColumn;
    @FXML
    private TableColumn<Focus, String> focusDescColumn;
    private FocusTree focusTree;
    //private FocusLocalizationFile focusLocFile;

    private final ObservableList<Focus> focusObservableList;

    public FocusLocalizationWindow() {
        /* window */
        setFxmlResource("FocusLocalizationWindow.fxml");
        setTitle("HOIIVUtils Focus Localization");

        focusObservableList = FXCollections.observableArrayList();
    }

    /**
     * {@inheritDoc}
     *
     */
    @FXML
    void initialize() {
        /* table */
        loadTableView(this, focusListTable, focusObservableList, Focus.getDataFunctions());

        /* buttons */
        saveButton.setDisable(true);
        // focusDescColumn.setEditable(true); // redundant
    }

    public void handleFocusTreeFileBrowseButtonAction() {
        File initialFocusDirectory = HOIIVFile.mod_focus_folder;
        File selectedFile = FXWindow.openChooser(focusTreeFileBrowseButton, initialFocusDirectory, false);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }

        if (selectedFile != null) {
            focusTreeFileTextField.setText(selectedFile.getAbsolutePath());
            focusTree = FocusTree.get(selectedFile);
            focusTreeNameLabel.setText(focusTree.toString());
        } else {
            focusTreeNameLabel.setText("[not found]");
        }
    }

    public void handleFocusLocFileBrowseButtonAction() {
//        File initialFocusLocDirectory = HOIIVFile.mod_localization_folder;
//        File selectedFile = openChooser(focusLocFileBrowseButton, initialFocusLocDirectory, false);
//        if (Settings.DEV_MODE.enabled()) {
//            System.out.println(selectedFile);
//        }
//        if (selectedFile != null) {
//            focusLocFileTextField.setText(selectedFile.getAbsolutePath());
//            try {
//                focusLocFile = new FocusLocalizationFile(selectedFile);
//            } catch (IllegalLocalizationFileTypeException e) {
//                openError(e);
//                return;
//            }
//            focusTree.setLocalization(focusLocFile);
//            System.out.println("Set localization file of " + focusTree + " to " + focusLocFile);
//        }
    }

    public void handleLoadButtonAction() {
//        if (focusLocFile == null || focusTree == null) {
//            // Handle the case where focusLocFile or focusTree is not properly initialized
//            MessageController window = new MessageController();
//            window.open("Error: Focus localization or focus tree not properly initialized.");
//            return;
//        }
//
//        /* load focus loc */
//        try {
//            int numLocalizedFocuses = FixFocus.addFocusLoc(focusTree, focusLocFile);
//            // todo didnt happe?
//            updateNumLocalizedFocuses(numLocalizedFocuses);
//        } catch (IOException e) {
//            openError(e);
//            return;
//        }
//
//        updateObservableFocusList();
//
//        /* enable saving of localization */
//        saveButton.setDisable(false);
    }

    private void updateObservableFocusList() {
        focusObservableList.clear();
        focusObservableList.addAll(focusTree.listFocuses());
    }

    @SuppressWarnings("unused")
    private void updateObservableFocusList(Focus focus) {
        if (focus == null) {
            System.out.println("Focus was null and therefore did not update focus list.");
            return;
        }

        int i = 0;
        Iterator<Focus> iterator = focusObservableList.iterator();
        while (iterator.hasNext()) {
            Focus f = iterator.next();
            if (f.id().equals(focus.id())) {
                iterator.remove(); // Remove the current element using the iterator
                focusObservableList.add(i, focus);
                return; // update is done
            }
            i++;
        }

        /* never updated list, means focus not found in list. */
        System.err.println(
                "Attempted to update focus " + focus + " in focus observable list, but it was not found in the list.");
    }

    private void updateNumLocalizedFocuses(int numLocalizedFocuses) {
        numLocAddedLabel.setText(numLocAddedLabel.getText()
                .replace("x", String.valueOf(numLocalizedFocuses)));
    }

    public void handleSaveButtonAction() {
//        if (focusLocFile == null) {
//            // Handle the case where focusLocFile is not properly initialized
//            MessageController window = new MessageController();
//            window.open("Error: Focus localization file not properly initialized.");
//            return;
//        }
//
//        focusLocFile.writeLocalization();
    }

    @Override
    public void setDataTableCellFactories() {
        /* column factory */
        focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // This makes the column editable
        setColumnOnEditCommits();

        /* row factory */
        focusListTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Focus focus, boolean empty) {
                super.updateItem(focus, empty);

                if (focus == null || empty) {
                    setGraphic(null); // Clear any previous content
                } else {
                    Localization.Status textStatus;
                    Localization.Status descStatus;
                    if (focus.localization(Localizable.Property.NAME) == null) {
                        textStatus = Localization.Status.MISSING;
                    } else {
                        textStatus = focus.localization(Localizable.Property.NAME).status();
                    }
                    if (focus.localization(Localizable.Property.DESCRIPTION) == null) {
                        descStatus = Localization.Status.MISSING;
                    } else {
                        descStatus = focus.localization(Localizable.Property.DESCRIPTION).status();
                    }

                    boolean hasStatusUpdated = textStatus == Localization.Status.UPDATED
                            || descStatus == Localization.Status.UPDATED;
                    boolean hasStatusNew = descStatus == Localization.Status.NEW
                            || textStatus == Localization.Status.NEW;
                    if (hasStatusUpdated || hasStatusNew) {
                        setTextFill(Color.BLACK); // Set text color to black
                        setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;"); // Apply bold text using CSS
                    } else {
                        setTextFill(Color.BLACK); // Set text color to black
                        setStyle("-fx-background-color: transparent;"); // Reset style
                    }
                }
            }
        });
    }

    private void setColumnOnEditCommits() {
        focusNameColumn.setOnEditCommit(event -> {
            Focus focus = event.getRowValue();
            focus.replaceLocalization(Localizable.Property.NAME, event.getNewValue());
        });
        focusDescColumn.setOnEditCommit(event -> {
            Focus focus = event.getRowValue();
            focus.replaceLocalization(Localizable.Property.DESCRIPTION, event.getNewValue());
        });
    }

}
