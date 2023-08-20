package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StatisticsWindow extends HOIUtilsWindow {

	public void open() {
		super.open("Statistics.fxml", "Statistics");
	}
}
