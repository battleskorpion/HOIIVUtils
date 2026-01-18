package com.hoi4utils.ui.countries

import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.history.countries.service.CountryService
import com.hoi4utils.hoi4.map.state.{State, StateService}
import com.hoi4utils.main.{HOIIVUtils, HOIIVUtilsConfig}
import com.hoi4utils.parser.ClausewitzDate
import com.hoi4utils.ui.javafx.application.{HOIIVUtilsAbstractController2, JavaFXUIManager}
import com.hoi4utils.ui.javafx.scene.control.*
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.{AnchorPane, VBox}
import zio.ZIO

import javax.swing.JOptionPane
import scala.compiletime.uninitialized
import scala.jdk.javaapi.CollectionConverters

class BuildingsByCountryController2 extends HOIIVUtilsAbstractController2 with TableViewWindow2 with LazyLogging:
  setFxmlFile("/com/hoi4utils/ui/countries/BuildingsByCountry.fxml")
  setTitle("HOIIVUtils Buildings By Country Window")

  @FXML private var bbccRoot: AnchorPane = uninitialized
  @FXML private var toolBar: ToolBar = uninitialized
  @FXML private var toolBar2: ToolBar = uninitialized
  @FXML private var exportToExcelButton: Button = uninitialized
  @FXML private var percentageCheckBox: CheckBox = uninitialized
  @FXML private var versionLabel: Label = uninitialized
  @FXML private var mainSplitPane: SplitPane = uninitialized
  @FXML private var detailsPane: VBox = uninitialized
  @FXML private var countryDataTable: TableView[CountryFile] = uninitialized
  @FXML private var saveStateButton: Button = uninitialized
  @FXML private var closeDetailsButton: Button = uninitialized
  @FXML private var statePercentageCheckBox: CheckBox = uninitialized
  @FXML private var stateTableScrollPane: ScrollPane = uninitialized
  @FXML private var stateDataTablePlaceholder: TableView[_] = uninitialized
  @FXML private var countryDataTableCountryColumn: TableColumn[CountryFile, String] = uninitialized
  @FXML private var countryDataTablePopulationColumn: TableColumn[CountryFile, Integer] = uninitialized
  @FXML private var countryDataTableCivFactoryColumn: TableColumn[CountryFile, Integer] = uninitialized
  @FXML private var countryDataTableMilFactoryColumn: TableColumn[CountryFile, Integer] = uninitialized
  @FXML private var countryDataTableDockyardsColumn: TableColumn[CountryFile, Integer] = uninitialized
  @FXML private var countryDataTableAirfieldsColumn: TableColumn[CountryFile, Integer] = uninitialized
  @FXML private var countryDataTableCivMilRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTablePopFactoryRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTablePopCivRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTablePopMilRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTablePopAirCapacityRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTablePopNumStatesRatioColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableAluminiumColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableChromiumColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableOilColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableRubberColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableSteelColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized
  @FXML private var countryDataTableTungstenColumn: TableColumn[CountryFile, java.lang.Double] = uninitialized

  private var _resourcesPercent = false
  private var _stateResourcesPercent = false
  private val date = ClausewitzDate.defaulty
  private var savedDividerPosition = 0.5
  private var stateDataTable: StateTable = uninitialized
  private var stateList: ObservableList[State] = FXCollections.observableArrayList()
  private var selectedCountry: CountryFile = uninitialized

  private val countryList: ObservableList[CountryFile] = {
    val countryService: CountryService = zio.Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[CountryService]).getOrThrowFiberFailure()
    }
    FXCollections.observableArrayList(CollectionConverters.asJava(countryService.list))
  }

  // Constructor initialization
  logger.info(s"Countries loaded: ${countryList.size()}")

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    versionLabel.setText(s"Version: ${HOIIVUtilsConfig.get("version")}")
    val dataFunctions = zio.Unsafe.unsafe(implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(CountryFile.getDataFunctions(_resourcesPercent)).getOrThrowFiberFailure()
    )
    loadTableView(this, countryDataTable, countryList, dataFunctions)

    closeDetailsPane()

    /* action listeners */
    countryDataTable.setOnMouseClicked: event =>
      if event.getButton.equals(MouseButton.PRIMARY) && event.getClickCount == 2 then
        viewCountryBuildingsByState()

  override def preSetup(): Unit = setupWindowControls(bbccRoot, toolBar, toolBar2)

  private def updateResourcesColumnsPercentBehavior(): Unit =
    val dataFunctions = zio.Unsafe.unsafe(implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(CountryFile.getDataFunctions(_resourcesPercent)).getOrThrowFiberFailure()
    )
    loadTableView(this, countryDataTable, countryList, dataFunctions)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableAluminiumColumn, _resourcesPercent)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableChromiumColumn, _resourcesPercent)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableOilColumn, _resourcesPercent)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableRubberColumn, _resourcesPercent)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableSteelColumn, _resourcesPercent)
    JavaFXUIManager.updateColumnPercentBehavior(countryDataTableTungstenColumn, _resourcesPercent)

  override def setDataTableCellFactories(): Unit =
    // table cell factories
    // todo these should also consider column percent behavior (percenttablecell)
    countryDataTableCivMilRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTablePopFactoryRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTablePopCivRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTablePopMilRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTablePopAirCapacityRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTablePopNumStatesRatioColumn.setCellFactory(_ => new DoubleTableCell[CountryFile])
    countryDataTableAluminiumColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])
    countryDataTableChromiumColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])
    countryDataTableOilColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])
    countryDataTableRubberColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])
    countryDataTableSteelColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])
    countryDataTableTungstenColumn.setCellFactory(_ => new DoubleOrPercentTableCell[CountryFile])

  @FXML def handleExportToExcelAction(): Unit =
    val excelExport = new ExcelExport[CountryFile]
    excelExport.`export`(countryDataTable)

  @FXML def handlePercentageCheckBoxAction(): Unit =
    if percentageCheckBox.isSelected then
      setResourcesPercent(true)
      logger.info("Percentage values are on")
    else
      setResourcesPercent(false)
      logger.info("Percentage values are off")

  def resourcesPercent(): Boolean = _resourcesPercent

  def setResourcesPercent(resourcesPercent: Boolean): Unit =
    this._resourcesPercent = resourcesPercent
    updateResourcesColumnsPercentBehavior()

  def toggleResourcesPercent(): Unit =
    _resourcesPercent = !_resourcesPercent
    updateResourcesColumnsPercentBehavior()

  def viewCountryBuildingsByState(): Unit =
    val country = countryDataTable.getSelectionModel.getSelectedItem
    if country == null then return

    if stateDataTable == null then
      stateDataTable = new StateTable()
      stateTableScrollPane.setContent(stateDataTable)
      stateDataTable.setEditableColumns(true)

    selectedCountry = country
    val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
    }
    stateList = FXCollections.observableArrayList(CollectionConverters.asJava(stateService.ownedStatesOfCountry(country)))
    loadTableView(this, stateDataTable, stateList, stateService.getDataFunctions(_stateResourcesPercent))

    openDetailsPane()

  @FXML def handleSaveStates(): Unit =
    if stateDataTable != null then
      stateDataTable.getItems.forEach(_.save())
      logger.info("States saved successfully")

  @FXML def handleCloseDetails(): Unit =
    closeDetailsPane()

  @FXML def handleStatePercentageCheckBoxAction(): Unit =
    if statePercentageCheckBox.isSelected then
      setStateResourcesPercent(true)
      logger.info("State percentage values are on")
    else
      setStateResourcesPercent(false)
      logger.info("State percentage values are off")

  def setStateResourcesPercent(stateResourcesPercent: Boolean): Unit =
    this._stateResourcesPercent = stateResourcesPercent
    if stateDataTable != null && stateList != null then
      val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
        HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
      }
      loadTableView(this, stateDataTable, stateList, stateService.getDataFunctions(_stateResourcesPercent))
      stateDataTable.updateResourcesColumnsPercentBehavior(_stateResourcesPercent)

  /**
   * Closes the bottom details pane and maximizes the table view
   */
  def closeDetailsPane(): Unit =
    if mainSplitPane.getItems.contains(detailsPane) then
      if mainSplitPane.getDividerPositions.length > 0 then
        savedDividerPosition = mainSplitPane.getDividerPositions()(0)
      mainSplitPane.getItems.remove(detailsPane)

  /**
   * Opens the bottom details pane and restores the previous divider position
   */
  def openDetailsPane(): Unit =
    if !mainSplitPane.getItems.contains(detailsPane) then
      mainSplitPane.getItems.add(detailsPane)
      // Need to delay the divider positioning to allow the pane to render
      javafx.application.Platform.runLater(() => mainSplitPane.setDividerPositions(savedDividerPosition))
