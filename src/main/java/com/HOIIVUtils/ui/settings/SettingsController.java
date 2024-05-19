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
import com.aparapi.internal.tool.InstructionViewer.Form.Check;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.HOIIVUtils.hoi4utils.Settings.MOD_PATH;

/**
 * SettingsWindow is the window and controller for the program settings
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
	public TextField idModPathTextField;
	@FXML
	public Label idHOIIVModFolderLabel;
	@FXML
	public Button idBrowseButton;
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

		if (Boolean.FALSE.equals(HOIIVUtils.firstTimeSetup)) {
			if (!"null".equals(MOD_PATH.getSetting())) {
				idModPathTextField.setText((String) MOD_PATH.getSetting());
			}
			devModeCheckBox.setSelected(Settings.DEV_MODE.enabled());
			drawFocusTreesCheckBox.setSelected(Settings.DRAW_FOCUS_TREE.enabled());
			idSkipSettingsCheckBox.setSelected(Settings.SKIP_SETTINGS.enabled());
			idDelSettingsButton.setDisable(false);
			enableOkButton();
		}

		preferredMonitorComboBox.setItems(Screen.getScreens());

		preferredMonitorComboBox.setCellFactory(cell -> new ListCell<>() {
			/**
			 * Updates the item in the list view to the given value.
			 * The text of the item is set to "Screen <number>: <width>x<height>" if the
			 * item is not empty,
			 * and null if the item is empty.
			 * 
			 * @param item  the item to be updated
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

	public void launchSettingsWindow(String[] args) {
		System.out.println("Settings Controller ran launchSettingsWindow");
		launch(args);
	}

	/**
	 * Starts the settings window. This method is called when the
	 * program is first launched, and it is responsible for creating
	 * the stage and setting the scene.
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

			System.out.println("Settings Controller created it's own stage and showed it");
		} catch (Exception e) {
			System.out.println("Failed to load fxml file!!!!!");
			e.printStackTrace();
		}
	}

	/**
	 * Shows the settings window. If the window is already open, it is
	 * simply shown again. If the window is not open, it is created and
	 * shown.
	 */
	public void open() {
		if (stage != null) {
			// If the window is already open, show it again
			System.out
					.println("Settings Controller showing settings stage with open cuz settings stage was NOT null...");
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

	/**
	 * User Interactive Text Feild in Settings Window
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
	 * returns true if the mod path in mod path text field is a directory path that
	 * exists.
	 * 
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
		devModeCheckBox.setSelected(false);
		drawFocusTreesCheckBox.setSelected(true);
		idOpenConsoleOnLaunchCheckBox.setSelected(false);
		idSkipSettingsCheckBox.setSelected(false);
		idDelSettingsButton.setDisable(true);
		disableOkButton();
	}

	/**
	 * User Interactive Button in Settings Window
	 * Opens up operating system Directory Chooser
	 * Will do nothing if the user exits or cancels window
	 * Updates Text Field when directory is selected
	 * Saves the directory path to MOD_PATH
	 */
	public void handleBrowseAction() {
		File initialModPath = new File(
				FileUtils.usersDocuments + File.separator + HOIIVFile.usersParadoxHOIIVModFolder);

		File selectedDirectory = openChooser(idBrowseButton, true, initialModPath); // ! im making this pass any class
																					// (that is a "Node" at least, bc
																					// that makes sense, something that
																					// can go on a fxwindow I think),
																					// much welcome :D
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
		updateTempSetting(Settings.DEV_MODE, devModeCheckBox.isSelected());
		boolean disabled = Settings.DEV_MODE.disabled();
		drawFocusTreesCheckBox.setDisable(disabled);
	}

	public void handleDrawFocusTreesCheckBoxAction() {
		updateTempSetting(Settings.DRAW_FOCUS_TREE, drawFocusTreesCheckBox.isSelected());
	}

	public void handleDemoModeCheckBoxAction() {
		updateTempSetting(Settings.DEMO_MODE, idDemoModeCheckBox.isSelected());

		enableOkButton();
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
	 * User Interactive Button in Settings Window
	 * Closes Settings Window
	 * Opens Menu Window
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
	 * Updates the settings and saves them to the settings file.
	 * If firstTimeSetup is true, it will create a new SettingsManager with the
	 * tempSettings.
	 * If firstTimeSetup is false, it will save the tempSettings to the settings
	 * file.
	 * If firstTimeSetup is true and the modPathFile is null, it will create a new
	 * HOIIVFilePaths object.
	 * 
	 * @return true if the settings were updated and saved successfully, false if
	 *         not.
	 */
	public boolean updateSettings() {
		try {
			if (Boolean.TRUE.equals(HOIIVUtils.firstTimeSetup)) {
				// If firstTimeSetup is true, create a new SettingsManager with the tempSettings
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

	/* from HOIUtilsWindow but can only extend one class */
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