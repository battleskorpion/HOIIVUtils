package ui.main_menu;

import ui.HOIUtilsWindow;
import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import hoi4utils.SettingsManager;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static hoi4utils.Settings.MOD_PATH;

/**
 * SettingsWindow is the window and controller for the program settings
 */
public class SettingsWindow extends Application {
	public final String fxmlResource = "SettingsWindow.fxml";
	final String title = "HOIIVUtils Settings";
	final String styleSheetURL = "resources/javafx_dark.css";
	Stage primaryStage;

	@FXML public Pane idPane;
	@FXML public Label idVersionLabel;
	@FXML public TextField idModPathTextField;
	@FXML public Label idHOIIVModFolderLabel;
	@FXML public Button idBrowseButton;
	@FXML public CheckBox idDevModeCheckBox;
	@FXML public CheckBox idSkipSettingsCheckBox;
	@FXML public Button idOkButton;
	@FXML public File selectedDirectory;

	/* Constructor */
	public SettingsWindow() {
		tempSettings = new HashMap<>();
	}

	@FXML
	void initialize() {
		includeVersion();
		includeSettingValues();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));
		primaryStage.setTitle(title);
		Scene scene = (new Scene(root));
		primaryStage.setScene(scene);
		this.primaryStage = primaryStage;

		/* style */
		scene.getStylesheets().add(styleSheetURL);

		primaryStage.show();
		lockWindowHorizontalAxis();
	}

	public void open() {
		try {
			if (primaryStage != null) {
				primaryStage.show();
				lockWindowHorizontalAxis();
			} else {
				start(new Stage());
			}
		} 
		catch (Exception exc) {
			HOIUtilsWindow.openError(exc);
		}
	}

	public void lockWindowHorizontalAxis() {
		primaryStage.maxWidthProperty().bind(primaryStage.widthProperty());
		primaryStage.minWidthProperty().bind(primaryStage.widthProperty());
	}

	public void launchSettingsWindow(String... var0) {
		super.launch(var0);
	}

	// * Settings Window Controller

	HashMap<Settings, String> tempSettings;

/* Start */

	public void includeVersion() {
		idVersionLabel.setText(HOIIVUtils.hoi4utilsVersion);
	}

	public void includeSettingValues() {
		boolean isFirstTime = HOIIVUtils.firstTimeSetup;
		if (!isFirstTime) {
			if ((String) MOD_PATH.getSetting() != "null") {
				idModPathTextField.setText((String) MOD_PATH.getSetting());
			}
			idDevModeCheckBox.setSelected(Settings.DEV_MODE.enabled());
			idSkipSettingsCheckBox.setSelected(Settings.SKIP_SETTINGS.enabled());
			enableOkButton();
		}
	}

	public void enableOkButton() {
		idOkButton.setDisable(false);
	}

	public void disableOkButton() {
		idOkButton.setDisable(true);
	}
	
	/** User Interactive Text Feild in Settings Window
	 * Allows the user to type in the text field.
	 * It detects whever the user entered a valid directory.
	 * Saves the directory path to hoi4utils settings: MOD_PATH
	*/
	public void handleModPathTextField() {
		if (modPathIsDirectory()) {
			if (idOkButton.isDisabled()) {
				enableOkButton();
			}
		} else {
			disableOkButton();
		}
		String pathText = idModPathTextField.getText();
		if (pathText.isEmpty()) {
			pathText = null;
		}
		tempSettings.put(MOD_PATH, pathText);
	}

	/**
	 * returns true if the mod path in mod path text field is a directory path that exists.
	 * @return
	 */
	public boolean modPathIsDirectory() {
		File fileModPath = new File(idModPathTextField.getText());
		boolean exists = fileModPath.exists();
		if (!exists) {
			return false;
		}
		return fileModPath.isDirectory();
	}
	
	/** User Interactive Button in Settings Window
	 * Opens up operating system Directory Chooser
	 * Will do nothing if the user exits or cancels window
	 * Updates Text Field when directory is selected
	 * Saves the directory path to MOD_PATH
	 */
	public void handleBrowseAction() {
		File selectedDirectory = HOIUtilsWindow.openChooser(idBrowseButton, true); // ? I don't know how to make this pass any Class, Class<?> didn't work for me
		if (selectedDirectory == null) {
			return;
		}
		idModPathTextField.setText(selectedDirectory.getAbsolutePath());
		updateModPath(selectedDirectory);
	}

	public void updateModPath(File selectedDirectory) {
		if (modPathIsDirectory()) {
			if (idOkButton.isDisabled()) {
				enableOkButton();
			}
		} else {
			disableOkButton();
		}
		tempSettings.put(MOD_PATH, selectedDirectory.getAbsolutePath());
	}

	public void handleDevModeCheckBoxAction() {
		updateTempSetting(Settings.DEV_MODE, idDevModeCheckBox.isSelected());
	}

	public void handleSkipSettingsCheckBoxAction() {
		updateTempSetting(Settings.SKIP_SETTINGS, idSkipSettingsCheckBox.isSelected());
	}

	public void updateTempSetting(Settings setting, Object property) {
		tempSettings.put(setting, String.valueOf(property));
	}


	/** User Interactive Button in Settings Window
	 * Closes Settings Window
	 * Opens Menu Window
	 */
	public void handleOkButtonAction() {
		boolean settingsSaved = updateSettings();
		if (!settingsSaved) {
			return;
		}
		hideCurrentWindow();
		openMenuWindow();
	}

	public boolean updateSettings() {
		try {
			if (HOIIVUtils.firstTimeSetup) {
				HOIIVUtils.settings = new SettingsManager(tempSettings);
			} else {
				SettingsManager.saveSettings(tempSettings);
			}
		} catch (IOException exception) {
			HOIUtilsWindow.openError("Settings failed to save.");
			return false;
		}
		createHOIIVFilePaths();
		return true;
	}

	public void createHOIIVFilePaths() {
		String modPath = SettingsManager.get(MOD_PATH);
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(modPath);
		}
		HOIIVUtils.common_folder = new File(modPath + "\\common");
		HOIIVUtils.states_folder = new File(modPath + "\\history\\states");
		HOIIVUtils.strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		HOIIVUtils.localization_eng_folder =  new File(modPath + "\\localisation\\english");
		HOIIVUtils.focus_folder = new File(modPath + "\\common\\national_focus");
	}
	public void hideCurrentWindow() {
		HOIUtilsWindow.hideWindow(idPane);
	}
	public void openMenuWindow() {
		MenuWindow menuWindow = new MenuWindow();
		menuWindow.open();
	}
}