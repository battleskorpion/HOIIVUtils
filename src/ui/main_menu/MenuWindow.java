package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuWindow extends hoiUtilsWindow {
	Stage primaryStage;

	public void open(){
		super.open("MenuWindow.fxml");
	}
}
