package com.hoi4utils.ui.settings;

import com.hoi4utils.FileUtils;
import com.hoi4utils.Settings;
import com.hoi4utils.SettingsManager;
import com.hoi4utils.clausewitz.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.hoi4utils.ui.FXWindow;
import com.hoi4utils.ui.menu.MenuController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.hoi4utils.Settings.HOI4_PATH;
import static com.hoi4utils.Settings.MOD_PATH;

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
	public TextField hoi4PathTextField;
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
		tempSettings = new HashMap<>();
	}

	@FXML
	void initialize() {
		System.out.println("Initializing SettingsController...");
		idVersionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);

		setDefault();

		if (Boolean.TRUE.equals(new File(SettingsManager.NEW_PROPERTIES_PATH).exists())) {
			loadUIWithSavedSettings();
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
					// Set the text of the item to "Screen <number>: <width>x<height>"
					setText("Screen " + (getIndex() + 1) + ": " + item.getBounds().getWidth() + "x" + item.getBounds().getHeight());
				}
			}
		});

	}

	/**
	 * Resets the settings to their default values. This involves clearing the mod path text field and
	 * setting all the checkboxes to their default values. It also disables the OK button and the delete
	 * settings button.
	 */
	private void setDefault() {
		// Clear the mod path text field
		modPathTextField.clear();
		hoi4PathTextField.clear();
		modPathTextField.setDisable(false);
		hoi4PathTextField.setDisable(false);
		// Set the checkboxes to their default values
		devModeCheckBox.setSelected(false);
		drawFocusTreesCheckBox.setDisable(true);
		drawFocusTreesCheckBox.setSelected(false);
		idDemoModeCheckBox.setSelected(false);
		idOpenConsoleOnLaunchCheckBox.setSelected(false);
		idSkipSettingsCheckBox.setSelected(false);
		// Disable the OK button and the delete settings button
		idDelSettingsButton.setDisable(true);
		disableOkButton();
		System.out.println("SettingsController: UI Settings have been reset.");
	}

	private void loadUIWithSavedSettings() {
		if (!"null".equals(MOD_PATH.getSetting())) {
			modPathTextField.setText((String) MOD_PATH.getSetting());
		}
		if (!"null".equals(HOI4_PATH.getSetting())) {
			hoi4PathTextField.setText((String) HOI4_PATH.getSetting());
		}
		if (!"null".equals(MOD_PATH.getSetting()) && !"null".equals(HOI4_PATH.getSetting())) {
			enableOkButton();
		}
		devModeCheckBox.setSelected(Settings.DEV_MODE.enabled());
		drawFocusTreesCheckBox.setSelected(Settings.DRAW_FOCUS_TREE.enabled());
		drawFocusTreesCheckBox.setDisable(!Settings.DEV_MODE.enabled());
		idDemoModeCheckBox.setSelected(Settings.DEMO_MODE.enabled());
		if (idDemoModeCheckBox.isSelected()) {
			setDisablePathSelection(true);
		}
		idOpenConsoleOnLaunchCheckBox.setSelected(Settings.OPEN_CONSOLE_ON_LAUNCH.enabled());
		idSkipSettingsCheckBox.setSelected(Settings.SKIP_SETTINGS.enabled());
		idDelSettingsButton.setDisable(false);
		if (Settings.DEMO_MODE.enabled()) {
			enableOkButton();
		}
		System.out.println("SettingsController: UI Settings have been updated with saved settings.");
	}

	public void launchSettingsWindow(String[] args) {
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));

		try {
			Parent rootFXML = loader.load();
			Scene scene = new Scene(rootFXML);
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
			this.stage = stage;
			stage.setScene(scene);
			stage.setTitle(title);
			stage.show();
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
			stage.show();
			stage.maxWidthProperty().bind(stage.widthProperty());
			stage.minWidthProperty().bind(stage.widthProperty());

		} else {
			start(new Stage());
		}
	}

	@Override
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
	}

	public void handleModPathTextField() {
		String pathText = modPathTextField.getText();
		if (pathText.isEmpty())
			return;

		File modFile = new File(pathText);
		setFolderSettingIfValid(modFile, MOD_PATH);
	}

	public void handleHOIIVPathTextField() {
		String pathText = hoi4PathTextField.getText();
		if (pathText.isEmpty())
			return;

		File modFile = new File(pathText);
		setFolderSettingIfValid(modFile, HOI4_PATH);
	}

	public void handleModFileBrowseAction() {
		File modFile = new File(FileUtils.usersDocuments + File.separator + HOIIVFile.usersParadoxHOIIVModFolder);

		modFile = FXWindow.openChooser(modFolderBrowseButton, modFile, true);
		if (modFile == null)
			return;
		modPathTextField.setText(modFile.getAbsolutePath());
		setFolderSettingIfValid(modFile, MOD_PATH);
	}

	public void handleHOIIVFileBrowseAction() {
		File hoi4File =
				FileUtils.ProgramFilesX86 == null ? null : new File(FileUtils.ProgramFilesX86 + File.separator + FileUtils.steamHOI4LocalPath);

		hoi4File = FXWindow.openChooser(hoi4FolderBrowseButton, hoi4File, true);
		if (hoi4File == null)
			return;
		hoi4PathTextField.setText(hoi4File.getAbsolutePath());
		setFolderSettingIfValid(hoi4File, HOI4_PATH);
	}

	private void setFolderSettingIfValid(File file, Settings modPath) {
		if (file.exists() && file.isDirectory()) {
			enableOkButton();
			tempSettings.put(modPath, file.getAbsolutePath());
		} else {
			disableOkButton();
		}
	}

	public void handleDevModeCheckBoxAction() {
		updateTempSetting(Settings.DEV_MODE, devModeCheckBox.isSelected());
		drawFocusTreesCheckBox.setDisable(!devModeCheckBox.isSelected());
		drawFocusTreesCheckBox.setSelected(false);
		handleDrawFocusTreesCheckBoxAction();
	}

	public void handleDrawFocusTreesCheckBoxAction() {
		updateTempSetting(Settings.DRAW_FOCUS_TREE, drawFocusTreesCheckBox.isSelected());
	}

	public void handleDemoModeCheckBoxAction() {
		updateTempSetting(Settings.DEMO_MODE, idDemoModeCheckBox.isSelected());
		// remove the mod path and hoi4 path from the temp settings if demo mode is enabled
		modPathTextField.clear();
		hoi4PathTextField.clear();
		tempSettings.put(MOD_PATH, null);
		tempSettings.put(HOI4_PATH, null);

		if (idDemoModeCheckBox.isSelected()) {
			setDisablePathSelection(true);
			enableOkButton();
		} else {
			setDisablePathSelection(false);
			disableOkButton();
		}
		if (Boolean.TRUE.equals(new File(SettingsManager.NEW_PROPERTIES_PATH).exists())) {
			updateSettings();
		}
	}

	private void setDisablePathSelection(boolean value) {
		modFolderBrowseButton.setDisable(value);
		modPathTextField.setDisable(value);
		hoi4FolderBrowseButton.setDisable(value);
		hoi4PathTextField.setDisable(value);
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
		System.out.println("Updated setting " + setting.name() + ": " + tempSettings.get(setting));
	}

	/**
	 * Handles the action of the delete settings button being clicked. This deletes all the settings and
	 * resets the settings to their default values. It also sets the firstTimeSetup flag to true, so
	 * that the SettingsManager will create a new SettingsManager with the default values when the
	 * program is next launched.
	 */
	public void handleDelSettingsButtonAction() {
		try {
			SettingsManager.deleteAllSettings();
			setDefault();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void enableOkButton() {
		idOkButton.setDisable(false);
	}

	public void disableOkButton() {
		idOkButton.setDisable(true);
	}

	/**
	 * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
	 */
	public void handleOkButtonAction() {
		updateSettings();
		hideWindow(idOkButton);
		new MenuController().open();
	}

	/**
	 * Updates the settings and saves them to the settings file. If firstTimeSetup is true, it will
	 * create a new SettingsManager with the tempSettings. If firstTimeSetup is false, it will save the
	 * tempSettings to the settings file. If firstTimeSetup is true and the modPathFile is null, it will
	 * create a new HOIIVFilePaths object.
	 * 
	 * @return true if the settings were updated and saved successfully, false if not.
	 */
	public void updateSettings() {
		if (Boolean.TRUE.equals(!new File(SettingsManager.NEW_PROPERTIES_PATH).exists())) {
			SettingsManager.initializeAndSaveSettings(tempSettings);
			// if (HOIIVFile.mod_folder == null) {
				HOIIVFile.createHOIIVFilePaths();
			// }
		} else {
			SettingsManager.initializeAndSaveSettings(tempSettings);
		}
		System.out.println("Settings Updated.");
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
