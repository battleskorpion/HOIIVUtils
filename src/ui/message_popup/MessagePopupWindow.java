package ui.message_popup;

import hoi4utils.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ui.FXWindow;
import ui.HOIUtilsWindow;

/**
 * Generates A Message Popup Window
 * It has a open method with a string paramater the gets the message from any HOIIVUtils sub class
 * To send a messgae, create a Message window and pass your string through the open method
 * * example:
 * * // Handle the case where focusLocFile or focusTree is not properly initialized
 * * MessagePopupWindow window = new MessagePopupWindow();
 * * window.open("Error: Focus localization or focus tree not properly initialized.");
 */
public class MessagePopupWindow extends HOIUtilsWindow {
	
	@FXML public Label messageLabel;
	@FXML public Button closeButton;

	String message;
	

	public MessagePopupWindow() {
		setFxmlResource("MessagePopupWindow.fxml");
		setTitle("HOIIVUtils Message");
	}

	void initData(String message) {
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(message);
		}	
     	messageLabel.setText(message);
	}

	/**
	 * Opens the window
	 * passes the message from any widow to the message pop up window
	 * @param message A string to sent to message pop up
	 */
	public void open(String message) {
		super.open();
		MessagePopupWindow controller = loader.getController();
		controller.initData(message);
	}

	public void handleCloseButtonAction() {
		closeWindow(closeButton);
	}
}
