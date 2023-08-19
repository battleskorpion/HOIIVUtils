package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import settings.HOIIVUtilsProperties;

import java.io.File;
import java.io.IOException;


/**
 * SettingsWindow is the window for the program settings
 */
public class SettingsWindow extends Application {

	Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		savedSettings(primaryStage);

		Parent root = FXMLLoader.load(getClass().getResource("SettingsWindow.fxml"));

		primaryStage.setTitle("HOIIVUtils Settings");

		primaryStage.setScene((new Scene(root)));

		this.primaryStage = primaryStage;

		primaryStage.show();
	}
	// ! todo fix
	private void savedSettings(Stage primaryStage) throws IOException {
		String hoi4UtilsPropertiesPath = HOIIVUtilsProperties.HOI4UTILS_PROPERTIES_PATH;

		if (new File(hoi4UtilsPropertiesPath + "\\HOIIVUtils_properties").exists()) {
			HOIIVUtils.firstTimeSetup = false;

			HOIIVUtils.settings = new HOIIVUtilsProperties();

			HOIIVUtils.decideScreen(primaryStage);
		}
		else {
			HOIIVUtils.firstTimeSetup = true;
		}
	}

	public void launchSettingsWindow(String... var0) {
		super.launch(var0);
	}

	public void open() {
		try {
			if (primaryStage != null) {
				primaryStage.show();
			} else {
				start(new Stage());
			}
		} 
		catch (Exception exc) {
			HOIIVUtils.openError(exc);
		}
	}
}