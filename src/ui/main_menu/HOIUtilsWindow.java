package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class HOIUtilsWindow {
	public String fxmlResource;
	public String title;
	Stage primaryStage;

	public void open() {
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