package ui.main_menu;

import hoi4utils.*;
import ui.FXWindow;
import ui.HOIUtilsWindow;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static hoi4utils.Settings.MOD_PATH;

/**
 * SettingsWindow is the window and controller for the program settings
 */
public class SettingsWindow extends Application implements FXWindow {
	String fxmlResource = "SettingsWindow.fxml";
	String title = "HOIIVUtils Settings";
	String styleSheetURL = "resources/javafx_dark.css";
	Stage stage;

	@FXML public Pane idPane;
	@FXML public Label idVersionLabel;
	@FXML public TextField idModPathTextField;
	@FXML public Label idHOIIVModFolderLabel;
	@FXML public Button idBrowseButton;
	@FXML public CheckBox idDevModeCheckBox;
	@FXML public CheckBox idSkipSettingsCheckBox;
	@FXML public Button idOkButton;
	@FXML public Button idDelSettingsButton;

	HashMap<Settings, String> tempSettings;

	public SettingsWindow() {
		tempSettings = new HashMap<>();// we should convert this to an EnumMap with default values
	}

	@FXML
	void initialize() {
		includeVersion();
		setDefault();
		includeSettingValues();
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
			getClass().getResource(
				fxmlResource
			)
		);

		this.stage = stage;
		Scene scene = new Scene(loader.load());
		stage.setScene(scene);
		stage.setTitle(title);

		/* style */
		scene.getStylesheets().add(styleSheetURL);

		stage.show();
		stage.maxWidthProperty().bind(stage.widthProperty());
		stage.minWidthProperty().bind(stage.widthProperty());
	}

	public void open() {
		try {
			if (stage != null) {
				stage.show();
				stage.maxWidthProperty().bind(stage.widthProperty());
				stage.minWidthProperty().bind(stage.widthProperty());
			} else {
				start(new Stage());
			}
		} 
		catch (Exception exc) {
			openError(exc);
		}
	}

	public void launchSettingsWindow(String... var0) {
		Application.launch(var0);
	}

	public void includeVersion() {
		idVersionLabel.setText(HOIIVUtils.hoi4utilsVersion);
	}

	public void includeSettingValues() {
		if (!HOIIVUtils.firstTimeSetup) {
			if (!"null".equals(MOD_PATH.getSetting())) {
				idModPathTextField.setText((String) MOD_PATH.getSetting());
			}
			idDevModeCheckBox.setSelected(Settings.DEV_MODE.enabled());
			idSkipSettingsCheckBox.setSelected(Settings.SKIP_SETTINGS.enabled());
			idDelSettingsButton.setDisable(false);
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

	public void handleDelSettingsButtonAction() {
		try {
			SettingsManager.deleteAllSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setDefault();
		HOIIVUtils.firstTimeSetup = true;
	}

	private void setDefault() {
		idModPathTextField.clear();
		idDevModeCheckBox.setSelected(false);
		idSkipSettingsCheckBox.setSelected(false);
		idDelSettingsButton.setDisable(true);
		disableOkButton();
	}
	
	/** User Interactive Button in Settings Window
	 * Opens up operating system Directory Chooser
	 * Will do nothing if the user exits or cancels window
	 * Updates Text Field when directory is selected
	 * Saves the directory path to MOD_PATH
	 */
	public void handleBrowseAction() {
		File initialModPath = new File(FileUtils.usersDocuments + File.separator + HOIIVFile.usersParadoxHOIIVModFolder);

		File selectedDirectory = openChooser(idBrowseButton, true, initialModPath); // ! im making this pass any class (that is a "Node" at least, bc that makes sense, something that can go on a fxwindow I think), much welcome :D
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
		hideWindow(idOkButton);
		MenuWindow menuWindow = new MenuWindow();
		menuWindow.open();
	}

	public boolean updateSettings() {
		try {
			if (HOIIVUtils.firstTimeSetup) {
				HOIIVUtils.settings = new SettingsManager(tempSettings);
				HOIIVUtils.firstTimeSetup = false;
			} else {
				SettingsManager.saveSettings(tempSettings);
			}
		} catch (IOException exception) {
			openError("Settings failed to save.");
			return false;
		}
		HOIIVFile.createHOIIVFilePaths();
		return true;
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
	public String getStyleSheetURL() {
		return styleSheetURL;
	}

	@Override
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setStyleSheetURL(String styleSheetURL) {
		this.styleSheetURL = styleSheetURL;
	}
}