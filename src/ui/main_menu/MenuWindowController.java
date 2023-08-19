package ui.main_menu;

import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.localization.FocusLocalizationFile;
import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import settings.HOIIVUtilsProperties;
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
        /* open focus loc window */
        FocusLocalizationWindow localizationWindow = new FocusLocalizationWindow();
        localizationWindow.open();
    }
}
