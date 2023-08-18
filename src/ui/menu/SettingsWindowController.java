package ui.menu;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SettingsWindowController {
    
    @FXML
    private CheckBox devModeCheckBox;
    private Button okButton;

    public void devMode() {

    }

    public void openMenu() {
        HOI4Fixes.closeWindow(okButton);
        HOI4Fixes.openMenu();
    }
}
