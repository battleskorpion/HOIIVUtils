package com.hoi4utils.ui.settings;

import com.hoi4utils.FileUtils;
import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.HOIIVUtils;
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
public class SettingsController extends Application implements JavaFXUIManager {
	public static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
	private String fxmlResource = "Settings.fxml";
	private String title = "HOIIVUtils Settings " + HOIIVUtils.get("version");
	private Stage stage;

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

	@FXML
	void initialize() {
		versionLabel.setText(HOIIVUtils.get("version"));
		loadUIWithSavedSettings();
		loadMonitor();
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

	private void loadUIWithSavedSettings() {
		modPathTextField.clear();
		if (!"null".equals(HOIIVUtils.get("mod.path"))) {
			modPathTextField.setText(HOIIVUtils.get("mod.path"));
		}
		hoi4PathTextField.clear();
		if (!"null".equals(HOIIVUtils.get("hoi4.path"))) {
			hoi4PathTextField.setText(HOIIVUtils.get("hoi4.path"));
		}
		darkTheme.setSelected(Objects.equals(HOIIVUtils.get("theme"), "dark"));
		lightTheme.setSelected(Objects.equals(HOIIVUtils.get("theme"), "light"));
		preferredMonitorComboBox.getSelectionModel().select(validateAndGetPreferredScreen());
		debugColorsTButton.setSelected(Boolean.parseBoolean(HOIIVUtils.get("debug.colors")));
		if (debugColorsTButton.isSelected()) {
			debugColorsTButton.setText("ON");
		} else {
			debugColorsTButton.setText("OFF");
		}
		// parser settings: 
		parserIgnoreCommentsCheckBox.setSelected(Boolean.parseBoolean(HOIIVUtils.get("parser.ignore_comments")));
	}

	/**
	 * Starts the settings window. This method is called when the program is first launched, and it is
	 * responsible for creating the stage and setting the scene.
	 *
	 * @param stage the stage to be used for the settings window
	 */
	@Override
	public void start(Stage stage) {
		try {
			Parent root = loadFXML();
			setupAndShowStage(root);
		} catch (IOException e) {
			handleFXMLLoadError(e);
		}
	}

	public void open() {
		if (stage != null) {
			showExistingStage();
			return;
		}

		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}
		
		start(new Stage());
	}

	private void showExistingStage() {
		stage.show();
		LOGGER.info("Stage already exists, showing: {}", title);
	}

	private void handleMissingFXMLResource() {
		String errorMessage = "Failed to open window\nError: FXML resource is null.";
		LOGGER.error(errorMessage);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private Parent loadFXML() throws IOException {
		FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			return launchLoader.load();
		} catch (IOException e) {
			throw new IOException("Failed to load FXML: " + fxmlResource, e);
		}
	}

	private void setupAndShowStage(Parent root) {
		Scene scene = new Scene(root);
		addSceneStylesheets(scene);
		this.stage = createLaunchStage(scene);
		LOGGER.debug("Stage created and shown: {}", title);
	}

	private void addSceneStylesheets(Scene scene) {
		scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");

		try {
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/hoi4utils/ui/highlight-background.css")).toExternalForm());
		} catch (NullPointerException e) {
			System.err.println("Warning: Stylesheet 'highlight-background.css' not found!");
		}
	}

	private Stage createLaunchStage(Scene scene) {
		Optional.ofNullable(stage).ifPresent(Stage::close);

		Stage launchStage = new Stage();
		launchStage.setScene(scene);
		launchStage.setTitle(title);
		decideScreen(launchStage);
		launchStage.show();

		return launchStage;
	}

	private void handleFXMLLoadError(IOException e) {
		String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource;
		LOGGER.fatal("Error loading FXML: {}", fxmlResource, e);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		throw new RuntimeException(errorMessage, e);
	}

	public void handleModPathTextField() {
		validateAndSetPath(modPathTextField.getText(), "mod.path");
	}

	public void handleHOIIVPathTextField() {
		validateAndSetPath(hoi4PathTextField.getText(), "hoi4.path");
	}

	public void handleModFileBrowseAction() {
		String usersDocuments = System.getProperty("user.home") + File.separator + "Documents";
		File initialModDir = new File(usersDocuments + File.separator + HOIIVFiles.usersParadoxHOIIVModFolder);
		handleFileBrowseAction(modPathTextField, modFolderBrowseButton, initialModDir, "mod.path");
	}

	public void handleHOIIVFileBrowseAction() {
        String steamHOI4LocalPath = "Steam" + File.separator + "steamapps" + File.separator + "common" + File.separator + "Hearts of Iron IV";
        File programFilesX86 = null;
        if (System.getenv("ProgramFiles(x86)") != null) {
            programFilesX86 = new File(System.getenv("ProgramFiles(x86)"));
        }
        File initialHoi4Dir = programFilesX86 == null ? null :
                new File(programFilesX86 + File.separator + steamHOI4LocalPath);
        handleFileBrowseAction(hoi4PathTextField, hoi4FolderBrowseButton, initialHoi4Dir, "hoi4.path");
    }
	
	// Generic method to validate a directory path and set it to settings
	private void validateAndSetPath(String path, String settingKey) {
		if (path.isEmpty()) return;
		
		File file = new File(path);
		
		boolean isValidFile = file.exists() && file.isDirectory();
		
		if (isValidFile) {
			HOIIVUtils.set(settingKey, String.valueOf(file));
		}
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
		if (darkTheme.isSelected()) {
			HOIIVUtils.set("theme", "dark");
		}
	}

	public void handleLightThemeRadioAction() {
		if (lightTheme.isSelected()) {
			HOIIVUtils.set("theme", "light");
		}
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

	@Override
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
}
