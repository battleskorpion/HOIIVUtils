package ui.hoi4localization;

import java.io.File;

import hoi4utils.HOIIVFile;
import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import hoi4utils.SettingsManager;
import hoi4utils.clausewitz_coding.focus.Focus;
import hoi4utils.clausewitz_coding.focus.FocusTree;
import hoi4utils.clausewitz_coding.localization.FocusLocalizationFile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ui.HOIUtilsWindow;
import ui.message_popup.MessagePopupWindow;

public class FocusLocalizationWindow extends HOIUtilsWindow {

    @FXML private Label numLocAddedLabel;
    @FXML private TextField focusTreeFileTextField;
    @FXML private Button focusTreeFileBrowseButton;
    @FXML private Label focusTreeNameLabel;
    @FXML private TextField focusLocFileTextField;
    @FXML private Button focusLocFileBrowseButton;
    @FXML private Button loadButton;
    @FXML private TableView<Focus> focusListTable;
    @FXML private TableColumn<Focus, String> focusIDColumn;
    @FXML private TableColumn<Focus, String> focusNameColumn;
    @FXML private TableColumn<Focus, String> focusDescColumn;
    private FocusTree focusTree;
    private FocusLocalizationFile focusLocFile;

    public FocusLocalizationWindow() {
        setFxmlResource("FocusLocalizationWindow.fxml");
        setTitle("HOIIVUtils Focus Localization");
    }

    public void handleFocusTreeFileBrowseButtonAction() {
        // todo replace initial with actual focus directory
        File initialFocusDirectory = HOIIVFile.focus_folder;
        File selectedFile = HOIUtilsWindow.openChooser(focusTreeFileBrowseButton, false, initialFocusDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }
        if (selectedFile != null) {
            focusTreeFileTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void handleFocusLocFileBrowseButtonAction() {
         // todo replace initial with actual focus loc directory
        File initialFocusLocDirectory = HOIIVFile.localization_eng_folder;
        File selectedFile = HOIUtilsWindow.openChooser(focusLocFileBrowseButton, false, initialFocusLocDirectory);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }
        if (selectedFile != null) {
            focusLocFileTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void handleLoadButtonAction() {
        if (focusLocFile == null || focusTree == null) {
            // Handle the case where focusLocFile or focusTree is not properly initialized
            MessagePopupWindow window = new MessagePopupWindow();
            window.open("Error: Focus localization or focus tree not properly initialized.");
        }
		// Add further handling logic here
    }
}