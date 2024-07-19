package main.java.com.hoi4utils.ui.parser;

import javafx.fxml.FXML;
import main.java.com.hoi4utils.ui.HOIIVUtilsWindow;

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
