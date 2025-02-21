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
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * The SettingsController class is responsible for handling the program settings window and its
 * associated functionality. It provides methods to interact with the settings UI and update the
 * settings accordingly.
 *
 * @author thiccchris
 */
public class SettingsController extends Application implements FXWindow {
	private String fxmlResource = "Settings.fxml";
	private String title = "HOIIVUtils Settings " + HOIIVUtils.HOIIVUTILS_VERSION;
	private Stage stage;

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
	public Button idOkButton;
	@FXML
	public CheckBox drawFocusTreesCheckBox;
	@FXML
	public CheckBox loadLocalizationCheckBox;
	@FXML
	public RadioButton darkTheme;
	@FXML
	public RadioButton lightTheme;
	@FXML
	public ComboBox<Screen> preferredMonitorComboBox;

	@FXML
	void initialize() {
		idVersionLabel.setText(HOIIVUtils.HOIIVUTILS_VERSION);
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
		devModeCheckBox.setSelected(HOIIVUtils.getBoolean("dev_mode.enabled"));
		drawFocusTreesCheckBox.setSelected(HOIIVUtils.getBoolean("draw_focus_tree.enabled"));
		loadLocalizationCheckBox.setSelected(HOIIVUtils.getBoolean("load_localization"));
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));


		Parent rootFXML;
		try {
			rootFXML = loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Scene scene = new Scene(rootFXML);
		if (Objects.equals(HOIIVUtils.get("theme"), "dark")) {
			scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
		}
		this.stage = stage;
		stage.setScene(scene);
		stage.setTitle(title);
		stage.show();
		stage.maxWidthProperty().bind(stage.widthProperty());
		stage.maxHeightProperty().bind(stage.heightProperty());

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


	public void handleDevModeCheckBoxAction() {
		HOIIVUtils.set("dev_mode.enabled", String.valueOf(devModeCheckBox.isSelected()));
	}

	public void handleDrawFocusTreesCheckBoxAction() {
		HOIIVUtils.set("draw_focus_tree.enabled", String.valueOf(drawFocusTreesCheckBox.isSelected()));
	}

	public void handleLoadLocalizationCheckBoxAction() {
		HOIIVUtils.set("load_localization", String.valueOf(loadLocalizationCheckBox.isSelected()));
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
	 * change preferred monitor setting. // todo future: change settings window
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
