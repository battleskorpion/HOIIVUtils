package ui.buildings;

import hoi4utils.HOIIVUtils;
import hoi4utils.Settings;
import hoi4utils.clausewitz_coding.country.Country;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import ui.DoubleTableCell;
import ui.HOIUtilsWindow;
import ui.IntegerOrPercentTableCell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BuildingsByCountryWindow extends HOIUtilsWindow {

	@FXML private MenuItem idExportToExcel;
	@FXML private CheckMenuItem idPercentageCheckMenuItem;
	@FXML private MenuItem idVersionMenuItem;
	@FXML TableView<Country> stateDataTable;
	@FXML TableColumn<Country, String> stateDataTableCountryColumn;
	@FXML TableColumn<Country, Integer> stateDataTablePopulationColumn;
	@FXML TableColumn<Country, Integer> stateDataTableCivFactoryColumn;
	@FXML TableColumn<Country, Integer> stateDataTableMilFactoryColumn;
	@FXML TableColumn<Country, Integer> stateDataTableDockyardsColumn;
	@FXML TableColumn<Country, Integer> stateDataTableAirfieldsColumn;
	@FXML TableColumn<Country, Double> stateDataTableCivMilRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTablePopFactoryRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTablePopCivRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTablePopMilRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTablePopAirCapacityRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTablePopNumStatesRatioColumn;
	@FXML TableColumn<Country, Double> stateDataTableAluminiumColumn;       // todo dont do these yet
	@FXML TableColumn<Country, Double> stateDataTableChromiumColumn;
	@FXML TableColumn<Country, Double> stateDataTableOilColumn;
	@FXML TableColumn<Country, Double> stateDataTableRubberColumn;
	@FXML TableColumn<Country, Double> stateDataTableSteelColumn;
	@FXML TableColumn<Country, Double> stateDataTableTungstenColumn;

	private final ObservableList<Country> countryList;

	public BuildingsByCountryWindow() {
		fxmlResource = "BuildingsByCountryWindow.fxml";
		title = "HOIIVUtils Buildings By Country Window";

		countryList = Country.loadCountries();
	}
	
/* Start */
	@FXML
	void initialize() {
		includeVersion();
		loadBuildingsByCountryTable();
	}

	private void includeVersion() {
		idVersionMenuItem.setText(HOIIVUtils.hoi4utilsVersion);
	}

	private void loadBuildingsByCountryTable() {
		List<Function<Country, ?>> countryDataFunctions = getCountryDataFunctions();
		ObservableList<TableColumn<Country, ?>> tableColumns = stateDataTable.getColumns();

		setStateDataTableCellFactories();

		for (int i = 0; i < countryDataFunctions.size(); i++) {
			TableColumn<Country, ?> tableColumn = tableColumns.get(i);
			Function<Country, ?> dataFunction = countryDataFunctions.get(i);

			tableColumn.setCellValueFactory(countryPropertyCallback(dataFunction));
		}

		stateDataTable.setItems(countryList);       // country objects, cool! and necessary for the cell value factory,
													// this is giving the factories the list of objects to collect
													// their data from.

		if (Settings.DEV_MODE.enabled()) {
			System.out.println("Loaded data of countries into state data table.");
		}
	}

	private void setStateDataTableCellFactories() {
		// table cell factories
		stateDataTableCivMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopFactoryRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopCivRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopAirCapacityRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopNumStatesRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTableAluminiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableChromiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableOilColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableRubberColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableSteelColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableTungstenColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
	}

	private <T> Callback<TableColumn.CellDataFeatures<Country, T>, ObservableValue<T>> countryPropertyCallback(Function<Country, ?> propertyGetter) {
		return cellData -> {
			if (Settings.DEV_MODE.enabled()) {
				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue()));
			}
			return new SimpleObjectProperty<T>((T) propertyGetter.apply(cellData.getValue())); // ? Type safety: Unchecked cast from capture#6-of ? to TJava(16777761)
		};
	}

	private List<Function<Country,?>> getCountryDataFunctions() {
		List<Function<Country, ?>> dataFunctions = new ArrayList<>(18);         // 18 for optimization, limited number of data functions.
		
		dataFunctions.add(Country::name);
		dataFunctions.add(Country::population);
		dataFunctions.add(Country::civilianFactories);
		dataFunctions.add(Country::militaryFactories);
		dataFunctions.add(Country::navalDockyards);
		dataFunctions.add(Country::airfields);
		dataFunctions.add(Country::civMilRatio);
		dataFunctions.add(Country::popPerFactoryRatio);
		dataFunctions.add(Country::popPerCivRatio);
		dataFunctions.add(Country::popPerMilRatio);
		dataFunctions.add(Country::popAirportCapacityRatio);
		dataFunctions.add(Country::popPerStateRatio);
		dataFunctions.add(Country::aluminum);
		dataFunctions.add(Country::chromium);
		dataFunctions.add(Country::oil);
		dataFunctions.add(Country::rubber);
		dataFunctions.add(Country::steel);
		dataFunctions.add(Country::tungsten);

		return dataFunctions;
	}

	@FXML
	public void handleExportToExcelAction() {
		    // Handle exporting to Excel
	}

	public void handlePercentageCheckMenuItemAction() {
        if (idPercentageCheckMenuItem.isSelected()) {
            System.out.println("Percentage values are on");
        } else {
            System.out.println("Percentage values are off");
        }
	}
}
/*	 
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
			public Class<?> getColumnClass(int column) {
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

		// add popup hoi4utils.settings
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
