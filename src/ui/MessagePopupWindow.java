package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MessagePopupWindow extends HOIUtilsWindow{
	@FXML
	Label idMessage;
	Button closeButton;

	public MessagePopupWindow(String message) {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";
		idMessage.setText(message);
	}

	@FXML
	void initialize() {
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}
}
