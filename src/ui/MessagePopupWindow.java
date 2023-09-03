package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MessagePopupWindow extends HOIUtilsWindow {
	
	@FXML Button closeButton;
	@FXML Label messageLabel;
	@FXML String message = "null";

	public MessagePopupWindow() {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";
	}

	@FXML
	void initialize() {
		setMessage();
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}

	public void setMessage() {
		if (message == null) {
			message = "null";
		}
		messageLabel.setText(message);
	}
}
