package com.HOIIVUtils.ui.hoi4localization;

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

public class AllFocusTreesWindow extends HOIIVUtilsStageLoader implements TableViewWindow {

	@FXML
	private TableView<Focus> focusListTable;
	@FXML
	private TableColumn<Focus, Country> countryColumn;
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
		//loadTreeTableView(this, focusListTreeTable, focusObservableList, Focus.getDataFunctions());
		//int numLocalizedFocus = 0;
		updateObservableFocusList();
	}

	@Override
	public void setDataTableCellFactories() {
		/* column factory */
		//countryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn());

		/* row factory */
		// todo maybe
		focusListTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Focus focus, boolean empty) {
				super.updateItem(focus, empty);

				if (focus == null || empty) {
					setGraphic(null); // Clear any previous content
				} else {
					Localization.Status textStatus = focus.getNameLocalization().status();
					Localization.Status descStatus = focus.getDescLocalization().status();
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
}
// private JPanel UnlocalizedFocusJPanel;
// private JTable unlocalizedFocusTable;
// private DefaultTableModel unlocalizedFocusTableModel;

// public UnlocalizedFocusWindow (List<FocusTree> focusTrees) {
// super("Focus Localization"); // JFrame

// // table model
// unlocalizedFocusTableModel = new DefaultTableModel() {

// @Override
// public int getRowCount() {
// return focusTrees.size();
// }

// @Override
// public int getColumnCount() {
// return 4;
// }

// @Override
// public boolean isCellEditable(int row, int column) {
// return false;
// }
// };
// String[] columns = {"Unlocalized Focus Tree", "Localization File", "Focus
// Tree File", "Status"};
// unlocalizedFocusTableModel.setColumnIdentifiers(columns);
// unlocalizedFocusTable.setModel(unlocalizedFocusTableModel);

// // data
// refreshFocusTreeTable(focusTrees);

// setContentPane(UnlocalizedFocusJPanel);
// setSize(1200, 500);
// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// pack();
// }

// public static void main(String[] args) {
// JFrame window = new
// UnlocalizedFocusWindow(FocusTree.unlocalizedFocusTrees());
// window.setVisible(true);
// }

// public void refreshFocusTreeTable(List<FocusTree> focusTrees) {
// // remove previous data
// unlocalizedFocusTableModel.getDataVector().removeAllElements();
// unlocalizedFocusTableModel.setRowCount(focusTrees.size());
// unlocalizedFocusTableModel.setColumnCount(4);
// unlocalizedFocusTableModel.fireTableDataChanged();

// for (int i = 0; i < focusTrees.size(); i++) {
// // focus tree name
// FocusTree tree = focusTrees.get(i);
// unlocalizedFocusTableModel.setValueAt(tree, i, 0);
// // localization file
// LocalizationFile localization = tree.locFile();
// if (localization == null) {
// unlocalizedFocusTableModel.setValueAt("<Not Found>", i, 1);
// } else {
// unlocalizedFocusTableModel.setValueAt(localization, i, 1);
// }
// // focus tree file
// unlocalizedFocusTableModel.setValueAt(tree.focusFile().getParentFile().getName()
// + "\\" + tree.focusFile().getName(), i, 2);
// }

// }

// }
