package com.HOIIVUtils.ui.buildings;

import com.HOIIVUtils.hoi4utils.HOIIVUtils;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.Country;
import com.HOIIVUtils.hoi4utils.clausewitz_map.state.State;
import com.HOIIVUtils.ui.FXWindow;
import com.HOIIVUtils.ui.javafx.export.ExcelExport;
import com.HOIIVUtils.ui.javafx.table.DoubleTableCell;
import com.HOIIVUtils.ui.javafx.table.IntegerOrPercentTableCell;
import com.HOIIVUtils.ui.javafx.table.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.HOIIVUtils.ui.HOIUtilsWindow;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import java.awt.*;
import java.io.IOException;

public class CountryBuildingsByStateWindow extends HOIUtilsWindow implements TableViewWindow {

	@FXML public Label idVersion;
	@FXML public String className = this.getClass().getName();
	@FXML private CheckMenuItem idPercentageCheckMenuItem;
	@FXML private MenuItem idExportToExcel;
	@FXML TableView<State> stateDataTable;
	@FXML
	TableColumn<State, String> stateDataTableStateColumn;
	@FXML TableColumn<State, Integer> stateDataTablePopulationColumn;
	@FXML TableColumn<State, Integer> stateDataTableCivFactoryColumn;
	@FXML TableColumn<State, Integer> stateDataTableMilFactoryColumn;
	@FXML TableColumn<State, Integer> stateDataTableDockyardsColumn;
	@FXML TableColumn<State, Integer> stateDataTableAirfieldsColumn;
	@FXML TableColumn<State, Double> stateDataTableCivMilRatioColumn;
	@FXML TableColumn<State, Double> stateDataTablePopFactoryRatioColumn;
	@FXML TableColumn<State, Double> stateDataTablePopCivRatioColumn;
	@FXML TableColumn<State, Double> stateDataTablePopMilRatioColumn;
	@FXML TableColumn<State, Double> stateDataTablePopAirCapacityRatioColumn;
	@FXML TableColumn<State, Double> stateDataTableAluminiumColumn;       // todo dont do these yet
	@FXML TableColumn<State, Double> stateDataTableChromiumColumn;
	@FXML TableColumn<State, Double> stateDataTableOilColumn;
	@FXML TableColumn<State, Double> stateDataTableRubberColumn;
	@FXML TableColumn<State, Double> stateDataTableSteelColumn;
	@FXML TableColumn<State, Double> stateDataTableTungstenColumn;

	private Boolean resourcesPercent;
	private final Country country;
	private final ObservableList<State> stateList;

	public CountryBuildingsByStateWindow(Country country) {
		setFxmlResource("CountryBuildingsByStateWindow.fxml");
		setTitle("HOIIVUtils Buildings By State Window, Country: " + country.name());

		this.country = country;
		stateList = FXCollections.observableArrayList(State.ownedStatesOfCountry(country));

		/* action listeners */
		// double click to view state file
		stateDataTable.setOnMouseClicked(event -> {
			if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				viewSelectedStateFile();
			}
		});
	}

	private void viewSelectedStateFile() {
		State state = stateDataTable.getSelectionModel().getSelectedItem();
		if (state == null) {
			return; // no/invalid state selected
		}

		try {
			Desktop.getDesktop().edit(state.getFile());
		} catch (IOException exc) {
			System.err.println("Unable to open state file: " + state);
			throw new RuntimeException(exc);
		}
	}

	@FXML
	void initialize() {
		includeVersion();
		loadTableView(this, stateDataTable, stateList, State.getStateDataFunctions(false));
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}

	private void updateResourcesColumnsPercentBehavior() {
		FXWindow.updateColumnPercentBehavior(stateDataTableAluminiumColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(stateDataTableChromiumColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(stateDataTableOilColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(stateDataTableRubberColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(stateDataTableSteelColumn, resourcesPercent);
		FXWindow.updateColumnPercentBehavior(stateDataTableTungstenColumn, resourcesPercent);
	}

	@Override
	public void setDataTableCellFactories() {
		// table cell factories
		stateDataTableCivMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopFactoryRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopCivRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTablePopAirCapacityRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		stateDataTableAluminiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableChromiumColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableOilColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableRubberColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableSteelColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
		stateDataTableTungstenColumn.setCellFactory(col -> new IntegerOrPercentTableCell<>());
	}

	@FXML
	public void handleExportToExcelAction() {
		ExcelExport<State> excelExport = new ExcelExport<>();
		excelExport.export(stateDataTable);
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

	public void setResourcesPercent(Boolean resourcesPercent) {
		this.resourcesPercent = resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}

	public void toggleResourcesPercent() {
		resourcesPercent = !resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}
}

	/*
	private JPanel CountryBuildingsByStateWindowJPanel;
	private JTable statesBuildingsTable;
	private JLabel countryLabel;
	private DefaultTableModel stateBuildingsTableModel;
	private CountryTag countryTag;

	// popup menu
	JPopupMenu popupSettings = new JPopupMenu();
	JCheckBoxMenuItem aluminumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Aluminum as Percent");
	JCheckBoxMenuItem chromiumDisplayAsPercentOption = new JCheckBoxMenuItem("Display Chromium as Percent ");
	JCheckBoxMenuItem oilDisplayAsPercentOption = new JCheckBoxMenuItem("Display Oil as Percent ");
	JCheckBoxMenuItem rubberDisplayAsPercentOption = new JCheckBoxMenuItem("Display Rubber as Percent ");
	JCheckBoxMenuItem steelDisplayAsPercentOption = new JCheckBoxMenuItem("Display Steel as Percent ");
	JCheckBoxMenuItem tungstenDisplayAsPercentOption = new JCheckBoxMenuItem("Display Tungsten as Percent ");


	public CountryBuildingsByStateWindow(CountryTag tag) {
		super("Buildings by State");

		if (tag == null) {
			throw new IllegalArgumentException("Null country tag");
		}

		// init special
		this.countryTag = tag;

		// table model
		stateBuildingsTableModel = new DefaultTableModel() {
			@Override
			public int getRowCount() {
				return State.numStates(countryTag);
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
						return String.class;	// state name
					case 1, 2, 3, 4, 5:
						return Integer.class;
					case 6, 7, 8, 9, 10, 11:
						return Double.class;
					default:
						return Integer.class;
				}
			}
		};
		String[] columns = {"State", "Population", "Civilian Factories", "Military Factories", "Dockyards", "Airfields",
				"Civ/Mil Ratio", "Pop / Factory", "Pop / Civ Ratio", "Pop / Mil Ratio", "Pop / Air Capacity", "Pop / State",
				"Aluminum", "Chromium", "Oil", "Rubber", "Steel", "Tungsten"};
		stateBuildingsTableModel.setColumnIdentifiers(columns);
		statesBuildingsTable.setModel(stateBuildingsTableModel);

		// row sorter
		statesBuildingsTable.setAutoCreateRowSorter(true);

		// add popup hoi4utils.settings
		popupSettings.add(aluminumDisplayAsPercentOption);
		popupSettings.add(chromiumDisplayAsPercentOption);
		popupSettings.add(oilDisplayAsPercentOption);
		popupSettings.add(rubberDisplayAsPercentOption);
		popupSettings.add(steelDisplayAsPercentOption);
		popupSettings.add(tungstenDisplayAsPercentOption);

		// table renderer (formatting)
		statesBuildingsTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = NumberFormat.getInstance();

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

				Integer value = (Integer) aValue;
				try {
					super.setValue(numberFormat.format(value));
				} catch (IllegalArgumentException exc) {
					exc.printStackTrace();
					System.err.println("\t" + "Object: " + aValue);
				}
			}
		});
		statesBuildingsTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

				Double value = (Double) aValue;
				if (value.equals(Double.NaN)) {
					super.setValue("N/A");
				}
				else {
					super.setValue(numberFormat.format(value));
				}
			}
		});
		statesBuildingsTable.setDefaultRenderer(Float.class, new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = NumberFormat.getPercentInstance();

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

				Float value = (Float) aValue;
				super.setValue(numberFormat.format(value));
			}
		});

		// option action listeners
		aluminumDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		chromiumDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		rubberDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		oilDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		steelDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		tungstenDisplayAsPercentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshStateBuildingsTable();
			}
		});
		statesBuildingsTable.addMouseListener( new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
//				logger.log("pressed");
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
		refreshStateBuildingsTable();
		countryLabel.setText(countryTag.toString());

		setContentPane(CountryBuildingsByStateWindowJPanel);
		setSize(700, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();

		/* file listener
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
						refreshStateBuildingsTable();
					} else {
						logger.log("Warning: refresh table not performed");
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
						refreshStateBuildingsTable();
					} else {
						logger.log("Warning: refresh table not performed");
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
						refreshStateBuildingsTable();
					} else {
						logger.log("Warning: refresh table not performed");
					}
				});
			}
		}).watch();

		/* action listeners
		statesBuildingsTable.addMouseListener(new MouseAdapter() {
			/**
			 * {@inheritDoc}
			 *
			 * @param e
			 *//*
			@Override
			public void mouseClicked(MouseEvent e) {
				//super.mouseClicked(e);
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
				} else {
					return;
				}

				// get state
				int row = statesBuildingsTable.rowAtPoint( e.getPoint() );
				int modelRow = HOIIVUtils.rowToModelIndex(statesBuildingsTable, row);
				String state_name = (String) stateBuildingsTableModel.getValueAt(modelRow, 0);	 // column 0 - country name

				State state = State.get(state_name);
				if (state == null) {
					throw new NullPointerException();
				}

				try {
					Desktop.getDesktop().edit(state.getFile());
				} catch (IOException ex) {
					System.err.println("Unable to open state file: " + state);
					throw new RuntimeException(ex);
				}
			}
		});
	}

	private void refreshStateBuildingsTable() {
		ArrayList<State> stateList = State.ownedStatesOfCountry(countryTag);
		logger.log("Number of states in " + countryTag + ": " + stateList.size());

		stateBuildingsTableModel.getDataVector().removeAllElements();
		stateBuildingsTableModel.setRowCount(stateList.size());
		stateBuildingsTableModel.setColumnCount(18);
		stateBuildingsTableModel.fireTableDataChanged();

		for (int i = 0; i < stateList.size(); i++) {
			State state = stateList.get(i);
			Infrastructure infrastructure = state.getStateInfrastructure();
			Resources resources = state.getResources();
			Resources resourcesAll = State.resourcesOfStates(); // global
// stats
			stateBuildingsTableModel.setValueAt(state.toString(), i, 0);
			stateBuildingsTableModel.setValueAt(infrastructure.population(), i, 1);
			stateBuildingsTableModel.setValueAt(infrastructure.civilianFactories(), i, 2);
			stateBuildingsTableModel.setValueAt(infrastructure.militaryFactories(), i, 3);
			stateBuildingsTableModel.setValueAt(infrastructure.navalDockyards(), i, 4);
			stateBuildingsTableModel.setValueAt(infrastructure.airfields(), i, 5);

			// percentages
			stateBuildingsTableModel.setValueAt((double)infrastructure.civilianFactories() / infrastructure.militaryFactories(), i, 6);
			stateBuildingsTableModel.setValueAt((double)infrastructure.population() / (infrastructure.civilianFactories() + infrastructure.militaryFactories()), i, 7);
			stateBuildingsTableModel.setValueAt((double)infrastructure.population() / infrastructure.civilianFactories(), i, 8);
			stateBuildingsTableModel.setValueAt((double)infrastructure.population() / infrastructure.militaryFactories(), i, 9);
			stateBuildingsTableModel.setValueAt((double)infrastructure.population() / (infrastructure.airfields() * 200), i, 10);
			stateBuildingsTableModel.setValueAt((double)infrastructure.population() / State.numStates(countryTag), i, 11);

			// resources
			if (aluminumDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.aluminum() / resourcesAll.aluminum(), i, 12);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.aluminum(), i, 12);
			}
			if (chromiumDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.chromium() / resourcesAll.chromium(), i, 13);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.chromium(), i, 13);
			}
			if (oilDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.oil() / resourcesAll.oil(), i, 14);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.oil(), i, 14);
			}
			if (rubberDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.rubber() / resourcesAll.rubber(), i, 15);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.rubber(), i, 15);
			}
			if (steelDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.steel() / resourcesAll.steel(), i, 16);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.steel(), i, 16);
			}
			if (tungstenDisplayAsPercentOption.isSelected()) {
				stateBuildingsTableModel.setValueAt((double) resources.tungsten() / resourcesAll.tungsten(), i, 17);
			} else {
				stateBuildingsTableModel.setValueAt((double) resources.tungsten(), i, 17);
			}
		}

		// table mouse listener


		// cell renderers
		TableColumn tableColumn = statesBuildingsTable.getColumnModel().getColumn(6);
		tableColumn.setCellRenderer(new CellColorRenderer(0.5, 3) {
			private NumberFormat numberFormat = DecimalFormat.getInstance();

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

				Double value = (Double) aValue;
				super.setValue(numberFormat.format(value));
			}
		});

		TableColumn tableColumn2 = statesBuildingsTable.getColumnModel().getColumn(12);
		tableColumn2.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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
		TableColumn tableColumn3 = statesBuildingsTable.getColumnModel().getColumn(13);
		tableColumn3.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");


			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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
		TableColumn tableColumn4 = statesBuildingsTable.getColumnModel().getColumn(14);
		tableColumn4.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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
		TableColumn tableColumn5 = statesBuildingsTable.getColumnModel().getColumn(15);
		tableColumn5.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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
		TableColumn tableColumn6 = statesBuildingsTable.getColumnModel().getColumn(16);
		tableColumn6.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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
		TableColumn tableColumn7 = statesBuildingsTable.getColumnModel().getColumn(17);
		tableColumn7.setCellRenderer(new DefaultTableCellRenderer() {
			private NumberFormat numberFormat = DecimalFormat.getInstance();
			private NumberFormat nfPercent = new DecimalFormat(" #,##0.#%");

			@Override
			protected void setValue(Object aValue) {
				if (aValue == null) {
					super.setValue("null");
					return;
				}

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

//			if (table.getValueAt(row, column) != null && (Double) table.getValueAt(row, column) < min){
//				cellComponent.setBackground(Color.YELLOW);
//			} else if (table.getValueAt(row, column) != null && (Double) table.getValueAt(row, column) >= max){
//				cellComponent.setBackground(Color.CYAN);
//			}
//			else {
//				cellComponent.setBackground(Color.WHITE);
//			}
			return cellComponent;
		}
	}

}
*/