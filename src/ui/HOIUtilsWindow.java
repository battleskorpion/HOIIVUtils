package ui;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
	protected Stage primaryStage;

	/**
	 * Opens window
	 */
	public void open() {
		try {
			if (primaryStage != null) {
				primaryStage.show();
			} else if (fxmlResource == null) {
				openError(".fxml resource null.");
			} else {
				Stage primaryStage = new Stage();
				
				Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));

				primaryStage.setTitle(title);
				Scene scene = new Scene(root);
				primaryStage.setScene(scene);

				/* style */
				if (Settings.DEV_MODE.enabled()) {
					System.out.println("use stylesheet: " + new File(styleSheetURL).getAbsolutePath());
				}
				scene.getStylesheets().add(styleSheetURL);

				HOIIVUtils.decideScreen(primaryStage);
				primaryStage.show();
			}
		} catch (Exception exception) {
			openError(exception);
		}
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
	 * @return It is up to the the page to handle what you do if the user returns a null
	 */
	public static File openChooser(Button button, Boolean ford) {
		File theChosenOne;
		if (ford) {
			Stage primaryStage = (Stage) (button.getScene().getWindow());
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File HOIIVModFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVModFolder.exists() && HOIIVModFolder.isDirectory()) {
				directoryChooser.setInitialDirectory(HOIIVModFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = directoryChooser.showDialog(primaryStage);
		} else {
			Stage primaryStage = (Stage) (button.getScene().getWindow());
			FileChooser fileChooser = new FileChooser();
			File HOIIVFocusFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVFocusFolder.exists() && HOIIVFocusFolder.isFile()) {
				fileChooser.setInitialDirectory(HOIIVFocusFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = fileChooser.showOpenDialog(primaryStage);;
		}
		return theChosenOne;
	}
}
