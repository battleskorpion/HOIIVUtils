package com.hoi4utils.ui.focus_view

import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTreeFile}
import com.hoi4utils.hoi4mod.localization.Property
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.{ContextMenu, MenuItem}
import javafx.scene.layout.Pane
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Hybrid focus tree view that combines Canvas rendering with JavaFX Node interactions.
 * Canvas handles background graphics and lines, while FocusNodes handle all mouse interactions.
 */
class FocusTreeView(private var _focusTree: Option[FocusTreeFile]) extends Pane with LazyLogging:

  // Constants from original implementation
  private val FOCUS_X_SCALE: Int = 90
  private val CENTER_FOCUS_X: Int = FOCUS_X_SCALE / 2
  private val FOCUS_Y_SCALE: Int = 140
  private val CENTER_FOCUS_Y: Int = FOCUS_Y_SCALE / 2
  private val X_OFFSET_FIX: Int = 30
  private val Y_OFFSET_FIX: Int = 40

  // Components
  private val backgroundCanvas = new Canvas()
  private val focusNodes = mutable.Map[Focus, FocusNode]()
  private val selectedFocuses = mutable.Set[Focus]()

  // State
  private var gridLines: Boolean = false
  private var onFocusDoubleClick: Focus => Unit = _ => ()
  private var onFocusRightClick: (Focus, MouseEvent) => Unit = (_, _) => ()

  // Dragging state
  private var isDragging = false
  private var dragStartX = 0.0
  private var dragStartY = 0.0
  private var onRequestPanning: (Boolean) => Unit = _ => ()

  // Initialize
  getChildren.add(backgroundCanvas)
  setupEventHandlers()
  redraw()

  /**
   * Set the focus tree and redraw
   */
  def focusTree: Option[FocusTreeFile] = _focusTree

  def focusTree_=(tree: Option[FocusTreeFile]): Unit =
    _focusTree = tree
    redraw()

  def focusTree_=(tree: FocusTreeFile): Unit =
    focusTree = Some(tree)

  /**
   * Set callback for focus double-click events
   */
  def setOnFocusDoubleClick(callback: Focus => Unit): Unit =
    onFocusDoubleClick = callback

  /**
   * Set callback for focus right-click events
   */
  def setOnFocusRightClick(callback: (Focus, MouseEvent) => Unit): Unit =
    onFocusRightClick = callback

  /**
   * Set callback for requesting panning mode from parent scroll pane
   */
  def setOnRequestPanning(callback: Boolean => Unit): Unit =
    onRequestPanning = callback

  /**
   * Toggle grid lines on/off
   */
  def toggleGridLines(): Unit =
    gridLines = !gridLines
    redraw()

  /**
   * Get currently selected focuses
   */
  def getSelectedFocuses: Set[Focus] = selectedFocuses.toSet

  /**
   * Select a focus
   */
  def selectFocus(focus: Focus): Unit =
    selectedFocuses.add(focus)
    focusNodes.get(focus).foreach(_.setSelected(true))

  /**
   * Deselect a focus
   */
  def deselectFocus(focus: Focus): Unit =
    selectedFocuses.remove(focus)
    focusNodes.get(focus).foreach(_.setSelected(false))

  /**
   * Clear all selections
   */
  def clearSelection(): Unit =
    selectedFocuses.foreach(focus => focusNodes.get(focus).foreach(_.setSelected(false)))
    selectedFocuses.clear()

  /**
   * Main redraw method
   */
  def redraw(): Unit =
    updateCanvasSize()
    clearFocusNodes()
    drawBackground()
    createFocusNodes()
    logger.debug("Focus tree view redrawn")

  /**
   * Update canvas size based on focus tree dimensions
   */
  private def updateCanvasSize(): Unit =
    val (width, height) = calculateRequiredSize()
    backgroundCanvas.setWidth(width)
    backgroundCanvas.setHeight(height)
    setPrefSize(width, height)

    // Ensure the background canvas covers the entire pane for better drag detection
    backgroundCanvas.setLayoutX(0)
    backgroundCanvas.setLayoutY(0)

  /**
   * Calculate required canvas size
   */
  private def calculateRequiredSize(): (Double, Double) =
    _focusTree match
      case Some(tree) if tree.focuses.nonEmpty =>
        val minX = tree.focuses.map(_.absoluteX).min
        val maxX = tree.focuses.map(_.absoluteX).max
        val minY = 0
        val maxY = tree.focuses.map(_.absoluteY).max

        val cols = maxX - minX + 1
        val rows = maxY - minY + 1
        val width = cols * FOCUS_X_SCALE + X_OFFSET_FIX * 2
        val height = rows * FOCUS_Y_SCALE + Y_OFFSET_FIX * 2

        (width.toDouble, height.toDouble)
      case _ =>
        (800.0, 600.0) // Default size

  /**
   * Remove all existing focus nodes
   */
  private def clearFocusNodes(): Unit =
    focusNodes.values.foreach(node => getChildren.remove(node))
    focusNodes.clear()

  /**
   * Draw background graphics on canvas
   */
  private def drawBackground(): Unit =
    val gc = backgroundCanvas.getGraphicsContext2D

    // Clear canvas
    gc.clearRect(0, 0, backgroundCanvas.getWidth, backgroundCanvas.getHeight)
    gc.setFill(Color.DarkGray)
    gc.fillRect(0, 0, backgroundCanvas.getWidth, backgroundCanvas.getHeight)

    _focusTree match
      case Some(tree) =>
        // Draw grid if enabled
        if gridLines then drawGridLines(gc, tree)

        // Draw focus graphics
        tree.focuses.foreach(focus => drawFocusGraphics(gc, focus, tree.minX))

        // Draw connections
        drawPrerequisiteLines(gc, tree.focuses, tree.minX)
        drawMutuallyExclusiveLines(gc, tree.focuses, tree.minX)

      case None =>
        // Draw empty state
        gc.setFill(Color.White)
        gc.fillText("No focus tree loaded", 50, 50)

  /**
   * Draw grid lines on canvas
   */
  private def drawGridLines(gc: GraphicsContext, tree: FocusTreeFile): Unit =
    val minX = tree.focuses.map(_.absoluteX).min
    val maxX = tree.focuses.map(_.absoluteX).max
    val maxY = tree.focuses.map(_.absoluteY).max

    gc.setStroke(Color.Gray)
    gc.setLineWidth(1)

    // Vertical lines
    for x <- minX to maxX do
      val x1 = focusToCanvasXAbs(x, minX)
      val y1 = 0
      val y2 = focusToCanvasYAbs(maxY)
      gc.strokeLine(x1, y1, x1, y2)

    // Horizontal lines
    for y <- 1 to maxY do
      val x1 = focusToCanvasXAbs(minX, minX)
      val x2 = focusToCanvasXAbs(maxX, minX)
      val y1 = focusToCanvasYAbs(y)
      gc.strokeLine(x1, y1, x2, y1)

  /**
   * Draw focus background graphics and icons
   */
  private def drawFocusGraphics(gc: GraphicsContext, focus: Focus, minX: Int): Unit =
    val x = focusToCanvasX(focus, minX)
    val y = focusToCanvasY(focus)

    // Draw focus background (simplified for now)
    gc.setFill(Color.LightGray)
    gc.fillRect(x - CENTER_FOCUS_X, y - CENTER_FOCUS_Y, FOCUS_X_SCALE, FOCUS_Y_SCALE * 0.7)

    // Draw focus icon if available
    focus.getDDSImage.foreach(img => gc.drawImage(img, x - 32, y - 32))

    // Draw focus name
    val name = focus.localizationText(Property.NAME).getOrElse(focus.id.getOrElse("[Unknown]"))
    gc.setFill(Color.White)
    gc.fillText(name, x - 40, y + 30)

  /**
   * Draw prerequisite lines
   */
  private def drawPrerequisiteLines(gc: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit =
    gc.setStroke(Color.Black)
    gc.setLineWidth(3)

    focuses.filter(_.hasPrerequisites).foreach { focus =>
      val prereqFocuses = focus.prerequisiteList
      val x1 = focusToCanvasX(focus, minX)
      val y1 = focusToCanvasY(focus)

      prereqFocuses.foreach { prereqFocus =>
        val x2 = focusToCanvasX(prereqFocus, minX)
        val y2 = focusToCanvasY(prereqFocus)

        // Draw L-shaped connection
        val midY = y1 - 20
        gc.strokeLine(x1, y1, x1, midY)
        gc.strokeLine(x1, midY, x2, midY)
        gc.strokeLine(x2, midY, x2, y2)
      }
    }

  /**
   * Draw mutually exclusive lines
   */
  private def drawMutuallyExclusiveLines(gc: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit =
    gc.setStroke(Color.DarkRed)
    gc.setLineWidth(3)

    focuses.filter(_.isMutuallyExclusive).foreach { focus =>
      val mutexFocuses = focus.mutuallyExclusiveList
      val x1 = focusToCanvasX(focus, minX)
      val y1 = focusToCanvasY(focus)

      mutexFocuses.foreach { mutexFocus =>
        val x2 = focusToCanvasX(mutexFocus, minX)
        val y2 = focusToCanvasY(mutexFocus)

        // Draw direct line
        gc.strokeLine(x1, y1, x2, y2)

        // Draw end circles
        gc.setFill(Color.DarkRed)
        gc.fillOval(x1 - 5, y1 - 5, 10, 10)
        gc.fillOval(x2 - 5, y2 - 5, 10, 10)
      }
    }

  /**
   * Create interactive focus nodes
   */
  private def createFocusNodes(): Unit =
    _focusTree.foreach { tree =>
      tree.focuses.foreach { focus =>
        val node = new FocusNode(focus, onFocusDoubleClick, onFocusRightClick)
        val x = focusToCanvasX(focus, tree.minX)
        val y = focusToCanvasY(focus)

        // Position the node over the rendered focus
        node.updatePosition(x, y, FOCUS_X_SCALE, FOCUS_Y_SCALE * 0.7)

        // Set up hover effects
        node.setOnMouseEntered(_ => node.setHovered(true))
        node.setOnMouseExited(_ => node.setHovered(false))

        focusNodes(focus) = node
        getChildren.add(node)

        // Apply selection state if focus is selected
        if selectedFocuses.contains(focus) then
          node.setSelected(true)
      }
    }

  /**
   * Set up event handlers for the pane itself
   */
  private def setupEventHandlers(): Unit =
    // Mouse pressed - start potential drag
    setOnMousePressed { event =>
      if event.getButton.name() == "PRIMARY" then
        isDragging = false
        dragStartX = event.getSceneX
        dragStartY = event.getSceneY
        onRequestPanning(false) // Disable scroll pane panning initially
    }

    // Mouse dragged - handle panning
    setOnMouseDragged { event =>
      if event.getButton.name() == "PRIMARY" then
        val deltaX = event.getSceneX - dragStartX
        val deltaY = event.getSceneY - dragStartY
        val threshold = 5.0 // Minimum distance to start dragging

        if !isDragging && (Math.abs(deltaX) > threshold || Math.abs(deltaY) > threshold) then
          isDragging = true
          onRequestPanning(true) // Enable scroll pane panning
          logger.debug("Started dragging background")

        if isDragging then
          // Let the scroll pane handle the actual scrolling
          event.consume() // Prevent other handlers
    }

    // Mouse released - end drag
    setOnMouseReleased { event =>
      if isDragging then
        isDragging = false
        onRequestPanning(false) // Disable scroll pane panning
        logger.debug("Stopped dragging background")
        event.consume()
    }

    // Mouse clicked - handle context menu and other clicks
    setOnMouseClicked { event =>
      if !isDragging then // Only handle clicks if we weren't dragging
        if event.getButton.name() == "SECONDARY" && getSelectedFocuses.isEmpty then
          // Right-click on empty space - show context menu
          val contextMenu = new ContextMenu()
          val addFocusItem = new MenuItem("Add Focus")

          addFocusItem.setOnAction(_ => handleAddFocus(event.getX, event.getY))

          contextMenu.getItems.add(addFocusItem)
          contextMenu.show(this, event.getScreenX, event.getScreenY)
        else if event.getButton.name() == "PRIMARY" then
          // Left-click on empty space - clear selection
          clearSelection()
    }

  /**
   * Handle adding a new focus at the specified location
   */
  private def handleAddFocus(x: Double, y: Double): Unit =
    _focusTree.foreach { tree =>
      val newFocus = new Focus(tree)
      val focusX = canvasToFocusX(x, tree.minX)
      val focusY = canvasToFocusY(y)

      newFocus.setAbsoluteXY(focusX, focusY, false)
      newFocus.setID(tree.nextTempFocusID())
      tree.addNewFocus(newFocus)

      redraw()
      logger.info(s"Added new focus at ($focusX, $focusY)")
    }

  // Coordinate transformation methods (same as original)
  private def focusToCanvasX(focus: Focus, minX: Int): Double =
    focusToCanvasXAbs(focus.absoluteX, minX)

  private def focusToCanvasXAbs(focusAbsX: Int, minX: Int): Double =
    FOCUS_X_SCALE * (focusAbsX - minX) + X_OFFSET_FIX + CENTER_FOCUS_X

  private def focusToCanvasY(focus: Focus): Double =
    focusToCanvasYAbs(focus.absoluteY)

  private def focusToCanvasYAbs(focusAbsY: Int): Double =
    FOCUS_Y_SCALE * focusAbsY + Y_OFFSET_FIX + CENTER_FOCUS_Y

  private def canvasToFocusX(canvasX: Double, minX: Int): Int =
    ((canvasX - X_OFFSET_FIX - CENTER_FOCUS_X) / FOCUS_X_SCALE + minX).toInt

  private def canvasToFocusY(canvasY: Double): Int =
    ((canvasY - Y_OFFSET_FIX - CENTER_FOCUS_Y) / FOCUS_Y_SCALE).toInt