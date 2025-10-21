package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTree, FocusTreesManager, Focus as gridX}
import com.hoi4utils.script.MultiPDX
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.javafx.scene.control.ZoomableScrollPane
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.{DragEvent, Dragboard, MouseEvent, TransferMode}
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import scalafx.scene.input.ClipboardContent
import sun.jvm.hotspot.HelloWorld.e

import java.awt.Point
import javax.sound.sampled.Clip
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.compiletime.uninitialized
import scala.jdk.javaapi.CollectionConverters

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("/com/hoi4utils/ui/focus/FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")
  private val focusGridColumnsSize: Int = 100
  private val focusGridRowSize: Int = 200
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"
  @FXML var focusTree2: VBox = uninitialized
  @FXML var menuBar: MenuBar = uninitialized
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

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    replaceWithZoomableScrollPane()
    setupZoomButtons()
    clear()
    welcome.setToggleGroup(toggleGroup)
    welcome.fire()
    focusTreeView.setGridLinesVisible(lines)
    populateFocusSelection()
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
    layeredContent = new StackPane(focusTreeView)
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

  private def populateFocusSelection(): Unit =
    focusTreeView.setGridLinesVisible(lines)
    Some(FocusTreesManager.observeFocusTrees).foreach: trees =>
      trees.forEach: someFocusTree =>
        val toggleButton = ToggleButton(someFocusTree.toString)
        focusTreesToggleButtons += toggleButton
        toggleButton.setToggleGroup(toggleGroup)
        toggleButton.setOnAction: _ =>
          if toggleButton.isSelected then
            // Manually deselect all other buttons
            focusTreesToggleButtons.foreach: btn =>
              if btn != toggleButton then
                btn.setSelected(false)
                welcome.setSelected(false)
            welcome.setSelected(false)
            loadFocusTreeView(someFocusTree)
          else
            toggleButton.setSelected(false)
            toggleButton.setSelected(true)
            loadFocusTreeView(someFocusTree)
        toggleButton.setPadding(Insets(5, 10, 5, 10))
        vbox.getChildren.add(toggleButton)
      focusTreesCount.setText(s"Focus Trees: ${trees.size()}")

  /** Loads the given FocusTreeFile into the focusTreeView GridPane by creating it in a separate thread */
  private def loadFocusTreeView(someFocusTree: FocusTree): Unit =
    focusCountLabel.setText("Focuses: 0")
    cancelCurrentTask()
    clear()
    focusGridToggleGroup = new ToggleGroup()
    val focuses: MultiPDX[Focus] = someFocusTree.focuses

    // Calculate offset needed for negative coordinates
    val (offsetX, offsetY) = calculateGridOffset(focuses)

    // Calculate actual dimensions needed
    val (gridCols, gridRows) = calculateGridDimensions(focuses, offsetX, offsetY)

    // Setup column and row constraints
    for _ <- 0 until gridCols do setCC()
    for _ <- 0 until gridRows do setRC()
    focusTreeView.setGridLinesVisible(lines)
    val loadFocusTreeTask = new Task[GridPane]():
      override def call(): GridPane =
        val newGridPane = new GridPane()

        // Copy properties from the existing focusTreeView
        newGridPane.setHgap(focusTreeView.getHgap)
        newGridPane.setVgap(focusTreeView.getVgap)
        newGridPane.setGridLinesVisible(lines)
        newGridPane.getStyleClass.addAll(focusTreeView.getStyleClass)

        focusTreeView.getColumnConstraints.forEach: cc =>
          val newCC = new ColumnConstraints()
          newCC.setMinWidth(cc.getMinWidth)
          newCC.setPrefWidth(cc.getPrefWidth)
          newCC.setMaxWidth(cc.getMaxWidth)
          newGridPane.getColumnConstraints.add(newCC)

        focusTreeView.getRowConstraints.forEach: rc =>
          val newRC = new RowConstraints()
          newRC.setMinHeight(rc.getMinHeight)
          newRC.setPrefHeight(rc.getPrefHeight)
          newRC.setMaxHeight(rc.getMaxHeight)
          newGridPane.getRowConstraints.add(newRC)

        newGridPane.setOnDragOver: de =>
          if de.getGestureSource != newGridPane && de.getDragboard.hasString then
            de.acceptTransferModes(TransferMode.MOVE)
          de.consume()

        newGridPane.setOnDragDropped: de =>
          var intersected = de.getPickResult.getIntersectedNode
          // climb up to the direct child of the grid
          while intersected != null && (intersected.getParent != newGridPane) do
            intersected = intersected.getParent

          var targetGridX = 0
          var targetGridY = 0
          if intersected != null then
            val col = GridPane.getColumnIndex(intersected)
            val row = GridPane.getRowIndex(intersected)
            targetGridX = if col != null then col else 0
            targetGridY = if row != null then row else 0

          val src = de.getGestureSource
          if !src.isInstanceOf[FocusToggleButton] then
            de.setDropCompleted(false)
            de.consume()
          else
            val sourceButton = src.asInstanceOf[FocusToggleButton]
            val db = de.getDragboard
            val isShiftDown = db.getString.contains("shift:true")
            var success = false
            if db.hasString then
              val data = db.getString
              // Process the dropped data (e.g., add the ToggleButton to the pane)
              // For example, if you want to move the actual ToggleButton:
              // ((Pane) myToggleButton.getParent()).getChildren().remove(myToggleButton);
              // dropTargetPane.getChildren().add(myToggleButton)
              updateFocusPosition(sourceButton.focus, gridToFocusXY(targetGridX, targetGridY, sourceButton.focusTree), isShiftDown)
              System.out.println("Dropped: " + data + "x: " + targetGridX + "y: " + targetGridY)
              success = true
            de.setDropCompleted(success)
            de.consume()

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
            val focusCount = focuses.size
            val emptyCount = (gridRows * gridCols) - focusCount
            val totalWork = focusCount
            var workDone = 0
            var cuteFocusCounter = 0

            // Add focus buttons
            focuses.foreach: focus =>
              if !isCancelled then
                val gridX = focus.absoluteX + offsetX
                val gridY = focus.absoluteY + offsetY
                val linePane = new AnchorPane()
                linePane.setPrefWidth(100)
                linePane.setPrefHeight(200)
                linePane.setMouseTransparent(true)
                linePane.setBackground(Background.EMPTY)

                focus.prerequisiteList.foreach(prerequisiteFocus ⇒
                  val preGridX = prerequisiteFocus.absoluteX + offsetX
                  val preGridY = prerequisiteFocus.absoluteY + offsetY

                  // check if our focus is more than 1 cell below prerequisite
                  if gridY == preGridY + 1 then
                    if gridX > preGridX then
                      val eastline = new Line(0, 10, 50, 10)
                      val southlinecut = new Line(50, 10, 50, 100)
                      linePane.getChildren.addAll(eastline, southlinecut)
                    else if gridX < preGridX then
                      val westline = new Line(100, 10, 50, 10)
                      val southlinecut = new Line(50, 10, 50, 100)
                      linePane.getChildren.addAll(westline, southlinecut)
                    else
                      val southline = new Line(50, 0, 50, 100)
                      linePane.getChildren.addAll(southline)
                  else
                    val southline = new Line(50, 0, 50, 100)
                    linePane.getChildren.addAll(southline)
                )

                focus.dependents.foreach(dependentFocus =>
                  val depGridX = dependentFocus.absoluteX + offsetX
                  val depGridY = dependentFocus.absoluteY + offsetY
                  val turnY = gridY + 1

                  // Only draw grid lines if dependent is MORE than 1 cell below
                  if turnY < depGridY then
                    val sameColumn = depGridX == gridX
                    val goingEast = depGridX > gridX

                    // First cell: corner or straight
                    val firstLine = if sameColumn then "south" else if goingEast then "eastsouth" else "southwest"
                    getLineAt(newGridPane, gridX, turnY) match
                      case Some(existingLine) =>
                        val combined = buildLineName(parseDirections(existingLine) ++ parseDirections(firstLine))
                        println(s"Cell ($gridX, $turnY) has $existingLine + $firstLine = $combined")
                        replaceLine(newGridPane, gridX, turnY, combined)
                      case None =>
                        val pane = loadLineFxml(firstLine)
                        if pane != null then newGridPane.add(pane, gridX, turnY)

                    // Horizontal
                    if !sameColumn then
                      val xRange = if goingEast then (gridX + 1) until depGridX else (depGridX + 1) until gridX
                      xRange.foreach: x =>
                        getLineAt(newGridPane, x, turnY) match
                          case Some(existingLine) =>
                            val combined = buildLineName(parseDirections(existingLine) ++ parseDirections("eastwest"))
                            println(s"Cell ($x, $turnY) has $existingLine + eastwest = $combined")
                            replaceLine(newGridPane, x, turnY, combined)
                          case None =>
                            val pane = loadLineFxml("eastwest")
                            if pane != null then newGridPane.add(pane, x, turnY)

                      // Corner
                      if turnY < depGridY then
                        val cornerLine = if goingEast then "southwest" else "eastsouth"
                        getLineAt(newGridPane, depGridX, turnY) match
                          case Some(existingLine) =>
                            val combined = buildLineName(parseDirections(existingLine) ++ parseDirections(cornerLine))
                            println(s"Cell ($depGridX, $turnY) has $existingLine + $cornerLine = $combined")
                            replaceLine(newGridPane, depGridX, turnY, combined)
                          case None =>
                            val pane = loadLineFxml(cornerLine)
                            if pane != null then newGridPane.add(pane, depGridX, turnY)

                    // Vertical
                    (turnY + 1 until depGridY).foreach: y =>
                      getLineAt(newGridPane, depGridX, y) match
                        case Some(existingLine) =>
                          val existingDirs = parseDirections(existingLine)
                          val newDirs = parseDirections("south")
                          val combined = buildLineName(existingDirs ++ newDirs)
                          if combined != existingLine then
                            println(s"Cell ($depGridX, $y) CHANGING: $existingLine → $combined")
                            replaceLine(newGridPane, depGridX, y, combined)
                          else
                            println(s"Cell ($depGridX, $y) already has south in $existingLine, no change")
                        case None =>
                          val pane = loadLineFxml("south")
                          if pane != null then newGridPane.add(pane, depGridX, y)
                )

                val focusButton = FocusToggleButton(focus)
                focusButton.setOnAction(_ => loadFocusView(focus))
                focusButton.setOnDragDetected(me => handleDraggedFocusButton(me, focusButton))
                focusButton.setToggleGroup(focusGridToggleGroup)
                val focusStackPane = new StackPane(linePane, focusButton)
                newGridPane.add(focusStackPane, gridX, gridY)

                workDone += 1
                cuteFocusCounter += 1

                Platform.runLater(() => focusCountLabel.setText(s"Focuses: $cuteFocusCounter"))
                updateProgress(workDone, totalWork)


        newGridPane

    // Load the completed UI
    loadFocusTreeTask.setOnSucceeded: _ =>
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
          currentLoadTask = None
        case _ =>
          logger.info("Task succeeded but was already replaced by another task")

    loadFocusTreeTask.setOnCancelled: _ =>
      focusTreeView.setGridLinesVisible(lines)
      logger.info(s"Task cancelled for focus tree: ${someFocusTree.name}")
      currentLoadTask = None

    loadFocusTreeTask.setOnFailed: _ =>
      focusTreeView.setGridLinesVisible(lines)
      logger.error(s"Task failed for focus tree: ${someFocusTree.name}", loadFocusTreeTask.getException)
      currentLoadTask = None

    updateProgressIndicator(loadFocusTreeTask)
    focusTreeView.setGridLinesVisible(lines)

    // Store the current task
    currentLoadTask = Some(loadFocusTreeTask)
    val thread = new Thread(loadFocusTreeTask)
    thread.setDaemon(true)
    thread.start()

  // Load the focus into the details pane
  private def loadFocusView(focus: Focus): Unit = if focusDetailsPaneController != null then focusDetailsPaneController.loadFocus(focus)

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
  private def calculateGridOffset(focuses: Iterable[Focus]): (Int, Int) =
    if focuses.isEmpty then
      (0, 0)
    else
      val minX = focuses.map(_.absoluteX).min
      val minY = focuses.map(_.absoluteY).min
      // Return absolute values if negative, otherwise 0
      (if minX < 0 then -minX else 0, if minY < 0 then -minY else 0)

  /**
   * Calculate the actual grid dimensions needed, accounting for offset
   */
  private def calculateGridDimensions(
                                       focuses: Iterable[Focus],
                                       offsetX: Int,
                                       offsetY: Int
                                     ): (Int, Int) =
    if focuses.isEmpty then
      (1, 1) // minimum grid size
    else
      val maxX = focuses.map(_.absoluteX + offsetX).max
      val maxY = focuses.map(_.absoluteY + offsetY).max
      (maxX + 1, maxY + 1) // +1 because we're 0-indexed

  private def gridToFocusX(gridX: Int, focusTree: FocusTree): Int = gridX + focusTree.minX
  private def gridToFocusY(gridY: Int, focusTree: FocusTree): Int = gridY
  private def gridToFocusXY(gridX: Int, gridY: Int, focusTree: FocusTree): Point =
    new Point(gridX + focusTree.minX, gridY)

  private def focusToGridX(focusX: Int, focusTree: FocusTree): Int = focusX - focusTree.minX
  private def focusToGridY(focusY: Int, focusTree: FocusTree): Int = focusY
  private def focusToGridXY(focusX: Int, focusY: Int, focusTree: FocusTree): Point =
    new Point(focusX - focusTree.minX, focusY)
  private def focusToGridX(focus: Focus): Int = focus.absoluteX - focus.focusTree.minX
  private def focusToGridY(focus: Focus): Int = focus.absoluteY
  private def focusToGridXY(focus: Focus): Point =
    new Point(focus.absoluteX - focus.focusTree.minX, focus.absoluteY)

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

  private def setCC() =
    val cc = new ColumnConstraints()
    cc.setMinWidth(focusGridColumnsSize)
    cc.setPrefWidth(focusGridColumnsSize)
    cc.setMaxWidth(focusGridColumnsSize)
    focusTreeView.getColumnConstraints.add(cc)

  private def setRC() =
    val rc = new RowConstraints()
    rc.setMinHeight(focusGridRowSize)
    rc.setPrefHeight(focusGridRowSize)
    rc.setMaxHeight(focusGridRowSize)
    focusTreeView.getRowConstraints.add(rc)

  /** Clears the focus tree view in the middle */
  private def clear(): Unit =
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()
    focusTreeView.setGridLinesVisible(lines)

  private def setupZoomButtons(): Unit =
    if zoomInButton != null then zoomInButton.setOnAction(_ => zoomableScrollPane.zoomIn())
    if zoomOutButton != null then zoomOutButton.setOnAction(_ => zoomableScrollPane.zoomOut())
    if resetZoomButton != null then resetZoomButton.setOnAction(_ => zoomableScrollPane.resetZoom())

  override def preSetup(): Unit = setupWindowControls(focusTree2, menuBar)

  @FXML def handleWelcome(): Unit =
    focusCountLabel.setText("")
    welcome.setSelected(true)
    focusTreesToggleButtons.foreach: btn =>
      btn.setSelected(false)
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

  def handleDraggedFocusButton(event: MouseEvent, toggleButton: FocusToggleButton): Unit =
    val db: Dragboard = toggleButton.startDragAndDrop(TransferMode.MOVE)
    val content: ClipboardContent = ClipboardContent()
    content.putString(if event.isShiftDown then "shift:true" else "shift:false")
    db.setContent(content)
    event.consume()

  def updateFocusPosition(focus: Focus, newFocusPos: Point, updateChildRelativeOffsets: Boolean): Unit =
    focus.setAbsoluteXY(newFocusPos, updateChildRelativeOffsets)
    // TODO: in future only do main button move if updateChild is on for now its debug time

    // get the valid Focus objects to match
    val relativelyPositionedFocuses = focus.selfAndRelativePositionedFocuses
    val focusToggleButtons = CollectionConverters.asScala(focusTreeView.getChildren)
    // filter children to only the FocusToggleButtons with matching focus
    val focusButtons = focusToggleButtons.collect:
      case btn: FocusToggleButton if relativelyPositionedFocuses.contains(btn.focus) => btn
    .toList

    System.out.println(focusButtons)
    focusButtons.foreach: fb =>
      val gridX = focusToGridX(fb.focus)
      val gridY = focusToGridY(fb.focus)

      System.out.println("removing fb")
      System.out.println(s"new x $gridX")
      System.out.println(s"new y $gridY")
      focusTreeView.getChildren.remove(fb)
      GridPane.setColumnIndex(fb, gridX)
      GridPane.setRowIndex(fb, gridY)
      focusTreeView.getChildren.add(fb)
      System.out.println(fb)

  /** Clear and redraw all connection lines on the lineLayer. */
  private def redrawConnections(): Unit =
    if lineLayer eq null then return
    lineLayer.getChildren.clear()

    // Example: connect each FocusToggleButton to its parent(s)
    // Adapt this to your real relation data
    val children = focusTreeView.getChildren
    val it = children.iterator()
    val btns = new scala.collection.mutable.ArrayBuffer[FocusToggleButton]()
    while it.hasNext do
      it.next() match
        case b: FocusToggleButton => btns += b
        case _ => ()

    // For each button, draw lines to its prerequisites/relatives (example)
    btns.foreach: b =>
      val from = centerInLayer(b)
      // Replace this with your real edges; e.g., b.focus.prerequisites
      //      val targets: Seq[FocusToggleButton] = computeTargetsFor(b, btns)
      val targets: List[FocusToggleButton] = btns.filter(btn => b.focus.prerequisiteList.contains(btn.focus)).toList
      targets.foreach: t =>
        val to = centerInLayer(t)
        val line = new Line(from._1, from._2, to._1, to._2)
        line.setStroke(Color.BLUE)
        line.setStrokeWidth(2.0)
        line.setMouseTransparent(true)
        lineLayer.getChildren.add(line)

  /** Compute the center coordinates of a node relative to the layeredContent */
  private def centerInLayer(n: Node): (Double, Double) =
    // convert the node's center to the coordinate space of lineLayer/layeredContent
    val b = n.getBoundsInParent
    val centerXInGrid = b.getMinX + b.getWidth / 2
    val centerYInGrid = b.getMinY + b.getHeight / 2
    val p = n.getParent.localToScene(centerXInGrid, centerYInGrid)
    val lp = layeredContent.sceneToLocal(p)
    (lp.getX, lp.getY)

  /** Load a line FXML component by name (e.g., "south", "eastwest", "eastsouth") */
  private def loadLineFxml(lineName: String): Pane =
    try
      val fxmlPath = s"/com/hoi4utils/ui/focus/$lineName.fxml"
      val loader = new javafx.fxml.FXMLLoader(getClass.getResource(fxmlPath))
      val pane = loader.load[Pane]()
      pane.setMouseTransparent(true)
      // Tag the pane with its line type so we can identify it later
      pane.setUserData(lineName)
      pane
    catch
      case e: Exception =>
        logger.error(s"Failed to load line FXML: $lineName", e)
        null

  /** Get the line type at a cell, returns None if empty or occupied by non-line */
  private def getLineAt(gridPane: GridPane, x: Int, y: Int): Option[String] =
    CollectionConverters.asScala(gridPane.getChildren).find: node =>
      val col = GridPane.getColumnIndex(node)
      val row = GridPane.getRowIndex(node)
      col != null && row != null && col == x && row == y
    match
      case Some(pane: Pane) if pane.getUserData.isInstanceOf[String] =>
        Some(pane.getUserData.asInstanceOf[String])
      case _ => None

  /** Parse directions from line name (e.g., "eastsouth" -> Set("east", "south")) */
  private def parseDirections(lineName: String): Set[String] =
    Set("east", "north", "south", "west").filter(lineName.contains)

  /** Build line name from directions (e.g., Set("east", "south", "west") -> "eastsouthwest") */
  private def buildLineName(dirs: Set[String]): String =
    Seq("east", "north", "south", "west").filter(dirs.contains).mkString("")

  /** Replace a line at given position with a new line */
  private def replaceLine(gridPane: GridPane, x: Int, y: Int, newLineName: String): Unit =
    CollectionConverters.asScala(gridPane.getChildren).find: node =>
      val col = GridPane.getColumnIndex(node)
      val row = GridPane.getRowIndex(node)
      col != null && row != null && col == x && row == y
    .foreach: oldNode =>
      gridPane.getChildren.remove(oldNode)
      val newPane = loadLineFxml(newLineName)
      if newPane != null then gridPane.add(newPane, x, y)