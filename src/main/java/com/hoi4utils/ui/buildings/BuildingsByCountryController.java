package com.hoi4utils.ui.buildings;


import com.hoi4utils.ClausewitzDate;
import com.hoi4utils.HOIIVUtils;
import com.hoi4utils.hoi4.country.CountryFile;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.custom_javafx.export.ExcelExport;
import com.hoi4utils.ui.custom_javafx.table.DoubleOrPercentTableCell;
import com.hoi4utils.ui.custom_javafx.table.DoubleTableCell;
import com.hoi4utils.ui.custom_javafx.table.TableViewWindow;
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

public class BuildingsByCountryController extends HOIIVUtilsAbstractController implements TableViewWindow {

	@FXML
	private MenuItem idExportToExcel;
	@FXML
	private CheckMenuItem idPercentageCheckMenuItem;
	@FXML
	private MenuItem idVersionMenuItem;
	@FXML
	TableView<CountryFile> countryDataTable;
	@FXML
	TableColumn<CountryFile, String> countryDataTableCountryColumn;
	@FXML
	TableColumn<CountryFile, Integer> countryDataTablePopulationColumn;
	@FXML
	TableColumn<CountryFile, Integer> countryDataTableCivFactoryColumn;
	@FXML
	TableColumn<CountryFile, Integer> countryDataTableMilFactoryColumn;
	@FXML
	TableColumn<CountryFile, Integer> countryDataTableDockyardsColumn;
	@FXML
	TableColumn<CountryFile, Integer> countryDataTableAirfieldsColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableCivMilRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTablePopFactoryRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTablePopCivRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTablePopMilRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTablePopAirCapacityRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTablePopNumStatesRatioColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableAluminiumColumn; // todo dont do these yet
	@FXML
	TableColumn<CountryFile, Double> countryDataTableChromiumColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableOilColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableRubberColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableSteelColumn;
	@FXML
	TableColumn<CountryFile, Double> countryDataTableTungstenColumn;

	private boolean resourcesPercent = false;
	private final ClausewitzDate date = ClausewitzDate.defaulty();

	private final ObservableList<CountryFile> countryList;

	public BuildingsByCountryController() {
		setFxmlResource("BuildingsByCountry.fxml");
		setTitle("HOIIVUtils Buildings By Country Window");

		countryList = FXCollections.observableArrayList(CollectionConverters.asJava(CountryFile.list()));
		System.out.println("Countries loaded: " + countryList.size());
	}

	@FXML
	void initialize() {
		includeVersion();
		loadTableView(this, countryDataTable, countryList, CountryFile.getDataFunctions(resourcesPercent));

		/* action listeners */
		countryDataTable.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				viewCountryBuildingsByState();
			}
		});
		JOptionPane.showMessageDialog(null, "dev - loaded rows: " + countryDataTable.getItems().size());
	}

	private void includeVersion() {
		idVersionMenuItem.setText(HOIIVUtils.get("version"));
	}

	// todo put this in hoi4window parent class or whatever

	private void updateResourcesColumnsPercentBehavior() {
		loadTableView(this, countryDataTable, countryList, CountryFile.getDataFunctions(resourcesPercent));
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableAluminiumColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableChromiumColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableOilColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableRubberColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableSteelColumn, resourcesPercent);
		JavaFXUIManager.updateColumnPercentBehavior(countryDataTableTungstenColumn, resourcesPercent);
	}

	public void setDataTableCellFactories() {
		// table cell factories
		// todo these should also consider column percent behavior (percenttablecell)
		countryDataTableCivMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopFactoryRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopCivRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopAirCapacityRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTablePopNumStatesRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
		countryDataTableAluminiumColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
		countryDataTableChromiumColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
		countryDataTableOilColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
		countryDataTableRubberColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
		countryDataTableSteelColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
		countryDataTableTungstenColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
	}

	@FXML
	private void handleExportToExcelAction() {
		ExcelExport<CountryFile> excelExport = new ExcelExport<>();
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

	public void setResourcesPercent(boolean resourcesPercent) {
		this.resourcesPercent = resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}

	public void toggleResourcesPercent() {
		resourcesPercent = !resourcesPercent;
		updateResourcesColumnsPercentBehavior();
	}

	public void viewCountryBuildingsByState() {
		CountryFile country = countryDataTable.getSelectionModel().getSelectedItem();
		if (country == null) {
			return;
		}
		CountryBuildingsByStateController window = new CountryBuildingsByStateController();
		window.open(country);
	}

}