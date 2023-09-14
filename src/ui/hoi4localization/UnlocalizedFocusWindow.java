package ui.hoi4localization;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.HOIUtilsWindow;

public class UnlocalizedFocusWindow extends HOIUtilsWindow {

	@FXML public Label idVersion;
	@FXML public Label idWindowName;

	public UnlocalizedFocusWindow() {
		setFxmlResource("UnlocalizedFocusWindow.fxml");
		setTitle("HOIIVUtils Unlocalized Focus Window");
	}

	@FXML
	void initialize() {
		includeVersion();
		idWindowName.setText("UnlocalizedFocusWindow" + " WIP");
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.hoi4utilsVersion);
	}
}
// 	private JPanel UnlocalizedFocusJPanel;
// 	private JTable unlocalizedFocusTable;
// 	private DefaultTableModel unlocalizedFocusTableModel;

// 	public UnlocalizedFocusWindow (List<FocusTree> focusTrees) {
// 		super("Focus Localization");		// JFrame

// 		// table model
// 		unlocalizedFocusTableModel = new DefaultTableModel() {

// 			@Override
// 			public int getRowCount() {
// 				return focusTrees.size();
// 			}

// 			@Override
// 			public int getColumnCount() {
// 				return 4;
// 			}

// 			@Override
// 			public boolean isCellEditable(int row, int column) {
// 				return false;
// 			}
// 		};
// 		String[] columns = {"Unlocalized Focus Tree", "Localization File", "Focus Tree File", "Status"};
// 		unlocalizedFocusTableModel.setColumnIdentifiers(columns);
// 		unlocalizedFocusTable.setModel(unlocalizedFocusTableModel);

// 		// data
// 		refreshFocusTreeTable(focusTrees);

// 		setContentPane(UnlocalizedFocusJPanel);
// 		setSize(1200, 500);
// 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// 		pack();
// 	}

// 	public static void main(String[] args) {
// 		JFrame window = new UnlocalizedFocusWindow(FocusTree.unlocalizedFocusTrees());
// 		window.setVisible(true);
// 	}

// 	public void refreshFocusTreeTable(List<FocusTree> focusTrees) {
// 		// remove previous data
// 		unlocalizedFocusTableModel.getDataVector().removeAllElements();
// 		unlocalizedFocusTableModel.setRowCount(focusTrees.size());
// 		unlocalizedFocusTableModel.setColumnCount(4);
// 		unlocalizedFocusTableModel.fireTableDataChanged();

// 		for (int i = 0; i < focusTrees.size(); i++) {
// 			// focus tree name
// 			FocusTree tree = focusTrees.get(i);
// 			unlocalizedFocusTableModel.setValueAt(tree, i, 0);
// 			// localization file
// 			LocalizationFile localization = tree.locFile();
// 			if (localization == null) {
// 				unlocalizedFocusTableModel.setValueAt("<Not Found>", i, 1);
// 			} else {
// 				unlocalizedFocusTableModel.setValueAt(localization, i, 1);
// 			}
// 			// focus tree file
// 			unlocalizedFocusTableModel.setValueAt(tree.focusFile().getParentFile().getName() + "\\" + tree.focusFile().getName(), i, 2);
// 		}

// 	}

// }