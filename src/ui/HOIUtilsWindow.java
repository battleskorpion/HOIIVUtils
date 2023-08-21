package ui;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

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
				HOIIVUtils.openError(".fxml resource null.");
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
			HOIIVUtils.openError(exception);
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
}
