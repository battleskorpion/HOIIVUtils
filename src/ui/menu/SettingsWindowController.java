package ui.menu;

import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import clausewitz_coding.HOI4Fixes;
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

    HashMap<HOIIVUtilsProperties.Settings, String> settings;

    public void devMode() {

    }

    public void handleBrowseAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage primaryStage = (Stage) (browseButton.getScene().getWindow());
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        System.out.println(selectedDirectory.getParentFile());
        System.out.println(selectedDirectory.getName());
        System.out.println(selectedDirectory.getPath());
        try{
        System.out.println(selectedDirectory.getCanonicalPath());
        }
        catch(Exception exception) {
            HOI4Fixes.openError(exception);
        }
        System.out.println(selectedDirectory.getAbsolutePath());
        System.out.println(selectedDirectory.getTotalSpace());
    }

    public void hoi4ModFolder() {

    }

    public void loadFolder() {

    }

    public void openMenu() {
        HOI4Fixes.closeWindow(okButton);
        HOI4Fixes.openMenu();
    }

    public void saveSettings() {
        try {
            if (HOI4Fixes.firstTimeSetup) {
                HOI4Fixes.settings = new HOIIVUtilsProperties(settings);
            } else {
                HOIIVUtilsProperties.saveSettings(settings);
            }
        } catch (IOException e) {
            HOI4Fixes.openError(e);
        }
    }

    public void tempUpdateSetting(HOIIVUtilsProperties.Settings setting, String property) {
        settings.put(setting, property);
    }
}
