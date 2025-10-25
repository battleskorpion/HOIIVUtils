package com.hoi4utils.ui.focus_view

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTree}
import com.hoi4utils.hoi4mod.localization.Property
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.pdxscript.PDXEditorController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.canvas.GraphicsContext
import scalafx.Includes.*
import scalafx.geometry.{Point2D, Rectangle2D}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.*
import scalafx.scene.image.Image
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.layout.AnchorPane
import scalafx.scene.paint.Color

import javax.swing.JOptionPane
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.boundary

@Deprecated
class FocusTreeScrollPane(private var _focusTree: Option[FocusTree]) extends ScrollPane with LazyLogging:
  // Constants
  private val FOCUS_X_SCALE: Int = 90
  private val CENTER_FOCUS_X: Int = FOCUS_X_SCALE / 2
  private val FOCUS_Y_SCALE: Int = 140
  private val CENTER_FOCUS_Y: Int = FOCUS_Y_SCALE / 2
  private val X_OFFSET_FIX: Int = 30
  private val Y_OFFSET_FIX: Int = 40

  // vars
  private var gridLines: Boolean = false
  private val selectedFocuses: ListBuffer[Focus] = ListBuffer.empty
  private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")
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
  private def drawFocusTree(): Unit =
    logger.debug("Drawing focus tree...")

    // Prevent concurrent drawing operations
    if isDrawing then
      logger.debug("Drawing already in progress, skipping...")
      return ()

    // Ensure the Canvas is properly initialized
    if focusTreeCanvas == null then
      logger.error("focusTreeCanvas is null. Cannot draw focus tree.")
      return ()

    updateContentSize()
    performDrawing()

  def getMinX: Int = _focusTree.map(_.minX).getOrElse(0)

  def getMinY: Int = 0

  private def focusTreeViewLength(): Int = getMaxX - getMinX

  private def focusTreeViewHeight(): Int = getMaxY - getMinY

  private def focusTreeViewDimension(): Dimension = Dimension(focusTreeViewLength(), focusTreeViewHeight())

  private def getMaxX: Int =
    _focusTree.map(_.focuses.map(_.absoluteX).maxOption.getOrElse(10)).getOrElse(10)

  private def getMaxY: Int =
    _focusTree.map(_.focuses.map(_.absoluteY).maxOption.getOrElse(10)).getOrElse(10)

  private def loadFocusUnavailableImage(focusUnavailablePath: String): Image = boundary:
    val inputStream =
      try
        getClass.getClassLoader.getResourceAsStream(focusUnavailablePath)
      catch
        case e: Exception =>
          logger.error(s"Failed to load focus unavailable image from $focusUnavailablePath", e)
          boundary.break(null)
    val buffer = new Array[Byte](inputStream.available)
    inputStream.read(buffer)
    inputStream.close()
    DDSReader.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match
        case Some(value) => value
        case None => ???,
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )

  /**
   * Performs the actual drawing operations with proper scaling
   */
  private def performDrawing(): Unit =
    isDrawing = true
    try
      val gc2D = focusTreeCanvas.getGraphicsContext2D

      if gc2D == null then
        logger.error("GraphicsContext2D is null. Cannot draw focus tree.")
        isDrawing = false
        return ()

      // Verify Canvas dimensions are valid and match expected values
      val canvasWidth = focusTreeCanvas.getWidth
      val canvasHeight = focusTreeCanvas.getHeight

      if canvasWidth <= 0 || canvasHeight <= 0 then
        logger.error(s"Invalid actual Canvas dimensions: width=$canvasWidth, height=$canvasHeight")
        isDrawing = false
        return ()

      // Apply scaling transformation if needed
      gc2D.save() // Save current transform state
      try
        if currentScaleX != 1.0 || currentScaleY != 1.0 then
          gc2D.scale(currentScaleX, currentScaleY)
          logger.debug(s"Applied scaling transformation: ${currentScaleX}x${currentScaleY}")

        // Clear the canvas with a dark gray color
        gc2D.setFill(Color.DarkGray)
        gc2D.fillRect(0, 0, canvasWidth / currentScaleX, canvasHeight / currentScaleY)

        // Draw grid lines if enabled
        if gridLines then drawGridLines(gc2D)

        // Draw focus tree content
        val focuses = _focusTree.map(_.focuses).orNull
        focuses match
          case null =>
            logger.warn("Focuses list is null, cannot draw focus tree.")
          case _ if focuses.isEmpty =>
            logger.warn("Focuses list is empty, nothing to draw.")
          case _ =>
            logger.debug(s"Drawing ${focuses.size} focuses...")
            drawPrerequisites(gc2D, focuses, getMinX)
            drawMutuallyExclusiveFocuses(gc2D, focuses, getMinX)
            focuses.foreach(focus => drawFocus(gc2D, focus, getMinX))

        logger.debug("Focus tree drawing completed successfully")

      finally
        gc2D.restore() // Always restore transform state

    catch
      case ex: Exception =>
        logger.error("Error during Canvas drawing", ex)
    finally
      // Always reset the drawing flag
      isDrawing = false

  /**
   * Safe method to redraw the focus tree (can be called from any thread)
   */
  def redrawFocusTree(): Unit =
    drawFocusTree()

  /**
   * Get current scale factors (useful for hit testing or coordinate conversion)
   */
  def getCurrentScale: (Double, Double) = (currentScaleX, currentScaleY)

  /**
   * Method to handle Canvas resize events (if needed)
   */
  private def addResizeHandlers(canvas: Canvas): Unit =
    canvas.widthProperty().addListener: (obs, oldVal, newVal) =>
      val newWidth = newVal.doubleValue()
      if newWidth > 0 && !isDrawing then Platform.runLater: () =>
        redrawFocusTree()

    canvas.heightProperty().addListener: (obs, oldVal, newVal) =>
      val newHeight = newVal.doubleValue()
      if newHeight > 0 && !isDrawing then Platform.runLater: () =>
        redrawFocusTree()

  /**
   * Initialization method to call when setting up the Canvas
   */
  def initializeFocusTreeCanvas(): Unit =
    if focusTreeCanvas != null then
      addResizeHandlers(focusTreeCanvas)
    else
      logger.error("Cannot initialize null focusTreeCanvas")

  /**
   * Check if Canvas dimensions are within safe limits
   */
  def validateCanvasDimensions(width: Double, height: Double): Boolean = width > 0 && height > 0

  def drawGridLines(gc: GraphicsContext): Unit =
    val minX = getMinX
    val maxX = getMaxX
    val maxY = getMaxY

    gc.setStroke(Color.Gray)
    gc.setLineWidth(1)

    // Vertical lines
    for x <- minX to maxX do
      val x1 = focusToCanvasXAbs(x)
      val y1 = 0
      val y2 = focusToCanvasYAbs(maxY)
      gc.strokeLine(x1, y1, x1, y2)

    // Horizontal lines
    for y <- 1 to maxY do
      val x1 = focusToCanvasXAbs(minX)
      val x2 = focusToCanvasXAbs(maxX)
      val y1 = focusToCanvasYAbs(y)
      gc.strokeLine(x1, y1, x2, y1)

    // Write coordinates
    val font = gc.getFont
    for
      x <- minX to maxX
      y <- 1 to maxY
    do
      val x1 = focusToCanvasXAbs(x) - 8
      val y1 = focusToCanvasYAbs(y) - 5
      gc.setFont(new javafx.scene.text.Font("Arial", 8))
      gc.fillText(s"$x, $y", x1, y1)
    // Reset font
    gc.setFont(font)

  def drawPrerequisites(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit =
    gc2D.setStroke(Color.Black)
    gc2D.setLineWidth(3)

    val focusesWithPrereqs = focuses.filter(_.hasPrerequisites)
    val numFocuses = focusesWithPrereqs.size
    logger.debug(s"Drawing prerequisites for $numFocuses focuses...")

    focusesWithPrereqs.foreach: focus =>
      val prereqFocuses = focus.prerequisiteList

      // Calculate the main focus coordinates
      val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + X_OFFSET_FIX
      val y1 = focusToCanvasY(focus)
      val lineX1 = x1 + (FOCUS_X_SCALE / 2)
      val liney1 = y1 + (FOCUS_Y_SCALE / 2)
      val lineX2 = lineX1
      val liney2 = y1 - 12 // Upward offset

      // For each prerequisite focus, draw the connecting lines
      for i <- prereqFocuses.indices do
        val prereqFocus = prereqFocuses(i)
        val lineX4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX - minX)) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val liney4 = (FOCUS_Y_SCALE * prereqFocus.absoluteY) + (FOCUS_Y_SCALE / 2) + Y_OFFSET_FIX
        val lineX3 = lineX4
        val liney3 = liney2

        // Draw the three segments
        gc2D.strokeLine(lineX1, liney1, lineX2, liney2)
        gc2D.strokeLine(lineX2, liney2, lineX3, liney3)
        gc2D.strokeLine(lineX3, liney3, lineX4, liney4)

  def drawMutuallyExclusiveFocuses(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit =
    gc2D.setStroke(Color.DarkRed)
    gc2D.setLineWidth(3)

    focuses.filter(_.isMutuallyExclusive).foreach: focus =>
      val mutuallyExclusiveFocuses = focus.mutuallyExclusiveList

      mutuallyExclusiveFocuses.foreach: mutexFocus =>
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

  def drawFocus(gc2D: GraphicsContext, focus: Focus, minX: Int): Unit =
    val isSelected = selectedFocuses.contains(focus)

    gc2D.setFill(Color.White)
    val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + X_OFFSET_FIX
    val y1 = focusToCanvasY(focus)
    val yAdj1 = (FOCUS_Y_SCALE / 2.2).toInt
    val yAdj2 = (FOCUS_Y_SCALE / 2) + 20

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
    val locName = focus.localizationText(Property.NAME)
    val name = locName match
      case None => if focus.id.str.nonEmpty then focus.id.str else "[Localization missing]"
      case Some(text) => text
    gc2D.fillText(name, x1 - 20, y1 + yAdj2)

    if isSelected then
      gc2D.setStroke(Color.Yellow)
      gc2D.setLineWidth(2)
      gc2D.strokeRect(x1 - FOCUS_X_SCALE / 2.3, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3)

  def getFocusHover(p: Point2D): Option[Focus] =
    _focusTree.flatMap: tree =>
      val x = (p.getX / FOCUS_X_SCALE).toInt + tree.minX
      val y = (p.getY / FOCUS_Y_SCALE).toInt

      tree.focuses.find(_.hasAbsolutePosition(x, y))

  @FXML
  def selectClosestMatch(comboBox: ComboBox[FocusTree], typedText: String): Unit =
    val matchingItem = comboBox.getItems.asScala.find: item =>
      item.countryTag match
        case Some(countryTag) => countryTag.toString.toLowerCase().startsWith(typedText.toLowerCase())
        case _ => false

    matchingItem.foreach: item =>
      comboBox.getSelectionModel.select(item)
      comboBox.getEditor.setText(item.toString)

  def focusToCanvasX(f: Focus): Int = focusToCanvasXAbs(f.absoluteX)

  def focusToCanvasXAbs(focusAbsX: Int): Int = FOCUS_X_SCALE * (focusAbsX - getMinX) + X_OFFSET_FIX

  def focusToCanvasY(f: Focus): Int = focusToCanvasYAbs(f.absoluteY)

  def focusToCanvasYAbs(focusAbsY: Int): Int = FOCUS_Y_SCALE * (focusAbsY - getMinY) + Y_OFFSET_FIX

  def canvasToFocusX(canvasX: Double): Int = ((canvasX - X_OFFSET_FIX) / FOCUS_X_SCALE + getMinX).toInt

  def canvasToFocusY(canvasY: Double): Int = ((canvasY - Y_OFFSET_FIX) / FOCUS_Y_SCALE).toInt

  def isWithinMarquee(f: Focus): Boolean =
    val focusX = focusToCanvasX(f).toDouble
    val focusY = focusToCanvasY(f).toDouble
    focusMarqueeRectangle().contains(focusX, focusY)

  def focusMarqueeRectangle(): Rectangle2D =
    (marqueeStartPoint, marqueeEndPoint) match
      case (Some(start), Some(end)) =>
        new Rectangle2D(
          math.min(start.getX, end.getX) - (CENTER_FOCUS_X / 2.0),
          math.min(start.getY, end.getY) - (CENTER_FOCUS_Y / 2.0),
          math.abs(end.getX - start.getX) + CENTER_FOCUS_X,
          math.abs(end.getY - start.getY) + CENTER_FOCUS_X
        )
      case _ => new Rectangle2D(0, 0, 0, 0)

  @FXML
  def toggleGridLines(): Unit =
    gridLines = !gridLines
    drawFocusTree()

  def limitFocusMoveX(newFocusX: Int): Int =
    val minX = getMinX
    val maxX = getMaxX
    math.max(minX, math.min(maxX, newFocusX))

  def limitFocusMoveY(newFocusY: Int): Int =
    val minY = getMinY
    val maxY = getMaxY
    math.max(minY, math.min(maxY, newFocusY))

  // Event handlers
  @FXML
  def handleFocusTreeViewMouseMoved(e: MouseEvent): Unit =
    val p = new Point2D(e.getX, e.getY)
    val focusTemp = getFocusHover(p)

    focusTemp match
      case None =>
        focusTooltipView.foreach(_.hide())
        focusTooltipView = None
        focusDetailsFocus = None
      case Some(focus) if focusDetailsFocus.contains(focus) =>
      // Same focus, do nothing
      case Some(focus) =>
        focusDetailsFocus = Some(focus)
        focusTooltipView.foreach(_.hide())

        val details = focus.toScript
        val tooltip = new Tooltip(details)
        tooltip.show(focusTreeCanvas, e.getScreenX + 10, e.getScreenY + 10)
        focusTooltipView = Some(tooltip)

  @FXML
  def handleFocusTreeViewMousePressed(e: MouseEvent): Unit =
    if e.isPrimaryButtonDown then
      selectedFocuses.clear()

      val internalX = ((e.getX - X_OFFSET_FIX) / FOCUS_X_SCALE).toInt + _focusTree.map(_.minX).getOrElse(0)
      val internalY = ((e.getY - Y_OFFSET_FIX) / FOCUS_Y_SCALE).toInt

      draggedFocus = _focusTree.flatMap(_.focuses.find(f => f.absoluteX == internalX && f.absoluteY == internalY))
      draggedFocus.foreach(focus => logger.info(s"Focus $focus selected"))
    else if e.isSecondaryButtonDown then
      val contextMenu = new ContextMenu()
      val addFocusItem = new MenuItem("Add Focus")
      val newFocusTreeItem = new MenuItem("New Focus Tree")

      addFocusItem.setOnAction: _ =>
        logger.info("Adding focus via context menu")
        _focusTree.foreach: tree =>
          val newFocus = new Focus(tree)
          tree.addNewFocus(newFocus)
          newFocus.setAbsoluteXY(canvasToFocusX(e.getX), canvasToFocusY(e.getY), false)
          newFocus.setID(tree.nextTempFocusID())
          openPDXEditor(newFocus, () => drawFocusTree()) // TODO TODO TODO

      newFocusTreeItem.setOnAction: _ =>
        logger.info("Creating new focus tree via context menu")
      //openNewFocusTreeWindow()      // TODO TODO TODO

      contextMenu.getItems.addAll(addFocusItem, newFocusTreeItem)
      contextMenu.show(focusTreeCanvas, e.getScreenX, e.getScreenY)

  @FXML
  def handleFocusTreeViewMouseDragged(e: MouseEvent): Unit =
    if e.isPrimaryButtonDown && draggedFocus.isDefined then
      val newX = limitFocusMoveX(canvasToFocusX(e.getX))
      val newY = limitFocusMoveY(canvasToFocusY(e.getY))

      draggedFocus.foreach: focus =>
        if !focus.hasRelativePosition(newX, newY) then
          val prevDim = focusTreeViewDimension()
          val prev = focus.setAbsoluteXY(newX, newY, e.isShiftDown)

          if !prev.equals(focus.relativePosition) then
            logger.info(s"Focus $focus moved to $newX, $newY")

          drawFocusTree()
          adjustFocusTreeViewport(prevDim)
    else if e.isSecondaryButtonDown then
      if marqueeStartPoint.isEmpty then
        marqueeStartPoint = Some(new Point2D(e.getX, e.getY))
      else
        marqueeEndPoint = Some(new Point2D(e.getX, e.getY))

  def adjustFocusTreeViewport(prevDim: Dimension): Unit =
    val dim = focusTreeViewDimension()
    if !dim.equals(prevDim) then
      val x = this.getHvalue
      val y = this.getVvalue
      val xRatio = x * prevDim.width / dim.width
      val yRatio = y * prevDim.height / dim.height
      this.setHvalue(xRatio)
      this.setVvalue(yRatio)

  @FXML
  def handleFocusTreeViewMouseReleased(@FXML e: MouseEvent): Unit =
    if draggedFocus.isDefined then
      draggedFocus = None

    if marqueeStartPoint.isDefined && marqueeEndPoint.isDefined then
      selectedFocuses.clear()
      _focusTree.foreach: tree =>
        selectedFocuses.addAll(tree.focuses.filter(isWithinMarquee))

      selectedFocuses.foreach(focus => logger.info(s"Marquee selected focus: $focus"))

      marqueeStartPoint = None
      marqueeEndPoint = None

    drawFocusTree()

  @FXML
  def handleFocusTreeViewMouseClicked(event: MouseEvent): Unit =
    event.button match
      case MouseButton.Primary =>
        if event.getClickCount == 2 then
          val clickedPoint = new Point2D(event.getX, event.getY)
          val clickedFocus = getFocusHover(clickedPoint)
          clickedFocus match
            case None =>
              logger.info("No focus clicked.")
              JOptionPane.showMessageDialog(null, "No focus clicked.", "Info", JOptionPane.INFORMATION_MESSAGE)
            case Some(focus) =>
              logger.info(s"Focus clicked: $focus")
              openPDXEditor(focus, () => drawFocusTree())
      case MouseButton.Secondary =>
        if selectedFocuses.nonEmpty then
          // open context menu via right click
          // actions for the selected focuses
          val contextMenu = new ContextMenu()
          val setRelativeFocusItem = new MenuItem("Set Relative Focus")
          setRelativeFocusItem.setOnAction: _ =>
            // TODO: Replace this with your desired action
            logger.info("Set relative focus for selected focuses")
          contextMenu.getItems.add(setRelativeFocusItem)
          contextMenu.show(focusTreeCanvas, event.getScreenX, event.getScreenY)
      case _ => // do nothing for other mouse buttons

  def focusTree: Option[FocusTree] = _focusTree

  def focusTree_= (focusTree: Option[FocusTree]): Unit =
    _focusTree = focusTree
    drawFocusTree()

  def focusTree_=(focusTree: FocusTree): Unit = this.focusTree = Some(focusTree)

  private def addFocusTreePaneHandlers(): Unit =
    addPanningHandlers()
    this.onMouseMoved = handleFocusTreeViewMouseMoved
    this.onMousePressed = handleFocusTreeViewMousePressed
    this.onMouseDragged = handleFocusTreeViewMouseDragged
    this.onMouseReleased = handleFocusTreeViewMouseReleased
    this.onMouseClicked = handleFocusTreeViewMouseClicked

  private def addPanningHandlers(): Unit =
    // Setup scroll pane behavior for middle mouse button
    this.setOnMousePressed: e =>
      if e.getButton == MouseButton.Middle then
        logger.debug("Middle mouse button pressed - enabling panning.")
        this.setPannable(true)

    this.setOnMouseReleased: e =>
      if e.getButton == MouseButton.Middle then
        logger.debug("Middle mouse button released - disabling panning.")
        this.setPannable(false)

  private def updateContentSize(): Unit =
    val cols = getMaxX - getMinX + 1
    val rows = getMaxY - getMinY + 1
    val w = cols * FOCUS_X_SCALE + X_OFFSET_FIX * 2
    val h = rows * FOCUS_Y_SCALE + Y_OFFSET_FIX * 2
    val fullMessage =
      s"""
         | Updating content size:
         | Max X: ${getMaxX}, Min X: ${getMinX}, Columns: $cols
         | Max Y: ${getMaxY}, Min Y: ${getMinY}, Rows: $rows
         | Calculated Width: $w, Calculated Height: $h
         |""".stripMargin
    logger.info(fullMessage)

    focusTreeCanvasAnchorPane.prefWidth = w
    focusTreeCanvasAnchorPane.prefHeight = h

  private def openPDXEditor(pdxScript: PDXScript[?], onUpdate: Runnable = null): Unit =
    val editor = PDXEditorController()
    if onUpdate != null then editor.open(pdxScript, onUpdate) else editor.open(pdxScript)