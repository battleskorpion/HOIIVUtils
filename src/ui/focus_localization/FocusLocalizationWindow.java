package ui.focus_localization;

import java.io.File;
import hoi4utils.clausewitz_coding.focus.Focus;
import hoi4utils.clausewitz_coding.focus.FocusTree;
import hoi4utils.clausewitz_coding.localization.FocusLocalizationFile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.HOIUtilsWindow;
import ui.MessagePopupWindow;

public class FocusLocalizationWindow extends HOIUtilsWindow {

	public void open(){
		super.open();
	}

	// * Focus Localization Window Controller
	@FXML
	Label numLocAddedLabel;
	Label focusTreeNameLabel;
	Button focusTreeFileBrowseButton;
	Button focusLocFileBrowseButton;
	Button loadButton;
	FocusTree focusTree;
	FocusLocalizationFile focusLocFile;
	TableView<Focus> focusListTable;
	TableColumn<Focus, String> focusIDColumn;
	TableColumn<Focus, String> focusNameColumn;
	TableColumn<Focus, String> focusDescColumn;

	private int numLocalizedFocuses;

	@FXML
	void initialize() {
		numLocAddedLabel.setText("Added localization to " + numLocalizedFocuses + " focuses");

		focusIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		focusNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameLocalizationProperty());
		focusDescColumn.setCellValueFactory(cellData -> cellData.getValue().descLocalizationProperty());

		focusListTable.setItems(focusTree.listFocuses());
	}
	
	public FocusLocalizationWindow() {
		// Windows properties
		fxmlResource = "FocusLocalizationWindow.fxml";
		title = "Focus Localization";
		
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
		File focusTree = HOIUtilsWindow.openChooser(focusTreeFileBrowseButton, false); // ? I don't know how to make this pass any Class, Class<?> didn't work for me
		if (focusTree == null) {
			return;
		}
	}

	public void handlefocusLocFileBrowseButtonAction() {
		File focusLocFiles = HOIUtilsWindow.openChooser(focusTreeFileBrowseButton, false); // ? I don't know how to make this pass any Class, Class<?> didn't work for me
		if (focusLocFiles == null) {
			return;
		}
	}
	
	public void handleLoadButtonAction() {
		if (focusLocFile == null || focusTree == null) {
			MessagePopupWindow window = new MessagePopupWindow("Can't load table because one of the two files has no been chosen, please pick both files");
			window.open();
		}
	}
}
