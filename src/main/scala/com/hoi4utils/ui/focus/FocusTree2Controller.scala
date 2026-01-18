package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4.common.national_focus.{Focus, FocusTree, FocusTreeManager, Point, PseudoSharedFocusTree, Focus as gridX}
import com.hoi4utils.script.MultiPDX
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.javafx.scene.control.ZoomableScrollPane
import com.hoi4utils.ui.javafx.scene.layout.ErrorIconPane
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.{DragEvent, Dragboard, MouseEvent, TransferMode}
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.*
import scalafx.scene.input.ClipboardContent
import zio.{UIO, URIO, ZIO}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.compiletime.uninitialized
import scala.jdk.javaapi.CollectionConverters

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  private val focusGridColumnsSize: Int = 100
  private val focusGridRowSize: Int = 200
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"
  private val PURPLE_LINE_COLOR: Color = Color.rgb(139, 92, 246)
  private val RED_LINE_COLOR: Color = Color.rgb(255, 0, 0)
  private val LINE_STROKE_WIDTH: Double = 2.0

  @FXML var focusTree2: AnchorPane = uninitialized
  @FXML var toolBar: ToolBar = uninitialized
  @FXML var focusTreeView: GridPane = uninitialized
  @FXML var focusSelection: ScrollPane = uninitialized
  @FXML var focusTreeScrollPane: ScrollPane = uninitialized
  @FXML var lineLayer: Pane = uninitialized
  @FXML var layeredContent: StackPane = uninitialized
  @FXML var ft2SplitPane: SplitPane = uninitialized
  @FXML var vbox: VBox = uninitialized
  @FXML var welcome: ToggleButton = uninitialized
  @FXML var toggleGroup: ToggleGroup = uninitialized
  @FXML var focusDetailsPane: AnchorPane = uninitialized
  @FXML var focusDetailsPaneController: FocusDetailsPaneController = uninitialized
  @FXML var focusTreesCount: Label = uninitialized
  @FXML var progressIndicator: ProgressIndicator = uninitialized
  @FXML var focusCountLabel: Label = uninitialized
  @FXML var zoomInButton: Button = uninitialized
  @FXML var zoomOutButton: Button = uninitialized
  @FXML var resetZoomButton: Button = uninitialized
  @FXML var gridlines: ToggleButton = uninitialized

  private var focusGridColumns: Int = 0
  private var focusGridRows: Int = 0
  private var lines: Boolean = false
  private var zoomableScrollPane: ZoomableScrollPane = uninitialized
  private var focusTreesToggleButtons: ListBuffer[ToggleButton] = ListBuffer.empty
  private var currentLoadTask: Option[Task[GridPane]] = None
  private var focusGridToggleGroup: ToggleGroup = new ToggleGroup()

  // Store the current offset for coordinate conversions
  private var currentOffsetX: Int = 0
  private var currentOffsetY: Int = 0
  private var currentFocusTree: Option[FocusTree] = None

  // Visual feedback for dragging
  private var dragHighlight: Region = uninitialized
  private var currentHighlightedCell: Option[(Int, Int)] = None
  private var dragImageView: ImageView = uninitialized

  // Initialize path calculator with grid dimensions
  private var pathCalculator: FocusConnectionPathCalculator = new FocusConnectionPathCalculator(
    cellWidth = focusGridColumnsSize.toDouble,
    cellHeight = focusGridRowSize.toDouble,
    lineOffset = 15.0
  )

  /* init */
  setFxmlFile("/com/hoi4utils/ui/focus/FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    replaceWithZoomableScrollPane()
    setupZoomButtons()

    clearFocusTreeView()
    welcome.setToggleGroup(toggleGroup)
    welcome.fire()
    focusTreeView.setGridLinesVisible(lines)
    populateFocusTreeSelection()
    focusDetailsPaneController.onUpdate = Some(() =>
      this.currentFocusTree match
        case Some(focusTree) =>
          // Reload the focus tree to reflect any changes
          loadFocusTreeView(focusTree)
        case None => ()
    )
    Platform.runLater(() => if progressIndicator != null then progressIndicator.setVisible(false))

  private def replaceWithZoomableScrollPane(): Unit =
    focusTreeScrollPane.setContent(null)
    focusTreeView.setGridLinesVisible(lines)
    // Layer: lines under grid
    lineLayer = new Pane()
    lineLayer.setMouseTransparent(true) // pass events to grid/buttons
    lineLayer.setPickOnBounds(false)
    lineLayer.setStyle("-fx-background-color: transparent;")
    // Stack: lineLayer (bottom) + focusTreeView (top)
    layeredContent = new StackPane(lineLayer, focusTreeView)
    lineLayer.toBack() // ensure under the grid

    // keep the line layer the same size as the grid's allocated space
    lineLayer.prefWidthProperty().bind(layeredContent.widthProperty())
    lineLayer.prefHeightProperty().bind(layeredContent.heightProperty())

    // Use layeredContent as the zoom target
    zoomableScrollPane = ZoomableScrollPane(layeredContent)

    // Copy properties from the original ScrollPane
    zoomableScrollPane.setPrefHeight(focusTreeScrollPane.getPrefHeight)
    zoomableScrollPane.setPrefWidth(focusTreeScrollPane.getPrefWidth)

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

  private def populateFocusTreeSelection(): URIO[FocusTreeManager, Unit] =
    ZIO.serviceWith[FocusTreeManager] { manager =>
      focusTreeView.setGridLinesVisible(lines)
      Some(manager.observeFocusTrees.sorted()).foreach(trees =>
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

          // Check if this focus tree has errors
          val hasErrors = manager.focusTreeErrors.exists(_.focusTreeId == someFocusTree.id.str)

          if hasErrors then
            // Create HBox to hold toggle button and error icon
            val hbox = new HBox(5)
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT)
            hbox.getChildren.add(toggleButton)

            // Get the error count for this focus tree
            val errorCount = manager.focusTreeErrors
              .find(_.focusTreeId == someFocusTree.id.str)
              .map(_.focusErrors.map(_.errors.size).sum)
              .getOrElse(0)

            // Create error icon pane
            val errorIcon = new ErrorIconPane(
              iconSize = 24,
              errorNumberCount = errorCount,
              onDoubleClick = Some(() => {
                // TODO: Open error details dialog or navigate to error list
                logger.info(s"Double-clicked error icon for ${someFocusTree.id.str}")
              }),
              tooltipText = Some(s"$errorCount error(s) in this focus tree")
            )
            errorIcon.build()
            hbox.getChildren.add(errorIcon)
            vbox.getChildren.add(hbox)
          else
            vbox.getChildren.add(toggleButton)
        )
        focusTreesCount.setText(s"Focus Trees: ${trees.size()}")
      )
      for {
        pseudoTrees <- manager.sharedFocusFilesAsPseudoTrees
        _ <- ZIO.foreachDiscard(pseudoTrees)(randomCode1)
      } yield ()
    }

  private def randomCode1(tree: FocusTree): URIO[FocusTreeManager, Unit] = {
    ZIO.serviceWith[FocusTreeManager] { manager =>
      val toggleButton = ToggleButton(tree.toString)
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
          loadFocusTreeView(tree)
        else
          toggleButton.setSelected(false)
          toggleButton.setSelected(true)
          loadFocusTreeView(tree)
      })
      toggleButton.setPadding(Insets(5, 10, 5, 10))

      // Check if this shared focus tree has errors
      val hasErrors = manager.focusTreeErrors.exists(_.focusTreeId == s"[Shared Focuses] ${tree.id.str}")

      if hasErrors then
        // Create HBox to hold toggle button and error icon
        val hbox = new HBox(5)
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT)
        hbox.getChildren.add(toggleButton)

        // Get the error count for this shared focus tree
        val errorCount = manager.focusTreeErrors
          .find(_.focusTreeId == s"[Shared Focuses] ${tree.id.str}")
          .map(_.focusErrors.map(_.errors.size).sum)
          .getOrElse(0)

        // Create error icon pane
        val errorIcon = new ErrorIconPane(
          iconSize = 24,
          errorNumberCount = errorCount,
          onDoubleClick = Some(() => {
            // TODO: Open error details dialog or navigate to error list
            logger.info(s"Double-clicked error icon for shared focuses ${tree.id.str}")
          }),
          tooltipText = Some(s"$errorCount error(s) in this shared focus file")
        )
        errorIcon.build()
        hbox.getChildren.add(errorIcon)
        vbox.getChildren.add(hbox)
        () 
      else
        vbox.getChildren.add(toggleButton)
        ()
    }
  }

  /** Loads the given FocusTreeFile into the focusTreeView GridPane by creating it in a separate thread */
  private def loadFocusTreeView(someFocusTree: FocusTree): Unit = {
    focusCountLabel.setText("Focuses: 0")
    cancelCurrentTask()
    clearFocusTreeView()
    focusGridToggleGroup = new ToggleGroup()
    val focuses: MultiPDX[Focus] = someFocusTree.focuses

    // Calculate offset needed for negative coordinates
    val (offsetX, offsetY) = calculateGridOffset(focuses)

    // STORE THE OFFSET and current focus tree for use in coordinate conversions
    currentOffsetX = offsetX
    currentOffsetY = offsetY
    currentFocusTree = Some(someFocusTree)

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

        newGridPane.setOnDragOver(de => {
          if (de.getGestureSource != newGridPane && de.getDragboard.hasString) {
            de.acceptTransferModes(TransferMode.MOVE)

            // Calculate which grid cell we're hovering over
            val localPoint = newGridPane.sceneToLocal(de.getSceneX, de.getSceneY)
            val hoverGridX = Math.max(0, Math.min((localPoint.getX / focusGridColumnsSize).toInt, gridCols - 1))
            val hoverGridY = Math.max(0, Math.min((localPoint.getY / focusGridRowSize).toInt, gridRows - 1))

            // Update highlight if we're over a different cell
            Platform.runLater(() => updateDragHighlight(newGridPane, hoverGridX, hoverGridY))

            // Update drag image position and scale
            if (dragImageView != null) {
              Platform.runLater(() => {
                val scale = zoomableScrollPane.getZoomLevel
                dragImageView.setScaleX(scale)
                dragImageView.setScaleY(scale)
              })
            }
          }
          de.consume()
        })
        newGridPane.setOnDragDropped(de => {
          // Remove highlight when drop completes
          Platform.runLater(() => removeDragHighlight())

          // Calculate grid position from mouse coordinates instead of relying on intersected node
          val localPoint = newGridPane.sceneToLocal(de.getSceneX, de.getSceneY)
          val targetGridX = Math.max(0, (localPoint.getX / focusGridColumnsSize).toInt)
          val targetGridY = Math.max(0, (localPoint.getY / focusGridRowSize).toInt)

          val db = de.getDragboard
          var success = false
          if (db.hasString) {
            val data = db.getString
            val parts = data.split("\\|")
            if (parts.length >= 2) {
              val focusId = parts(0)
              val isShiftDown = parts(1).contains("true")

              // Find the focus and update its position
              someFocusTree.focuses.find(_.id @== focusId).foreach { focus =>
                val newFocusPos = gridToFocusXY(targetGridX, targetGridY, someFocusTree)
                updateFocusPosition(focus, newFocusPos, isShiftDown)
                logger.info(s"Dropped focus $focusId at grid ($targetGridX, $targetGridY) -> focus coords $newFocusPos")
                success = true
              }
            }
          }
          de.setDropCompleted(success)
          de.consume()
        })
        newGridPane.layoutBoundsProperty().addListener((_, _, _) =>
          Platform.runLater(() => redrawConnections())
        )
        newGridPane.getChildren.addListener((_: javafx.collections.ListChangeListener.Change[? <: Node]) =>
          Platform.runLater(() => redrawConnections())
        )

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
            val totalWork = focusCount
            var workDone = 0
            var cuteFocusCounter = 0

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
                focusButton.setOnDragDetected(me => handleDraggedFocusButton(me, focusButton))
                focusButton.setToggleGroup(focusGridToggleGroup)
                newGridPane.add(focusButton, gridX, gridY)

                workDone += 1
                cuteFocusCounter += 1

                Platform.runLater(() => focusCountLabel.setText(s"Focuses: $cuteFocusCounter"))
                updateProgress(workDone, totalWork)
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
          // Rebuild layered content with same lineLayer (or new one if you prefer)
          layeredContent.getChildren.setAll(lineLayer, newPane)
          lineLayer.toBack() // ensure under the grid
          zoomableScrollPane.setTarget(layeredContent)
          zoomableScrollPane.setZoomLevel(currentZoom)

          focusTreeView = newPane
          setupZoomButtons()

          // Draw connections after grid is loaded
          Platform.runLater(() => redrawConnections())

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
  private def calculateGridDimensions(focuses: Iterable[Focus],
                                      offsetX: Int,
                                      offsetY: Int): (Int, Int) = {
    if (focuses.isEmpty) {
      (1, 1) // minimum grid size
    } else {
      val maxX = focuses.map(_.absoluteX + offsetX).max
      val maxY = focuses.map(_.absoluteY + offsetY).max
      (maxX + 1, maxY + 1) // +1 because we're 0-indexed
    }
  }

  // Fixed coordinate conversion methods
  // Grid to Focus: Remove the offset that was added to make grid coordinates positive
  private def gridToFocusX(gridX: Int, focusTree: FocusTree): Int =
    gridX - currentOffsetX

  private def gridToFocusY(gridY: Int, focusTree: FocusTree): Int =
    gridY - currentOffsetY

  private def gridToFocusXY(gridX: Int, gridY: Int, focusTree: FocusTree): Point =
    Point(gridX, gridY) - Point(currentOffsetX, currentOffsetY)

  // Focus to Grid: Add the offset to make focus coordinates positive for the grid
  private def focusToGridX(focus: Focus): Int =
    focus.absoluteX + currentOffsetX

  private def focusToGridY(focus: Focus): Int =
    focus.absoluteY + currentOffsetY

  private def focusToGridXY(focus: Focus): Point =
    focus.absolutePosition + Point(currentOffsetX, currentOffsetY)

  private def updateProgressIndicator(task: Task[GridPane]): Unit =
    if progressIndicator != null then
      progressIndicator.progressProperty().unbind()
      progressIndicator.visibleProperty().unbind()
      progressIndicator.progressProperty().bind(task.progressProperty())
      progressIndicator.visibleProperty().bind(task.runningProperty())

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
  private def clearFocusTreeView(): Unit =
    removeDragHighlight()
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()
    if lineLayer != null then lineLayer.getChildren.clear()
    focusTreeView.setGridLinesVisible(lines)

  private def setupZoomButtons(): Unit = {
    if zoomInButton != null then zoomInButton.setOnAction(_ => zoomableScrollPane.zoomIn())
    if zoomOutButton != null then zoomOutButton.setOnAction(_ => zoomableScrollPane.zoomOut())
    if resetZoomButton != null then resetZoomButton.setOnAction(_ => zoomableScrollPane.resetZoom())
  }

  override def preSetup(): Unit = setupWindowControls(focusTree2, toolBar)

  @FXML def handleWelcome(): Unit =
    focusCountLabel.setText("")
    welcome.setSelected(true)
    focusTreesToggleButtons.foreach(btn =>
      btn.setSelected(false)
    )
    cancelCurrentTask()
    clearFocusTreeView()
    setCC()
    setRC()
    focusTreeView.add(Label(welcomeMessage), 0, 0)
    focusTreeView.setGridLinesVisible(lines)
    if focusDetailsPaneController != null then
      focusDetailsPaneController.clear()

  @FXML def handleGridlines(): Unit =
    lines = gridlines.isSelected
    focusTreeView.setGridLinesVisible(gridlines.isSelected)

  def handleDraggedFocusButton(event: MouseEvent, toggleButton: FocusToggleButton): Unit =
    val db: Dragboard = toggleButton.startDragAndDrop(TransferMode.MOVE)
    val content: ClipboardContent = ClipboardContent()
    // Store the focus ID so we can find it reliably
    content.putString(s"${toggleButton.focus.id.str}|shift:${event.isShiftDown}")

    // Create a snapshot with transparent background
    val params = new javafx.scene.SnapshotParameters()
    params.setFill(Color.TRANSPARENT)
    val snapshot = toggleButton.snapshot(params, null)

    // Store reference to update scale during drag
    dragImageView = new ImageView(snapshot)
    val currentScale = zoomableScrollPane.getZoomLevel
    dragImageView.setScaleX(currentScale)
    dragImageView.setScaleY(currentScale)

    // Set the drag view with scaled image
    db.setDragView(snapshot, snapshot.getWidth / 2, snapshot.getHeight / 2)

    db.setContent(content)
    event.consume()

  private def updateDragHighlight(gridPane: GridPane, gridX: Int, gridY: Int): Unit =
    // Remove old highlight if it's in a different cell
    currentHighlightedCell match
      case Some((oldX, oldY)) if oldX != gridX || oldY != gridY =>
        removeDragHighlight()
      case Some(_) =>
        return // Same cell, no need to update
      case None =>

    // Create new highlight
    dragHighlight = new Region()
    dragHighlight.setStyle(
      "-fx-background-color: rgba(117,71,222,0.3); " +
        "-fx-border-color: rgba(139, 92, 246, 0.8); " +
        "-fx-border-width: 2px;"
    )
    dragHighlight.setPrefSize(focusGridColumnsSize, focusGridRowSize)
    dragHighlight.setMouseTransparent(true)

    gridPane.add(dragHighlight, gridX, gridY)
    currentHighlightedCell = Some((gridX, gridY))

  private def removeDragHighlight(): Unit =
    if (dragHighlight != null && focusTreeView != null) {
      focusTreeView.getChildren.remove(dragHighlight)
      dragHighlight = null
      currentHighlightedCell = None
    }

  def updateFocusPosition(focus: Focus, newFocusPos: Point, updateChildRelativeOffsets: Boolean): Unit = {
    focus.setAbsoluteXY(newFocusPos, updateChildRelativeOffsets)

    // get the valid Focus objects to match
    val relativelyPositionedFocuses = focus.selfAndRelativePositionedFocuses
    val focusToggleButtons = CollectionConverters.asScala(focusTreeView.getChildren)
    // filter children to only the FocusToggleButtons with matching focus
    val focusButtons = focusToggleButtons.collect {
      case btn: FocusToggleButton if relativelyPositionedFocuses.contains(btn.focus) => btn
    }.toList

    focusButtons.foreach(fb =>
      val gridX = focusToGridX(fb.focus)
      val gridY = focusToGridY(fb.focus)

      focusTreeView.getChildren.remove(fb)
      GridPane.setColumnIndex(fb, gridX)
      GridPane.setRowIndex(fb, gridY)
      focusTreeView.getChildren.add(fb)
    )

    // Redraw connections after position update
    Platform.runLater(() => redrawConnections())
  }

  /** Clear and redraw all connection lines on the lineLayer. */
  private def redrawConnections(): Unit =
    if (lineLayer eq null) return
    lineLayer.getChildren.clear()

    // Collect all FocusToggleButtons from the grid
    val buttons = collectFocusButtons()

    // Build a map of Focus -> FocusToggleButton for quick lookup
    val buttonMap = buttons.map(b => b.focus -> b).toMap

    // draw focus prerequisite paths
    buttons.foreach { sourceButton =>
      val prereqFocuses = sourceButton.focus.prerequisiteList
      prereqFocuses.foreach { prereqFocus =>
        buttonMap.get(prereqFocus).foreach { prereqButton =>
          drawPrereqConnection(sourceButton, prereqButton)
        }
      }
    }

    // draw focus mutual exclusivity
    buttons.foreach { sourceButton =>
      // todo only draw for one. but im being so lazy.
      val mutexclFocuses = sourceButton.focus.mutuallyExclusiveList
      mutexclFocuses.foreach { meFocus =>
        buttonMap.get(meFocus).foreach { meButton =>
          drawMutuallyExclusiveConnection(sourceButton, meButton)
        }
      }
    }

  /**
   * Draw a single connection from source focus to prerequisite.
   */
  private def drawPrereqConnection(fromButton: FocusToggleButton, toButton: FocusToggleButton): Unit =
    val segments = pathCalculator.calculatePrereqPath(fromButton, toButton)

    val path = new Path()
    path.setStrokeWidth(LINE_STROKE_WIDTH)
    path.setStroke(PURPLE_LINE_COLOR)
    path.setMouseTransparent(true)

    segments.foreach { s =>
      path.getElements.addAll(new MoveTo(s.from.x, s.from.y), new LineTo(s.to.x, s.to.y))
    }
    lineLayer.getChildren.addAll(path) // single node

    // Draw junction points at corners (optional, for visual clarity)
    segments.tail.foreach { segment =>
      val corner = createCornerCircle(segment.from)
      lineLayer.getChildren.add(corner)
    }

  /**
   * Draw a single connection from source focus to prerequisite.
   */
  private def drawMutuallyExclusiveConnection(fromButton: FocusToggleButton, toButton: FocusToggleButton): Unit =
    val segments = pathCalculator.calculateMutuallyExclusivePath(fromButton, toButton)

    val path = new Path()
    path.setStrokeWidth(LINE_STROKE_WIDTH)
    path.setStroke(RED_LINE_COLOR)
    path.setMouseTransparent(true)

    segments.foreach { s =>
      path.getElements.addAll(new MoveTo(s.from.x, s.from.y), new LineTo(s.to.x, s.to.y))
    }
    lineLayer.getChildren.addAll(path) // single node

  /**
   * Create a JavaFX Line from a path segment, converting to scene coordinates.
   */
  private def createLine(segment: FocusConnectionPathCalculator#PathSegment): Line =
    // Convert grid coordinates to scene coordinates
    val fromScene = gridToScene(segment.from)
    val toScene = gridToScene(segment.to)

    val line = new Line(fromScene.x, fromScene.y, toScene.x, toScene.y)
    line.setStroke(PURPLE_LINE_COLOR) // Purple color like the game
    line.setStrokeWidth(LINE_STROKE_WIDTH)
    line.setMouseTransparent(true)
    line

  /**
   * Create a small circle at junction points (corners).
   */
  private def createCornerCircle(point: FocusConnectionPathCalculator#Point): Circle =
    val scenePoint = gridToScene(point)
    val circle = new Circle(scenePoint.x, scenePoint.y, 3.0)
    circle.setFill(PURPLE_LINE_COLOR)
    circle.setMouseTransparent(true)
    circle

  /**
   * Convert grid-based coordinates to scene coordinates relative to layeredContent.
   */
  private def gridToScene(gridPoint: FocusConnectionPathCalculator#Point): FocusConnectionPathCalculator#Point =
    // The grid coordinates are already in focusTreeView's coordinate space
    // We need to convert them to layeredContent's coordinate space
    val p = focusTreeView.localToScene(gridPoint.x, gridPoint.y)
    val lp = layeredContent.sceneToLocal(p)
    pathCalculator.Point(lp.getX, lp.getY)

  /**
   * Collect all FocusToggleButton instances from the grid.
   */
  private def collectFocusButtons(): List[FocusToggleButton] =
    val children = focusTreeView.getChildren
    val buttons = new scala.collection.mutable.ArrayBuffer[FocusToggleButton]()
    val it = children.iterator()

    while (it.hasNext) {
      it.next() match
        case btn: FocusToggleButton => buttons += btn
        case _ => ()
    }

    buttons.toList
