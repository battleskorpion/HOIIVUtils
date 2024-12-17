package com.hoi4utils.ui.menu;


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
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MenuController extends Application implements FXWindow {
	private String fxmlResource = "Menu.fxml";
	private String title = "HOIIVUtils Menu " + HOIIVUtils.HOIIVUTILS_VERSION;
	private Stage stage;

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
	@FXML
	public Button buttonTest;

	public void launchMenuWindow(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Locale currentLocale = Locale.getDefault();

		ResourceBundle bundle = ResourceBundle.getBundle("menu", currentLocale);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource), bundle);

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
		if (Objects.equals(HOIIVUtils.get("theme"), "dark")) {
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
		}
		this.stage = stage;
		stage.setScene(scene);
		stage.setTitle(title);
		stage.show();
	}

	public void open() {
		if (stage != null) {
			stage.show();
		} else {
			start(new Stage());
		}
	}

	public void openSettings() {
		closeWindow(settingsButton); // closes the menu window
		new SettingsController().open();
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
