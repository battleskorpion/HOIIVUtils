package main.java.com.hoi4utils.ui.menu;

import main.java.com.hoi4utils.Settings;
import main.java.com.hoi4utils.clauzewitz.data.focus.FocusTree;
import main.java.com.hoi4utils.clauzewitz.localization.EnglishLocalizationManager;
import main.java.com.hoi4utils.clauzewitz.localization.LocalizationManager;
import main.java.com.hoi4utils.ui.HOIIVUtilsWindow;
import main.java.com.hoi4utils.ui.console.ConsoleController;
import main.java.com.hoi4utils.ui.hoi4localization.CustomTooltipWindow;
import main.java.com.hoi4utils.ui.hoi4localization.FocusLocalizationWindow;
import main.java.com.hoi4utils.ui.hoi4localization.IdeaLocalizationWindow;
import main.java.com.hoi4utils.ui.hoi4localization.AllFocusTreesWindow;
import main.java.com.hoi4utils.ui.statistics.StatisticsController;
import main.java.com.hoi4utils.clauzewitz.HOIIVUtils;
import main.java.com.hoi4utils.clauzewitz.map.state.State;
import main.java.com.hoi4utils.ui.units.CompareUnitsWindow;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import main.java.com.hoi4utils.ui.FXWindow;
import main.java.com.hoi4utils.ui.buildings.BuildingsByCountryWindow;
import main.java.com.hoi4utils.ui.clausewitz_gfx.InterfaceFileListWindow;
import main.java.com.hoi4utils.ui.focus_view.FocusTreeWindow;
import main.java.com.hoi4utils.ui.map.MapGenerationWindow;
import main.java.com.hoi4utils.ui.parser.ParserViewerWindow;
import main.java.com.hoi4utils.ui.settings.SettingsController;

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
		BuildingsByCountryWindow window = new BuildingsByCountryWindow();
		window.open();
	}

	public void openInterfaceFileList() {
		InterfaceFileListWindow window = new InterfaceFileListWindow();
		window.open();
	}

	public void openFocusTreeViewer() {
		FocusTreeWindow window = new FocusTreeWindow();
		window.open();
	}

	public void openUnitComparisonView() {
		CompareUnitsWindow window = new CompareUnitsWindow();
		window.open();
	}

	public void openMapGeneration() {
		MapGenerationWindow window = new MapGenerationWindow();
		window.open();
	}

	public void openParserView() {
		ParserViewerWindow window = new ParserViewerWindow();
		window.open();
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
