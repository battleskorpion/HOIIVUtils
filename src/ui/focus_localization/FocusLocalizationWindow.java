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
import ui.MessagePopupWindow;

public class FocusLocalizationWindow extends HOIUtilsWindow {

	// * Focus Localization Window Controller
	@FXML Label numLocAddedLabel;
	@FXML TextField focusTreeFileTextField;
	@FXML Button focusTreeFileBrowseButton;
	@FXML Label focusTreeNameLabel;
	@FXML TextField focusLocFileTextField;
	@FXML Button focusLocFileBrowseButton;
	@FXML Button loadButton;
	@FXML FocusTree focusTree;
	@FXML FocusLocalizationFile focusLocFile;
	@FXML TableView<Focus> focusListTable;
	@FXML TableColumn<Focus, String> focusIDColumn;
	@FXML TableColumn<Focus, String> focusNameColumn;
	@FXML TableColumn<Focus, String> focusDescColumn;

	// private int numLocalizedFocuses;

	@FXML
	void initialize() {
		// numLocAddedLabel.setText("Added localization to " + numLocalizedFocuses + " focuses");

		// focusIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		// focusNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameLocalizationProperty());
		// focusDescColumn.setCellValueFactory(cellData -> cellData.getValue().descLocalizationProperty());

		// focusListTable.setItems(focusTree.listFocuses());
	}
	
	public FocusLocalizationWindow() {
		// Windows properties
		fxmlResource = "FocusLocalizationWindow.fxml";
		title = "HOIIVUtils Focus Localization";
		
		// // Windows Two File Chooser

		// File selectedFile;

		// /* choose focus tree */
		// FocusTree focusTree;
		// try{
		// 	selectedFile = openFileChooserDialog(HOIIVUtils.focus_folder);

		// 	if (selectedFile == null) {
		// 		HOIUtilsWindow.openError("Selected directory was null.");
		// 		return;
		// 	}

		// }
		// catch(Exception exception) {
		// 	HOIUtilsWindow.openError(exception);
		// 	return;
		// }
		// try {
		// 	focusTree = new FocusTree(selectedFile);
		// } catch (IOException e) {
		// 	HOIUtilsWindow.openError(e);
		// 	return;
		// }
		// this.focusTree = focusTree;

		// /* choose localization file */
		// FocusLocalizationFile focusLocFile;
		// try {
		// 	selectedFile = openFileChooserDialog(HOIIVUtils.localization_eng_folder);

		// 	if (selectedFile == null) {
		// 		HOIUtilsWindow.openError("Selected directory was null.");
		// 		return;
		// 	}

		// }
		// catch(Exception exception) {
		// 	HOIUtilsWindow.openError(exception);
		// 	return;
		// }
		// try {
		// 	focusLocFile = new FocusLocalizationFile(selectedFile);
		// } catch (IOException e) {
		// 	HOIUtilsWindow.openError(e);
		// 	return;
		// }
		// this.focusLocFile = focusLocFile;

		/* add focus loc */
		// focusLocFile.readLocalization();
		// try {
		// 	numLocalizedFocuses = FixFocus.addFocusLoc(focusTree, focusLocFile);
		// } catch (IOException e) {
		// 	HOIUtilsWindow.openError(e);
		// 	return;
		// }
	}

	public void handlefocusTreeFileBrowseButtonAction() {
		File focusTree = HOIUtilsWindow.openChooser(focusTreeFileBrowseButton, false); 
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(focusTree);
		}
		if (focusTree == null) {
			return;
		}
		focusTreeFileTextField.setText(focusTree.getAbsolutePath());
	}

	public void handlefocusLocFileBrowseButtonAction() {
		File focusLocFiles = HOIUtilsWindow.openChooser(focusLocFileBrowseButton, false);
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(focusLocFiles);
		}
		if (focusLocFiles == null) {
			return;
		}
		focusLocFileTextField.setText(focusLocFiles.getAbsolutePath());
	}
	
	public void handleLoadButtonAction() {
		if (focusLocFile == null || focusTree == null) {
			MessagePopupWindow window = new MessagePopupWindow();
			window.open("IT WORKED, SEEING THIS MESSAGE TOOK 6 HOURS OF WORK REWRITING");
		}
	}
}
