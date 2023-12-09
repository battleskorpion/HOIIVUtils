package com.HOIIVUtils.ui.parser;

import com.HOIIVUtils.hoi4utils.clausewitz_data.idea.Idea;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import com.HOIIVUtils.ui.HOIUtilsWindow;

public class ParserViewerWindow extends HOIUtilsWindow {
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
