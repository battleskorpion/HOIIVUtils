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

import javax.swing.*;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.hoi4utils.clausewitz.HOIIVUtils.LOGGER;

public class MenuController extends Application implements FXWindow {
	private String fxmlResource = "Menu.fxml";
	private String title = "HOIIVUtils Menu " + HOIIVUtils.HOIIVUTILS_VERSION;
	private Stage stage;

	@FXML
	public Button settingsButton;
	@FXML
	public Button focusLocalizButton;
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

	public void launchMenuWindow(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Locale currentLocale = Locale.getDefault();
		ResourceBundle bundle;

		try {
			bundle = ResourceBundle.getBundle("menu", currentLocale);
		} catch (MissingResourceException e) {
			LOGGER.warn("Could not find ResourceBundle for locale {}. Falling back to English.", currentLocale);
			bundle = ResourceBundle.getBundle("menu", Locale.ENGLISH);
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource), bundle);
		Parent root;
		try {
			root = loader.load();
		} catch (IOException e) {
			LOGGER.error("Failed to load FXML resource: {}", fxmlResource, e);
			return;
		}

		Scene scene = new Scene(root);
		if ("dark".equals(HOIIVUtils.get("theme"))) {
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
		}

		this.stage = stage;
		stage.setScene(scene);
		stage.setTitle(title);
		stage.show();
	}


	public void open() {
		Platform.runLater(() -> {
			if (stage == null) {
				start(new Stage());
			} else {
				stage.show();
			}
		});
	}


	public void openSettings() {
		closeWindow(settingsButton); // closes the menu window
		new SettingsController().open();
	}

	private void openUtilsWindow(HOIIVUtilsWindow utilsWindow) {
		utilsWindow.open();
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
			showErrorDialog("Unit folders not found. Please check your HOI4 installation or the chosen mod directory.");
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

		if (stage != null) {
			Platform.runLater(() -> {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
					Parent root = loader.load();
					stage.setScene(new Scene(root));
					stage.setTitle(title);
					stage.show();
				} catch (IOException e) {
					LOGGER.error("Failed to reload FXML resource: {}", fxmlResource, e);
				}
			});
		}
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

	private void showErrorDialog(String message) {
		Platform.runLater(() -> JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE));
	}
}
