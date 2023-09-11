package ui.main_menu;

import hoi4utils.clausewitz_coding.state.State;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.HOIUtilsWindow;
import ui.buildings.BuildingsByCountryWindow;
import ui.clausewitz_gfx.InterfaceFileListWindow;
import ui.custom_tooltip.CustomTooltipWindow;
import ui.focus_localization.FocusLocalizationWindow;
import ui.focus_localization.UnlocalizedFocusWindow;

public class MenuWindow extends Application{
	String fxmlResource = "MenuWindow.fxml";
	String title = "HOIIVUtils Menu";
	String styleSheetURL = "resources/javafx_dark.css";
	Stage stage;

	@FXML public Button settingsButton;
	@FXML public Button statisticsButton;
	@FXML public Button focusLocalizButton;
	@FXML public Button findFocusesWithoutLocalization;
	@FXML public Button customTooltipLocalizationButton;
	@FXML public Button viewBuilding;
	@FXML public Button viewGFX;
	

	/* Constructor */
	public MenuWindow() {
		fxmlResource = "MenuWindow.fxml";
		title = "HOIIVUtils Menu";
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
			getClass().getResource(
				fxmlResource
			)
		);

		this.stage = stage;
		Scene scene = new Scene(loader.load());
		stage.setScene(scene);
		stage.setTitle(title);

		/* style */
		scene.getStylesheets().add(styleSheetURL);

		stage.show();

		stage.show();
	}

	public void launchMenuWindow(String... var0) {
		Application.launch(var0);
	}

	public void open(){
		try {
			if (stage != null) {
				stage.show();
			} else {
				start(new Stage());
			}
		} 
		catch (Exception exc) {
			HOIUtilsWindow.openError(exc);
		}
	}

	public void openSettings() {
		HOIUtilsWindow.closeWindow(settingsButton); //closes the menu window
		SettingsWindow window = new SettingsWindow();
		window.open();
	}

	public void openStatistics() {
		StatisticsWindow window = new StatisticsWindow();
		window.open();
	}

	public void openLocalizeFocusTree() {
		FocusLocalizationWindow window = new FocusLocalizationWindow();
		window.open();
	}

	public void openUnlocalizedFocus() {
		UnlocalizedFocusWindow window = new UnlocalizedFocusWindow();
		window.open();
	}

	public void openCustomTooltip() {
		CustomTooltipWindow window = new CustomTooltipWindow();
		window.open();
	}

	public void openBuildingsByCountry() {
		State.readStates();
		BuildingsByCountryWindow window = new BuildingsByCountryWindow();
		window.open();
	}

	public void openInterfaceFileList() {
		InterfaceFileListWindow window = new InterfaceFileListWindow();
		window.open();
	}
}
