package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTree, FocusTreesManager}
import com.hoi4utils.script.MultiPDX
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.custom_javafx.layout.ZoomableScrollPane
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")
  private val focusGridColumnsSize: Int = 100
  private val focusGridRowSize: Int = 200
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"
  @FXML var focusTree2: VBox = uninitialized
  @FXML var menuBar: MenuBar = uninitialized
  @FXML var focusTreeView: GridPane = uninitialized
  @FXML var ft2Close: Button = uninitialized
  @FXML var ft2Square: Button = uninitialized
  @FXML var ft2Minimize: Button = uninitialized
  @FXML var focusSelection: ScrollPane = uninitialized
  @FXML var focusTreeScrollPane: ScrollPane = uninitialized
  @FXML var ft2SplitPane: SplitPane = uninitialized
  @FXML var vbox: VBox = uninitialized
  @FXML var welcome: ToggleButton = uninitialized
  @FXML var toggleGroup: ToggleGroup = uninitialized
  @FXML var focusDetailsPane: AnchorPane = uninitialized
  @FXML var focusDetailsPaneController: FocusDetailsPaneController = uninitialized
  @FXML var progressIndicator: ProgressIndicator = uninitialized
  @FXML var gridlines: ToggleButton = uninitialized
  @FXML var zoomInButton: Button = uninitialized
  @FXML var zoomOutButton: Button = uninitialized
  @FXML var resetZoomButton: Button = uninitialized
  private var focusGridColumns: Int = 0
  private var focusGridRows: Int = 0
  private var lines: Boolean = false
  private var zoomableScrollPane: ZoomableScrollPane = uninitialized
  private var focusTreesToggleButtons: ListBuffer[ToggleButton] = ListBuffer.empty
  private var currentLoadTask: Option[Task[GridPane]] = None
  private var focusGridToggleGroup: ToggleGroup = new ToggleGroup()

  private var isEmbedded: Boolean = false

  @FXML def initialize(): Unit =
    Platform.runLater(() =>
      hideButtons()
    )
    // Replace the regular ScrollPane with ZoomableScrollPane
    replaceWithZoomableScrollPane()

    // Setup zoom buttons if they exist
    setupZoomButtons()
    clear()
    welcome.setToggleGroup(toggleGroup)
    welcome.fire()
    focusTreeView.setGridLinesVisible(lines)
    populateFocusSelection()
    Platform.runLater(() => if progressIndicator != null then progressIndicator.setVisible(false))

  private def hideButtons(): Unit =
    isEmbedded = primaryScene == null
    ft2Close.setVisible(!isEmbedded)
    ft2Square.setVisible(!isEmbedded)
    ft2Minimize.setVisible(!isEmbedded)

  private def replaceWithZoomableScrollPane(): Unit =
    // Remove the GridPane from the original ScrollPane first
    focusTreeScrollPane.setContent(null)

    // Create the ZoomableScrollPane with the GridPane as target
    zoomableScrollPane = ZoomableScrollPane(focusTreeView)
    focusTreeView.setGridLinesVisible(lines)

    // Copy properties from the original ScrollPane
    zoomableScrollPane.setPrefHeight(focusTreeScrollPane.getPrefHeight)
    zoomableScrollPane.setPrefWidth(focusTreeScrollPane.getPrefWidth)
    focusTreeView.setGridLinesVisible(lines)
    // Replace in SplitPane
    if ft2SplitPane != null then
      val items = ft2SplitPane.getItems
      val index = items.indexOf(focusTreeScrollPane)
      if index >= 0 then
        items.set(index, zoomableScrollPane)
      else
        logger.error("Could not find focusTreeScrollPane in ft2SplitPane items")
    else
      logger.error("splitPane is null - check FXML fx:id")

  private def populateFocusSelection(): Unit =
    focusTreeView.setGridLinesVisible(lines)
    Some(FocusTreesManager.observeFocusTrees).foreach(trees =>
      trees.forEach(someFocusTree =>
        val toggleButton = ToggleButton(someFocusTree.toString)
        focusTreesToggleButtons += toggleButton
        toggleButton.setToggleGroup(toggleGroup)
        toggleButton.setOnAction(_ => {
          if toggleButton.isSelected then
            // Manually deselect all other buttons
            focusTreesToggleButtons.foreach(btn =>
              if btn != toggleButton then
                btn.setSelected(false)
                welcome.setSelected(false)
            )
            welcome.setSelected(false)
            loadFocusTreeView(someFocusTree)
          else
            toggleButton.setSelected(false)
            toggleButton.setSelected(true)
            loadFocusTreeView(someFocusTree)
        })
        toggleButton.setPadding(Insets(5, 10, 5, 10))
        vbox.getChildren.add(toggleButton)
      )
    )

  /** Loads the given FocusTreeFile into the focusTreeView GridPane by creating it in a separate thread */
  private def loadFocusTreeView(someFocusTree: FocusTree): Unit = {
    cancelCurrentTask()
    clear()
    focusGridToggleGroup = new ToggleGroup()
    val focuses: MultiPDX[Focus] = someFocusTree.focuses

    // Calculate offset needed for negative coordinates
    val (offsetX, offsetY) = calculateGridOffset(focuses)

    // Calculate actual dimensions needed
    val (gridCols, gridRows) = calculateGridDimensions(focuses, offsetX, offsetY)

    // Setup column and row constraints
    for (_ <- 0 until gridCols) setCC()
    for (_ <- 0 until gridRows) setRC()
    focusTreeView.setGridLinesVisible(lines)
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

        focuses match {
          case null =>
            if (!isCancelled) {
              newGridPane.add(
                Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"),
                0,
                0
              )
              logger.warn("Focuses list is null, cannot draw focus tree.")
            }
          case _ if focuses.isEmpty =>
            if (!isCancelled) {
              newGridPane.add(
                Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"),
                0,
                0
              )
              logger.warn("Focuses list is empty, nothing to draw.")
            }
          case _ =>
            val focusCount = focuses.size
            val emptyCount = (gridRows * gridCols) - focusCount
            val totalWork = focusCount + emptyCount
            var workDone = 0

            // Add focus buttons
            focuses.foreach { focus =>
              if (!isCancelled) {
                val gridX = focus.absoluteX + offsetX
                val gridY = focus.absoluteY + offsetY

                val focusButton = FocusToggleButton(
                  focus,
                  focusGridColumnsSize,
                  focusGridRowSize
                )
                focusButton.setOnAction(_ => loadFocusView(focus))
                focusButton.setToggleGroup(focusGridToggleGroup)
                newGridPane.add(focusButton, gridX, gridY)

                workDone += 1
                updateProgress(workDone, totalWork)
              }
            }

            // Fill empty cells with placeholders
            if (!isCancelled) {
              for {
                row <- 0 until gridRows
                col <- 0 until gridCols
              } {
                if (!isCancelled) {
                  val hasFocus = focuses.exists { f =>
                    (f.absoluteX + offsetX == col) && (f.absoluteY + offsetY == row)
                  }

                  if (!hasFocus) {
                    val placeholder = createEmptyCell()
                    newGridPane.add(placeholder, col, row)

                    workDone += 1
                    updateProgress(workDone, totalWork)
                  }
                }
              }
            }
        }
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
          zoomableScrollPane.setTarget(newPane)
          zoomableScrollPane.setZoomLevel(currentZoom)
          focusTreeView = newPane
          setupZoomButtons()
          currentLoadTask = None
        case _ =>
          logger.info("Task succeeded but was already replaced by another task")
    })
    loadFocusTreeTask.setOnCancelled(_ => {
      focusTreeView.setGridLinesVisible(lines)
      logger.info(s"Task cancelled for focus tree: ${someFocusTree.name}")
      currentLoadTask = None
    })
    loadFocusTreeTask.setOnFailed(_ => {
      focusTreeView.setGridLinesVisible(lines)
      logger.error(s"Task failed for focus tree: ${someFocusTree.name}", loadFocusTreeTask.getException)
      currentLoadTask = None
    })
    updateProgressIndicator(loadFocusTreeTask)
    focusTreeView.setGridLinesVisible(lines)

    // Store the current task
    currentLoadTask = Some(loadFocusTreeTask)
    val thread = new Thread(loadFocusTreeTask)
    thread.setDaemon(true)
    thread.start()
  }

  private def loadFocusView(focus: Focus): Unit =
    // Load the focus into the details pane
    if focusDetailsPaneController != null then
      focusDetailsPaneController.loadFocus(focus)

  /** Cancels the currently running task if one exists */
  private def cancelCurrentTask(): Unit =
    currentLoadTask match
      case Some(task) if task.isRunning =>
        task.cancel()
        currentLoadTask = None
      case Some(task) =>
        currentLoadTask = None
      case None =>

  /**
   * Calculate the minimum x and y coordinates across all focuses
   * to determine the offset needed for GridPane (which can't handle negative coords)
   */
  private def calculateGridOffset(focuses: Iterable[Focus]): (Int, Int) = {
    if (focuses.isEmpty) {
      (0, 0)
    } else {
      val minX = focuses.map(_.absoluteX).min
      val minY = focuses.map(_.absoluteY).min
      // Return absolute values if negative, otherwise 0
      (if (minX < 0) -minX else 0, if (minY < 0) -minY else 0)
    }
  }

  /**
   * Calculate the actual grid dimensions needed, accounting for offset
   */
  private def calculateGridDimensions(
                                       focuses: Iterable[Focus],
                                       offsetX: Int,
                                       offsetY: Int
                                     ): (Int, Int) = {
    if (focuses.isEmpty) {
      (1, 1) // minimum grid size
    } else {
      val maxX = focuses.map(_.absoluteX + offsetX).max
      val maxY = focuses.map(_.absoluteY + offsetY).max
      (maxX + 1, maxY + 1) // +1 because we're 0-indexed
    }
  }

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

  /** Clears the focus tree view in the middle */
  private def clear(): Unit =
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()
    focusTreeView.setGridLinesVisible(lines)

  private def setupZoomButtons(): Unit = {
    if zoomInButton != null then zoomInButton.setOnAction(_ => zoomableScrollPane.zoomIn())
    if zoomOutButton != null then zoomOutButton.setOnAction(_ => zoomableScrollPane.zoomOut())
    if resetZoomButton != null then resetZoomButton.setOnAction(_ => zoomableScrollPane.resetZoom())
  }

  override def preSetup(): Unit = setupWindowControls(focusTree2, ft2Close, ft2Square, ft2Minimize, menuBar)

  @FXML def handleWelcome(): Unit =
    welcome.setSelected(true)
    focusTreesToggleButtons.foreach(btn =>
        btn.setSelected(false)
    )
    cancelCurrentTask()
    clear()
    setCC()
    setRC()
    focusTreeView.add(Label(welcomeMessage), 0, 0)
    focusTreeView.setGridLinesVisible(lines)
    if focusDetailsPaneController != null then
      focusDetailsPaneController.clear()

  @FXML def handleGridlines(): Unit =
    lines = gridlines.isSelected
    focusTreeView.setGridLinesVisible(gridlines.isSelected)