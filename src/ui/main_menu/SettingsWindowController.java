package ui.main_menu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import hoi4utils.HOIIVUtils;
import settings.HOIIVUtilsProperties;

import static settings.HOIIVUtilsProperties.Settings.MOD_PATH;

public class SettingsWindowController {
	
	HashMap<HOIIVUtilsProperties.Settings, String> settings;

	@FXML
	public GridPane settingsGridPain;
	public Label versionLabel;
	public CheckBox idDevModeCheckBox;
	public Label hoi4ModFolderLabel;
	public Button browseButton;
	public TextField hoi4ModPathTextField;
	public Button okButton;

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
		String inlcudeSetting = (String) HOIIVUtilsProperties.Settings.MOD_PATH.getSetting();
		if (inlcudeSetting != "null") {
			hoi4ModPathTextField.setText(inlcudeSetting);
		}
	}
	private void setDevModeCheckBoxOnOrOff() {
		boolean getDevModeSetting = HOIIVUtilsProperties.Settings.enabled(HOIIVUtilsProperties.Settings.DEV_MODE);
		idDevModeCheckBox.setSelected(getDevModeSetting);
	}
	private void enableOkButton() {
		okButton.setDisable(false);
	}
	private void disableOkButton() {
		okButton.setDisable(true);
	}

	public void handleDevModeCheckBoxAction() {
		if (idDevModeCheckBox.isSelected()) {
			settings.put(HOIIVUtilsProperties.Settings.DEV_MODE, "true");
		} else {
			settings.put(HOIIVUtilsProperties.Settings.DEV_MODE, "false");
		}
	}
	
	public SettingsWindowController() {
		settings = new HashMap<>();
	}
	public void tempUpdateSetting(HOIIVUtilsProperties.Settings setting, String property) {
		settings.put(setting, property);
	}
	
	private boolean saveSettings() {
		try {
			if (HOIIVUtils.firstTimeSetup) {
				HOIIVUtils.settings = new HOIIVUtilsProperties(settings);
			} else {
				HOIIVUtilsProperties.saveSettings(settings);
			}
		} catch (IOException exception) {
			HOIIVUtils.openError("Settings failed to save.");
			return false;
		}

		String modPath = HOIIVUtilsProperties.get(MOD_PATH);
		System.out.println(modPath);
		HOIIVUtils.states_folder = new File(modPath + "\\history\\states");
		HOIIVUtils.strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		HOIIVUtils.localization_eng_folder =  new File(modPath + "\\localisation\\english");
		HOIIVUtils.focus_folder = new File(modPath + "\\common\\national_focus");

		return true;
	}
	
	public void handleBrowseAction() {
			getDirectoryChooser();
			hoi4ModPathTextField.setText(selectedDirectory.getAbsolutePath());
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

		String pathText = hoi4ModPathTextField.getText();
		if (pathText == null || pathText.isEmpty()) {
			pathText = null;
		}
		settings.put(HOIIVUtilsProperties.Settings.MOD_PATH, pathText);
	}

	private void updateModPath(File selectedDirectory) {
		getIsDirectory();

		settings.put(HOIIVUtilsProperties.Settings.MOD_PATH, selectedDirectory.getAbsolutePath());
	}

	private void getIsDirectory() {
		File fileModPath = new File(hoi4ModPathTextField.getText());

		boolean exists = fileModPath.exists();

		boolean isDirectory = fileModPath.isDirectory();
				
		if (okButton.isDisabled() && exists && isDirectory) {
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
		HOIIVUtils.hideWindow(okButton);
		MenuWindow menuWindow = new MenuWindow();
		menuWindow.open();
	}
}
