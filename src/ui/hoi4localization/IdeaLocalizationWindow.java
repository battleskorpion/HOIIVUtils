package ui.hoi4localization;

import hoi4utils.HOIIVFile;
import hoi4utils.Settings;
import hoi4utils.clausewitz_coding.idea.FixIdea;
import hoi4utils.clausewitz_coding.idea.Idea;
import hoi4utils.clausewitz_coding.idea.IdeaFile;
import hoi4utils.clausewitz_coding.localization.LocalizationFile;
import hoi4utils.clausewitz_coding.localization.Localization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import ui.HOIUtilsWindow;
import ui.javafx.table.TableViewWindow;
import ui.message.MessageController;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class IdeaLocalizationWindow extends HOIUtilsWindow implements TableViewWindow {

    @FXML private Label numLocAddedLabel;
    @FXML private TextField ideaFileTextField;
    @FXML private Button ideaFileBrowseButton;
    @FXML private Label ideaFileNameLabel;
    @FXML private TextField ideaLocFileTextField;
    @FXML private Button ideaLocFileBrowseButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private TableView<Idea> ideaListTable;
    @FXML private TableColumn<Idea, String> ideaIDColumn;
    @FXML private TableColumn<Idea, String> ideaNameColumn;

    private IdeaFile ideaFile;
    private LocalizationFile ideaLocFile;

    private final ObservableList<Idea> ideaObservableList;

    public IdeaLocalizationWindow() {
        /* window */
        setFxmlResource("IdeaLocalizationWindow.fxml");
        setTitle("HOIIVUtils Idea Localization");

        ideaObservableList = FXCollections.observableArrayList();
    }

    /**
     * {@inheritDoc}
     *
     */
    @FXML
    void initialize() {
        /* table */
        loadTableView(this, ideaListTable, ideaObservableList, Idea.getDataFunctions());

        /* buttons */
        saveButton.setDisable(true);
        // ideaDescColumn.setEditable(true);       // redundant
    }

    public void handleIdeaFileBrowseButtonAction() {
        File initialIdeaDirectory = HOIIVFile.ideas_folder;
        File selectedFile = openChooser(ideaFileBrowseButton, false, initialIdeaDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }

        if (selectedFile != null) {
            ideaFileTextField.setText(selectedFile.getAbsolutePath());
            ideaFile = new IdeaFile(selectedFile.getAbsolutePath());    // todo?
            ideaFileNameLabel.setText(ideaFile.toString());
        } else {
            ideaFileNameLabel.setText("[not found]");
        }
    }

    public void handleIdeaLocFileBrowseButtonAction() {
        File initialIdeaLocDirectory = HOIIVFile.localization_eng_folder;
        File selectedFile = openChooser(ideaLocFileBrowseButton, false, initialIdeaLocDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }
        if (selectedFile != null) {
            ideaLocFileTextField.setText(selectedFile.getAbsolutePath());
            ideaLocFile = new LocalizationFile(selectedFile);
//            ideaFile.setLocalization(ideaLocFile); // todo ?
            System.out.println("Set localization file of " + ideaFile+ " to " + ideaLocFile);
        }
    }

    public void handleLoadButtonAction() {
        if (ideaLocFile == null || ideaFile == null) {
            // Handle the case where ideaLocFile or ideaFileis not properly initialized
            MessageController window = new MessageController();
            window.open("Error: Idea localization or idea tree not properly initialized.");
            return;
        }

	    /* load idea loc */
	    try {
	        int numLocalizedIdeaes = FixIdea.addIdeaLoc(ideaFile, ideaLocFile);
	        // todo didnt happe?
	        updateNumLocalizedIdeaes(numLocalizedIdeaes);
	    } catch (IOException e) {
	        openError(e);
	        return;
	    }

        updateObservableIdeaList();

        /* enable saving of localization */
        saveButton.setDisable(false);
    }

    private void updateObservableIdeaList() {
        ideaObservableList.clear();
        ideaObservableList.addAll(ideaFile.listIdeas());
    }

    private void updateObservableIdeaList(Idea idea) {
        if (idea == null) {
            System.out.println("Idea was null and therefore did not update idea list.");
            return;
        }

        int i = 0;
        Iterator<Idea> iterator = ideaObservableList.iterator();
        while (iterator.hasNext()) {
            Idea f = iterator.next();
            if (f.id().equals(idea.id())) {
                iterator.remove(); // Remove the current element using the iterator
                ideaObservableList.add(i, idea);
                return; // update is done
            }
            i++;
        }

        /* never updated list, means idea not found in list. */
        System.err.println("Attempted to update idea " + idea + " in idea observable list, but it was not found in the list.");
    }

    private void updateNumLocalizedIdeaes(int numLocalizedIdeaes) {
        numLocAddedLabel.setText(numLocAddedLabel.getText()
                .replace("x", String.valueOf(numLocalizedIdeaes)));
    }

    public void handleSaveButtonAction() {
        if (ideaLocFile == null) {
            // Handle the case where ideaLocFile is not properly initialized
            MessageController window = new MessageController();
            window.open("Error: Idea localization file not properly initialized.");
        }

        try {
            ideaLocFile.writeLocalization();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDataTableCellFactories() {
        /* column factory */
//        ideaDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // This makes the column editable

        /* row factory */
        ideaListTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Idea idea, boolean empty) {
                super.updateItem(idea, empty);

                if (idea == null || empty) {
                    setGraphic(null); // Clear any previous content
                } else {
                    Localization.Status textStatus = idea.getLocalization().status();   // todo
                    boolean hasStatusUpdated = textStatus == Localization.Status.UPDATED;
                    boolean hasStatusNew = textStatus == Localization.Status.NEW;

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

}
