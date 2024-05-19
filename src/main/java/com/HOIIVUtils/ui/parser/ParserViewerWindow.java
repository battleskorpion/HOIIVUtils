package com.HOIIVUtils.ui.parser;

import javafx.fxml.FXML;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;

public class ParserViewerWindow extends HOIIVUtilsStageLoader {
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
