package ui;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class HOIUtilsWindow {
	public String fxmlResource;
	public String title = "HOIIVUtils Window";
	Stage primaryStage;

	/**
	 * Opens window
	 */
	public void open() {
		try {
			if (primaryStage != null) {
				primaryStage.show();
			} else if (fxmlResource == null) {
				HOIIVUtils.openError("fxml resource null.");
			} else {
				Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));

				Stage primaryStage = new Stage();

				primaryStage.setTitle(title);
				primaryStage.setScene((new Scene(root)));

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

		try {
			if (primaryStage != null) {
				primaryStage.show();
			} else {
				Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));

				Stage primaryStage = new Stage();

				primaryStage.setTitle(title);
				primaryStage.setScene((new Scene(root)));

				HOIIVUtils.decideScreen(primaryStage);
				primaryStage.show();
			}
		} catch (Exception exception) {
			HOIIVUtils.openError(exception);
		}
	}
}
