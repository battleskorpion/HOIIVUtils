package ui.menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuWindowController {

    @FXML
    public Button settingsButton;
    public Button focusLocalizButton;

    public void openSettings() {
        HOIIVUtils.openSettings();
    }

    
}
