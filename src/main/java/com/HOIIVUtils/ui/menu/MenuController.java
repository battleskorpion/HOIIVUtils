package com.HOIIVUtils.ui.menu;

import com.HOIIVUtils.ui.console.ConsoleController;
import com.HOIIVUtils.ui.hoi4localization.CustomTooltipWindow;
import com.HOIIVUtils.ui.hoi4localization.FocusLocalizationWindow;
import com.HOIIVUtils.ui.hoi4localization.IdeaLocalizationWindow;
import com.HOIIVUtils.ui.hoi4localization.UnlocalizedFocusWindow;
import com.HOIIVUtils.ui.statistics.StatisticsController;
import com.HOIIVUtils.hoi4utils.HOIIVUtils;
import com.HOIIVUtils.hoi4utils.clausewitz_data.state.State;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.HOIIVUtils.ui.FXWindow;
import com.HOIIVUtils.ui.buildings.BuildingsByCountryWindow;
import com.HOIIVUtils.ui.clausewitz_gfx.InterfaceFileListWindow;
import com.HOIIVUtils.ui.focus_view.FocusTreeWindow;
import com.HOIIVUtils.ui.map.MapGenerationWindow;
import com.HOIIVUtils.ui.parser.ParserViewerWindow;
import com.HOIIVUtils.ui.settings.SettingsController;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuController extends Application implements FXWindow {
	private Stage stage;
	private String fxmlResource = "Menu.fxml";
	private String title;

	@FXML public Button settingsButton;
	@FXML public Button statisticsButton;
	@FXML public Button consoleButton;
	@FXML public Button focusLocalizButton;
	@FXML public Button findFocusesWithoutLocalization;
	@FXML public Button customTooltipLocalizationButton;
	@FXML public Button viewBuilding;
	@FXML public Button viewGFX;
	@FXML public Button focusTreeViewButton;
	

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
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Error menu controller!");
			e.printStackTrace();
		}
		State.readStates();
	}

	public void open(){
		if (stage != null) {
			stage.show();
			System.out.println("Menu controller showed stage with open");
		} else {
			start(new Stage());
			System.out.println("Menu controller started a new stage with open cuz stage was null");
		}
	}

	public void openSettings() {
		closeWindow(settingsButton); //closes the menu window
		SettingsController window = new SettingsController();
		window.open();
	}

	public void openStatistics() {
		StatisticsController window = new StatisticsController();
		window.open();
	}

	public void openConsole() {
		ConsoleController window = new ConsoleController();
		window.open();
	}

	public void openLocalizeFocusTree() {
		FocusLocalizationWindow window = new FocusLocalizationWindow();
		window.open();
	}

	public void openLocalizeIdeaFile() {
		IdeaLocalizationWindow window = new IdeaLocalizationWindow();
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
//		State.readStates();
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

	public void openMapGeneration() {
		MapGenerationWindow window = new MapGenerationWindow();
		window.open();
	}

	public void openParserView() {
		ParserViewerWindow window = new ParserViewerWindow();
		window.open();
	}

	/* from HOIUtilsWindow but can only extend one class */
	/**
	 * Opens window and updates fxmlResource and title
	 * @param fxmlResource window .fxml resource
	 * @param title window title
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
