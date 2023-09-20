package ui.hoi4localization;

import hoi4utils.HOIIVFile;
import hoi4utils.Settings;
import hoi4utils.clausewitz_coding.focus.FixFocus;
import hoi4utils.clausewitz_coding.focus.Focus;
import hoi4utils.clausewitz_coding.focus.FocusTree;
import hoi4utils.clausewitz_coding.localization.FocusLocalizationFile;
import hoi4utils.clausewitz_coding.localization.Localization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import ui.HOIUtilsWindow;
import ui.javafx.table.TableViewWindow;
import ui.message_popup.MessagePopupWindow;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FocusLocalizationWindow extends HOIUtilsWindow implements TableViewWindow {

    @FXML private Label numLocAddedLabel;
    @FXML private TextField focusTreeFileTextField;
    @FXML private Button focusTreeFileBrowseButton;
    @FXML private Label focusTreeNameLabel;
    @FXML private TextField focusLocFileTextField;
    @FXML private Button focusLocFileBrowseButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private TableView<Focus> focusListTable;
    @FXML private TableColumn<Focus, String> focusIDColumn;
    @FXML private TableColumn<Focus, String> focusNameColumn;
    @FXML private TableColumn<Focus, String> focusDescColumn;
    private FocusTree focusTree;
    private FocusLocalizationFile focusLocFile;

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
        // focusDescColumn.setEditable(true);       // redundant
    }

    public void handleFocusTreeFileBrowseButtonAction() {
        File initialFocusDirectory = HOIIVFile.focus_folder;
        File selectedFile = openChooser(focusTreeFileBrowseButton, false, initialFocusDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }

        if (selectedFile != null) {
            focusTreeFileTextField.setText(selectedFile.getAbsolutePath());
            focusTree = new FocusTree(selectedFile);
            focusTreeNameLabel.setText(focusTree.toString());
        } else {
            focusTreeNameLabel.setText("[not found]");
        }
    }

    public void handleFocusLocFileBrowseButtonAction() {
        File initialFocusLocDirectory = HOIIVFile.localization_eng_folder;
        File selectedFile = openChooser(focusLocFileBrowseButton, false, initialFocusLocDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }
        if (selectedFile != null) {
            focusLocFileTextField.setText(selectedFile.getAbsolutePath());
            focusLocFile = new FocusLocalizationFile(selectedFile);
            focusTree.setLocalization(focusLocFile);
            System.out.println("Set localization file of " + focusTree + " to " + focusLocFile);
        }
    }

    public void handleLoadButtonAction() {
        if (focusLocFile == null || focusTree == null) {
            // Handle the case where focusLocFile or focusTree is not properly initialized
            MessagePopupWindow window = new MessagePopupWindow();
            window.open("Error: Focus localization or focus tree not properly initialized.");
            return;
        }
		// Add further handling logic here // todo remove da comment when done

        // todo temp lazy flow control

	    /* load focus loc */
	    try {
	        int numLocalizedFocuses = FixFocus.addFocusLoc(focusTree, focusLocFile);
	        // todo didnt happe?
	        updateNumLocalizedFocuses(numLocalizedFocuses);
	    } catch (IOException e) {
	        openError(e);
	        return;
	    }

        updateObservableFocusList();

        /* enable saving of localization */
        saveButton.setDisable(false);
    }

    private void updateObservableFocusList() {
        focusObservableList.clear();
        focusObservableList.addAll(focusTree.listFocuses());
    }

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
        System.err.println("Attempted to update focus " + focus + " in focus observable list, but it was not found in the list.");
    }

    private void updateNumLocalizedFocuses(int numLocalizedFocuses) {
        numLocAddedLabel.setText(numLocAddedLabel.getText()
                .replace("x", String.valueOf(numLocalizedFocuses)));
    }

    public void handleSaveButtonAction() {
        if (focusLocFile == null) {
            // Handle the case where focusLocFile is not properly initialized
            MessagePopupWindow window = new MessagePopupWindow();
            window.open("Error: Focus localization file not properly initialized.");
        }

        focusLocFile.writeLocalization();
    }

    @Override
    public void setDataTableCellFactories() {
        /* column factory */
        focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // This makes the column editable
        setOnFocusColumnEditCommit();

        /* row factory */
        focusListTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Focus focus, boolean empty) {
                super.updateItem(focus, empty);

                if (focus == null || empty) {
                    setGraphic(null); // Clear any previous content
                } else {
                    Localization.Status textStatus = focus.getNameLocalization().status();
                    Localization.Status descStatus = focus.getDescLocalization().status();
                    boolean hasStatusUpdated = textStatus == Localization.Status.UPDATED || descStatus == Localization.Status.UPDATED;
                    boolean hasStatusNew = descStatus == Localization.Status.NEW || textStatus == Localization.Status.NEW;

//                    // Apply text highlighting style if needed
//                    if (hasStatusUpdated) {
//                        setTextFill(Color.BLACK); // Set text color to black
//                        setStyle("-fx-background-color: yellow;"); // Customize background color here
//                    } else if (hasStatusNew) {
//                        setTextFill(Color.BLACK); // Set text color to black
//                        setStyle("-fx-background-color: cyan;"); // Customize background color here
//                    } else {
//                        setTextFill(Color.BLACK); // Set text color to black
//                        setStyle(""); // Reset style
//                    }
                    // Apply text style
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

    private void setOnFocusColumnEditCommit() {
        focusDescColumn.setOnEditCommit(event -> {
            // This method will be called when a user edits and commits a cell value.
            Focus focus = event.getRowValue();
            focus.setDescLocalization(event.getNewValue());
        });
    }


}
