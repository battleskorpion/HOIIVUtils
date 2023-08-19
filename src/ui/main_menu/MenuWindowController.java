package ui.main_menu;

import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.localization.FocusLocalizationFile;
import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ui.focus_localization.FocusLocalizationWindow;

import java.io.File;
import java.io.IOException;

public class MenuWindowController {

    @FXML
    public Button settingsButton;
    public Button statisticsButton;
    public Button focusLocalizButton;

    @FXML
    void initialize() {

    }

    public void openSettings() {
        SettingsWindow settingsWindow = new SettingsWindow();

        settingsWindow.open();
    }

    public void openStatistics() {
        StatisticsWindow statisticsWindow = new StatisticsWindow();

        statisticsWindow.open();
    }

    public void openLocalizeFocusTree() {
        File selectedFile;

        /* choose focus tree */
        FocusTree focusTree;
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(HOIIVUtils.focus_folder);
            Stage stage = (Stage) (focusLocalizButton.getScene().getWindow());
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

        /* choose localization file */
        FocusLocalizationFile focusLocFile;
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(HOIIVUtils.localization_eng_folder);
            Stage stage = (Stage) (focusLocalizButton.getScene().getWindow());
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

        /* open focus loc window */
        FocusLocalizationWindow localizationWindow = new FocusLocalizationWindow(focusTree, focusLocFile);
        localizationWindow.open();
    }
}
