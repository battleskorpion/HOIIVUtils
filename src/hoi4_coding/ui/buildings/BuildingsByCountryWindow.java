package hoi4_coding.ui.buildings;

import hoi4_coding.country.CountryTag;
import hoi4_coding.country.CountryTags;
import hoi4_coding.history.State;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;

public class BuildingsByCountryWindow extends JFrame {
    private JPanel BuildingsByCountryWindowJPanel;
    private JTable buildingsTable;
    private DefaultTableModel buildingsTableModel;

    public BuildingsByCountryWindow() {
        super("Buildings by Country");

        // table model
        buildingsTableModel = new DefaultTableModel() {
            @Override
            public int getRowCount() {
                return CountryTags.list().size();
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
        String[] columns = {"Country", "Population", "Civilian Factories", "Military Factories"};
        buildingsTableModel.setColumnIdentifiers(columns);
        buildingsTable.setModel(buildingsTableModel);

        // data
        refreshBuildingsTable();

        setContentPane(BuildingsByCountryWindowJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void refreshBuildingsTable() {
        ArrayList<CountryTag> countryList = CountryTags.list();

        buildingsTableModel.getDataVector().removeAllElements();
        buildingsTableModel.setRowCount(countryList.size());
        buildingsTableModel.setColumnCount(4);
        buildingsTableModel.fireTableDataChanged();

        for(int i = 0; i < countryList.size(); i++) {
            buildingsTableModel.setValueAt(countryList.get(i).toString(), i, 0);
            buildingsTableModel.setValueAt(State.list().get(i).getStateData().infrastructure(), i, 1);
        }
    }
}
