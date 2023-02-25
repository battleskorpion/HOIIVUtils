package hoi4_coding.ui.buildings;

import hoi4_coding.buildings.Infrastructure;
import hoi4_coding.buildings.Resources;
import hoi4_coding.country.CountryTag;
import hoi4_coding.country.CountryTags;
import hoi4_coding.history.State;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class BuildingsByCountryWindow extends JFrame {
    private JPanel BuildingsByCountryWindowJPanel;
    private JTable buildingsTable;
    private DefaultTableModel buildingsTableModel;

    // popup menu
    JPopupMenu popupSettings = new JPopupMenu();
    JCheckBoxMenuItem aluminumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Aluminum as Percent");
    JCheckBoxMenuItem chromiumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Chromium as Percent ");
    JCheckBoxMenuItem oilDisplayAsPercentOption = new JCheckBoxMenuItem("Display Oil as Percent ");
    JCheckBoxMenuItem rubberDisplayAsPercentOption = new JCheckBoxMenuItem("Display Rubber as Percent ");
    JCheckBoxMenuItem steelDisplayAsPercentOption = new JCheckBoxMenuItem("Display Steel as Percent ");
    JCheckBoxMenuItem tungstenDisplayAsPercentOption = new JCheckBoxMenuItem("Display Tungsten as Percent ");

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
                return 18;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1, 2, 3, 4, 5:
                        return Integer.class;
                    case 6, 7, 8, 9, 10, 11:
                        return Double.class;
                    default:
                        return Integer.class;
                }
            }
        };
        String[] columns = {"Country", "Population", "Civilian Factories", "Military Factories", "Dockyards", "Airfields",
                "Civ/Mil Ratio", "Pop / Factory", "Pop / Civ Ratio", "Pop / Mil Ratio", "Pop / Air Capacity", "Pop / State",
                "Aluminum", "Chromium", "Oil", "Rubber", "Steel", "Tungsten"};
        buildingsTableModel.setColumnIdentifiers(columns);
        buildingsTable.setModel(buildingsTableModel);

        // row sorter
        buildingsTable.setAutoCreateRowSorter(true);

        // add popup settings
        popupSettings.add(aluminumDisplayAsPercentOption);
        popupSettings.add(chromiumDisplayAsPercentOption);
        popupSettings.add(oilDisplayAsPercentOption);
        popupSettings.add(rubberDisplayAsPercentOption);
        popupSettings.add(steelDisplayAsPercentOption);
        popupSettings.add(tungstenDisplayAsPercentOption);

        // table renderer (formatting)
        buildingsTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = NumberFormat.getInstance();

            @Override
            protected void setValue(Object aValue) {
                Integer value = (Integer) aValue;
                super.setValue(numberFormat.format(value));
            }
        });
        buildingsTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();

            @Override
            protected void setValue(Object aValue) {
                Double value = (Double) aValue;
                if (value.equals(Double.NaN)) {
                    super.setValue("N/A");
                }
                else {
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        buildingsTable.setDefaultRenderer(Float.class, new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                Float value = (Float) aValue;
                super.setValue(numberFormat.format(value));
            }
        });

        // option action listeners
        aluminumDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        chromiumDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        rubberDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        oilDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        steelDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        tungstenDisplayAsPercentOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBuildingsTable();
            }
        });
        buildingsTable.addMouseListener( new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
//                System.out.println("pressed");
            }

            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    popupSettings.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // data
        refreshBuildingsTable();

        setContentPane(BuildingsByCountryWindowJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void refreshBuildingsTable() {

        // table data update
        ArrayList<CountryTag> countryList = CountryTags.list();

        buildingsTableModel.getDataVector().removeAllElements();
        buildingsTableModel.setRowCount(countryList.size());
        buildingsTableModel.setColumnCount(18);
        buildingsTableModel.fireTableDataChanged();

        for(int i = 0; i < countryList.size(); i++) {
            CountryTag country = countryList.get(i);
            Infrastructure infrastructure = State.infrastructureOfStates(State.listFromCountry(country));
            Resources resources = State.resourcesOfStates(State.listFromCountry(country));
            Resources resourcesAll = State.resourcesOfStates();

            buildingsTableModel.setValueAt(country.toString(), i, 0);
            buildingsTableModel.setValueAt(infrastructure.population(), i, 1);
            buildingsTableModel.setValueAt(infrastructure.civilianFactories(), i, 2);
            buildingsTableModel.setValueAt(infrastructure.militaryFactories(), i, 3);
            buildingsTableModel.setValueAt(infrastructure.navalDockyards(), i, 4);
            buildingsTableModel.setValueAt(infrastructure.airfields(), i, 5);
//            buildingsTableModel.setValueAt(State.list().get(i).getStateData().population(), i, 1);
//            System.out.println(State.list().get(i).getStateData().infrastructure());
            // percentages
            buildingsTableModel.setValueAt((double)infrastructure.civilianFactories() / infrastructure.militaryFactories(), i, 6);
            buildingsTableModel.setValueAt((double)infrastructure.population() / (infrastructure.civilianFactories() + infrastructure.militaryFactories()), i, 7);
            buildingsTableModel.setValueAt((double)infrastructure.population() / infrastructure.civilianFactories(), i, 8);
            buildingsTableModel.setValueAt((double)infrastructure.population() / infrastructure.militaryFactories(), i, 9);
            buildingsTableModel.setValueAt((double)infrastructure.population() / (infrastructure.airfields() * 200), i, 10);
            buildingsTableModel.setValueAt((double)infrastructure.population() / State.numStates(country), i, 11);
            // resources
            if (aluminumDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.aluminum() / resourcesAll.aluminum(), i, 12);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.aluminum(), i, 12);
            }
            if (chromiumDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.chromium() / resourcesAll.chromium(), i, 13);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.chromium(), i, 13);
            }
            if (oilDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.oil() / resourcesAll.oil(), i, 14);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.oil(), i, 14);
            }
            if (rubberDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.rubber() / resourcesAll.rubber(), i, 15);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.rubber(), i, 15);
            }
            if (steelDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.steel() / resourcesAll.steel(), i, 16);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.steel(), i, 16);
            }
            if (tungstenDisplayAsPercentOption.isSelected()) {
                buildingsTableModel.setValueAt((double) resources.tungsten() / resourcesAll.tungsten(), i, 17);
            }
            else {
                buildingsTableModel.setValueAt((double) resources.tungsten(), i, 17);
            }
        }

        // table mouse listener


        // cell renderers
        TableColumn tableColumn = buildingsTable.getColumnModel().getColumn(6);
        tableColumn.setCellRenderer(new CellColorRenderer(0.5, 3) {
            private NumberFormat numberFormat = DecimalFormat.getInstance();

            @Override
            protected void setValue(Object aValue) {
                Double value = (Double) aValue;
                super.setValue(numberFormat.format(value));
            }
        });

        TableColumn tableColumn2 = buildingsTable.getColumnModel().getColumn(12);
        tableColumn2.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                if (aluminumDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        TableColumn tableColumn3 = buildingsTable.getColumnModel().getColumn(13);
        tableColumn3.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();


            @Override
            protected void setValue(Object aValue) {
                if (chromiumDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        TableColumn tableColumn4 = buildingsTable.getColumnModel().getColumn(14);
        tableColumn4.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                if (oilDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        TableColumn tableColumn5 = buildingsTable.getColumnModel().getColumn(15);
        tableColumn5.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                if (rubberDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        TableColumn tableColumn6 = buildingsTable.getColumnModel().getColumn(16);
        tableColumn6.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                if (steelDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
        TableColumn tableColumn7 = buildingsTable.getColumnModel().getColumn(17);
        tableColumn7.setCellRenderer(new DefaultTableCellRenderer() {
            private NumberFormat numberFormat = DecimalFormat.getInstance();
            private NumberFormat nfPercent = NumberFormat.getPercentInstance();

            @Override
            protected void setValue(Object aValue) {
                if (tungstenDisplayAsPercentOption.isSelected()) {
                    Double value = (Double) aValue;
                    super.setValue(nfPercent.format(value));
                }
                else {
                    Double value = (Double) aValue;
                    super.setValue(numberFormat.format(value));
                }
            }
        });
    }

    static class CellColorRenderer extends DefaultTableCellRenderer
    {
//        private static final long serialVersionUID = 6703872492730589499L;
        double min;
        double max;

        public CellColorRenderer(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if((Double) table.getValueAt(row, column) < min){
                cellComponent.setBackground(Color.YELLOW);
            } else if((Double) table.getValueAt(row, column) >= max){
                cellComponent.setBackground(Color.CYAN);
            }
            else {
                cellComponent.setBackground(Color.WHITE);
            }
            return cellComponent;
        }
    }
}
