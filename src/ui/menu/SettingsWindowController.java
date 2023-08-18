package ui.menu;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;

public class SettingsWindowController {
    
    @FXML
    public CheckBox devModeCheckBox;
    public Button okButton;

    public void devMode() {

    }

    public void openMenu() {
        HOI4Fixes.closeWindow(okButton);
        HOI4Fixes.openMenu();
    }
}
