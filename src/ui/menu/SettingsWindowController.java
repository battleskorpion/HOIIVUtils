package ui.menu;

import java.awt.TextField;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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

    public void openMenu() {
        HOI4Fixes.closeWindow(okButton);
        HOI4Fixes.openMenu();
    }
}
