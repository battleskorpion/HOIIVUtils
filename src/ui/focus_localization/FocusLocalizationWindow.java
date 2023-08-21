package ui.focus_localization;

import java.io.File;
import java.io.IOException;

import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_coding.focus.FixFocus;
import hoi4utils.clausewitz_coding.focus.Focus;
import hoi4utils.clausewitz_coding.focus.FocusTree;
import hoi4utils.clausewitz_coding.localization.FocusLocalizationFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ui.HOIUtilsWindow;

public class FocusLocalizationWindow extends HOIUtilsWindow {

	public void open(){
		super.open();
	}

		


	// * Focus Localization Window Controller
	@FXML Label numLocAddedLabel;
	@FXML Label focusTreeNameLabel;
	@FXML FocusTree focusTree;
	@FXML FocusLocalizationFile focusLocFile;
	@FXML TableView<Focus> focusListTable;
	@FXML TableColumn<Focus, String> focusIDColumn;
	@FXML TableColumn<Focus, String> focusNameColumn;
	@FXML TableColumn<Focus, String> focusDescColumn;

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


		
		// Windows Two File Chooser

		File selectedFile;

		/* choose focus tree */
		FocusTree focusTree;
		try{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(HOIIVUtils.focus_folder);
			Stage stage = new Stage();
			selectedFile = fileChooser.showOpenDialog(stage);

			if (selectedFile == null) {
				HOIIVUtils.openError("Selected directory was null.");
				return;
			}

		}
		catch(Exception exception) {
			HOIIVUtils.openError(exception);
			return;
		}
		try {
			focusTree = new FocusTree(selectedFile);
		} catch (IOException e) {
			HOIIVUtils.openError(e);
			return;
		}
		this.focusTree = focusTree;

		/* choose localization file */
		FocusLocalizationFile focusLocFile;
		try{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(HOIIVUtils.localization_eng_folder);
			Stage stage = new Stage();
			selectedFile = fileChooser.showOpenDialog(stage);

			if (selectedFile == null) {
				HOIIVUtils.openError("Selected directory was null.");
				return;
			}

		}
		catch(Exception exception) {
			HOIIVUtils.openError(exception);
			return;
		}
		try {
			focusLocFile = new FocusLocalizationFile(selectedFile);
		} catch (IOException e) {
			HOIIVUtils.openError(e);
			return;
		}
		this.focusLocFile = focusLocFile;

		/* add focus loc */
		focusLocFile.readLocalization();
		try {
			numLocalizedFocuses = FixFocus.addFocusLoc(focusTree, focusLocFile);
		} catch (IOException e) {
			HOIIVUtils.openError(e);
			return;
		}
	}
}
