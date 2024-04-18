package com.HOIIVUtils.ui.buildings;

import com.HOIIVUtils.hoi4utils.Settings;
import com.HOIIVUtils.hoi4utils.clausewitz_code.ClausewitzDate;
import com.HOIIVUtils.ui.javafx.export.ExcelExport;
import com.HOIIVUtils.ui.javafx.table.DoubleTableCell;
import com.HOIIVUtils.hoi4utils.HOIIVUtils;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.Country;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.HOIIVUtils.ui.FXWindow;
import com.HOIIVUtils.ui.HOIUtilsWindow;
import com.HOIIVUtils.ui.javafx.table.IntegerOrPercentTableCell;
import com.HOIIVUtils.ui.javafx.table.TableViewWindow;
import javafx.scene.input.MouseButton;

import javax.swing.*;

public class BuildingsByCountryWindow extends HOIUtilsWindow implements TableViewWindow {

	@FXML private MenuItem idExportToExcel;
	@FXML private CheckMenuItem idPercentageCheckMenuItem;
	@FXML private MenuItem idVersionMenuItem;
	@FXML TableView<Country> countryDataTable;
	@FXML TableColumn<Country, String> countryDataTableCountryColumn;
	@FXML TableColumn<Country, Integer> countryDataTablePopulationColumn;
	@FXML TableColumn<Country, Integer> countryDataTableCivFactoryColumn;
	@FXML TableColumn<Country, Integer> countryDataTableMilFactoryColumn;
	@FXML TableColumn<Country, Integer> countryDataTableDockyardsColumn;
	@FXML TableColumn<Country, Integer> countryDataTableAirfieldsColumn;
	@FXML TableColumn<Country, Double> countryDataTableCivMilRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTablePopFactoryRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTablePopCivRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTablePopMilRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTablePopAirCapacityRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTablePopNumStatesRatioColumn;
	@FXML TableColumn<Country, Double> countryDataTableAluminiumColumn;       // todo dont do these yet
	@FXML TableColumn<Country, Double> countryDataTableChromiumColumn;
	@FXML TableColumn<Country, Double> countryDataTableOilColumn;
	@FXML TableColumn<Country, Double> countryDataTableRubberColumn;
	@FXML TableColumn<Country, Double> countryDataTableSteelColumn;
	@FXML TableColumn<Country, Double> countryDataTableTungstenColumn;

	private Boolean resourcesPercent;
	private ClausewitzDate date = ClausewitzDate.defaulty();

	private final ObservableList<Country> countryList;

	public BuildingsByCountryWindow() {
		setFxmlResource("BuildingsByCountryWindow.fxml");
		setTitle("HOIIVUtils Buildings By Country Window");

		countryList = Country.loadCountries();
	}

	@FXML
	void initialize() {
		includeVersion();
		loadTableView(this, countryDataTable, countryList, Country.getCountryDataFunctions(false));

		/* action listeners */
		countryDataTable.setOnMouseClicked(event -> {
			if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				viewCountryBuildingsByState();
			}
		});

		if (Settings.DEV_MODE.enabled()) {
			JOptionPane.showMessageDialog(null, "dev - loaded rows: " + countryDataTable.getItems().size());
		}
	}

	private void includeVersion() {
		idVersionMenuItem.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}

	// todo put this in hoi4window parent class or whatever

	private void updateResourcesColumnsPercentBehavior() {
		FXWindow.updateColumnPercentBehavior(countryDataTableAluminiumColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(countryDataTableChromiumColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(countryDataTableOilColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(countryDataTableRubberColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(countryDataTableSteelColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(countryDataTableTungstenColumn, resourcesPercent);
	}

	public void setDataTableCellFactories() {
		// table cell factories
		countryDataTableCivMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopFactoryRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopCivRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopAirCapacityRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopNumStatesRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTableAluminiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		countryDataTableChromiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		countryDataTableOilColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		countryDataTableRubberColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		countryDataTableSteelColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		countryDataTableTungstenColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
	}

	@FXML
	public void handleExportToExcelAction() {
		ExcelExport<Country> excelExport = new ExcelExport<>();
		excelExport.export(countryDataTable);
	}

	public void handlePercentageCheckMenuItemAction() {
        if (idPercentageCheckMenuItem.isSelected()) {
	        setResourcesPercent(true);
            System.out.println("Percentage values are on");
        } else {
	        setResourcesPercent(false);
            System.out.println("Percentage values are off");
        }
	}

	public Boolean resourcesPercent() {
		return resourcesPercent;
	}

	public void setResourcesPercent(Boolean resourcesPercent) {
		this.resourcesPercent = resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}

	public void toggleResourcesPercent() {
		resourcesPercent = !resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}

	public void viewCountryBuildingsByState() {
		Country country = countryDataTable.getSelectionModel().getSelectedItem();
		if (country == null) {
			return;
		}
		CountryBuildingsByStateWindow window = new CountryBuildingsByStateWindow();
		window.open(country);
	}

}
/*
	// popup menu
	JPopupMenu popupSettings = new JPopupMenu();
	JCheckBoxMenuItem aluminumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Aluminum as Percent");
	JCheckBoxMenuItem chromiumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Chromium as Percent ");
	JCheckBoxMenuItem oilDisplayAsPercentOption = new JCheckBoxMenuItem("Display Oil as Percent ");
	JCheckBoxMenuItem rubberDisplayAsPercentOption = new JCheckBoxMenuItem("Display Rubber as Percent ");
	JCheckBoxMenuItem steelDisplayAsPercentOption = new JCheckBoxMenuItem("Display Steel as Percent ");
	JCheckBoxMenuItem tungstenDisplayAsPercentOption = new JCheckBoxMenuItem("Display Tungsten as Percent ");

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
//				System.out.println("pressed");
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					popupSettings.show(e.getComponent(), e.getX(), e.getY());
				}
			}


            // todo similar to this for new ui for popup menu thing idk do like this.
			public void mouseClicked(MouseEvent e) {
				//super.mouseClicked(e);
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
				} else {
					return;
				}

				// get country
				int row = buildingsTable.rowAtPoint( e.getPoint() );
				int modelRow = HOIIVUtils.rowToModelIndex(buildingsTable, row);
				String country_name = (String) buildingsTableModel.getValueAt(modelRow, 0);	 // column 0 - country name

				CountryTag country = new CountryTag(country_name);

				CountryBuildingsByStateWindow countryBuildingsByStateWindow = new CountryBuildingsByStateWindow(country);
				countryBuildingsByStateWindow.setVisible(true);
			}
		});

		// data
		refreshBuildingsTable();

		setContentPane(BuildingsByCountryWindowJPanel);
		setSize(700, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();

		/* file listener *//*
		stateDirWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
				EventQueue.invokeLater(() -> {
					while (stateDirWatcher.listenerPerformAction > 0) {
						try {
							wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
					if (stateDirWatcher.listenerPerformAction == 0) {
						refreshBuildingsTable();
					} else {
						System.out.println("Warning: refresh table not performed");
					}
				});
			}

			@Override
			public void onModified(FileEvent event) {
				EventQueue.invokeLater(() -> {
					while (stateDirWatcher.listenerPerformAction > 0) {
						try {
							wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
					if (stateDirWatcher.listenerPerformAction == 0) {
						refreshBuildingsTable();
					} else {
						System.out.println("Warning: refresh table not performed");
					}
				});
			}

			@Override
			public void onDeleted(FileEvent event) {
				EventQueue.invokeLater(() -> {
					while (stateDirWatcher.listenerPerformAction > 0) {
						try {
							wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
					if (stateDirWatcher.listenerPerformAction == 0) {
						refreshBuildingsTable();
					} else {
						System.out.println("Warning: refresh table not performed");
					}
				});
			}
		}).watch();
	}

	public void refreshBuildingsTable() {
		// table data update
		ArrayList<CountryTag> countryList = CountryTags.list();

		buildingsTableModel.getDataVector().removeAllElements();
		buildingsTableModel.setRowCount(countryList.size());
		buildingsTableModel.setColumnCount(18);
		buildingsTableModel.fireTableDataChanged();

		for (int i = 0; i < countryList.size(); i++) {
			CountryTag country = countryList.get(i);
			Infrastructure infrastructure = State.infrastructureOfStates(State.listFromCountry(country));
			Resources resources = State.resourcesOfStates(State.listFromCountry(country));
			Resources resourcesAll = State.resourcesOfStates();

			// stats
			buildingsTableModel.setValueAt(country.toString(), i, 0);
			buildingsTableModel.setValueAt(infrastructure.population(), i, 1);
			buildingsTableModel.setValueAt(infrastructure.civilianFactories(), i, 2);
			buildingsTableModel.setValueAt(infrastructure.militaryFactories(), i, 3);
			buildingsTableModel.setValueAt(infrastructure.navalDockyards(), i, 4);
			buildingsTableModel.setValueAt(infrastructure.airfields(), i, 5);

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
			} else {
				buildingsTableModel.setValueAt((double) resources.aluminum(), i, 12);
			}
			if (chromiumDisplayAsPercentOption.isSelected()) {
				buildingsTableModel.setValueAt((double) resources.chromium() / resourcesAll.chromium(), i, 13);
			} else {
				buildingsTableModel.setValueAt((double) resources.chromium(), i, 13);
			}
			if (oilDisplayAsPercentOption.isSelected()) {
				buildingsTableModel.setValueAt((double) resources.oil() / resourcesAll.oil(), i, 14);
			} else {
				buildingsTableModel.setValueAt((double) resources.oil(), i, 14);
			}
			if (rubberDisplayAsPercentOption.isSelected()) {
				buildingsTableModel.setValueAt((double) resources.rubber() / resourcesAll.rubber(), i, 15);
			} else {
				buildingsTableModel.setValueAt((double) resources.rubber(), i, 15);
			}
			if (steelDisplayAsPercentOption.isSelected()) {
				buildingsTableModel.setValueAt((double) resources.steel() / resourcesAll.steel(), i, 16);
			} else {
				buildingsTableModel.setValueAt((double) resources.steel(), i, 16);
			}
			if (tungstenDisplayAsPercentOption.isSelected()) {
				buildingsTableModel.setValueAt((double) resources.tungsten() / resourcesAll.tungsten(), i, 17);
			} else {
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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");


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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

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
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

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
//		private static final long serialVersionUID = 6703872492730589499L;
		double min;
		double max;

		public CellColorRenderer(double min, double max) {
			this.min = min;
			this.max = max;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (table.getValueAt(row, column) != null && (Double) table.getValueAt(row, column) < min){
				cellComponent.setBackground(Color.YELLOW);
			} else if (table.getValueAt(row, column) != null && (Double) table.getValueAt(row, column) >= max){
				cellComponent.setBackground(Color.CYAN);
			}
			else {
				cellComponent.setBackground(Color.WHITE);
			}
			return cellComponent;
		}
	}
}*/
