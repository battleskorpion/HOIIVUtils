package ui.statistics;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class StatisticsController extends HOIUtilsWindow{

	@FXML public Label idVersion;
	@FXML public Label idWindowName;

	public StatisticsController() {
		setFxmlResource("Statistics.fxml");
		setTitle("HOIIVUtils Statistics");
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
