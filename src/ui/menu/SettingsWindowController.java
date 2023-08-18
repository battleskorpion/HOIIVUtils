package ui.menu;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import static clausewitz_coding.HOI4Fixes.launchMenu;

public class SettingsWindowController {
    
    @FXML
    private CheckBox devModeCheckBox;

    public void devMode() {

    }

    public void openMenu() {
        launchMenu();
    }
}
