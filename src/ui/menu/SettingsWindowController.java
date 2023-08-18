package ui.menu;

import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import settings.HOIIVUtilsProperties;

public class SettingsWindowController {

    @FXML
    public CheckBox devModeCheckBox;
    public Label hoi4ModFolderLabel;
    public Button browseButton;
    public TextField hoi4ModPathTextField;
    public Button okButton;
    public File selectedDirectory;

    HashMap<HOIIVUtilsProperties.Settings, String> settings;

    public void devMode() {

    }

    public void handleBrowseAction() {
        try{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            Stage primaryStage = (Stage) (browseButton.getScene().getWindow());

            selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory == null) {
                return;
            }

            updateModPath(selectedDirectory);
        }
        catch(Exception exception) {
            HOIIVUtils.openError(exception);
        }
    }

    private void updateModPath(File selectedDirectory) {
        hoi4ModPathTextField.setText(selectedDirectory.getAbsolutePath());
        settings.put(HOIIVUtilsProperties.Settings.MOD_PATH, selectedDirectory.getAbsolutePath());
    }

    public void openMenu() {
        HOIIVUtils.closeWindow(okButton);
        HOIIVUtils.openMenu();
    }

    public void saveSettings() {
        try {
            if (HOIIVUtils.firstTimeSetup) {
                HOIIVUtils.settings = new HOIIVUtilsProperties(settings);
            } else {
                HOIIVUtilsProperties.saveSettings(settings);
            }
        } catch (IOException e) {
            HOIIVUtils.openError(e);
        }
    }

    public void tempUpdateSetting(HOIIVUtilsProperties.Settings setting, String property) {
        settings.put(setting, property);
    }
}
