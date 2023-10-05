package ui.menu;

import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_coding.idea.Idea;
import hoi4utils.clausewitz_coding.state.State;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.FXWindow;
import ui.buildings.BuildingsByCountryWindow;
import ui.clausewitz_gfx.InterfaceFileListWindow;
import ui.console.ConsoleController;
import ui.focus.FocusTreeWindow;
import ui.hoi4localization.CustomTooltipWindow;
import ui.hoi4localization.FocusLocalizationWindow;
import ui.hoi4localization.IdeaLocalizationWindow;
import ui.hoi4localization.UnlocalizedFocusWindow;
import ui.settings.SettingsController;
import ui.statistics.StatisticsController;

public class MenuController extends Application implements FXWindow {
	private Stage stage;
	private String fxmlResource = "Menu.fxml";
	private String title = "Menu";

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
	
			this.stage = stage;
			stage.setScene(scene);
			
			stage.setTitle(title);
			stage.show();
			System.out.println("Menu Controller created it's own stage and showed it");
		} catch(Exception e) {
			e.printStackTrace();
		}
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
		State.readStates();
		BuildingsByCountryWindow window = new BuildingsByCountryWindow();
		window.open();
	}

	public void openInterfaceFileList() {
		InterfaceFileListWindow window = new InterfaceFileListWindow();
		window.open();
	}

	public void openFocusTreeViewer() {
		FocusTreeWindow window = new FocusTreeWindow();
		window.setVisible(true);
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
