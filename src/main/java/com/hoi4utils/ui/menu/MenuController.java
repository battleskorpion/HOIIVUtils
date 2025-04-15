package com.hoi4utils.ui.menu;


import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.buildings.BuildingsByCountryController;
import com.hoi4utils.ui.clausewitz_gfx.InterfaceFileListController;
import com.hoi4utils.ui.focus_view.FocusTreeController;
import com.hoi4utils.ui.hoi4localization.ManageFocusTreesController;
import com.hoi4utils.ui.hoi4localization.CustomTooltipController;
import com.hoi4utils.ui.hoi4localization.FocusLocalizationController;
import com.hoi4utils.ui.hoi4localization.IdeaLocalizationController;
import com.hoi4utils.ui.log_viewer.LogViewerController;
import com.hoi4utils.ui.map.MapEditorController;
import com.hoi4utils.ui.map.MapGenerationController;
import com.hoi4utils.ui.parser.ParserViewerController;
import com.hoi4utils.ui.province_colors.ProvinceColorsController;
import com.hoi4utils.ui.settings.SettingsController;
import com.hoi4utils.ui.units.CompareUnitsController;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class MenuController extends Application implements JavaFXUIManager {
	public static final Logger LOGGER = LogManager.getLogger(MenuController.class);
	public Button focusLocalizButton;
	public Button openAllFocusesButton;
	public Button ideasLocalizationButton;
	public Button customTooltipLocalizationButton;
	public Button viewBuilding;
	public Button viewGFX;
	public Button focusTreeViewButton;
	public Button viewUnitComparison;
	public Button viewProvinceColors;
	public Button settingsButton;
	

	private String fxmlResource = "Menu.fxml";
	private String title = "HOIIVUtils Menu " + HOIIVUtils.HOIIVUTILS_VERSION;


	
	@FXML
	void initialize() {
		LOGGER.debug("MenuController initialized");
		
		// Check for invalid folder paths and show appropriate warnings
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() throws Exception {
				SettingsValidator.checkForInvalidSettingsAndShowWarnings(settingsButton, LOGGER);
				return null;
			}
		};
		
		new Thread(task).start();

		boolean valid = HOIIVUtils.get("valid.Settings").equals("true");
		focusLocalizButton.setDisable(!valid);
		openAllFocusesButton.setDisable(!valid);
		ideasLocalizationButton.setDisable(!valid);
		customTooltipLocalizationButton.setDisable(!valid);
		viewBuilding.setDisable(!valid);
		viewGFX.setDisable(!valid);
		focusTreeViewButton.setDisable(!valid);
		viewUnitComparison.setDisable(!valid);
	}

	public void launchMenuWindow(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) {
		try {
			Parent result;
			FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource), getResourceBundle());
			result = launchLoader.load();
			Parent root = result;
			Scene scene = new Scene(root);

			if ("dark".equals(HOIIVUtils.get("theme"))) {
				scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");
			}
//			scene.getStylesheets().add("com/hoi4utils/ui/highlight-background.css");

            stage.setScene(scene);
			stage.setTitle(title);
			decideScreen(stage);
			stage.show();

			LOGGER.debug("Stage created and shown: {}", title);
		} catch (IOException e) {
			String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource;
			LOGGER.fatal("Error loading FXML: {}", fxmlResource, e);
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(errorMessage, e);
		}
	}


	public void open() {
		if (fxmlResource == null) {
			String errorMessage = "Failed to open window\nError: FXML resource is null.";
			LOGGER.error(errorMessage);
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		start(new Stage());
	}

	private static ResourceBundle getResourceBundle() {
		Locale currentLocale = Locale.getDefault();
		ResourceBundle bundle;
		try {
			bundle = ResourceBundle.getBundle("menu", currentLocale);
		} catch (MissingResourceException e) {
			LOGGER.warn("Could not find ResourceBundle for locale {}. Falling back to English.", currentLocale);
			bundle = ResourceBundle.getBundle("menu", Locale.ENGLISH);
		}
		return bundle;
	}

	public void openSettings() {
		closeWindow(settingsButton); // closes the menu window
		openUtilsWindow(new SettingsController());
	}

	public void openLogViewer() {
		openUtilsWindow(new LogViewerController());
	}

	public void openLocalizeFocusTree() {
		openUtilsWindow(new FocusLocalizationController());
	}

	public void openLocalizeIdeaFile() {
		openUtilsWindow(new IdeaLocalizationController());
	}

	public void openAllFocusesWindow() {
		openUtilsWindow(new ManageFocusTreesController());
	}

	public void openCustomTooltip() {
		openUtilsWindow(new CustomTooltipController());
	}

	public void openBuildingsByCountry() {
		openUtilsWindow(new BuildingsByCountryController());
	}

	public void openInterfaceFileList() {
		openUtilsWindow(new InterfaceFileListController());
	}

	public void openFocusTreeViewer() {
		openUtilsWindow(new FocusTreeController());
	}

	public void openUnitComparisonView() {
		if (!HOIIVFiles.isUnitsFolderValid()) {
			LOGGER.warn("Unit comparison view cannot open: missing base or mod units folder.");
			JOptionPane.showMessageDialog(null, "Unit folders not found. Please check your HOI4 installation or the chosen mod directory.", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		openUtilsWindow(new CompareUnitsController());
	}

	public void openProvinceColors() {
		openUtilsWindow(new ProvinceColorsController());
	}

	public void openMapGeneration() {
		openUtilsWindow(new MapGenerationController());
	}
	
	public void openMapEditor() { openUtilsWindow(new MapEditorController()); }

	public void openParserView() {
		openUtilsWindow(new ParserViewerController());
	}

	private void openUtilsWindow(HOIIVUtilsAbstractController utilsWindow) {
		utilsWindow.open();
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
