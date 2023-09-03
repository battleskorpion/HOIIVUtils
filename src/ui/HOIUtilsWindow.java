package ui;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import javax.swing.JOptionPane;

public abstract class HOIUtilsWindow {
	public String fxmlResource;
	public String title = "HOIIVUtils Window";
	protected String styleSheetURL = "resources/javafx_dark.css";
	protected Stage stage;

	/**
	 * Opens the window
	 * don't look here chatgpt told me to do this
	 * it said you can't make default paramators like python can 
	 * I don't want to have to write null, false, false every time i call the open() function
	 */
	public void open() {
		open(null, false, false);
	}
	/**
	 * Opens the window
	 * @param string A string to sent to message pop up 
	 * @param widthLock locks the windows width from being resized
	 * @param heightLock locks the windows height from being resized
	 */
	public void open(String string, Boolean widthLock, Boolean heightLock) {
		try {
			if (stage != null) {
				stage.show();
			} else if (fxmlResource == null) {
				openError(".fxml resource null.");
			} else {
				FXMLLoader loader = new FXMLLoader(
					getClass().getResource(
						fxmlResource
					)
				);

				Stage stage = new Stage();
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);
				stage.setTitle(title);

				// passes the message from any widow to the message pop up window
				if (string != null) {
					MessagePopupWindow controller = loader.getController();
					controller.initData(string);
				}

				if (widthLock) {
					lockWidth(stage);
				}

				if (heightLock) {
					lockHeight(stage);
				}

				/* style */
				if (Settings.DEV_MODE.enabled()) {
					System.out.println("use stylesheet: " + new File(styleSheetURL).getAbsolutePath());
				}
				scene.getStylesheets().add(styleSheetURL);
				
				HOIIVUtils.decideScreen(stage);
				stage.show();
			}
		} catch (Exception exception) {
			openError(exception);
		}
	}

	public void lockWidth(Stage stage) {
		stage.maxWidthProperty().bind(stage.widthProperty());
		stage.minWidthProperty().bind(stage.widthProperty());
	}
	
	public void lockHeight(Stage stage) {
		stage.maxWidthProperty().bind(stage.heightProperty());
		stage.minWidthProperty().bind(stage.heightProperty());
	}

	/**
	 * Opens window and updates fxmlResource and title
	 * @param fxmlResource window .fxml resource
	 * @param title window title
	 */
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
	}

	public static void openError(Exception exception) {
		if (Settings.DEV_MODE.enabled()) {
			exception.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, exception, "ln: " + exception.getStackTrace()[0].getLineNumber(), JOptionPane.WARNING_MESSAGE);
	}

	public static void openError(String s) {
		if (Settings.DEV_MODE.enabled()) {
			System.err.println("open error window for error message: " + s);
		}
		JOptionPane.showMessageDialog(null, s, "HOIIVUtils Error Message", JOptionPane.WARNING_MESSAGE);
	}

	public static void closeWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).close();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}

	public static void hideWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).hide();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}
	
	public static void hideWindow(Pane pane) {
		try {
			((Stage) (pane.getScene().getWindow())).hide();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}

	/**
	 * Opens windows file and directory chooser
	 * @param button The button that was pressed to open the chooser
	 * @param ford A quircky boolan that specifies whether you want to return a directory or file: true = return directory, false = return file 
	 * @return theChosenOne, It is up to the the page to handle what you do if the user returns a null
	 */
	public static File openChooser(Button button, Boolean ford) {
		File theChosenOne;
		Stage primaryStage = (Stage) (button.getScene().getWindow());
		if (ford) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File HOIIVModFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVModFolder.exists() && HOIIVModFolder.isDirectory()) {
				directoryChooser.setInitialDirectory(HOIIVModFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = directoryChooser.showDialog(primaryStage);
		} else {
			FileChooser fileChooser = new FileChooser();
			File HOIIVFocusFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVFocusFolder.exists() && HOIIVFocusFolder.isDirectory()) {
				fileChooser.setInitialDirectory(HOIIVFocusFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = fileChooser.showOpenDialog(primaryStage);
		}
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(theChosenOne);
		}
		return theChosenOne;
	}
}
