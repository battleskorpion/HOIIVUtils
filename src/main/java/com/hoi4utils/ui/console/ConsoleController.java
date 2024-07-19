package main.java.com.hoi4utils.ui.console;

import main.java.com.hoi4utils.clauzewitz.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.java.com.hoi4utils.ui.HOIIVUtilsWindow;

public class ConsoleController extends HOIIVUtilsWindow {
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
