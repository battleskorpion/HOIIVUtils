package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MessagePopupWindow extends HOIUtilsWindow{
	@FXML
	Label messageLabel;
	Button closeButton;

	String message;

	public MessagePopupWindow(String incomingMessage) {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";
		message = incomingMessage;
	}

	@FXML
	void initialize() {
		setMessage(message);
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}

	public void setMessage(String message) {
		messageLabel.setText(message);
	}
}
