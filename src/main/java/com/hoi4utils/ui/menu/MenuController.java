package com.hoi4utils.ui.menu;


import com.hoi4utils.clausewitz.HOIIVFile;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import com.hoi4utils.ui.hoi4localization.CustomTooltipWindow;
import com.hoi4utils.ui.hoi4localization.FocusLocalizationWindow;
import com.hoi4utils.ui.hoi4localization.IdeaLocalizationWindow;
import com.hoi4utils.ui.hoi4localization.AllFocusTreesWindow;
import com.hoi4utils.ui.log_viewer.LogViewerController;
import com.hoi4utils.ui.province_colors.ProvinceColorsController;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.units.CompareUnitsWindow;
import javafx.application.Application;
import javafx.application.Platform;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class MenuController extends Application implements FXWindow {
	public static final Logger LOGGER = LogManager.getLogger(MenuController.class);
	private String fxmlResource = "Menu.fxml";
	private String title = "HOIIVUtils Menu " + HOIIVUtils.HOIIVUTILS_VERSION;
	private Stage stage;

	@FXML
	public Button settingsButton;

	public void launchMenuWindow(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		try {
			Parent root = loadFXML();
			setupAndShowStage(root);
		} catch (IOException e) {
			handleFXMLLoadError(e);
		}
	}


	public void open() {
		if (stage != null) {
			showExistingStage();
			return;
		}

		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}

		start(new Stage());
	}

	private void showExistingStage() {
		stage.show();
		LOGGER.info("Stage already exists, showing: {}", title);
	}

	private void handleMissingFXMLResource() {
		String errorMessage = "Failed to open window\nError: FXML resource is null.";
		LOGGER.error(errorMessage);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private Parent loadFXML() throws IOException {
		FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource), getResourceBundle());
		try {
			return launchLoader.load();
		} catch (IOException e) {
			throw new IOException("Failed to load FXML: " + fxmlResource, e);
		}
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

	private void setupAndShowStage(Parent root) {
		Scene scene = new Scene(root);
		addSceneStylesheets(scene);
		this.stage = createLaunchStage(scene);
		LOGGER.debug("Stage created and shown: {}", title);
	}

	private void addSceneStylesheets(Scene scene) {
		scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");

		try {
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/hoi4utils/ui/highlight-background.css")).toExternalForm());
		} catch (NullPointerException e) {
			System.err.println("Warning: Stylesheet 'highlight-background.css' not found!");
		}
	}

	private Stage createLaunchStage(Scene scene) {
		Optional.ofNullable(stage).ifPresent(Stage::close);

		Stage launchStage = new Stage();
		launchStage.setScene(scene);
		launchStage.setTitle(title);
		decideScreen(launchStage);
		launchStage.show();

		return launchStage;
	}

	private void handleFXMLLoadError(IOException e) {
		String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource;
		LOGGER.fatal("Error loading FXML: {}", fxmlResource, e);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		throw new RuntimeException(errorMessage, e);
	}

	public void openSettings() {
		closeWindow(settingsButton); // closes the menu window
		new SettingsController().open();
	}

	public void openLogViewer() {
		openUtilsWindow(new LogViewerController());
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
		if (!HOIIVFile.isUnitsFolderValid()) {
			LOGGER.warn("Unit comparison view cannot open: missing base or mod units folder.");
			JOptionPane.showMessageDialog(null, "Unit folders not found. Please check your HOI4 installation or the chosen mod directory.", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
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

	private void openUtilsWindow(HOIIVUtilsWindow utilsWindow) {
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
