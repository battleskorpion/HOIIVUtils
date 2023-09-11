package ui;

import hoi4utils.Settings;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static hoi4utils.Settings.PREFERRED_SCREEN;

import java.io.File;

import javax.swing.JOptionPane;

public abstract class HOIUtilsWindow {
	public String fxmlResource;
	public String title = "HOIIVUtils Window";
	protected String styleSheetURL = "resources/javafx_dark.css";
	protected Stage stage;
	protected FXMLLoader loader;
	
	/**
	 * Opens the window
	 */
	public void open() {
		try {
			if (stage != null) {
				stage.show();
			} else if (fxmlResource == null) {
				openError("This Stage's FXML Resource Doesn't exsist");
			} else {
				loader = new FXMLLoader(
						getClass().getResource(
								fxmlResource
						)
				);

				Stage stage = new Stage();
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);
				stage.setTitle(title);

				/* style */
				if (Settings.DEV_MODE.enabled()) {
					System.out.println("use stylesheet: " + new File(styleSheetURL).getAbsolutePath());
				}
				scene.getStylesheets().add(styleSheetURL);

				HOIUtilsWindow.decideScreen(stage);
				stage.show();
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

	/**
	 * 
	 * @param stage
	 */
	public static void decideScreen(Stage stage) {
		Integer preferredScreen = (Integer) PREFERRED_SCREEN.getSetting();
		ObservableList<Screen> screens = Screen.getScreens();
		if (preferredScreen > screens.size() - 1) {
			if (Settings.DEV_MODE.enabled()) {
				System.err.println( "Preferred screen does not exist, resorting to defaults.");
			}
			return;
		}
		Screen screen = screens.get(preferredScreen);
		if (screen == null) {
			if (Settings.DEV_MODE.enabled()) {
				System.err.println( "Preferred screen is null error, resorting to defaults.");
			}
			return;
		}
		Rectangle2D bounds = screen.getVisualBounds();
		stage.setX(bounds.getMinX() + 200);
		stage.setY(bounds.getMinY() + 200);
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

	/**
	 *
	 *
	 * @param fxcomponent The node (javafx component), must belong to a scene. The stage the scene belongs to will be hidden.
	 * @see Node
	 * @see javafx.stage.Window
	 */
	public static void hideWindow(Node fxcomponent) {
		try {
			((Stage) (fxcomponent.getScene().getWindow())).hide();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}

	/**
	 * Opens windows file and directory chooser
	 * @param fxcomponent The node (javafx component) that was pressed to open the chooser, must belong to a scene
	 * @param ford A quirky boolean that specifies whether you want to return a directory or file: true = return directory, false = return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the user returns a null
	 * @see File
	 * @see Node
	 */
	public static File openChooser(Node fxcomponent, Boolean ford) {
		File theChosenOne;
		Stage stage = (Stage) (fxcomponent.getScene().getWindow());      // .getScene() works on a Node object, which is why this function is okay to accept any Node object/descendants.
		if (ford) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File HOIIVModFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVModFolder.exists() && HOIIVModFolder.isDirectory()) {
				directoryChooser.setInitialDirectory(HOIIVModFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = directoryChooser.showDialog(stage);
		} else {
			FileChooser fileChooser = new FileChooser();
			File HOIIVFocusFolder = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");
			if (HOIIVFocusFolder.exists() && HOIIVFocusFolder.isDirectory()) {
				fileChooser.setInitialDirectory(HOIIVFocusFolder);
			} else if (Settings.DEV_MODE.enabled()) {
				openError("Couldn't find directory/folder because it does not exist.");
			}
			theChosenOne = fileChooser.showOpenDialog(stage);
		}
		return theChosenOne;
	}
}
