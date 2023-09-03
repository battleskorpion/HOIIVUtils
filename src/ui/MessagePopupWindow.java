package ui;

import hoi4utils.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MessagePopupWindow extends HOIUtilsWindow {
	
	@FXML public Label messageLabel;
	@FXML public Button closeButton;

	public String message;
	

	public MessagePopupWindow() {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";
	}

	@FXML
	void initialize() {

	}

	void initData(String message) {
		if (message == null) {
			message = "null";
		}
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(message);
		}	
     	messageLabel.setText(message);
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}
}
