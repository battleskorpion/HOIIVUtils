package com.hoi4utils.ui.buildings

import com.hoi4utils.hoi4mod.history.countries.CountryFile
import com.hoi4utils.hoi4mod.map.state.State
import com.hoi4utils.ui.custom_javafx.`export`.ExcelExport
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController
import com.hoi4utils.ui.custom_javafx.state.StateTable
import com.hoi4utils.ui.custom_javafx.table.TableViewWindow
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.scene.control.{CheckMenuItem, MenuItem}
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane

import java.awt.*
import java.io.IOException
import javax.swing.*
import scala.compiletime.uninitialized
import scala.jdk.javaapi.CollectionConverters


class CountryBuildingsByStateController extends HOIIVUtilsAbstractController with TableViewWindow with LazyLogging {
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
	private var _country: CountryFile = null
	private var stateList: ObservableList[State] = FXCollections.observableArrayList()

	/**
	 * This constructor is used internally by javafx.
	 * Use {@link # CountryBuildingsByStateController ( )} to create a new instance.
	 * Then call {@link # open ( Object...)} to set the country.
	 *
	 * @param country
	 */
	def this(country: CountryFile) = {
		this()
		setCountry(country)
	}

	@FXML def initialize(): Unit = {
		logger.debug("Country: " + country)

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
				logger.error("Unable to open state file: " + state)
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
		setResourcesPercent(idPercentageCheckMenuItem.isSelected)
		logger.debug(s"Percentage values on: ${idPercentageCheckMenuItem.isSelected}")
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

	def setCountry(country: CountryFile): Unit = {
		this._country = country
		setTitle("HOIIVUtils Buildings By State Window, Country: " + country.name)
		stateList = FXCollections.observableArrayList(CollectionConverters.asJava(State.ownedStatesOfCountry(country)))
		logger.debug("1 - Country: " + country)
	}

	def country: CountryFile = _country

	@FXML def handleSavePDX(): Unit = {
		stateDataTable.getItems.forEach(_.save())
	}
}