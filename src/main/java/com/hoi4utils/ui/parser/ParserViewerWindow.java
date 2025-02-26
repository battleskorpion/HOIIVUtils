package com.hoi4utils.ui.parser;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ParserViewerWindow extends HOIIVUtilsWindow {

	@FXML
	public Label idVersion;
	@FXML
	public Label idWindowName;
	
	public ParserViewerWindow() {
		setFxmlResource("ParserViewerWindow.fxml");
		setTitle("HOIIVUtils Parser Viewer");
	}

	@FXML
	void initialize() {
		includeVersion();
		idWindowName.setText("ParserViewerWindow" + " WIP");
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}
}
