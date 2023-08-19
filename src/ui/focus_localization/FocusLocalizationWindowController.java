package ui.focus_localization;

import clausewitz_coding.focus.FixFocus;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.localization.FocusLocalizationFile;
import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FocusLocalizationWindowController {
    @FXML Label numLocAddedLabel;
    @FXML Label focusTreeNameLabel;
    @FXML FocusTree focusTree;
    @FXML FocusLocalizationFile focusLocFile;
    @FXML TableView<Focus> focusListTable;
    @FXML TableColumn<Focus, String> focusIDColumn;
    @FXML TableColumn<Focus, String> focusNameColumn;
    @FXML TableColumn<Focus, String> focusDescColumn;

    private int numLocalizedFocuses;

    public FocusLocalizationWindowController() {
        File selectedFile;

        /* choose focus tree */
        FocusTree focusTree;
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(HOIIVUtils.focus_folder);
            Stage stage = new Stage();
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                HOIIVUtils.openError("Selected directory was null.");
                return;
            }

        }
        catch(Exception exception) {
            HOIIVUtils.openError(exception);
            return;
        }
        try {
            focusTree = new FocusTree(selectedFile);
        } catch (IOException e) {
            HOIIVUtils.openError(e);
            return;
        }
        this.focusTree = focusTree;

        /* choose localization file */
        FocusLocalizationFile focusLocFile;
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(HOIIVUtils.localization_eng_folder);
            Stage stage = new Stage();
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                HOIIVUtils.openError("Selected directory was null.");
                return;
            }

        }
        catch(Exception exception) {
            HOIIVUtils.openError(exception);
            return;
        }
        try {
            focusLocFile = new FocusLocalizationFile(selectedFile);
        } catch (IOException e) {
            HOIIVUtils.openError(e);
            return;
        }
        this.focusLocFile = focusLocFile;

        try {
            numLocalizedFocuses = FixFocus.addFocusLoc(focusTree, focusLocFile);
        } catch (IOException e) {
            HOIIVUtils.openError(e);
            return;
        }
    }

    @FXML
    void initialize() {
        numLocAddedLabel.setText("Added localization to " + numLocalizedFocuses + " focuses");

        focusIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        focusNameColumn.setCellValueFactory(cellData -> cellData.getValue().locNameProperty());
        focusDescColumn.setCellValueFactory(cellData -> cellData.getValue().descLocalizationProperty());

        focusListTable.setItems(focusTree.listFocuses());
    }


}
