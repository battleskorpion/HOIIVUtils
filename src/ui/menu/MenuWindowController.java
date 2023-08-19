package ui.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuWindowController {

    @FXML
    public Button settingsButton;
    public Button focusLocalizButton;

    public void openSettings() {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.open();
    }
}
