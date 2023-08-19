package ui.main_menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.buildings.BuildingsByCountryWindow;
import ui.focus_localization.FocusLocalizationWindow;

public class MenuWindowController {

    @FXML
    public Button settingsButton;
    public Button statisticsButton;
    public Button focusLocalizButton;
    public Button viewBuilding;

    @FXML
    void initialize() {

    }
    // todo close menu
    public void openSettings() {
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
