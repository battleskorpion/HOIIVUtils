package ui.main_menu;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import hoi4utils.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.buildings.BuildingsByCountryWindow;

import java.io.File;
import java.io.IOException;

public class MenuWindow extends Application{
	public String fxmlResource = "MenuWindow.fxml";
	String title = "HOIIVUtils Menu";
	String styleSheetURL = "resources/javafx_dark.css";
	Stage primaryStage;

	@FXML
	public Button settingsButton;
	public Button statisticsButton;
	public Button focusLocalizButton;
	public Button viewBuilding;
	

	/* Constructor */
	public MenuWindow() {
		fxmlResource = "MenuWindow.fxml";
		title = "HOIIVUtils Menu";
	}

	@FXML
	void initialize() {

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));
		primaryStage.setTitle(title);
		Scene scene = (new Scene(root));
		primaryStage.setScene(scene);
		this.primaryStage = primaryStage;

		/* style */
		scene.getStylesheets().add(styleSheetURL);

		primaryStage.show();
	}

	public void launchMenuWindow(String... var0) {
		super.launch(var0);
	}

	// * Menu Window Controller

	public void open(){
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

	// todo close menu
	public void openSettings() {
		HOIIVUtils.closeWindow(settingsButton);

		SettingsWindow settingsWindow = new SettingsWindow();

		settingsWindow.open();
	}

	public void openStatistics() {
		StatisticsWindow statisticsWindow = new StatisticsWindow();

		statisticsWindow.open();
	}

	public void openLocalizeFocusTree() {
		//FocusLocalizationWindow localizationWindow = new FocusLocalizationWindow();
		//localizationWindow.open();
	}

	public void openBuildingsByCountry() {
		BuildingsByCountryWindow buildingsByCountryWindow = new BuildingsByCountryWindow();

		buildingsByCountryWindow.open();
	}
}
