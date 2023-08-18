package ui.menu;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class SettingsWindowController {
    
    @FXML
    private CheckBox devModeCheckBox;

    public void devMode() {

    }

    public void openMenu() {
        HOI4Fixes.openMenu();
    }
}
