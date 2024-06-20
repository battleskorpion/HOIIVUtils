package com.HOIIVUtils.ui.hoi4localization;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.data.country.Country;
import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import com.HOIIVUtils.clauzewitz.data.focus.FocusTree;
import com.HOIIVUtils.clauzewitz.localization.Localization;
import com.HOIIVUtils.ui.javafx.table.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;

import java.io.*;

public class AllFocusTreesWindow extends HOIIVUtilsStageLoader implements TableViewWindow {

	@FXML
	private TableView<Focus> focusListTable;
	@FXML
	private TableColumn<Focus, String> focusIDColumn;
	@FXML
	private TableColumn<Focus, String> focusNameColumn;
	@FXML
	private TableColumn<Focus, String> focusDescColumn;
	private final ObservableList<Focus> focusObservableList;

	public AllFocusTreesWindow() {
		/* window */
		setFxmlResource("AllFocusTreesWindow.fxml");
		setTitle("HOIIVUtils Unlocalized Focus Window");

		focusObservableList = FXCollections.observableArrayList();
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@FXML
	void initialize() {
		/* table */
		loadTableView(this, focusListTable, focusObservableList, Focus.getDataFunctions());
		updateObservableFocusList();

	}

	@Override
	public void setDataTableCellFactories() {
		/* column factory */
		//focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		//focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn());

		/* row factory */
		// todo maybe
		focusListTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Focus focus, boolean empty) {
				super.updateItem(focus, empty);

				if (focus == null || empty) {
					setGraphic(null); // Clear any previous content
				} else {
					Localization.Status textStatus;
					Localization.Status descStatus;
					if (focus.getNameLocalization() == null) {
						textStatus = Localization.Status.MISSING;
					} else {
						textStatus = focus.getNameLocalization().status();
					}
					if (focus.getDescLocalization() == null) {
						descStatus = Localization.Status.MISSING;
					} else {
						descStatus = focus.getDescLocalization().status();
					}

					boolean hasStatusUpdated = textStatus == Localization.Status.UPDATED
							|| descStatus == Localization.Status.UPDATED;
					boolean hasStatusNew = descStatus == Localization.Status.NEW
							|| textStatus == Localization.Status.NEW;
					if (hasStatusUpdated || hasStatusNew) {
						setTextFill(Color.BLACK); // Set text color to black
						setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;"); // Apply bold text using CSS
					} else {
						setTextFill(Color.BLACK); // Set text color to black
						setStyle("-fx-background-color: transparent;"); // Reset style
					}
				}
			}
		});
	}

	private void updateObservableFocusList() {
		focusObservableList.clear();
		for (var focusTree : FocusTree.listFocusTrees()) {
			focusObservableList.addAll(focusTree.focuses());
		}
	}

	@FXML
	private void handleExportFocusesMissingDescriptionsAction() {
		// write to new file
		File file = new File("focuses_missing_descriptions.csv");
		try {
			FileWriter writer = new FileWriter(file, false);
			BufferedWriter BWriter = new BufferedWriter(writer);
			PrintWriter PWriter = new PrintWriter(BWriter);

			PWriter.println("Focus ID; Focus Name; Focus Description; Notes");
			for (var focus : focusObservableList) {
				if (focus.getDescLocalization() == null) {
					Focus.getDataFunctions().forEach(dataFunction -> {
						PWriter.print(dataFunction.apply(focus));
						PWriter.print(";");
					});
					PWriter.print("Missing description (no localization key exists);");
					PWriter.println();
				}
				else if (focus.getDescLocalization().text().isEmpty()) {
					Focus.getDataFunctions().forEach(dataFunction -> {
						PWriter.print(dataFunction.apply(focus));
						PWriter.print(";");
					});
					PWriter.print("Empty description (localization key exists);");
					PWriter.println();
				}
			}
			PWriter.close();
			System.out.println("Exported focuses missing descriptions csv to " + file.getAbsolutePath());
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}
}
