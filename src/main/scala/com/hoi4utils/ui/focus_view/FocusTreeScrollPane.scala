package com.hoi4utils.ui.focus_view

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4.focus.{Focus, FocusTree}
import com.hoi4utils.localization.Property
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.custom_javafx.image.ScalaFXImageUtils
import com.hoi4utils.ui.pdxscript.PDXEditorController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.canvas.GraphicsContext
import scalafx.geometry.{Point2D, Rectangle2D}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{ComboBox, ContextMenu, MenuItem, ScrollPane, Tooltip}
import scalafx.scene.image.Image
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.layout.{AnchorPane, VBox}
import scalafx.scene.paint.Color
import scalafx.Includes.*

import javax.swing.JOptionPane
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTreeScrollPane(private var _focusTree: Option[FocusTree]) extends ScrollPane with LazyLogging {
  // Constants
  private val FOCUS_X_SCALE: Int = 165 // Focus (box around icon, text, and text background icon) horizontal size starting from center where 2 is 1 width left and 1 width right + set with extra for padding
  private val CENTER_FOCUS_X: Int = FOCUS_X_SCALE / 2
  private val FOCUS_Y_SCALE: Int = 100 // Focus (box around icon, text, and text background icon) vertical size starting from the top where 2 is 2 area down from top, set with extra for padding
  private val CENTER_FOCUS_Y: Int = FOCUS_Y_SCALE / 2
  private val X_OFFSET_FIX: Int = 37 // FOCUS_X_SCALE starts on the left side of the focus icon, so offset it to the left <- to have x center on center icon and include the name plate, no padding
  private val Y_OFFSET_FIX: Int = 0
  // vars
  private var selectedFocuses: ListBuffer[Focus] = ListBuffer.empty
  private var gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")
  private var gridLines: Boolean = false
  private var focusTooltipView: Option[Tooltip] = None
  private var focusDetailsFocus: Option[Focus] = None
  private var draggedFocus: Option[Focus] = None
  private var marqueeStartPoint: Option[Point2D] = None
  private var marqueeEndPoint: Option[Point2D] = None
  // Thread-safe flag to prevent concurrent drawing operations
  @volatile private var isDrawing = false
  // Scale factors
  private var currentScaleX = 1.0
  private var currentScaleY = 1.0

  /* init */
  private val focusTreeCanvas = Canvas()
  private val focusTreeCanvasAnchorPane = AnchorPane()

  focusTreeCanvasAnchorPane.getChildren.add(focusTreeCanvas)
  this.setContent(focusTreeCanvasAnchorPane)
  AnchorPane.setTopAnchor(focusTreeCanvas, 0.0)
  AnchorPane.setBottomAnchor(focusTreeCanvas, 0.0)
  AnchorPane.setLeftAnchor(focusTreeCanvas, 0.0)
  AnchorPane.setRightAnchor(focusTreeCanvas, 0.0)

  this.fitToWidth = false
  this.fitToHeight = false
  focusTreeCanvas.width <== focusTreeCanvasAnchorPane.prefWidth
  focusTreeCanvas.height <== focusTreeCanvasAnchorPane.prefHeight

  addFocusTreePaneHandlers()

  /**
   * Main method to draw the focus tree with proper Canvas handling and size limits
   */
  private def drawFocusTree(): Unit = {
    logger.debug("Drawing focus tree...")

    // Prevent concurrent drawing operations
    if (isDrawing) {
      logger.debug("Drawing already in progress, skipping...")
      return
    }

    // Ensure the Canvas is properly initialized
    if (focusTreeCanvas == null) {
      logger.error("focusTreeCanvas is null. Cannot draw focus tree.")
      return
    }

    updateContentSize()
    performDrawing()
  }

  def getMinX: Int = _focusTree.map(_.minX).getOrElse(0)

  def getMinY: Int = 0

  private def focusTreeViewLength(): Int = getMaxX - getMinX

  private def focusTreeViewHeight(): Int = getMaxY - getMinY

  private def focusTreeViewDimension(): Dimension = Dimension(focusTreeViewLength(), focusTreeViewHeight())

  private def getMaxX: Int = {
    _focusTree.map(_.focuses.map(_.absoluteX).maxOption.getOrElse(10)).getOrElse(10)
  }

  private def getMaxY: Int = {
    _focusTree.map(_.focuses.map(_.absoluteY).maxOption.getOrElse(10)).getOrElse(10)
  }

  /**
   * Update the Canvas and AnchorPane sizes based on focus tree dimensions
   * @param focusUnavailablePath Path to the focus unavailable image resource
   */
  private def loadFocusUnavailableImage(focusUnavailablePath: String): Image = {
    val inputStream =
      try
        getClass.getClassLoader.getResourceAsStream(focusUnavailablePath)
      catch {
        case e: Exception =>
          logger.error(s"Failed to load focus unavailable image from $focusUnavailablePath", e)
          return null
      }
    val buffer = new Array[Byte](inputStream.available)
    inputStream.read(buffer)
    inputStream.close()
    ScalaFXImageUtils.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match {
        case Some(value) => value
        case None => ???
      },
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )
  }

  /**
   * Performs the actual drawing operations with proper scaling
   */
  private def performDrawing(): Unit = {
    isDrawing = true
    try {
      val gc2D = focusTreeCanvas.getGraphicsContext2D

      if (gc2D == null) {
        logger.error("GraphicsContext2D is null. Cannot draw focus tree.")
        isDrawing = false
        return
      }

      // Verify Canvas dimensions are valid and match expected values
      val canvasWidth = focusTreeCanvas.getWidth
      val canvasHeight = focusTreeCanvas.getHeight

      if (canvasWidth <= 0 || canvasHeight <= 0) {
        logger.error(s"Invalid actual Canvas dimensions: width=$canvasWidth, height=$canvasHeight")
        isDrawing = false
        return
      }

      // Apply scaling transformation if needed
      gc2D.save() // Save current transform state
      try {
        if (currentScaleX != 1.0 || currentScaleY != 1.0) {
          gc2D.scale(currentScaleX, currentScaleY)
          logger.debug(s"Applied scaling transformation: ${currentScaleX}x${currentScaleY}")
        }

        // Clear the canvas with a dark gray color
        gc2D.setFill(Color.DarkGray)
        gc2D.fillRect(0, 0, canvasWidth / currentScaleX, canvasHeight / currentScaleY)

        // Draw grid lines if enabled
        if (gridLines) drawGridLines(gc2D)

        // Draw focus tree content
        val focuses = _focusTree.map(_.focuses).orNull
        focuses match {
          case null =>
            logger.warn("Focuses list is null, cannot draw focus tree.")
          case _ if focuses.isEmpty =>
            logger.warn("Focuses list is empty, nothing to draw.")
          case _ =>
            logger.debug(s"Drawing ${focuses.size} focuses...")
            drawPrerequisites(gc2D, focuses, getMinX)
            drawMutuallyExclusiveFocuses(gc2D, focuses, getMinX)
            focuses.foreach { focus => drawFocus(gc2D, focus, getMinX) }
        }

        logger.debug("Focus tree drawing completed successfully")

      } finally {
        gc2D.restore() // Always restore transform state
      }

    } catch {
      case ex: Exception =>
        logger.error("Error during Canvas drawing", ex)
    } finally {
      // Always reset the drawing flag
      isDrawing = false
    }
  }

  /**
   * Safe method to redraw the focus tree (can be called from any thread)
   */
  def redrawFocusTree(): Unit = {
    drawFocusTree()
  }

  /**
   * Get current scale factors (useful for hit testing or coordinate conversion)
   */
  def getCurrentScale: (Double, Double) = (currentScaleX, currentScaleY)

  /**
   * Method to handle Canvas resize events (if needed)
   * @param canvas Canvas to add listeners to
   */
  private def addResizeHandlers(canvas: Canvas): Unit = {
    canvas.widthProperty().addListener((obs, oldVal, newVal) => {
      val newWidth = newVal.doubleValue()
      if (newWidth > 0 && !isDrawing) Platform.runLater(() => redrawFocusTree())
    })

    canvas.heightProperty().addListener((obs, oldVal, newVal) => {
      val newHeight = newVal.doubleValue()
      if (newHeight > 0 && !isDrawing) Platform.runLater(() => redrawFocusTree())
    })
  }

  /**
   * Initialization method to call when setting up the Canvas
   */
  def initializeFocusTreeCanvas(): Unit = {
    if (focusTreeCanvas != null) {
      addResizeHandlers(focusTreeCanvas)
    } else {
      logger.error("Cannot initialize null focusTreeCanvas")
    }
  }

  /**
   * Check if Canvas dimensions are within safe limits
   * @param width Desired Canvas width
   * @param height Desired Canvas height
   */
  def validateCanvasDimensions(width: Double, height: Double): Boolean = width > 0 && height > 0

  /**
   * Update the Canvas and AnchorPane sizes based on focus tree dimensions
   * @param gc GraphicsContext to use for size calculations
   */
  def drawGridLines(gc: GraphicsContext): Unit = {
    val minX = getMinX
    val maxX = getMaxX
    val maxY = getMaxY

    gc.setStroke(Color.Gray)
    gc.setLineWidth(1)

    // Vertical lines
    for (x <- minX to maxX) {
      val x1 = focusToCanvasXAbs(x)
      val y1 = 0
      val y2 = focusToCanvasYAbs(maxY)
      gc.strokeLine(x1, y1, x1, y2)
    }

    // Horizontal lines
    for (y <- 1 to maxY) {
      val x1 = focusToCanvasXAbs(minX)
      val x2 = focusToCanvasXAbs(maxX)
      val y1 = focusToCanvasYAbs(y)
      gc.strokeLine(x1, y1, x2, y1)
    }

    // Write coordinates
    val font = gc.getFont
    for {
      x <- minX to maxX
      y <- 1 to maxY
    } {
      val x1 = focusToCanvasXAbs(x) - 8
      val y1 = focusToCanvasYAbs(y) - 5
      gc.setFont(new javafx.scene.text.Font("Arial", 8))
      gc.fillText(s"$x, $y", x1, y1)
    }
    // Reset font
    gc.setFont(font)
  }

  /**
   * Draw lines for focus prerequisites
   * @param gc2D GraphicsContext to draw on
   * @param focuses List of focuses to process
   * @param minX Minimum X offset for positioning
   */
  def drawPrerequisites(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit = {
    gc2D.setStroke(Color.Black)
    gc2D.setLineWidth(3)

    val focusesWithPrereqs = focuses.filter(_.hasPrerequisites)
    val numFocuses = focusesWithPrereqs.size
    logger.debug(s"Drawing prerequisites for $numFocuses focuses...")

    focusesWithPrereqs.foreach { focus =>
      val prereqFocuses = focus.prerequisiteList

      // Calculate the main focus coordinates
      val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + X_OFFSET_FIX
      val y1 = focusToCanvasY(focus)
      val lineX1 = x1 + (FOCUS_X_SCALE / 2)
      val liney1 = y1 + (FOCUS_Y_SCALE / 2)
      val lineX2 = lineX1
      val liney2 = y1 - 12 // Upward offset

      // For each prerequisite focus, draw the connecting lines
      for (i <- prereqFocuses.indices) {
        val prereqFocus = prereqFocuses(i)
        val lineX4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX - minX)) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val liney4 = (FOCUS_Y_SCALE * prereqFocus.absoluteY) + (FOCUS_Y_SCALE / 2) + Y_OFFSET_FIX
        val lineX3 = lineX4
        val liney3 = liney2

        // Draw the three segments
        gc2D.strokeLine(lineX1, liney1, lineX2, liney2)
        gc2D.strokeLine(lineX2, liney2, lineX3, liney3)
        gc2D.strokeLine(lineX3, liney3, lineX4, liney4)
      }
    }
  }

  /**
   * Draw lines and circles for mutually exclusive focuses
   * @param gc2D GraphicsContext to draw on
   * @param focuses List of focuses to process
   * @param minX Minimum X offset for positioning
   */
  def drawMutuallyExclusiveFocuses(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit = {
    gc2D.setStroke(Color.DarkRed)
    gc2D.setLineWidth(3)

    focuses.filter(_.isMutuallyExclusive).foreach { focus =>
      val mutuallyExclusiveFocuses = focus.mutuallyExclusiveList

      mutuallyExclusiveFocuses.foreach { mutexFocus =>
        val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val y1 = FOCUS_Y_SCALE * focus.absoluteY + (FOCUS_Y_SCALE / 1.6).toInt + Y_OFFSET_FIX
        val x2 = FOCUS_X_SCALE * (mutexFocus.absoluteX - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val y2 = FOCUS_Y_SCALE * mutexFocus.absoluteY + (FOCUS_Y_SCALE / 1.6).toInt + Y_OFFSET_FIX

        // Draw a line between the two mutually exclusive focuses
        gc2D.strokeLine(x1, y1, x2, y2)

        // Draw circles at both ends
        gc2D.setFill(Color.DarkRed)
        gc2D.fillOval(x1 - 5, y1 - 5, 10, 10)
        gc2D.fillOval(x2 - 5, y2 - 5, 10, 10)
      }
    }
  }

  /**
   * Draw a single focus at its calculated position
   * @param gc2D GraphicsContext to draw on
   * @param focus Focus to draw
   * @param minX Minimum X offset for positioning
   */
  def drawFocus(gc2D: GraphicsContext, focus: Focus, minX: Int): Unit = {
    val isSelected = selectedFocuses.contains(focus)
    val isHovered = hoveredFocus.contains(focus)

    gc2D.setFill(Color.White)
    val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + X_OFFSET_FIX
    val y1 = focusToCanvasY(focus)
    val yAdj1 = (FOCUS_Y_SCALE / 2.2).toInt
    val yAdj2 = (FOCUS_Y_SCALE / 2) + 20

    // Calculate the full focus area bounds (icon + name plate + text)
    val focusAreaX = x1 - 32  // Slightly wider than name plate (-32)
    val focusAreaY = y1       // Start from the focus icon top
    val focusAreaWidth = FOCUS_X_SCALE  // Name plate width
    val focusAreaHeight = FOCUS_Y_SCALE  // From icon top to below text

    // Draw hover border first (behind everything else) if hovered
    if (isHovered && !isSelected) {
      gc2D.setStroke(Color.White)
      gc2D.setLineWidth(2)
      gc2D.strokeRect(focusAreaX, focusAreaY, focusAreaWidth, focusAreaHeight)
    }

    // Focus name plate gfx (focus unavailable version)
    gfxFocusUnavailable match
      case null =>
        logger.warn("Focus unavailable image is null, using default color fill.")
        gc2D.setFill(Color.Gray)
        gc2D.fillRect(x1 - 32, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3)
      case img =>
        gc2D.drawImage(img, x1 - 32, y1 + yAdj1)

    // Focus icon
    focus.getDDSImage.foreach(img => gc2D.drawImage(img, x1, y1))

    // Focus name text
    val name = focus.localizationText(Property.NAME) match
      case Some(text) => text
      case None => if (focus.id.nonEmpty) focus.id.str else "[localization missing]"
    gc2D.fillText(name, x1 - 20, y1 + yAdj2)

    // Draw selection border (on top of everything) if selected
    if (isSelected) {
      gc2D.setStroke(Color.Yellow)
      gc2D.setLineWidth(2)
      gc2D.strokeRect(focusAreaX, focusAreaY, focusAreaWidth, focusAreaHeight)
    }
  }


  /**
   * Convert mouse coordinates to content coordinates accounting for scroll position
   * @param mouseX Mouse X coordinate relative to the ScrollPane
   * @param mouseY Mouse Y coordinate relative to the ScrollPane
   */
  private def adjustMouseCoordinatesForScroll(mouseX: Double, mouseY: Double): Point2D = {
    // Get the current scroll position
    val scrollX = this.getHvalue * math.max(0, focusTreeCanvasAnchorPane.prefWidth.value - this.getViewportBounds.getWidth)
    val scrollY = this.getVvalue * math.max(0, focusTreeCanvasAnchorPane.prefHeight.value - this.getViewportBounds.getHeight)

    // Adjust mouse coordinates by scroll offset
    new Point2D(mouseX + scrollX, mouseY + scrollY)
  }

  /**
   * Add mouse event handlers to the focus tree pane
   * @param p Pane to add handlers to
   */
  def getFocusHover(p: Point2D): Option[Focus] = {
    _focusTree.flatMap { tree =>
      val adjustedP = adjustMouseCoordinatesForScroll(p.getX, p.getY)
      val x = (adjustedP.getX / FOCUS_X_SCALE).toInt + tree.minX
      val y = (adjustedP.getY / FOCUS_Y_SCALE).toInt

      tree.focuses.find(_.hasAbsolutePosition(x, y))
    }
  }

  def focusToCanvasX(f: Focus): Int = focusToCanvasXAbs(f.absoluteX)

  def focusToCanvasXAbs(focusAbsX: Int): Int = FOCUS_X_SCALE * (focusAbsX - getMinX) + X_OFFSET_FIX

  def focusToCanvasY(f: Focus): Int = focusToCanvasYAbs(f.absoluteY)

  def focusToCanvasYAbs(focusAbsY: Int): Int = FOCUS_Y_SCALE * (focusAbsY - getMinY) + Y_OFFSET_FIX

  def canvasToFocusX(canvasX: Double): Int = ((canvasX - X_OFFSET_FIX) / FOCUS_X_SCALE + getMinX).toInt

  def canvasToFocusY(canvasY: Double): Int = ((canvasY - Y_OFFSET_FIX) / FOCUS_Y_SCALE).toInt

  def isWithinMarquee(f: Focus): Boolean = {
    val focusX = focusToCanvasX(f).toDouble
    val focusY = focusToCanvasY(f).toDouble
    focusMarqueeRectangle().contains(focusX, focusY)
  }

  /**
   * Get the current marquee selection rectangle in canvas coordinates
   * @return Rectangle2D representing the marquee selection area
   */
  def focusMarqueeRectangle(): Rectangle2D = {
    (marqueeStartPoint, marqueeEndPoint) match {
      case (Some(start), Some(end)) =>
        new Rectangle2D(
          math.min(start.getX, end.getX) - (CENTER_FOCUS_X / 2.0),
          math.min(start.getY, end.getY) - (CENTER_FOCUS_Y / 2.0),
          math.abs(end.getX - start.getX) + CENTER_FOCUS_X,
          math.abs(end.getY - start.getY) + CENTER_FOCUS_X
        )
      case _ => new Rectangle2D(0, 0, 0, 0)
    }
  }

  def limitFocusMoveX(newFocusX: Int): Int = {
    val minX = getMinX
    val maxX = getMaxX
    math.max(minX, math.min(maxX, newFocusX))
  }

  def limitFocusMoveY(newFocusY: Int): Int = {
    val minY = getMinY
    val maxY = getMaxY
    math.max(minY, math.min(maxY, newFocusY))
  }

  // Event handlers:

  /**
   * Add mouse event handlers to the focus tree pane
   *
   * @param comboBox  ComboBox to add handlers to
   * @param typedText Text typed by the user
   */
  @FXML
  def selectClosestMatch(comboBox: ComboBox[FocusTree], typedText: String): Unit = {
    comboBox.getItems.forEach { item =>
      item.countryTag match {
        case Some(countryTag) if countryTag.toString.toLowerCase().startsWith(typedText.toLowerCase()) =>
          comboBox.getSelectionModel.select(item)
          comboBox.getEditor.setText(item.toString)
          return
        case _ => // Continue to next item
      }
    }
  }

  @FXML
  def toggleGridLines(): Unit = {
    gridLines = !gridLines
    drawFocusTree()
  }

  private var hoveredFocus: Option[Focus] = None

  /**
   * Handle mouse move events for showing focus tooltips
   * @param e MouseEvent
   */
  @FXML
  def handleFocusTreeViewMouseMoved(e: MouseEvent): Unit = {
    val p = new Point2D(e.getX, e.getY)
    val focusTemp = getFocusHover(p) // getFocusHover now handles the adjustment internally

    focusTemp match {
      case None =>
        focusTooltipView.foreach(_.hide())
        focusTooltipView = None
        focusDetailsFocus = None
        // Clear hovered focus and redraw if there was one
        if (hoveredFocus.isDefined) {
          hoveredFocus = None
          drawFocusTree()
        }
      case Some(focus) if focusDetailsFocus.contains(focus) =>
        // Same focus for tooltip, but check if hover changed
        if (!hoveredFocus.contains(focus)) {
          hoveredFocus = Some(focus)
          drawFocusTree()
        }
      case Some(focus) =>
        focusDetailsFocus = Some(focus)
        focusTooltipView.foreach(_.hide())

        val details = focus.toScript
        val tooltip = new Tooltip(details)
        tooltip.show(focusTreeCanvas, e.getScreenX + 10, e.getScreenY + 10)
        focusTooltipView = Some(tooltip)

        // Update hovered focus and redraw if changed
        if (!hoveredFocus.contains(focus)) {
          hoveredFocus = Some(focus)
          drawFocusTree()
        }
    }
  }

  /**
   * Handle mouse press events for selecting focuses or starting marquee selection
   * @param e MouseEvent
   */
  @FXML
  def handleFocusTreeViewMousePressed(e: MouseEvent): Unit = {
    if (e.isPrimaryButtonDown) {
      selectedFocuses.clear()

      val adjustedP = adjustMouseCoordinatesForScroll(e.getX, e.getY)
      val internalX = ((adjustedP.getX - X_OFFSET_FIX) / FOCUS_X_SCALE).toInt + _focusTree.map(_.minX).getOrElse(0)
      val internalY = ((adjustedP.getY - Y_OFFSET_FIX) / FOCUS_Y_SCALE).toInt

      draggedFocus = _focusTree.flatMap(_.focuses.find(f => f.absoluteX == internalX && f.absoluteY == internalY))
      draggedFocus.foreach(focus => logger.info(s"Focus $focus selected"))
    } else if (e.isSecondaryButtonDown) {
      val contextMenu = new ContextMenu()
      val addFocusItem = new MenuItem("Add Focus")
      val newFocusTreeItem = new MenuItem("New Focus Tree")

      addFocusItem.setOnAction { _ =>
        logger.info("Adding focus via context menu")
        _focusTree.foreach { tree =>
          val newFocus = new Focus(tree)
          tree.addNewFocus(newFocus)
          val adjustedP = adjustMouseCoordinatesForScroll(e.getX, e.getY)
          newFocus.setAbsoluteXY(canvasToFocusX(adjustedP.getX), canvasToFocusY(adjustedP.getY), false)
          newFocus.setID(tree.nextTempFocusID())
          openPDXEditor(newFocus, () => drawFocusTree()) // TODO TODO TODO
        }
      }

      newFocusTreeItem.setOnAction { _ =>
        logger.info("Creating new focus tree via context menu")
        //openNewFocusTreeWindow()      // TODO TODO TODO
      }

      contextMenu.getItems.addAll(addFocusItem, newFocusTreeItem)
      contextMenu.show(focusTreeCanvas, e.getScreenX, e.getScreenY)
    }
  }

  /**
   * Handle mouse drag events for moving focuses or drawing marquee selection
   * @param e MouseEvent
   */
  @FXML
  def handleFocusTreeViewMouseDragged(e: MouseEvent): Unit = {
    if (e.isPrimaryButtonDown && draggedFocus.isDefined) {
      val adjustedP = adjustMouseCoordinatesForScroll(e.getX, e.getY)
      val newX = limitFocusMoveX(canvasToFocusX(adjustedP.getX))
      val newY = limitFocusMoveY(canvasToFocusY(adjustedP.getY))

      draggedFocus.foreach { focus =>
        if (!focus.hasRelativePosition(newX, newY)) {
          val prevDim = focusTreeViewDimension()
          val prev = focus.setAbsoluteXY(newX, newY, e.isShiftDown)

          if (!prev.equals(focus.position)) {
            logger.info(s"Focus $focus moved to $newX, $newY")
          }

          drawFocusTree()
          adjustFocusTreeViewport(prevDim)
        }
      }
    } else if (e.isSecondaryButtonDown) {
      val adjustedP = adjustMouseCoordinatesForScroll(e.getX, e.getY)
      if (marqueeStartPoint.isEmpty) {
        marqueeStartPoint = Some(adjustedP)
      } else {
        marqueeEndPoint = Some(adjustedP)
      }
    }
  }

  def adjustFocusTreeViewport(prevDim: Dimension): Unit = {
    val dim = focusTreeViewDimension()
    if (!dim.equals(prevDim)) {
      val x = this.getHvalue
      val y = this.getVvalue
      val xRatio = x * prevDim.width / dim.width
      val yRatio = y * prevDim.height / dim.height
      this.setHvalue(xRatio)
      this.setVvalue(yRatio)
    }
  }

  @FXML
  def handleFocusTreeViewMouseReleased(@FXML e: MouseEvent): Unit = {
    if (draggedFocus.isDefined) {
      draggedFocus = None
    }

    if (marqueeStartPoint.isDefined && marqueeEndPoint.isDefined) {
      selectedFocuses.clear()
      _focusTree.foreach { tree =>
        selectedFocuses.addAll(tree.focuses.filter(isWithinMarquee))
      }

      selectedFocuses.foreach(focus => logger.info(s"Marquee selected focus: $focus"))

      marqueeStartPoint = None
      marqueeEndPoint = None
    }

    drawFocusTree()
  }

  @FXML
  def handleFocusTreeViewMouseClicked(event: MouseEvent): Unit = {
    event.button match {
      case MouseButton.Primary =>
        if (event.getClickCount == 2) {
          val clickedPoint = new Point2D(event.getX, event.getY)
          val clickedFocus = getFocusHover(clickedPoint) // getFocusHover handles adjustment internally
          clickedFocus match {
            case None =>
              logger.info("No focus clicked.")
              JOptionPane.showMessageDialog(null, "No focus clicked.", "Info", JOptionPane.INFORMATION_MESSAGE)
            case Some(focus) =>
              logger.info(s"Focus clicked: $focus")
              openPDXEditor(focus, () => drawFocusTree())
          }
        }
      case MouseButton.Secondary =>
        if (selectedFocuses.nonEmpty) {
          // open context menu via right click
          // actions for the selected focuses
          val contextMenu = new ContextMenu()
          val setRelativeFocusItem = new MenuItem("Set Relative Focus")
          setRelativeFocusItem.setOnAction(_ => {
            // TODO: Replace this with your desired action
            logger.info("Set relative focus for selected focuses")
          })
          contextMenu.getItems.add(setRelativeFocusItem)
          contextMenu.show(focusTreeCanvas, event.getScreenX, event.getScreenY)
        }
      case _ => // do nothing for other mouse buttons
    }
  }

  def focusTree: Option[FocusTree] = _focusTree

  def focusTree_= (focusTree: Option[FocusTree]): Unit = {
    _focusTree = focusTree
    drawFocusTree()
  }

  def focusTree_=(focusTree: FocusTree): Unit = this.focusTree = Some(focusTree)

  private def addFocusTreePaneHandlers(): Unit = {
    addPanningHandlers()
    this.onMouseMoved = handleFocusTreeViewMouseMoved
    this.onMousePressed = handleFocusTreeViewMousePressed
    this.onMouseDragged = handleFocusTreeViewMouseDragged
    this.onMouseReleased = handleFocusTreeViewMouseReleased
    this.onMouseClicked = handleFocusTreeViewMouseClicked
  }

  private def addPanningHandlers(): Unit = {
    // Setup scroll pane behavior for middle mouse button
    this.setOnMousePressed { e =>
      if (e.getButton == MouseButton.Middle) {
        logger.debug("Middle mouse button pressed - enabling panning.")
        this.setPannable(true)
      }
    }

    this.setOnMouseReleased { e =>
      if (e.getButton == MouseButton.Middle) {
        logger.debug("Middle mouse button released - disabling panning.")
        this.setPannable(false)
      }
    }
  }

  private def updateContentSize(): Unit = {
    val cols = getMaxX - getMinX + 1
    val rows = getMaxY - getMinY + 1
    val w = cols * FOCUS_X_SCALE + X_OFFSET_FIX * 2
    val h = rows * FOCUS_Y_SCALE + Y_OFFSET_FIX * 2

    focusTreeCanvasAnchorPane.prefWidth = w
    focusTreeCanvasAnchorPane.prefHeight = h
  }

  private def openPDXEditor(pdxScript: PDXScript[?], onUpdate: Runnable = null): Unit = {
    val editor = PDXEditorController()
    if (onUpdate != null) editor.open(pdxScript, onUpdate) else editor.open(pdxScript)
  }

}