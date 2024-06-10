package com.HOIIVUtils.ui.console;

import com.HOIIVUtils.clauzewitz.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;

public class ConsoleController extends HOIIVUtilsStageLoader {
	String fxmlResource = "ConsoleWindow.fxml";
	String title = "Console";

	@FXML
	public Label idVersion;
	@FXML
	public Label idWindowName;

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
