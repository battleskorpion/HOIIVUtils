package ui.menu;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SettingsWindowController {
    
    @FXML
    public CheckBox devModeCheckBox;
    public Button okButton;

    public void devMode() {

    }

    public void openMenu() {
        ((Stage) okButton.getScene().getWindow()).close();
        HOI4Fixes.openMenu();
    }
}
