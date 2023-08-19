package ui.main_menu;

import clausewitz_coding.focus.FocusTree;
import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ui.focus_localization.FocusLocalizationWindow;

import java.io.File;
import java.io.IOException;

public class MenuWindowController {

    @FXML
    public Button settingsButton;
    public Button statisticsButton;
    public Button focusLocalizButton;

    public void openSettings() {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.open();
    }

    public void openStatistics() {
        StatisticsWindow statisticsWindow = new StatisticsWindow();
        statisticsWindow.open();
    }

    public void openLocalizeFocusTree() {
        File selectedDirectory;
        try{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            Stage stage = (Stage) (focusLocalizButton.getScene().getWindow());
            selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory == null) {
                HOIIVUtils.openError("Selected directory was null.");
                return;
            }

        }
        catch(Exception exception) {
            HOIIVUtils.openError(exception);
            return; 
        }

        try {
            FocusTree focustree = new FocusTree(selectedDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FocusLocalizationWindow localizationWindow = new FocusLocalizationWindow();
        localizationWindow.open();
    }
}
