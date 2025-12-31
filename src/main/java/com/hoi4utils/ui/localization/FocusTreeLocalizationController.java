package com.hoi4utils.ui.localization;

import com.hoi4utils.exceptions.LocalizationPreconditionException;
import com.hoi4utils.hoi4.common.national_focus.FixFocus;
import com.hoi4utils.hoi4.common.national_focus.Focus;
import com.hoi4utils.hoi4.common.national_focus.FocusTree;
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager;
import com.hoi4utils.hoi4.localization.Localization;
import com.hoi4utils.hoi4.localization.LocalizationController;
import com.hoi4utils.hoi4.localization.LocalizationManager;
import com.hoi4utils.hoi4.localization.Property;
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.javafx.application.JavaFXUIManager;
import com.hoi4utils.ui.javafx.scene.control.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import scala.jdk.javaapi.CollectionConverters;
import wvlet.airframe.Design;
import wvlet.airframe.Session;

import javax.swing.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;


/**
 * TODO: have to redo some methods/design to fit with the new localization system
 */
public class FocusTreeLocalizationController extends HOIIVUtilsAbstractController implements TableViewWindow {

    /* ui components */
    @FXML
    private Label numLocAddedLabel;
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
    @FXML
    private TableColumn<Focus, String> focusLocStatusColumn;
    @FXML
    private RadioMenuItem localizationStatusRadioItem;
    @FXML
    private ComboBox<FocusTree> focusTreeComboBox;
    @FXML
    private Label focusTreeFileLabel;

    private FocusTree focusTreeFile;
    private boolean generateDummyDescriptions = false;
    //private FocusLocalizationFile focusLocFile;
    private final ObservableList<Focus> focusObservableList;

	private final LocalizationController localizationController = com.hoi4utils.Registry.getLocalizationController();

    public FocusTreeLocalizationController() {
        /* window */
        setFxmlFile("FocusTreeLocalization.fxml");
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
        Function<Focus, String> dataFunction = (Focus focus) ->
                "Name: " + focus.localizationStatus(Property.valueOf("NAME")).toString()
                + ", Desc: " + focus.localizationStatus(Property.valueOf("DESCRIPTION")).toString();
        focusLocStatusColumn.setCellValueFactory(JavaFXUIManager.tableCellDataCallback(dataFunction));

        /* load data */
        focusTreeComboBox.setItems(FocusTreeManager.observeFocusTrees().sorted());

        /* buttons */
        saveButton.setDisable(true);

        /* effects */
        focusLocStatusColumn.setVisible(localizationStatusRadioItem.isSelected());
        // focusDescColumn.setEditable(true); // redundant
    }

    @FXML
    private void handleSelectFocusTree() {
        this.focusTreeFile = focusTreeComboBox.getSelectionModel().getSelectedItem();
        if (this.focusTreeFile == null) { return; }

        focusTreeFileLabel.setText(focusTreeFile.fileName());
        System.out.println("Selected focus tree: " + focusTreeFile);
    }

    @FXML
    private void handleFocusLocFileBrowseButtonAction() {
//        File initialFocusLocDirectory = HOIIVUtilsFiles.mod_localization_folder;
//        File selectedFile = openChooser(focusLocFileBrowseButton, initialFocusLocDirectory, false);
//
//        System.out.println(selectedFile);
//
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

    @FXML
    private void handleLoadButtonAction() {
        if (focusTreeFile == null) {
            JOptionPane.showMessageDialog(null, "Error: Focus tree not properly initialized.");
            return;
        }

        /* load focus loc */
        try {
            int numUpdated = FixFocus.fixLocalization(focusTreeFile, generateDummyDescriptions);
            updateNumAddedLocalization(numUpdated);
        } catch (IOException | LocalizationPreconditionException e) {
            openError(e);
            return;
        }

	    updateObservableFocusList();
        /* enable saving of localization */
        saveButton.setDisable(false);
    }

    private void updateObservableFocusList() {
        focusObservableList.clear();
        focusObservableList.addAll(CollectionConverters.asJava(focusTreeFile.listFocuses()));
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
            if (f.id().value().equals(focus.id().value())) {
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

    private void updateNumAddedLocalization(int numLocalizedFocuses) {
        numLocAddedLabel.setText(numLocAddedLabel.getText()
                .replace("x", String.valueOf(numLocalizedFocuses)));
    }

    @FXML
    private void handleSaveButtonAction() {
//        if (focusLocFile == null) {
//            // Handle the case where focusLocFile is not properly initialized
//            MessageController window = new MessageController();
//            window.open("Error: Focus localization file not properly initialized.");
//            return;
//        }
//
//        focusLocFile.writeLocalization();
		localizationController.saveLocalization(); 
    }

    @FXML
    private void handleShowFocusLocalizationSettingAction() {
	    focusLocStatusColumn.setVisible(localizationStatusRadioItem.isSelected());
    }

    @Override
    public void setDataTableCellFactories() {
        /* column factory */
        focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // This makes the column editable
        focusLocStatusColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        setColumnOnEditCommits();

        /* row factory */
        focusListTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Focus focus, boolean empty) {
                super.updateItem(focus, empty);

                if (focus == null || empty) {
                    setGraphic(null); // Clear any previous content
                } else {
                    Localization.Status textStatus = focus.localizationStatus(Property.valueOf("NAME"));
                    Localization.Status descStatus = focus.localizationStatus(Property.valueOf("DESCRIPTION"));

                    boolean hasStatusUpdated = textStatus == Localization.Status.valueOf("UPDATED")
                            || descStatus == Localization.Status.valueOf("UPDATED");
                    boolean hasStatusNew = descStatus == Localization.Status.valueOf("NEW")
                            || textStatus == Localization.Status.valueOf("NEW");
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

    @FXML
    private void handleToggleAutoGenerateDescription() {
        generateDummyDescriptions = !generateDummyDescriptions;
        String status = generateDummyDescriptions ? "enabled" : "disabled";
        logger.debug("Auto-generation of dummy descriptions is now " + status + ".");
    }

    private void setColumnOnEditCommits() {
        focusNameColumn.setOnEditCommit(event -> {
            Focus focus = event.getRowValue();
            focus.replaceName(event.getNewValue());
            focusListTable.refresh();  // Force update of the table view
        });
        focusDescColumn.setOnEditCommit(event -> {
            Focus focus = event.getRowValue();
            focus.replaceLocalization(Property.valueOf("DESCRIPTION"), event.getNewValue());
            focusListTable.refresh();  // Force update of the table view
        });
    }

}
