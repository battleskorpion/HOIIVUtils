package com.hoi4utils.ui.menu;

import com.hoi4utils.Settings;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.localization.EnglishLocalizationManager;
import com.hoi4utils.clausewitz.localization.LocalizationManager;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import com.hoi4utils.ui.console.ConsoleController;
import com.hoi4utils.ui.hoi4localization.CustomTooltipWindow;
import com.hoi4utils.ui.hoi4localization.FocusLocalizationWindow;
import com.hoi4utils.ui.hoi4localization.IdeaLocalizationWindow;
import com.hoi4utils.ui.hoi4localization.AllFocusTreesWindow;
import com.hoi4utils.ui.statistics.StatisticsController;
import com.hoi4utils.ui.province_colors.ProvinceColorsController;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.units.CompareUnitsWindow;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.hoi4utils.ui.FXWindow;
import com.hoi4utils.ui.buildings.BuildingsByCountryWindow;
import com.hoi4utils.ui.clausewitz_gfx.InterfaceFileListWindow;
import com.hoi4utils.ui.focus_view.FocusTreeWindow;
import com.hoi4utils.ui.map.MapGenerationWindow;
import com.hoi4utils.ui.parser.ParserViewerWindow;
import com.hoi4utils.ui.settings.SettingsController;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuController extends Application implements FXWindow {
	private Stage stage;
	private String fxmlResource = "Menu.fxml";
	private String title;

	@FXML
	public Button settingsButton;
	@FXML
	public Button statisticsButton;
	@FXML
	public Button consoleButton;
	@FXML
	public Button focusLocalizButton;
	@FXML
	public Button findFocusesWithoutLocalization;
	@FXML
	public Button customTooltipLocalizationButton;
	@FXML
	public Button viewBuilding;
	@FXML
	public Button viewGFX;
	@FXML
	public Button viewProvinceColors;
	@FXML
	public Button focusTreeViewButton;
	@FXML
	public Button viewUnitComparison;

	/* Constructor */
	public MenuController() {
		fxmlResource = "Menu.fxml";
		title = "Menu";
	}

	public void launchMenuWindow(String[] args) {
		System.out.println("Menu Controller ran launchMenuWindow");
		launch(args);
	}

	@FXML
	void initialize() {
		System.out.println("Menu Controller initialized");
	}

	@Override
	public void start(Stage stage) {
		try {
			Locale currentLocale = Locale.getDefault();

			ResourceBundle bundle = ResourceBundle.getBundle("menu", currentLocale);
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource), bundle);
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);

			this.stage = stage;
			stage.setScene(scene);

			stage.setTitle(title);
			stage.show();
			System.out.println("Menu Controller created it's own stage and showed it");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error menu controller!");
			e.printStackTrace();
		}
		if (!Settings.DEMO_MODE.enabled()) {
			if (Settings.LOAD_LOCALIZATION.enabled()) {
				LocalizationManager LocalizationManager = new EnglishLocalizationManager();
				LocalizationManager.reload();
			}
			State.read();
			FocusTree.read();
		}
	}

	public void open() {
		if (stage != null) {
			stage.show();
			System.out.println("Menu controller showed stage with open");
		} else {
			start(new Stage());
			System.out.println("Menu controller started a new stage with open cuz stage was null");
		}
	}

	public void openSettings() {
		closeWindow(settingsButton); // closes the menu window
		SettingsController window = new SettingsController();
		window.open();
	}

	private void openUtilsWindow(HOIIVUtilsWindow utilsWindow) {
		utilsWindow.open();
	}

	public void openStatistics() {
		openUtilsWindow(new StatisticsController());
	}

	public void openConsole() {
		openUtilsWindow(new ConsoleController());
	}

	public void openLocalizeFocusTree() {
		openUtilsWindow(new FocusLocalizationWindow());
	}

	public void openLocalizeIdeaFile() {
		openUtilsWindow(new IdeaLocalizationWindow());
	}

	public void openAllFocusesWindow() {
		openUtilsWindow(new AllFocusTreesWindow());
	}

	public void openCustomTooltip() {
		openUtilsWindow(new CustomTooltipWindow());
	}

	public void openBuildingsByCountry() {
		openUtilsWindow(new BuildingsByCountryWindow());
	}

	public void openInterfaceFileList() {
		openUtilsWindow(new InterfaceFileListWindow());
	}

	public void openFocusTreeViewer() {
		openUtilsWindow(new FocusTreeWindow());
	}

	public void openUnitComparisonView() {
		openUtilsWindow(new CompareUnitsWindow());
	}

	public void openProvinceColors() {
		openUtilsWindow(new ProvinceColorsController());
	}

	public void openMapGeneration() {
		openUtilsWindow(new MapGenerationWindow());
	}

	public void openParserView() {
		openUtilsWindow(new ParserViewerWindow());
	}

	/* from HOIIVUtilsStageLoader but can only extend one class */
	/**
	 * Opens window and updates fxmlResource and title
	 * 
	 * @param fxmlResource window .fxml resource
	 * @param title        window title
	 */
	@Override
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
	}

	@Override
	public String getFxmlResource() {
		return fxmlResource;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
}
