package com.hoi4utils.ui.buildings;


import com.hoi4utils.clausewitz.data.country.Country;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.javafx.export.ExcelExport;
import com.hoi4utils.ui.javafx.table.DoubleTableCell;
import com.hoi4utils.ui.javafx.table.IntegerOrPercentTableCell;
import com.hoi4utils.ui.javafx.table.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CountryBuildingsByStateController extends HOIIVUtilsAbstractController implements TableViewWindow {

	// @FXML public Label idVersion;
	@FXML
	public String className = this.getClass().getName();
	@FXML
	private CheckMenuItem idPercentageCheckMenuItem;
	@FXML
	private MenuItem idExportToExcel;
	@FXML
	TableView<State> stateDataTable;
	@FXML
	TableColumn<State, String> stateDataTableStateColumn;
	@FXML
	TableColumn<State, Integer> stateDataTablePopulationColumn;
	@FXML
	TableColumn<State, Integer> stateDataTableCivFactoryColumn;
	@FXML
	TableColumn<State, Integer> stateDataTableMilFactoryColumn;
	@FXML
	TableColumn<State, Integer> stateDataTableDockyardsColumn;
	@FXML
	TableColumn<State, Integer> stateDataTableAirfieldsColumn;
	@FXML
	TableColumn<State, Double> stateDataTableCivMilRatioColumn;
	@FXML
	TableColumn<State, Double> stateDataTablePopFactoryRatioColumn;
	@FXML
	TableColumn<State, Double> stateDataTablePopCivRatioColumn;
	@FXML
	TableColumn<State, Double> stateDataTablePopMilRatioColumn;
	@FXML
	TableColumn<State, Double> stateDataTablePopAirCapacityRatioColumn;
	@FXML
	TableColumn<State, Double> stateDataTableAluminiumColumn; // todo dont do these yet
	@FXML
	TableColumn<State, Double> stateDataTableChromiumColumn;
	@FXML
	TableColumn<State, Double> stateDataTableOilColumn;
	@FXML
	TableColumn<State, Double> stateDataTableRubberColumn;
	@FXML
	TableColumn<State, Double> stateDataTableSteelColumn;
	@FXML
	TableColumn<State, Double> stateDataTableTungstenColumn;

	private Boolean resourcesPercent;
	private Country country;
	private ObservableList<State> stateList;

	public CountryBuildingsByStateController() {
		setFxmlResource("CountryBuildingsByState.fxml");
		setTitle("HOIIVUtils Buildings By State Window");
	}

	/**
	 * This constructor is used internally by javafx.
	 * Use {@link #CountryBuildingsByStateController()} to create a new instance.
	 * Then call {@link #open(Object...)} to set the country.
	 *
	 * @param country
	 */
	public CountryBuildingsByStateController(Country country) {
		setCountry(country);
	}

	@FXML
	void initialize() {
		System.out.println("Country: " + country);
		// includeVersion();
		loadTableView(this, stateDataTable, stateList, State.getStateDataFunctions(false));

		JOptionPane.showMessageDialog(null, "dev - loaded rows: " + stateDataTable.getItems().size());

		/* action listeners */
		// double click to view state file
		stateDataTable.setOnMouseClicked(event -> { // todo init stateDataTable in initialization() or whatever it is :)
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
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

	// private void includeVersion() {
	// idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	// }

	private void updateResourcesColumnsPercentBehavior() {
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableAluminiumColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableChromiumColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableOilColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableRubberColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableSteelColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(stateDataTableTungstenColumn, resourcesPercent);
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

	public void setCountry(Country country) {
		this.country = country;

		setTitle("HOIIVUtils Buildings By State Window, Country: " + country.name());
		stateList = FXCollections.observableArrayList(CollectionConverters.asJava(State.ownedStatesOfCountry(country)));
		System.out.println("1 - Country: " + country);
	}

	public Country country() {
		return country;
	}
}