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

import static com.HOIIVUtils.hoi4utils.Settings.MOD_PATH;

/**
 * SettingsWindow is the window and controller for the program settings
 */
public class SettingsController extends Application implements FXWindow {
	private String fxmlResource = "Settings.fxml";
	private String title = "Settings";
	private Stage stage;
	
	@FXML public Pane idPane;
	@FXML public Label idVersionLabel;
	@FXML public TextField idModPathTextField;
	@FXML public Label idHOIIVModFolderLabel;
	@FXML public Button idBrowseButton;
	@FXML public CheckBox idDevModeCheckBox;
	@FXML public CheckBox idOpenConsoleOnLaunchCheckBox;
	@FXML public CheckBox idSkipSettingsCheckBox;
	@FXML public Button idOkButton;
	@FXML public Button idDelSettingsButton;
	@FXML public ComboBox<Screen> preferredMonitorComboBox;

	HashMap<Settings, String> tempSettings;

	public SettingsController() {
		tempSettings = new HashMap<>();// we should convert this to an EnumMap with default values
	}

	@FXML
	void initialize() {
		includeVersion();
		setDefault();
		includeSettingValues();

		preferredMonitorComboBox.setItems(Screen.getScreens());
		preferredMonitorComboBox.setCellFactory(cell -> new ListCell<>() {
			@Override
			protected void updateItem(Screen item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
				} else {
					int i = getIndex() + 1; // Entry number starts from 1
					setText("Screen " + i + ": " + item.getBounds().getWidth() + "x" + item.getBounds().getHeight());
				}
			}
		});

	}
	
	public void launchSettingsWindow(String[] args) {
		System.out.println("Settings Controller ran launchSettingsWindow");
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		try{
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
			System.out.println("loader things: " + loader + " " + loader.getLocation() + " " + loader.getResources() + " " + loader.getController() + " " + loader.getCharset() + " " + loader.getControllerFactory() + " " + loader.getNamespace() + " " + loader.getBuilderFactory() + " " + loader.getLoadListener());
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);

			this.stage = stage;
			stage.setScene(scene);
			
			stage.setTitle(title);
			stage.show();
//			if (fxmlResource.equals("fxml/Settings.fxml")) {
				stage.maxWidthProperty().bind(stage.widthProperty());
				stage.minWidthProperty().bind(stage.widthProperty());
//			}
			System.out.println("Settings Controller created it's own stage and showed it");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		if (stage != null) {
			stage.show();
//			if (fxmlResource.equals("fxml/Settings.fxml")) {
				stage.maxWidthProperty().bind(stage.widthProperty());
				stage.minWidthProperty().bind(stage.widthProperty());
//			}
			System.out.println("Settings Controller showed setting stage with open cuz settings stage was NOT null");
			
		} else {
			start(new Stage());
			System.out.println("Settings Controller create settings stage with open cuz settings stage was null");
		}
	}

	public void includeVersion() {
		idVersionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}

	public void includeSettingValues() {
		if (Boolean.FALSE.equals(HOIIVUtils.firstTimeSetup)) {
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
		idOpenConsoleOnLaunchCheckBox.setSelected(false);
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

	public void handleOpenConsoleOnLaunchCheckBoxAction() {
		updateTempSetting(Settings.OPEN_CONSOLE_ON_LAUNCH, idOpenConsoleOnLaunchCheckBox.isSelected());
	}

	public void handleSkipSettingsCheckBoxAction() {
		updateTempSetting(Settings.SKIP_SETTINGS, idSkipSettingsCheckBox.isSelected());
	}

	public void handlePreferredMonitorSelection() {
		// change preferred monitor setting. // todo future: change settings window location upon decision/etc?
		// monitors are labeled with ints, default being 0
		// interpret index of selection as monitor selection
		updateTempSetting(Settings.PREFERRED_SCREEN, preferredMonitorComboBox.getSelectionModel().getSelectedIndex());
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
		openMenuWindow();
	}
	
	private void openMenuWindow() {
		MenuController menuWindow = new MenuController();
		menuWindow.open();
	}
	
	public boolean updateSettings() {
		try {
			if (Boolean.TRUE.equals(HOIIVUtils.firstTimeSetup)) {
				SettingsManager.settings = new SettingsManager(tempSettings);
				HOIIVUtils.firstTimeSetup = false;
				SettingsManager.getSavedSettings();
				if (HOIIVFile.modPathFile == null) {
					HOIIVFile.createHOIIVFilePaths();
				}
			} else {
				SettingsManager.saveSettings(tempSettings);
			}
		} catch (IOException exception) {
			openError("Settings failed to save.");
			return false;
		}
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
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
}