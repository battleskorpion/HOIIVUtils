package com.hoi4utils.ui.buildings;


import com.hoi4utils.hoi4.country.Country;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.javafx_ui.ExcelExport;
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import com.map.State;
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
	StateTable stateDataTable;

	private boolean resourcesPercent;
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
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent));

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
			if (state.stateFile().isDefined()) {
				Desktop.getDesktop().edit(state.stateFile().get());
			}
		} catch (IOException exc) {
			System.err.println("Unable to open state file: " + state);
			throw new RuntimeException(exc);
		}
	}

	// private void includeVersion() {
	// idVersion.setText(HOIIVUtils.get("version").toString());
	// }

	@Override
	public void setDataTableCellFactories() {
		stateDataTable.setDataTableCellFactories();
	}

	@FXML
	public void handleExportToExcelAction() {
		ExcelExport<State> excelExport = new ExcelExport<>();
		excelExport.hExport(stateDataTable);
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
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent));
		stateDataTable.updateResourcesColumnsPercentBehavior(resourcesPercent);
	}

	public void toggleResourcesPercent() {
		resourcesPercent = !resourcesPercent;
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent));
		stateDataTable.updateResourcesColumnsPercentBehavior(resourcesPercent);
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