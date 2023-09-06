package ui;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;

public class MessagePopupWindow extends HOIUtilsWindow {
	
	@FXML public Label messageLabel;
	@FXML public Button closeButton;

	public String message;
	

	public MessagePopupWindow() {
		fxmlResource = "MessagePopupWindow.fxml";
		title = "HOIIVUtils Statistics Window";         // todo
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

	/**
	 * Opens the window
	 * @param string A string to sent to message pop up
	 */
	public void open(String string) {
		super.open();

		// passes the message from any widow to the message pop up window
		if (string != null) {
			MessagePopupWindow controller = loader.getController();
			controller.initData(string);
		}
	}

	public void handleCloseButtonAction() {
		HOIUtilsWindow.closeWindow(closeButton);
	}
}
