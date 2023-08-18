package ui.menu;

import java.awt.TextField;
import java.io.File;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Button;

public class SettingsWindowController {
    
    @FXML
    public CheckBox devModeCheckBox;
    public Label hoi4ModFolderLabel;
    public Button browseButton;
    public TextField hoi4ModFolderTextField;
    public Button okButton;
    
    public void devMode() {

    }

    private void handleBrowseAction() {
        
    }

    public void openMenu() {
        HOI4Fixes.closeWindow(okButton);
        HOI4Fixes.openMenu();
    }
}
