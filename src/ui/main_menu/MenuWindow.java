package ui.main_menu;

import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.clausewitz_coding.state.StateCategory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.HOIUtilsWindow;
import ui.buildings.BuildingsByCountryWindow;
import ui.focus_localization.FocusLocalizationWindow;

import java.io.IOException;

import static hoi4utils.HOIIVUtils.openError;
import static hoi4utils.HOIIVUtils.watchStateFiles;

public class MenuWindow extends HOIUtilsWindow {
	// * Menu Window Controller

	@FXML
	public Button settingsButton;
	public Button statisticsButton;
	public Button focusLocalizButton;
	public Button viewBuilding;

	public MenuWindow() {
		fxmlResource = "MenuWindow.fxml";
		title = "HOIIVUtils Menu";
	}

	@FXML
	void initialize() {

	}

	public void open(){
		if (super.primaryStage == null) {
			/* load */
			StateCategory.loadStateCategories();
			State.readStates();
			//		Interface.loadGFX();    // !todo restore

			/* main listeners */
			try {
				watchStateFiles(HOIIVUtils.states_folder);
			} catch (NullPointerException | IOException exc) {
				openError(exc);
				return;
			}
		}
		super.open();
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
		/* open focus loc window */
		FocusLocalizationWindow localizationWindow = new FocusLocalizationWindow();
		localizationWindow.open();
	}

	public void openBuildingsByCountry() {
		BuildingsByCountryWindow buildingsByCountryWindow = new BuildingsByCountryWindow();

		buildingsByCountryWindow.open();
	}
}
