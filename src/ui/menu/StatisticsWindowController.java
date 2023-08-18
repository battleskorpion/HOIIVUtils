package ui.menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StatisticsWindowController {
    @FXML
    public Label tempLabel;
    public Button okButton;
    
    
    public void openMenu() {
        HOIIVUtils.closeWindow(okButton);
        HOIIVUtils.openMenu();
    }
}
