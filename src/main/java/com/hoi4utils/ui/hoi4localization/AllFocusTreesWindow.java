package main.java.com.hoi4utils.ui.hoi4localization;

import main.java.com.hoi4utils.clauzewitz.data.focus.FixFocus;
import main.java.com.hoi4utils.clauzewitz.data.focus.Focus;
import main.java.com.hoi4utils.clauzewitz.data.focus.FocusTree;
import main.java.com.hoi4utils.clauzewitz.localization.Localizable;
import main.java.com.hoi4utils.clauzewitz.localization.Localization;
import main.java.com.hoi4utils.clauzewitz.localization.LocalizationManager;
import main.java.com.hoi4utils.ui.javafx.table.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import main.java.com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AllFocusTreesWindow extends HOIIVUtilsWindow implements TableViewWindow {

	private static final Localizable.Property NAME_PROPERTY = Localizable.Property.NAME;
	private static final Localizable.Property DESC_PROPERTY = Localizable.Property.DESCRIPTION;

	@FXML
	private TableView<Focus> focusListTable;
	@FXML
	private TableColumn<Focus, String> focusIDColumn;
	@FXML
	private TableColumn<Focus, String> focusNameColumn;
	@FXML
	private TableColumn<Focus, String> focusDescColumn;
	private final ObservableList<Focus> focusObservableList;
	private final List<Runnable> onSaveActions = new ArrayList<>();

	public AllFocusTreesWindow() {
		/* window */
		setFxmlResource("AllFocusTreesWindow.fxml");
		setTitle("HOIIVUtils Unlocalized Focus Window");

		focusObservableList = FXCollections.observableArrayList();
	}

	/**
	 * {@inheritDoc}
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
		focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		setColumnOnEditCommits();

		/* row factory */
		// todo maybe
		focusListTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Focus focus, boolean empty) {
				super.updateItem(focus, empty);

				if (focus == null || empty) {
					setGraphic(null); // Clear any previous content
					setEditable(true);
				} else {
					Localization.Status textStatus;
					Localization.Status descStatus;
					if (focus.localization(NAME_PROPERTY) == null) {
						textStatus = Localization.Status.MISSING;
					} else {
						textStatus = focus.localization(NAME_PROPERTY).status();
					}
					if (focus.localization(DESC_PROPERTY) == null) {
						descStatus = Localization.Status.MISSING;
					} else {
						descStatus = focus.localization(DESC_PROPERTY).status();
					}

					boolean hasStatusUpdated = textStatus == Localization.Status.UPDATED
							|| descStatus == Localization.Status.UPDATED;
					boolean hasStatusNew = descStatus == Localization.Status.NEW
							|| textStatus == Localization.Status.NEW;
					boolean isVanilla = textStatus == Localization.Status.VANILLA;
					if (hasStatusUpdated || hasStatusNew) {
						setTextFill(Color.BLACK); // Set text color to black
						setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;"); // Apply bold text using CSS
						setEditable(true);
					} else if (isVanilla) {
						setTextFill(Color.BLACK); // Set text color to black
						setStyle("-fx-background-color: #5d5353;");
						// if vanilla disable editability of row
						setEditable(false);
					} else {
						setTextFill(Color.BLACK); // Set text color to black
						setStyle("-fx-background-color: transparent;"); // Reset style
						setEditable(true);
					}
				}
			}
		});
	}

	private void updateObservableFocusList() {
		focusObservableList.clear();
		for (var focusTree : FocusTree.listFocusTrees()) {
			focusObservableList.addAll(focusTree.focuses());
			try {
				FixFocus.fixLocalization(focusTree);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
				if (focus.localization(DESC_PROPERTY) == null) {
					Focus.getDataFunctions().forEach(dataFunction -> {
						PWriter.print(dataFunction.apply(focus));
						PWriter.print(";");
					});
					PWriter.print("Missing description (no localization key exists);");
					PWriter.println();
				}
				else if (focus.localization(DESC_PROPERTY).text().isEmpty()) {
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

	private void setColumnOnEditCommits() {
		// todo this is a great idea but will it work visually? (will changes get lost visually)?
		focusNameColumn.setOnEditCommit(event -> {
			Focus focus = event.getRowValue();
			onSaveActions.add(() -> focus.replaceLocalization(NAME_PROPERTY, event.getNewValue()));
		});
		focusDescColumn.setOnEditCommit(event -> {
			Focus focus = event.getRowValue();
			onSaveActions.add(() -> focus.replaceLocalization(DESC_PROPERTY, event.getNewValue()));
		});
	}

	private void handleSaveButtonAction() {
		// todo handle any exceptions?
		if (onSaveActions.isEmpty()) return;
		onSaveActions.forEach(Runnable::run);
		/*
		 * todo should make sure we are maybe using just one from the beginning
		 *  makes sure that we avoid any funny issues in the future.
		 */
		LocalizationManager.get().saveLocalization();
		onSaveActions.clear();
	}
}
