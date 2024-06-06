package com.HOIIVUtils.ui.settings;

import com.HOIIVUtils.hoi4utils.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.HOIIVUtils.ui.FXWindow;
import com.HOIIVUtils.ui.menu.MenuController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.HOIIVUtils.hoi4utils.Settings.HOI4_PATH;
import static com.HOIIVUtils.hoi4utils.Settings.MOD_PATH;

/**
 * The SettingsController class is responsible for handling the program settings window and its
 * associated functionality. It provides methods to interact with the settings UI and update the
 * settings accordingly.
 *
 * @author thiccchris
 */
public class SettingsController extends Application implements FXWindow {
	protected String fxmlResource = "Settings.fxml";
	protected String title = "Settings";
	protected Stage stage;

	@FXML
	public Pane idPane;
	@FXML
	public Label idVersionLabel;
	@FXML
	public TextField modPathTextField;
	@FXML
	public TextField HOI4PathTextField;
	@FXML
	public Label idHOIIVModFolderLabel;
	@FXML
	public Button modFolderBrowseButton;
	@FXML
	public Button hoi4FolderBrowseButton;
	@FXML
	public CheckBox devModeCheckBox;
	@FXML
	public CheckBox idOpenConsoleOnLaunchCheckBox;
	@FXML
	public CheckBox idSkipSettingsCheckBox;
	@FXML
	public Button idOkButton;
	@FXML
	public Button idDelSettingsButton;
	@FXML
	public CheckBox drawFocusTreesCheckBox;
	@FXML
	public ComboBox<Screen> preferredMonitorComboBox;
	@FXML
	public CheckBox idDemoModeCheckBox;

	HashMap<Settings, String> tempSettings;

	public SettingsController() {
		tempSettings = new HashMap<>();// we should convert this to an EnumMap with default values
	}

	@FXML
	void initialize() {
		idVersionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);

		setDefault();

		// If there is saved settings, load them into the settings window
		if (Boolean.FALSE.equals(HOIIVUtils.firstTimeSetup)) {
			// who wrote it in this order lmao
			if (!"null".equals(MOD_PATH.getSetting())) {
				modPathTextField.setText((String) MOD_PATH.getSetting());
			}
			if (!"null".equals(HOI4_PATH.getSetting())) {
				HOI4PathTextField.setText((String) HOI4_PATH.getSetting());
			}
			devModeCheckBox.setSelected(Settings.DEV_MODE.enabled());
			drawFocusTreesCheckBox.setSelected(Settings.DRAW_FOCUS_TREE.enabled());
			idDemoModeCheckBox.setSelected(Settings.DEMO_MODE.enabled());
			if (idDemoModeCheckBox.isSelected()) {
				setDisablePathSelection(true);
			}
			idOpenConsoleOnLaunchCheckBox.setSelected(Settings.OPEN_CONSOLE_ON_LAUNCH.enabled());
			idSkipSettingsCheckBox.setSelected(Settings.SKIP_SETTINGS.enabled());
			idDelSettingsButton.setDisable(false);
			enableOkButton();
		}

		preferredMonitorComboBox.setItems(Screen.getScreens());

		preferredMonitorComboBox.setCellFactory(cell -> new ListCell<>() {
			/**
			 * Updates the item in the list view to the given value. The text of the item is set to "Screen
			 * <number>: <width>x<height>" if the item is not empty, and null if the item is empty.
			 * 
			 * @param item the item to be updated
			 * @param empty whether the item is empty
			 */
			@Override
			protected void updateItem(Screen item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
				} else {
					// The entry number starts from 1 (not 0) so add 1 to the index
					int i = getIndex() + 1;
					// Set the text of the item to "Screen <number>: <width>x<height>"
					setText("Screen " + i + ": " + item.getBounds().getWidth() + "x" + item.getBounds().getHeight());
				}
			}
		});

	}

	private void setDisablePathSelection(boolean value) {
		modFolderBrowseButton.setDisable(value);
		modPathTextField.setDisable(value);
		hoi4FolderBrowseButton.setDisable(value);
		HOI4PathTextField.setDisable(value);
	}

	public void launchSettingsWindow(String[] args) {
		System.out.println("Settings Controller ran launchSettingsWindow");
		launch(args);
	}

	/**
	 * Starts the settings window. This method is called when the program is first launched, and it is
	 * responsible for creating the stage and setting the scene.
	 * 
	 * @param stage the stage to be used for the settings window
	 */
	@Override
	public void start(Stage stage) {
		System.out.println("Settings Controller ran start method, attempting to load fxml file: " + fxmlResource);
		// Create a FXMLLoader to load the fxml file
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));

		try {
			// Load the fxml file
			System.out.println("Attempting to load fxml file...");
			Parent root = loader.load();
			System.out.println("Successfully loaded fxml file.");

			// Create a new scene and set the root node of the scene to
			// the loaded fxml node
			System.out.println("Attempting to create a new scene and set its root node to the loaded fxml node...");
			Scene scene = new Scene(root);

			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);

			// Save the stage and set its scene to the created scene
			this.stage = stage;
			stage.setScene(scene);

			stage.setTitle(title);

			stage.show();

			// Bind the width and height of the stage to its maximum value
			// to make the stage full screen
			stage.maxWidthProperty().bind(stage.widthProperty());
			stage.maxHeightProperty().bind(stage.heightProperty());

			System.out.println("The SettingsController instantiated its own Stage and displayed it.");
		} catch (Exception e) {
			System.out.println("Failed to load fxml file!!!!!");
			e.printStackTrace();
		}
	}

	/**
	 * Shows the settings window. If the window is already open, it is simply shown again. If the window
	 * is not open, it is created and shown.
	 */
	public void open() {
		if (stage != null) {
			// If the window is already open, show it again
			System.out.println("Settings Controller showing settings stage with open cuz settings stage was NOT null...");
			stage.show();
			System.out.println("Settings Controller showed settings stage");

			// Bind the width and height of the stage to its maximum value
			// to make the stage full screen
			stage.maxWidthProperty().bind(stage.widthProperty());
			stage.minWidthProperty().bind(stage.widthProperty());

		} else {
			// If the window is not open, create a new one and show it
			System.out.println("Settings Controller creating settings stage with open cuz settings stage was null...");
			start(new Stage());
			System.out.println("Settings Controller created settings stage");
		}
	}

	public void enableOkButton() {
		idOkButton.setDisable(false);
	}

	public void disableOkButton() {
		idOkButton.setDisable(true);
	}

	// todo fix the spelling an grammar :(
	/**
	 * User Interactive Text Feild in Settings Window Allows the user to type in the text field. It
	 * detects whever the user entered a valid directory. Saves the directory path to hoi4utils
	 * settings: MOD_PATH
	 */
	public void handleModPathTextField() {
		String pathText = modPathTextField.getText();
		if (pathText.isEmpty()) return;

		File modFile = new File(pathText);
		setFolderSettingIfValid(modFile, MOD_PATH);
	}

	public void handleHOI4PathTextField() {
		String pathText = HOI4PathTextField.getText();
		if (pathText.isEmpty()) return;

		File modFile = new File(pathText);
		setFolderSettingIfValid(modFile, HOI4_PATH);
	}


	/**
	 * Handles the action of the delete settings button being clicked. This deletes all the settings and
	 * resets the settings to their default values. It also sets the firstTimeSetup flag to true, so
	 * that the SettingsManager will create a new SettingsManager with the default values when the
	 * program is next launched.
	 */
	public void handleDelSettingsButtonAction() {
		try {
			// Delete all the settings
			SettingsManager.deleteAllSettings();
		} catch (IOException e) {
			// If there was an IOException while deleting the settings, print the stack
			// trace
			e.printStackTrace();
		}
		// Reset the settings to their default values
		setDefault();
		// Set the firstTimeSetup flag to true
		HOIIVUtils.firstTimeSetup = true;
	}

	/**
	 * Resets the settings to their default values. This involves clearing the mod path text field and
	 * setting all the checkboxes to their default values. It also disables the OK button and the delete
	 * settings button.
	 */
	private void setDefault() {
		// Clear the mod path text field
		modPathTextField.clear();
		HOI4PathTextField.clear();
		// Set the checkboxes to their default values
		devModeCheckBox.setSelected(false);
		drawFocusTreesCheckBox.setSelected(true);
		idDemoModeCheckBox.setSelected(false);
		idOpenConsoleOnLaunchCheckBox.setSelected(false);
		idSkipSettingsCheckBox.setSelected(false);
		// Disable the OK button and the delete settings button
		idDelSettingsButton.setDisable(true);
		disableOkButton();
	}

	/**
	 * User Interactive Button in Settings Window Opens up operating system Directory Chooser Will do
	 * nothing if the user exits or cancels window Updates Text Field when directory is selected Saves
	 * the directory path to MOD_PATH
	 */
	public void handleModFileBrowseAction() {
		File modFile = new File(FileUtils.usersDocuments + File.separator + HOIIVFile.usersParadoxHOIIVModFolder);

		modFile = openChooser(modFolderBrowseButton, modFile, true);
		if (modFile == null) return;
		modPathTextField.setText(modFile.getAbsolutePath());
		setFolderSettingIfValid(modFile, MOD_PATH);
	}

	public void handleHOI4FileBrowseAction() {
		File hoi4File = FileUtils.ProgramFilesX86 == null
				? null
				: new File(FileUtils.ProgramFilesX86 + File.separator + FileUtils.steamHOI4LocalPath);

		hoi4File = openChooser(hoi4FolderBrowseButton, hoi4File, true);
		if (hoi4File == null) return;
		HOI4PathTextField.setText(hoi4File.getAbsolutePath());
		setFolderSettingIfValid(hoi4File, HOI4_PATH);
	}

	public void handleDevModeCheckBoxAction() {
		updateTempSetting(Settings.DEV_MODE, devModeCheckBox.isSelected());
		boolean disabled = Settings.DEV_MODE.disabled();
		drawFocusTreesCheckBox.setDisable(disabled);
		// TODO this is causing a exception when it is first time setup, but it still saves the dev mode
	}

	public void handleDrawFocusTreesCheckBoxAction() {
		updateTempSetting(Settings.DRAW_FOCUS_TREE, drawFocusTreesCheckBox.isSelected());
	}

	/**
	 * Handles the action of the Demo Mode checkbox being clicked. Updates the temporary setting for
	 * DEMO_MODE to the value of the checkbox. Enables or disables the OK button based on the value of
	 * the checkbox.
	 */
	public void handleDemoModeCheckBoxAction() {
		updateTempSetting(Settings.DEMO_MODE, idDemoModeCheckBox.isSelected());

		if (idDemoModeCheckBox.isSelected()) {
			setDisablePathSelection(true);
			enableOkButton();
		} else {
			setDisablePathSelection(false);
			disableOkButton();
		}
	}

	public void handleOpenConsoleOnLaunchCheckBoxAction() {
		updateTempSetting(Settings.OPEN_CONSOLE_ON_LAUNCH, idOpenConsoleOnLaunchCheckBox.isSelected());
	}

	public void handleSkipSettingsCheckBoxAction() {
		updateTempSetting(Settings.SKIP_SETTINGS, idSkipSettingsCheckBox.isSelected());
	}

	public void handlePreferredMonitorSelection() {
		// change preferred monitor setting. // todo future: change settings window
		// location upon decision/etc?
		// monitors are labeled with ints, default being 0
		// interpret index of selection as monitor selection
		updateTempSetting(Settings.PREFERRED_SCREEN, preferredMonitorComboBox.getSelectionModel().getSelectedIndex());
	}

	public void updateTempSetting(Settings setting, Object property) {
		tempSettings.put(setting, String.valueOf(property));
	}

	/**
	 * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
	 */
	public void handleOkButtonAction() {
		boolean settingsSaved = updateSettings();
		if (!settingsSaved) {
			return;
		}
		hideWindow(idOkButton);
		openMenuWindow();
	}

	private void openMenuWindow() {
		MenuController menuWindow = new MenuController();
		menuWindow.open();
	}

	/**
	 * Updates the settings and saves them to the settings file. If firstTimeSetup is true, it will
	 * create a new SettingsManager with the tempSettings. If firstTimeSetup is false, it will save the
	 * tempSettings to the settings file. If firstTimeSetup is true and the modPathFile is null, it will
	 * create a new HOIIVFilePaths object.
	 * 
	 * @return true if the settings were updated and saved successfully, false if not.
	 */
	public boolean updateSettings() {
		try {
			if (Boolean.TRUE.equals(HOIIVUtils.firstTimeSetup)) {
				// If firstTimeSetup is true, create a new SettingsManager with the tempSettings
				// This is also cancer
				SettingsManager.settings = new SettingsManager(tempSettings);
				HOIIVUtils.firstTimeSetup = false;
				// Load the saved settings into the SettingsManager
				SettingsManager.getSavedSettings();
				// If the modPathFile is null, create a new HOIIVFilePaths object
				if (HOIIVFile.modPathFile == null) {
					HOIIVFile.createHOIIVFilePaths();
				}
			} else {
				// If firstTimeSetup is false, save the tempSettings to the settings file
				SettingsManager.saveSettings(tempSettings);
			}
		} catch (IOException exception) {
			// If there was an IOException while saving the settings, open an error window
			openError("Settings failed to save.");
			return false;
		}
		// Return true if the settings were updated and saved successfully, false if not
		return true;
	}

	private void setFolderSettingIfValid(File file, Settings modPath) {
		if (file.exists() && file.isDirectory()) {
			enableOkButton();
			tempSettings.put(modPath, file.getAbsolutePath());
		} else {
			disableOkButton();
		}
	}

	/* from HOIIVUtilsStageLoader but can only extend one class */
	/**
	 * Opens window and updates fxmlResource and title
	 * 
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
