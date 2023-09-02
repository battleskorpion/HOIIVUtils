package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MessagePopupWindow extends HOIUtilsWindow{
	@FXML
	Label idMessage;
	Button closeButton;

	public MessagePopupWindow() {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";
	}

	@FXML
	void initialize(String message) {
		idMessage.setText(message);
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}
}
