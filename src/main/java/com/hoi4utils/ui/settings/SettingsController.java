package com.hoi4utils.ui.settings;

import com.hoi4utils.FileUtils;
import com.hoi4utils.clausewitz.HOIIVFile;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.FXWindow;
import com.hoi4utils.ui.menu.MenuController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
public class SettingsController extends Application implements FXWindow {
	public static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
	private String fxmlResource = "Settings.fxml";
	private String title = "HOIIVUtils Settings " + HOIIVUtils.HOIIVUTILS_VERSION;
	private Stage stage;

	@FXML
	public Label versionLabel;
	@FXML
	public TextField modPathTextField;
	@FXML
	public TextField hoi4PathTextField;
	@FXML
	public Button modFolderBrowseButton;
	@FXML
	public Button hoi4FolderBrowseButton;
	@FXML
	public Button idOkButton;
	@FXML
	public RadioButton darkTheme;
	@FXML
	public RadioButton lightTheme;
	@FXML
	public ComboBox<Screen> preferredMonitorComboBox;

	@FXML
	void initialize() {
		versionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);
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
		String pathText = modPathTextField.getText();

		if (pathText.isEmpty()) return;

		File modFile = new File(pathText);
		if (modFile.exists() && modFile.isDirectory()) {HOIIVUtils.set("mod.path", String.valueOf(modFile));}
	}

	public void handleHOIIVPathTextField() {
		String pathText = hoi4PathTextField.getText();

		if (pathText.isEmpty()) return;

		File hoi4File = new File(pathText);
		if (hoi4File.exists() && hoi4File.isDirectory()) {HOIIVUtils.set("hoi4.path", String.valueOf(hoi4File));}
	}

	public void handleModFileBrowseAction() {
		File modFile = new File(FileUtils.usersDocuments + File.separator + HOIIVFile.usersParadoxHOIIVModFolder);
		modFile = FXWindow.openChooser(modFolderBrowseButton, modFile, true);

		if (modFile == null) return;
		if (modFile.exists() && modFile.isDirectory()) {HOIIVUtils.set("mod.path", String.valueOf(modFile));}

		modPathTextField.setText(modFile.getAbsolutePath());
	}

	public void handleHOIIVFileBrowseAction() {
		File hoi4File =	FileUtils.ProgramFilesX86 == null ? null : new File(FileUtils.ProgramFilesX86 + File.separator + FileUtils.steamHOI4LocalPath);
		hoi4File = FXWindow.openChooser(hoi4FolderBrowseButton, hoi4File, true);

		if (hoi4File == null) return;
		if (hoi4File.exists() && hoi4File.isDirectory()) {HOIIVUtils.set("hoi4.path", String.valueOf(hoi4File));}

		hoi4PathTextField.setText(hoi4File.getAbsolutePath());
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
		HOIIVUtils.set("preferred_screen", String.valueOf(preferredMonitorComboBox.getSelectionModel().getSelectedIndex()));
	}

	/**
	 * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
	 */
	public void handleOkButtonAction() {
		HOIIVUtils.loadMod();
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
