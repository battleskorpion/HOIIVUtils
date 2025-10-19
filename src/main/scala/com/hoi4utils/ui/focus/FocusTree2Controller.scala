package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.FocusTreeFile
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.custom_javafx.layout.ZoomableScrollPane
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")
  private var focusGridColumns: Int = 0
  private var focusGridRows: Int = 0
  private val focusGridColumnsSize: Int = 100
  private val focusGridRowSize: Int = 200
  private var lines: Boolean = true
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"

  @FXML var focusTree2: VBox = uninitialized
  @FXML var menuBar: MenuBar = uninitialized
  @FXML var focusTreeView: GridPane = uninitialized
  @FXML var mClose: Button = uninitialized
  @FXML var mSquare: Button = uninitialized
  @FXML var mMinimize: Button = uninitialized
  @FXML var focusSelection: ScrollPane = uninitialized
  @FXML var focusTreeScrollPane: ScrollPane = uninitialized
  @FXML var splitPane: SplitPane = uninitialized
  @FXML var vbox: VBox = uninitialized
  @FXML var welcome: ToggleButton = uninitialized
  @FXML var toggleGroup: ToggleGroup = uninitialized
  @FXML var progressIndicator: ProgressIndicator = uninitialized
  @FXML var gridlines: ToggleButton = uninitialized
  @FXML var zoomInButton: Button = uninitialized
  @FXML var zoomOutButton: Button = uninitialized
  @FXML var resetZoomButton: Button = uninitialized

  private var toggleButtons: ListBuffer[ToggleButton] = ListBuffer.empty
  private var zoomableScrollPane: ZoomableScrollPane = uninitialized

  // Track the current running task
  private var currentLoadTask: Option[Task[GridPane]] = None

  @FXML def initialize(): Unit =
    // Replace the regular ScrollPane with ZoomableScrollPane
    replaceWithZoomableScrollPane()

    // Setup zoom buttons if they exist
    setupZoomButtons()
    clear()
    welcome.setToggleGroup(toggleGroup)
    welcome.fire()
    populateFocusSelection()
    Platform.runLater(() => if progressIndicator != null then progressIndicator.setVisible(false))

  override def preSetup(): Unit = setupWindowControls(focusTree2, mClose, mSquare, mMinimize, menuBar)

  private def replaceWithZoomableScrollPane(): Unit =
    // Remove the GridPane from the original ScrollPane first
    focusTreeScrollPane.setContent(null)

    // Create the ZoomableScrollPane with the GridPane as target
    zoomableScrollPane = ZoomableScrollPane(focusTreeView)

    // Copy properties from the original ScrollPane
    zoomableScrollPane.setPrefHeight(focusTreeScrollPane.getPrefHeight)
    zoomableScrollPane.setPrefWidth(focusTreeScrollPane.getPrefWidth)

    // Replace in SplitPane
    if splitPane != null then
      val items = splitPane.getItems
      val index = items.indexOf(focusTreeScrollPane)
      if index >= 0 then
        items.set(index, zoomableScrollPane)
      else
        logger.error("Could not find focusTreeScrollPane in SplitPane items")
    else
      logger.error("splitPane is null - check FXML fx:id")

  private def populateFocusSelection(): Unit =
    Some(FocusTreeFile.observeFocusTrees).foreach(trees =>
      trees.forEach(someFocusTree =>
        val toggleButton = ToggleButton(someFocusTree.toString)
        toggleButtons += toggleButton
        toggleButton.setToggleGroup(toggleGroup)
        toggleButton.setOnAction(_ => loadFocusTreeView(someFocusTree))
        vbox.getChildren.add(toggleButton)
      )
    )

  /** Clears the focus tree view in the middle */
  private def clear(): Unit =
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()
    focusTreeView.setGridLinesVisible(lines)

  private def loadWelcomeMessage(): Unit =
    // Cancel any running task first
    cancelCurrentTask()

    clear()
    setCC()
    setRC()
    focusTreeView.add(Label(welcomeMessage), 0, 0)

  /** Loads the given FocusTreeFile into the focusTreeView GridPane by creating it in a separate thread */
  private def loadFocusTreeView(someFocusTree: FocusTreeFile): Unit =
    // Cancel any currently running task
    cancelCurrentTask()

    clear()

    for _ <- 0 until someFocusTree.columns do setCC()
    for _ <- 0 until someFocusTree.rows do setRC()

    val loadFocusTreeTask = new Task[GridPane]() {
      override def call(): GridPane = {
        val newGridPane = new GridPane()

        // Copy properties from the existing focusTreeView
        newGridPane.setHgap(focusTreeView.getHgap)
        newGridPane.setVgap(focusTreeView.getVgap)
        newGridPane.setGridLinesVisible(lines)
        newGridPane.getStyleClass.addAll(focusTreeView.getStyleClass)
        focusTreeView.getColumnConstraints.forEach(cc => {
          val newCC = new ColumnConstraints()
          newCC.setMinWidth(cc.getMinWidth)
          newCC.setPrefWidth(cc.getPrefWidth)
          newCC.setMaxWidth(cc.getMaxWidth)
          newGridPane.getColumnConstraints.add(newCC)
        })
        focusTreeView.getRowConstraints.forEach(rc => {
          val newRC = new RowConstraints()
          newRC.setMinHeight(rc.getMinHeight)
          newRC.setPrefHeight(rc.getPrefHeight)
          newRC.setMaxHeight(rc.getMaxHeight)
          newGridPane.getRowConstraints.add(newRC)
        })

        val focuses = someFocusTree.focuses
        focuses match
          case null =>
            if !isCancelled then
              newGridPane.add(Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"), 0, 0)
              logger.warn("Focuses list is null, cannot draw focus tree.")
          case _ if focuses.isEmpty =>
            if !isCancelled then
              newGridPane.add(Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"), 0, 0)
              logger.warn("Focuses list is empty, nothing to draw.")
          case _ =>
            val totalWork = someFocusTree.rows * someFocusTree.columns
            var workDone = 0

            for row <- 0 until someFocusTree.rows do
              if isCancelled then
                logger.info(s"Task cancelled while loading row $row")
                return newGridPane  // Exit early if cancelled

              for column <- 0 until someFocusTree.columns do
                if isCancelled then
                  logger.info(s"Task cancelled while loading column $column in row $row")
                  return newGridPane  // Exit early if cancelled

                focuses.find(f => f.hasAbsolutePosition(column, row)) match
                  case Some(focus) =>
                    val focusButton = FocusToggleButton(focus.toString, focusGridColumnsSize, focusGridRowSize)
                    newGridPane.add(focusButton, column, row)
                  case None =>
                    val placeholder = createEmptyCell()
                    newGridPane.add(placeholder, column, row)

                workDone += 1
                updateProgress(workDone, totalWork)

        newGridPane
      }
    }

    // Load the completed UI
    loadFocusTreeTask.setOnSucceeded(_ => {
      // Only update if this task is still the current one
      currentLoadTask match
        case Some(task) if task == loadFocusTreeTask =>
          val newPane = loadFocusTreeTask.getValue
          val currentZoom = zoomableScrollPane.getZoomLevel
          val dividerPositions = splitPane.getDividerPositions
          val newZoomableScrollPane = ZoomableScrollPane(newPane)
          newZoomableScrollPane.setPrefHeight(zoomableScrollPane.getPrefHeight)
          newZoomableScrollPane.setPrefWidth(zoomableScrollPane.getPrefWidth)
          newZoomableScrollPane.setZoomLevel(currentZoom)
          val items = splitPane.getItems
          val index = items.indexOf(zoomableScrollPane)
          if index >= 0 then
            items.set(index, newZoomableScrollPane)
          Platform.runLater(() => {
            splitPane.setDividerPositions(dividerPositions: _*)
          })
          zoomableScrollPane = newZoomableScrollPane
          focusTreeView = newPane
          setupZoomButtons()
          currentLoadTask = None
        case _ =>
          logger.info("Task succeeded but was already replaced by another task")
    })

    loadFocusTreeTask.setOnCancelled(_ => {
      logger.info(s"Task cancelled for focus tree: ${someFocusTree.name}")
      currentLoadTask = None
    })

    loadFocusTreeTask.setOnFailed(_ => {
      logger.error(s"Task failed for focus tree: ${someFocusTree.name}", loadFocusTreeTask.getException)
      currentLoadTask = None
    })

    updateProgressIndicator(loadFocusTreeTask)

    // Store the current task
    currentLoadTask = Some(loadFocusTreeTask)

    val thread = new Thread(loadFocusTreeTask)
    thread.setDaemon(true)
    thread.start()

  private def setupZoomButtons(): Unit = {
    if zoomInButton != null then zoomInButton.setOnAction(_ => zoomableScrollPane.zoomIn())
    if zoomOutButton != null then zoomOutButton.setOnAction(_ => zoomableScrollPane.zoomOut())
    if resetZoomButton != null then resetZoomButton.setOnAction(_ => zoomableScrollPane.resetZoom())
  }

  /** Cancels the currently running task if one exists */
  private def cancelCurrentTask(): Unit =
    currentLoadTask match
      case Some(task) if task.isRunning =>
        logger.info("Cancelling current load task")
        task.cancel()
        currentLoadTask = None
      case Some(task) =>
        logger.debug("Current task exists but is not running")
        currentLoadTask = None
      case None =>

  private def updateProgressIndicator(task: Task[GridPane]): Unit =
    if progressIndicator != null then
      progressIndicator.progressProperty().unbind()
      progressIndicator.visibleProperty().unbind()
      progressIndicator.progressProperty().bind(task.progressProperty())
      progressIndicator.visibleProperty().bind(task.runningProperty())

  /** Creates an invisible pane that allows dragging/panning on empty grid cells */
  private def createEmptyCell(): Pane =
    val pane = new Pane()
    pane.setPrefSize(focusGridColumnsSize, focusGridRowSize)
    pane.setMinSize(focusGridColumnsSize, focusGridRowSize)
    pane.setMaxSize(focusGridColumnsSize, focusGridRowSize)
    // Make it transparent but still mouse-transparent = false so it captures events
    pane.setStyle("-fx-background-color: transparent;")
    pane.setMouseTransparent(false)
    pane

  private def setCC() = {
    val cc = new ColumnConstraints()
    cc.setMinWidth(focusGridColumnsSize)
    cc.setPrefWidth(focusGridColumnsSize)
    cc.setMaxWidth(focusGridColumnsSize)
    focusTreeView.getColumnConstraints.add(cc)
  }

  private def setRC() = {
    val rc = new RowConstraints()
    rc.setMinHeight(focusGridRowSize)
    rc.setPrefHeight(focusGridRowSize)
    rc.setMaxHeight(focusGridRowSize)
    focusTreeView.getRowConstraints.add(rc)
  }

  @FXML def handleWelcome(): Unit =
    cancelCurrentTask()
    clear()
    loadWelcomeMessage()

  @FXML def handleGridlines(): Unit =
    lines = gridlines.isSelected
    focusTreeView.setGridLinesVisible(gridlines.isSelected)