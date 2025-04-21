package com.hoi4utils.ui.settings;

import com.hoi4utils.FileUtils;
import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.menu.MenuController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * The SettingsController class is responsible for handling the program settings window and its
 * associated functionality. It provides methods to interact with the settings UI and update the
 * settings accordingly.
 *
 * @author thiccchris
 */
public class SettingsController extends HOIIVUtilsAbstractController implements JavaFXUIManager {
	public static final Logger LOGGER = LogManager.getLogger(SettingsController.class);

	@FXML
	Label versionLabel;
	@FXML
	TextField modPathTextField;
	@FXML
	TextField hoi4PathTextField;
	@FXML
	Button modFolderBrowseButton;
	@FXML
	Button hoi4FolderBrowseButton;
	@FXML
	Button idOkButton;
	@FXML
	RadioButton darkTheme;
	@FXML
	RadioButton lightTheme;
	@FXML
	ComboBox<Screen> preferredMonitorComboBox;
	@FXML
	ToggleButton debugColorsTButton;
	@FXML
	CheckBox parserIgnoreCommentsCheckBox;

	public SettingsController() {
		setFxmlResource("Settings.fxml");
		setTitle("HOIIVUtils Settings " + HOIIVUtils.HOIIVUTILS_VERSION);
	}
	
	@FXML
	void initialize() {
		versionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);
		loadUIWithSavedSettings();
		loadMonitor();
	}

	private void loadUIWithSavedSettings() {
		// Load paths
		updateTextField(modPathTextField, HOIIVUtils.get("mod.path"));
		updateTextField(hoi4PathTextField, HOIIVUtils.get("hoi4.path"));
		idOkButton.setDisable(HOIIVUtils.get("hoi4.path").isEmpty());
	
		// Load theme
		boolean isDarkTheme = "dark".equals(HOIIVUtils.get("theme"));
		darkTheme.setSelected(isDarkTheme);
		lightTheme.setSelected(!isDarkTheme);
	
		// Load monitor preference
		preferredMonitorComboBox.getSelectionModel().select(validateAndGetPreferredScreen());
	
		// Load debug colors
		boolean debugColors = Boolean.parseBoolean(HOIIVUtils.get("debug.colors"));
		debugColorsTButton.setSelected(debugColors);
		debugColorsTButton.setText(debugColors ? "ON" : "OFF");
	
		// Load parser settings
		parserIgnoreCommentsCheckBox.setSelected(Boolean.parseBoolean(HOIIVUtils.get("parser.ignore_comments")));
	}

	private void updateTextField(TextField textField, String value) {
		textField.clear();
		if (!"".equals(value)) {
			textField.setText(value);
		}
	}
	
	private void loadMonitor() {
		preferredMonitorComboBox.setItems(Screen.getScreens());
		preferredMonitorComboBox.setCellFactory(_ -> new ListCell<>() {
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

	public void handleModPathTextField() {
		HOIIVUtils.set("mod.path", modPathTextField.getText());
	}

	public void handleHOIIVPathTextField() {
		HOIIVUtils.set("hoi4.path", hoi4PathTextField.getText());
        idOkButton.setDisable(HOIIVUtils.get("hoi4.path").isEmpty());
	}

	public void handleModFileBrowseAction() {
		File initialModDir = new File(FileUtils.usersDocuments + File.separator + HOIIVFiles.usersParadoxHOIIVModFolder);
		handleFileBrowseAction(modPathTextField, modFolderBrowseButton, initialModDir, "mod.path");
	}

	public void handleHOIIVFileBrowseAction() {
		File initialHoi4Dir = FileUtils.ProgramFilesX86 == null ? null :
			new File(FileUtils.ProgramFilesX86 + File.separator + FileUtils.steamHOI4LocalPath);
		handleFileBrowseAction(hoi4PathTextField, hoi4FolderBrowseButton, initialHoi4Dir, "hoi4.path");
		idOkButton.setDisable(HOIIVUtils.get("hoi4.path").isEmpty());
	}
	
	// Generic method to handle file browser actions
	private void handleFileBrowseAction(TextField textField, Node browseButton,	File initialDirectory, String settingKey) {
		File selectedFile = JavaFXUIManager.openChooser(browseButton, initialDirectory, true);
		
		if (selectedFile == null) return;
		
		boolean isValidFile = selectedFile.exists() && selectedFile.isDirectory();
		
		if (isValidFile) {
			HOIIVUtils.set(settingKey, String.valueOf(selectedFile));
		}
		
		textField.setText(selectedFile.getAbsolutePath());
	}

	public void handleDarkThemeRadioAction() {
		HOIIVUtils.set("theme", "dark");
	}

	public void handleLightThemeRadioAction() {
		HOIIVUtils.set("theme", "light");
	}

	/**
	 * change preferred monitor setting.
	 * location upon decision/etc?
	 * monitors are labeled with ints, default being 0
	 * interpret index of selection as monitor selection
	 */
	public void handlePreferredMonitorSelection() {
		HOIIVUtils.set("preferred.screen", String.valueOf(preferredMonitorComboBox.getSelectionModel().getSelectedIndex()));
	}

	public void handleDebugColorsAction() {
		if (debugColorsTButton.isSelected()) {
			HOIIVUtils.set("debug.colors", "true");
			debugColorsTButton.setText("ON");
		} else {
			HOIIVUtils.set("debug.colors", "false");
			debugColorsTButton.setText("OFF");
		}
	}

	public void handleParserIgnoreCommentsAction() {
		if (parserIgnoreCommentsCheckBox.isSelected()) {
			HOIIVUtils.set("parser.ignore_comments", "true");
		} else {
			HOIIVUtils.set("parser.ignore_comments", "false");
		}
	}

	/**
	 * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
	 */
	public void handleOkButtonAction() {
		HOIIVUtils.loadMod();
		HOIIVUtils.save();
		hideWindow(idOkButton);
		new MenuController().open();
	}
}
