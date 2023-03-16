package hoi4_coding.ui.focus;

import hoi4_coding.country.CountryTags;
import hoi4_coding.focus.Focus;
import hoi4_coding.focus.FocusTree;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;

public class FocusTreeLocProgress extends JFrame {
    private JPanel FocusTreeLocProgressJPanel;
    private JProgressBar progressBarLoc;
    private JLabel focusTreeName;
    private JLabel focusTreeLabel;
    private JLabel unlocalizedFocusesLabel;
    private JLabel unlocFocusesAmtLabel;
    private JTable unlocFocusesTable;
    private DefaultTableModel unlocFocusesTableModel;


    public FocusTreeLocProgress(FocusTree focusTree) {
        super("Focus Tree Localization");

        focusTreeName.setText(focusTree.toString());
        //progressBarLoc.setIndeterminate();
        progressBarLoc.setMaximum(4);

        /* table model */
        unlocFocusesTableModel = new DefaultTableModel() {
            @Override
            public int getRowCount() {
                try {
                    return focusTree.list().size();
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        unlocFocusesTable.setModel(unlocFocusesTableModel);

        setContentPane(FocusTreeLocProgressJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
    }

    public void incrementProgressBar() {
        progressBarLoc.setValue(progressBarLoc.getValue() + 1);
    }
    public void setNumFocusesUnloc(int num) {
        unlocFocusesAmtLabel.setText(Integer.toString(num));
    }

    public void refreshUnlocFocusesTable(ArrayList<Focus> focuses) {
        for (int i = 0; i < focuses.size(); i++) {
            unlocFocusesTableModel.setValueAt(focuses.get(i), i, 0);
            unlocFocusesTableModel.setValueAt(focuses.get(i).locName(), i, 1);
        }
    }
}
