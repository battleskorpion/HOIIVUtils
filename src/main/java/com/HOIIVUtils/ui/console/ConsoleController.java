package com.HOIIVUtils.ui.console;

import com.HOIIVUtils.hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class ConsoleController extends HOIUtilsWindow {
	String fxmlResource = "ConsoleWindow.fxml";
	String title = "Console";

    @FXML public Label idVersion;
	@FXML public Label idWindowName;

	public ConsoleController() {
		setFxmlResource("Console.fxml");
		setTitle("Console");
	}

	@FXML
	void initialize() {
		includeVersion();
		idWindowName.setText("StatisticsWindow" + " WIP");
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}
}
