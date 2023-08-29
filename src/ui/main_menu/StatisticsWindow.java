package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class StatisticsWindow extends HOIUtilsWindow{

	@FXML
	public Label idVersion;
	public Label idWindowName;
	public String className = this.getClass().getName();

	public StatisticsWindow() {
		fxmlResource = className + ".fxml";
		title = "HOIIVUtils Statistics Window";
	}

	@FXML
	void initialize() {
		includeVersion();
		idWindowName.setText(className + "WIP");
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.hoi4utilsVersion);
	}
}
