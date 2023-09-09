package ui.focus_localization;

import java.io.File;

import hoi4utils.Settings;
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
    @FXML private FocusTree focusTree;
    @FXML private FocusLocalizationFile focusLocFile;
    @FXML private TableView<Focus> focusListTable;
    @FXML private TableColumn<Focus, String> focusIDColumn;
    @FXML private TableColumn<Focus, String> focusNameColumn;
    @FXML private TableColumn<Focus, String> focusDescColumn;

    public FocusLocalizationWindow() {
        fxmlResource = "FocusLocalizationWindow.fxml";
        title = "HOIIVUtils Focus Localization";
    }

    public void handleFocusTreeFileBrowseButtonAction() {
        File selectedFile = HOIUtilsWindow.openChooser(focusTreeFileBrowseButton, false);
        if (Settings.DEV_MODE.enabled()) {
            System.out.println(selectedFile);
        }
        if (selectedFile != null) {
            focusTreeFileTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void handleFocusLocFileBrowseButtonAction() {
        File selectedFile = HOIUtilsWindow.openChooser(focusLocFileBrowseButton, false);
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
