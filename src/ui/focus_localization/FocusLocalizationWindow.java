package ui.focus_localization;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.main_menu.HOIUtilsWindow;

public class FocusLocalizationWindow extends HOIUtilsWindow {

	public FocusLocalizationWindow() {

	}

	public void open(){
		super.open("FocusLocalizationWindow.fxml", "Focus Localization");
	}
}
