package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class StatisticsWindow extends HOIUtilsWindow{

	@FXML
	public Label idVersion;
	public Label idWindowName;

	public StatisticsWindow() {
		fxmlResource = "StatisticsWindow.fxml";
		title = "HOIIVUtils Statistics Window";
	}

	@FXML
	void initialize() {
		includeVersion();
		idWindowName.setText("StatisticsWindow" + " WIP");
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.hoi4utilsVersion);
	}
}
