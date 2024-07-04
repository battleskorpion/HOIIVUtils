package com.HOIIVUtils.ui.parser;

import javafx.fxml.FXML;
import com.HOIIVUtils.ui.HOIIVUtilsWindow;

public class ParserViewerWindow extends HOIIVUtilsWindow {
	public ParserViewerWindow() {
		/* window */
		setFxmlResource("ParserViewerWindow.fxml");
		setTitle("HOIIVUtils Parser Viewer");
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	void initialize() {
	}
}
