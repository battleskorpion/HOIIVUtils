package ui.menu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import hoi4utils.HOIIVUtils;
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
    
    public SettingsWindowController() {
        settings = new HashMap<>();
    }
    
    private void saveSettings() {
        try {
            if (HOIIVUtils.firstTimeSetup) {
                HOIIVUtils.settings = new HOIIVUtilsProperties(settings);
            } else {
                HOIIVUtilsProperties.saveSettings(settings);
            }
        } catch (IOException exception) {
            HOIIVUtils.openError(exception);
        }
    }
    
    public void devMode() {
        try {
            if (devModeCheckBox.isSelected()) {
                settings.put(HOIIVUtilsProperties.Settings.DEV_MODE, "true");
            }
            else {
                settings.put(HOIIVUtilsProperties.Settings.DEV_MODE, "false");
            }
        }
        catch (Exception exception) {
            HOIIVUtils.openError(exception);
        }
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
        saveSettings();
        HOIIVUtils.closeWindow(okButton);
        HOIIVUtils.openMenu();
    }


    public void tempUpdateSetting(HOIIVUtilsProperties.Settings setting, String property) {
        settings.put(setting, property);
    }
}
