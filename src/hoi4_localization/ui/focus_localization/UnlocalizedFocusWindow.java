package hoi4_localization.ui.focus_localization;

import hoi4_localization.focus.FocusTree;
import hoi4_localization.focus.FocusTrees;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class UnlocalizedFocusWindow extends JFrame {
    private JPanel UnlocalizedFocusJPanel;
    private JLabel focusloqreqlabel;
    private JTable unlocalizedFocusTable;
    private JButton localizeButton;
    private JButton createLocalizationFilesButton;

    private DefaultTableModel unlocalizedFocusTableModel;

    public UnlocalizedFocusWindow (List<FocusTree> focusTrees) {
        super("Focus Localization");        // JFrame

        // table model
        unlocalizedFocusTableModel = new DefaultTableModel() {

            @Override
            public int getRowCount() {
                return focusTrees.size();
            }

            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String[] columns = {"Unlocalized Focus Tree", "Localization File"};
        unlocalizedFocusTableModel.setColumnIdentifiers(columns);
        unlocalizedFocusTable.setModel(unlocalizedFocusTableModel);

        // data
        refreshFocusTreeTable(focusTrees);

        setContentPane(UnlocalizedFocusJPanel);
        setSize(1200, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public static void main(String[] args) {
        JFrame window = new UnlocalizedFocusWindow(FocusTrees.unlocalizedFocusTrees());
        window.setVisible(true);
    }

    private void createUIComponents() {

    }

    public void refreshFocusTreeTable(List<FocusTree> focusTrees) {
        // remove previous data
        unlocalizedFocusTableModel.getDataVector().removeAllElements();
        unlocalizedFocusTableModel.fireTableDataChanged();
        unlocalizedFocusTableModel.setRowCount(focusTrees.size());
        unlocalizedFocusTableModel.setColumnCount(4);

        for (int i = 0; i < focusTrees.size(); i++) {
            unlocalizedFocusTableModel.setValueAt(focusTrees.get(i), i, 0);
        }

    }

}
