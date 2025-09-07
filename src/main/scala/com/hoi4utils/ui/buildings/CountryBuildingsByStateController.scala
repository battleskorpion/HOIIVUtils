package com.hoi4utils.ui.buildings

import com.hoi4utils.hoi4.country.Country
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.custom_javafx.`export`.ExcelExport
import com.hoi4utils.ui.custom_javafx.state.StateTable
import com.hoi4utils.ui.custom_javafx.table.TableViewWindow
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuItem
import javafx.scene.input.MouseButton
import map.State
import javafx.scene.layout.AnchorPane

import scala.jdk.javaapi.CollectionConverters
import javax.swing.*
import java.awt.*
import java.io.IOException
import scala.compiletime.uninitialized


class CountryBuildingsByStateController extends HOIIVUtilsAbstractController with TableViewWindow {
	setFxmlResource("CountryBuildingsByState.fxml")
	setTitle("HOIIVUtils Buildings By State Window")

	// @FXML public Label idVersion;
	@FXML private var className: String = this.getClass.getName
	@FXML private var idPercentageCheckMenuItem: CheckMenuItem = uninitialized
	@FXML private var idExportToExcel: MenuItem = uninitialized
	@FXML private var saveButton: Button = uninitialized
	@FXML private var stateTableAnchorPane: AnchorPane = uninitialized

	private var stateDataTable: StateTable = uninitialized

	private var resourcesPercent = false
	private var _country: Country = null
	private var stateList: ObservableList[State] = FXCollections.observableArrayList()

	/**
	 * This constructor is used internally by javafx.
	 * Use {@link # CountryBuildingsByStateController ( )} to create a new instance.
	 * Then call {@link # open ( Object...)} to set the country.
	 *
	 * @param country
	 */
	def this(country: Country) = {
		this()
		setCountry(country)
	}

	@FXML def initialize(): Unit = {
		System.out.println("Country: " + country)

		/* state data table */
		stateDataTable = new StateTable()
		AnchorPane.setTopAnchor(stateDataTable, 0.0)
		AnchorPane.setBottomAnchor(stateDataTable, 30.0)
		AnchorPane.setLeftAnchor(stateDataTable, 0.0)
		AnchorPane.setRightAnchor(stateDataTable, 0.0)
		stateTableAnchorPane.getChildren.add(stateDataTable)

		// includeVersion();
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent))
		JOptionPane.showMessageDialog(null, "dev - loaded rows: " + stateDataTable.getItems.size)
		/* action listeners */
		// double click to view state file
		stateDataTable.setOnMouseClicked((event) => {
			// todo init stateDataTable in initialization() or whatever it is :)
			if (event.getButton.equals(MouseButton.PRIMARY) && (event.getClickCount eq 2)) viewSelectedStateFile()

		})
		stateDataTable.setEditableColumns(true)
	}

	private def viewSelectedStateFile(): Unit = {
		val state = stateDataTable.getSelectionModel.getSelectedItem
		if (state == null) return // no/invalid state selected
		try if (state.stateFile.isDefined) Desktop.getDesktop.edit(state.stateFile.get)
		catch {
			case exc: IOException =>
				System.err.println("Unable to open state file: " + state)
				throw new RuntimeException(exc)
		}
	}

	// private void includeVersion() {
	// idVersion.setText(HOIIVUtils.get("version").toString());
	// }
	override def setDataTableCellFactories(): Unit = {
		//stateDataTable.setDataTableCellFactories()
	}

	@FXML def handleExportToExcelAction(): Unit = {
		val excelExport = new ExcelExport[State]
		excelExport.`export`(stateDataTable)
	}

	def handlePercentageCheckMenuItemAction(): Unit = {
		if (idPercentageCheckMenuItem.isSelected) {
			setResourcesPercent(true)
			System.out.println("Percentage values are on")
		}
		else {
			setResourcesPercent(false)
			System.out.println("Percentage values are off")
		}
	}

	def setResourcesPercent(resourcesPercent: Boolean): Unit = {
		this.resourcesPercent = resourcesPercent
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent))
		stateDataTable.updateResourcesColumnsPercentBehavior(resourcesPercent)
	}

	def toggleResourcesPercent(): Unit = {
		resourcesPercent = !resourcesPercent
		loadTableView(this, stateDataTable, stateList, State.getDataFunctions(resourcesPercent))
		stateDataTable.updateResourcesColumnsPercentBehavior(resourcesPercent)
	}

	def setCountry(country: Country): Unit = {
		this._country = country
		setTitle("HOIIVUtils Buildings By State Window, Country: " + country.name)
		stateList = FXCollections.observableArrayList(CollectionConverters.asJava(State.ownedStatesOfCountry(country)))
		System.out.println("1 - Country: " + country)
	}

	def country: Country = _country

	@FXML def handleSavePDX(): Unit = {
		stateDataTable.getItems.forEach(_.save())
	}
}