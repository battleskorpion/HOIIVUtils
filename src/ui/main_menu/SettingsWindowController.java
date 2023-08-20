package ui.main_menu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import hoi4utils.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import hoi4utils.HOIIVUtils;
import hoi4utils.SettingsManager;

import static hoi4utils.Settings.MOD_PATH;

public class SettingsWindowController {
	
	HashMap<Settings, String> settings;

	@FXML
	public GridPane settingsGridPain;
	public Label versionLabel;
	public CheckBox idDevModeCheckBox;
	public Label hoi4ModFolderLabel;
	public Button browseButton;
	public TextField idModPathTextField;
	public Button idOkButton;

	public File selectedDirectory;
	
	@FXML
	void initialize() {
		includeVersion();
		setupFirstTime();
	}

	private void includeVersion() {
		versionLabel.setText(HOIIVUtils.hoi4utilsVersion);
	}
	private void setupFirstTime() {
		boolean isFirstTime = HOIIVUtils.firstTimeSetup;
		if (!isFirstTime) {
			setModPathTextFeildFromSettings();
			setDevModeCheckBoxOnOrOff();
			enableOkButton();
		}
	}
	private void setModPathTextFeildFromSettings() {
		String inlcudeSetting = (String) MOD_PATH.getSetting();
		if (inlcudeSetting != "null") {
			idModPathTextField.setText(inlcudeSetting);
		}
	}
	private void setDevModeCheckBoxOnOrOff() {
		boolean getDevModeSetting = Settings.DEV_MODE.enabled();
		idDevModeCheckBox.setSelected(getDevModeSetting);
	}
	private void enableOkButton() {
		idOkButton.setDisable(false);
	}
	private void disableOkButton() {
		idOkButton.setDisable(true);
	}

	public SettingsWindowController() {
		settings = new HashMap<>();
	}
	public void handleDevModeCheckBoxAction() {
		if (idDevModeCheckBox.isSelected()) {
			settings.put(Settings.DEV_MODE, "true");
			Settings.DEV_MODE.setValue("true");
		} else {
			settings.put(Settings.DEV_MODE, "false");
		}
	}
	
	public void tempUpdateSetting(Settings setting, String property) {
		settings.put(setting, property);
	}
	
	private boolean saveSettings() {
		try {
			if (HOIIVUtils.firstTimeSetup) {
				HOIIVUtils.settings = new SettingsManager(settings);
			} else {
				SettingsManager.saveSettings(settings);
			}
		} catch (IOException exception) {
			HOIIVUtils.openError("Settings failed to save.");
			return false;
		}

		String modPath = SettingsManager.get(MOD_PATH);
		System.out.println(modPath);
		HOIIVUtils.states_folder = new File(modPath + "\\history\\states");
		HOIIVUtils.strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		HOIIVUtils.localization_eng_folder =  new File(modPath + "\\localisation\\english");
		HOIIVUtils.focus_folder = new File(modPath + "\\common\\national_focus");

		return true;
	}
	
	public void handleBrowseAction() {
			getDirectoryChooser();
			idModPathTextField.setText(selectedDirectory.getAbsolutePath());
			updateModPath(selectedDirectory);
	}

	private void getDirectoryChooser() {
		// Opens Windows Default Directory Chooser
		Stage primaryStage = (Stage) (browseButton.getScene().getWindow());
		DirectoryChooser directoryChooser = new DirectoryChooser();
		selectedDirectory = directoryChooser.showDialog(primaryStage);
		if (selectedDirectory == null) {
			return;
		}
	}

	public void handleModPathTextField() {
		getIsDirectory();

		String pathText = idModPathTextField.getText();
		if (pathText == null || pathText.isEmpty()) {
			pathText = null;
		}
		settings.put(MOD_PATH, pathText);
	}

	private void updateModPath(File selectedDirectory) {
		getIsDirectory();

		settings.put(MOD_PATH, selectedDirectory.getAbsolutePath());
	}

	private void getIsDirectory() {
		File fileModPath = new File(idModPathTextField.getText());

		boolean exists = fileModPath.exists();

		boolean isDirectory = fileModPath.isDirectory();
				
		if (idOkButton.isDisabled() && exists && isDirectory) {
			disableOkButton();
		} else {
			enableOkButton();
		}
	}

	public void handleOkButtonAction() {
		boolean settingsSaved = saveSettings();
		if (!settingsSaved) {
			return;
		}
		HOIIVUtils.hideWindow(idOkButton);
		MenuWindow menuWindow = new MenuWindow();
		menuWindow.open();
	}
}
