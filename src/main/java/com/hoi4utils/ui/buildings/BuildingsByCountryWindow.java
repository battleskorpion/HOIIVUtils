package com.hoi4utils.ui.buildings;

import com.hoi4utils.Settings;
import com.hoi4utils.clausewitz.code.ClausewitzDate;
import com.hoi4utils.ui.javafx.export.ExcelExport;
import com.hoi4utils.ui.javafx.table.DoubleTableCell;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.Country;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.hoi4utils.ui.FXWindow;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import com.hoi4utils.ui.javafx.table.IntegerOrPercentTableCell;
import com.hoi4utils.ui.javafx.table.TableViewWindow;
import javafx.scene.input.MouseButton;

import javax.swing.*;

public class BuildingsByCountryWindow extends HOIIVUtilsWindow implements TableViewWindow {

	@FXML
	private MenuItem idExportToExcel;
	@FXML
	private CheckMenuItem idPercentageCheckMenuItem;
	@FXML
	private MenuItem idVersionMenuItem;
	@FXML
	TableView<Country> countryDataTable;
	@FXML
	TableColumn<Country, String> countryDataTableCountryColumn;
	@FXML
	TableColumn<Country, Integer> countryDataTablePopulationColumn;
	@FXML
	TableColumn<Country, Integer> countryDataTableCivFactoryColumn;
	@FXML
	TableColumn<Country, Integer> countryDataTableMilFactoryColumn;
	@FXML
	TableColumn<Country, Integer> countryDataTableDockyardsColumn;
	@FXML
	TableColumn<Country, Integer> countryDataTableAirfieldsColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableCivMilRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTablePopFactoryRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTablePopCivRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTablePopMilRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTablePopAirCapacityRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTablePopNumStatesRatioColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableAluminiumColumn; // todo dont do these yet
	@FXML
	TableColumn<Country, Double> countryDataTableChromiumColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableOilColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableRubberColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableSteelColumn;
	@FXML
	TableColumn<Country, Double> countryDataTableTungstenColumn;

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
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
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
	private void handleExportToExcelAction() {
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