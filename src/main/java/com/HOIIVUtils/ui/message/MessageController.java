package com.HOIIVUtils.ui.message;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
public class MessageController extends HOIUtilsWindow {
	private String fxmlResource = "Message.fxml";
	private String title = "Message";
	
	@FXML Label messageLabel;
	@FXML Button closeButton;
	@FXML AnchorPane anchorPane;

	public MessageController() {
		setFxmlResource(fxmlResource);
		setTitle(title);
	}
	
	/**
	 * Opens the window
	 * passes the message from any widow to the message pop up window
	 * @param message A string to sent to message pop up
	 */
	public void open(String message) {
		super.open();
		System.out.println("Message Stage started with message: " + message + "\n and loader is: " + fxmlResource);
		MessageController controller = loader.getController();
		controller.setMessage(message);
	}

	/**
	 * Changes the label to display the message we want and then formats the window to fit the text
	 * @param message A String that is used to set the message
	 */
	public void setMessage(String message) {
		Stage stage = (Stage) messageLabel.getScene().getWindow();
		messageLabel.setText(message);
		double labelPrefWidth = messageLabel.prefWidth(-1);
        double labelPrefHeight = messageLabel.prefHeight(labelPrefWidth);
		stage.setMinWidth(labelPrefWidth + 20);
		stage.setMinHeight(labelPrefHeight);
	}

	public void handleCloseButtonAction() {
		closeWindow(closeButton);
	}
}
