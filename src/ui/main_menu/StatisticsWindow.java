package ui.main_menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class StatisticsWindow extends HOIUtilsWindow {

	public StatisticsWindow() {
		fxmlResource = "Statistics.fxml";
		title = "Statistics";
	}

	public void open() {
		super.open();
	}


	// * Statistics Window Controller

	@FXML
	public Label tempLabel;
}
