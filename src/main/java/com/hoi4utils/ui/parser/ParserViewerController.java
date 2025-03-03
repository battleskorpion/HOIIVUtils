package com.hoi4utils.ui.parser;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ParserViewerController extends HOIIVUtilsAbstractController {

	@FXML
	public Label idVersion;
	@FXML
	public Label idWindowName;
	
	public ParserViewerController() {
		setFxmlResource("ParserViewer.fxml");
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
