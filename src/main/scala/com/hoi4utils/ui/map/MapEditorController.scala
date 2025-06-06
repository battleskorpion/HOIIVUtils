package com.hoi4utils.ui.map

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.buildings.StateTable
import com.hoi4utils.ui.pdxscript.PDXEditorPane
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.{ScrollPane, Slider, SplitPane, Tooltip}
import javafx.scene.image.{Image, PixelReader, PixelWriter, WritableImage}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import map.{DefinitionCSV, Province, State}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

class MapEditorController extends HOIIVUtilsAbstractController with LazyLogging {


  @FXML private var mapCanvas: Canvas = uninitialized
  @FXML private var zoomSlider: Slider = uninitialized
  @FXML private var pdxScrollPane: ScrollPane = uninitialized
  @FXML private var mapEditorSplitPane: SplitPane = uninitialized

  private var mapImage: Image = uninitialized
  // Keep the original province map image (with definition colors) for hover lookup.
  private var originalMapImage: Image = uninitialized

  private var zoomFactor: Double = 1.0

  private var showBuildingsTable: Boolean = false

  // Tooltip to display state info on hover.
  private val stateTooltip = new Tooltip()
  private var stateTable: StateTable = null

  private var selectedState: State = null // currently selected state
  private var selectedProvince: Province = null // currently selected province

  setFxmlResource("MapEditor.fxml")
  setTitle("Map Editor")

  @FXML
  def initialize(): Unit = {
    // Try loading a default province map image (adjust the path as needed)
    try {
      val file = HOIIVFiles.Mod.province_map_file
      if (file.exists()) {
        mapImage = new Image(file.toURI.toString)
        originalMapImage = mapImage // store original
        logger.info(s"Default province map loaded: ${file.getAbsolutePath}")
      } else {
        logger.warn(s"Default province map not found at ${file.getAbsolutePath}")
      }
    } catch {
      case e: Exception =>
        logger.error("Error loading default province map image", e)
    }

    zoomSlider.setValue(1.0)
    pdxScrollPane.setVisible(false)
    drawMap()
  }

  private def drawMap(): Unit = {
    if (mapImage == null) return

    val width = mapImage.getWidth * zoomFactor
    val height = mapImage.getHeight * zoomFactor
    mapCanvas.setWidth(width)
    mapCanvas.setHeight(height)
    val gc: GraphicsContext = mapCanvas.getGraphicsContext2D
    gc.clearRect(0, 0, width, height)
    gc.drawImage(mapImage, 0, 0, width, height)
  }

  /**
   * Helper function that calculates and returns the state at the given canvas coordinates.
   *
   * @param canvasX               X coordinate on the canvas.
   * @param canvasY               Y coordinate on the canvas.
   * @param provinceColorToId     Mapping from province RGB to province id.
   * @param provinceIdToStateMap  Mapping from province id to State.
   * @return the State at the given coordinates, or null if none.
   */
  private def getStateAtCanvasCoordinates(canvasX: Double, canvasY: Double,
                                          provinceColorToId: Map[Int, Int],
                                          provinceIdToStateMap: Map[Int, State]): State = {
    if (originalMapImage == null) return null

    // Convert canvas coordinates to original image coordinates.
    val origX = (canvasX / zoomFactor).toInt
    val origY = (canvasY / zoomFactor).toInt

    if (origX < 0 || origY < 0 || origX >= originalMapImage.getWidth || origY >= originalMapImage.getHeight) {
      return null
    }

    val origReader: PixelReader = originalMapImage.getPixelReader
    val origPixelColor: Color = origReader.getColor(origX, origY)
    val pr = (origPixelColor.getRed * 255).toInt
    val pg = (origPixelColor.getGreen * 255).toInt
    val pb = (origPixelColor.getBlue * 255).toInt
    val origRgb = (pr << 16) | (pg << 8) | pb

    provinceColorToId.get(origRgb).flatMap(provinceIdToStateMap.get).orNull
  }

  /**
   * Helper to render the map coloured by some per‐state integer metric.
   * @param metricFn    extracts the metric (e.g. civ or mil factories) from a State
   * @param colorFn     maps a normalized [0–1] value to a Color
   */
  private def viewByStateMetric(metricFn: State => Int, colorFn: Double => Color): Unit = {
    if (originalMapImage == null) return

    // load province → ID
    val defFile = HOIIVFiles.Mod.definition_csv_file
    if (!defFile.exists()) return

    val scalaDefs = DefinitionCSV.load(defFile)
    val defs = scalaDefs.toMap
    val provinceColorToId = mutable.Map[Int, Int]()

    for (d <- defs.values) {
      val rgb = (d.red << 16) | (d.green << 8) | d.blue
      provinceColorToId(rgb) = d.id
    }

    // load states and compute max metric
    val states: ObservableList[State] = State.observeStates
    val max = states.asScala.map(metricFn).maxOption.getOrElse(1)

    // build province→stateColor map
    val provinceIdToColor = mutable.Map[Int, Color]()
    for (s <- states.asScala) {
      val value = metricFn(s)
      val norm = value.toDouble / max
      val color = colorFn(norm)
      for (p <- s.provinces.toList.asJava.asScala) {
        provinceIdToColor(p.id.get.asInstanceOf[Int]) = color
      }
    }

    // recolour every pixel
    val w = originalMapImage.getWidth.toInt
    val h = originalMapImage.getHeight.toInt
    val newImg = new WritableImage(w, h)
    val rdr: PixelReader = originalMapImage.getPixelReader
    val wtr: PixelWriter = newImg.getPixelWriter

    for (y <- 0 until h; x <- 0 until w) {
      val oc = rdr.getColor(x, y)
      val pr = (oc.getRed * 255).toInt
      val pg = (oc.getGreen * 255).toInt
      val pb = (oc.getBlue * 255).toInt
      val rgb = (pr << 16) | (pg << 8) | pb

      provinceColorToId.get(rgb) match {
        case Some(pid) if provinceIdToColor.contains(pid) =>
          wtr.setColor(x, y, provinceIdToColor(pid))
        case _ =>
          wtr.setColor(x, y, oc)
      }
    }

    mapImage = newImg
    drawMap()
    mapCanvas.setOnMouseMoved(null) // no tooltip for these views
  }

  @FXML
  def onLoadProvinceMap(): Unit = {
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Load Province Map")
    fileChooser.getExtensionFilters.add(
      new FileChooser.ExtensionFilter("Bitmap Images", "*.bmp", "*.png", "*.jpg"))
    val file = fileChooser.showOpenDialog(mapCanvas.getScene.getWindow)

    if (file != null) {
      try {
        mapImage = new Image(file.toURI.toString)
        originalMapImage = mapImage // update original as well
        logger.info(s"Loaded province map: ${file.getAbsolutePath}")
        drawMap()
      } catch {
        case e: Exception =>
          logger.error("Failed to load image", e)
      }
    }
  }

  @FXML
  def onViewByProvince(): Unit = {
    logger.info("Switching view mode to Province.")
    // Remove any existing mouse handler.
    mapCanvas.setOnMouseMoved(null)
    drawMap()
  }

  /**
   * Implements the state view using the definitions CSV.
   * For each pixel in the province map, we:
   *   1. Look up its RGB value (from the original image) in the definitions mapping to determine the province id.
   *   2. Use a mapping from province id to State (built from loaded states) to get a state color.
   *   3. Recolor the pixel with the state color (if available), or leave it unchanged otherwise.
   * Also, install a mouse moved event handler that shows a tooltip with the state name under the mouse.
   */
  @FXML
  def onViewByState(): Unit = {
    logger.info("Switching view mode to State.")
    if (originalMapImage == null) {
      logger.warn("No province map image loaded.")
      return
    }

    // Load definitions CSV. Assume HOIIVFiles.Mod.definition_csv_file exists.
    val defFile = HOIIVFiles.Mod.definition_csv_file
    if (!defFile.exists()) {
      logger.warn(s"Definitions file not found: ${defFile.getAbsolutePath}")
      return
    }

    // Load the province definitions from CSV (Scala object)
    val scalaDefs = DefinitionCSV.load(defFile)
    val defs = scalaDefs.toMap

    // Build a mapping from province RGB (as in the original image) to province id.
    val provinceColorToId = mutable.Map[Int, Int]()
    for (defEntry <- defs.values) {
      val rgb = (defEntry.red << 16) | (defEntry.green << 8) | defEntry.blue
      provinceColorToId(rgb) = defEntry.id
    }

    // Build a mapping from province id to a state color and a mapping to the state.
    val provinceIdToStateColor = mutable.Map[Int, Color]()
    val provinceIdToStateMap = mutable.Map[Int, State]()
    val states: ObservableList[State] = State.observeStates

    for (state <- states.asScala) {
      // Assign a random color for each state.
      val stateColor = Color.hsb(math.random * 360, 0.5, 0.9)
      // Assume state.provinces() returns an Iterable<Province> (convert Scala collection to Java)
      for (province <- state.provinces.toList.asJava.asScala) {
        val id = province.id.get.asInstanceOf[Int]
        provinceIdToStateColor(id) = stateColor
        provinceIdToStateMap(id) = state
      }
    }

    // Create a new WritableImage by recoloring the original image.
    val width = originalMapImage.getWidth.toInt
    val height = originalMapImage.getHeight.toInt
    val newImage = new WritableImage(width, height)
    val reader: PixelReader = originalMapImage.getPixelReader
    val writer: PixelWriter = newImage.getPixelWriter

    for (y <- 0 until height; x <- 0 until width) {
      val origColor = reader.getColor(x, y)
      val r = (origColor.getRed * 255).toInt
      val g = (origColor.getGreen * 255).toInt
      val b = (origColor.getBlue * 255).toInt
      val pixelRgb = (r << 16) | (g << 8) | b

      provinceColorToId.get(pixelRgb) match {
        case Some(provinceId) =>
          provinceIdToStateColor.get(provinceId) match {
            case Some(stateColor) => writer.setColor(x, y, stateColor)
            case None => writer.setColor(x, y, origColor)
          }
        case None => writer.setColor(x, y, origColor)
      }
    }

    // Update the displayed image.
    mapImage = newImage
    drawMap()

    // Install a mouse moved handler to update a tooltip with the state name.
    mapCanvas.setOnMouseMoved((event: MouseEvent) => {
      val s = getStateAtCanvasCoordinates(event.getX, event.getY,
        provinceColorToId.toMap, provinceIdToStateMap.toMap)
      if (s != null) {
        stateTooltip.setText(s"State: $s")
        stateTooltip.show(mapCanvas, event.getScreenX + 10, event.getScreenY + 10)
      } else {
        stateTooltip.hide()
      }
    })
  }

  @FXML
  def onViewByStrategicRegion(): Unit = {
    logger.info("Switching view mode to Strategic Region.")
    // TODO: Implement strategic region view rendering.
    // Remove tooltip handler if active.
    mapCanvas.setOnMouseMoved(null)
    drawMap()
  }

  @FXML
  def onViewByCivFactories(): Unit = {
    logger.info("Switching view mode to Civilian Factories.")
    viewByStateMetric(
      _.civilianFactories,       // extract civilianFactories
      norm => Color.hsb(120, 0.8, 0.3 + 0.7 * norm) // greenish scale, brighter = more factories
    )
  }

  @FXML
  def onViewByMilFactories(): Unit = {
    logger.info("Switching view mode to Military Factories.")
    viewByStateMetric(
      _.militaryFactories,       // extract militaryFactories
      norm => Color.hsb(0, 0.8, 0.3 + 0.7 * norm)   // reddish scale, brighter = more factories
    )
  }

  @FXML
  def onZoomIn(): Unit = {
    zoomFactor *= 1.2
    zoomSlider.setValue(zoomFactor)
    drawMap()
  }

  @FXML
  def onZoomOut(): Unit = {
    zoomFactor /= 1.2
    zoomSlider.setValue(zoomFactor)
    drawMap()
  }

  @FXML
  def onResetZoom(): Unit = {
    zoomFactor = 1.0
    zoomSlider.setValue(zoomFactor)
    drawMap()
  }

  @FXML
  def onZoomSliderReleased(event: MouseEvent): Unit = {
    zoomFactor = zoomSlider.getValue
    drawMap()
  }

  @FXML
  def onToggleBuildingsTable(): Unit = {
    showBuildingsTable = !showBuildingsTable
    if (showBuildingsTable) {
      val buildingsTable = new Pane()
      val content = new StateTable()
      buildingsTable.getChildren.add(content)
      buildingsTable.setId("buildingsTable")
      mapEditorSplitPane.getItems.add(buildingsTable)
      buildingsTable.setVisible(true)
      if (selectedState != null) content.setStates(selectedState)
      else content.clearStates()
      stateTable = content
    } else {
      val buildingsTable = mapEditorSplitPane.lookup("#buildingsTable")
      if (buildingsTable != null) {
        mapEditorSplitPane.getItems.remove(buildingsTable)
      }
      stateTable = null
    }
  }

  /**
   * Mouse click handler that calls getStateAtCanvasCoordinates and processes the clicked state.
   * This method is meant to be used as a right-click handler.
   *
   * @param event the mouse event.
   */
  @FXML
  def onCanvasMouseClick(event: MouseEvent): Unit = {
    if (event.getButton == MouseButton.PRIMARY) {
      // For simplicity, we rebuild the mappings as in onViewByState().
      // In a production version, consider caching these maps.
      val defFile = HOIIVFiles.Mod.definition_csv_file
      if (!defFile.exists()) {
        logger.warn(s"Definitions file not found: ${defFile.getAbsolutePath}")
        return
      }

      val scalaDefs = DefinitionCSV.load(defFile)
      val defs = scalaDefs.toMap
      val provinceColorToId = mutable.Map[Int, Int]()

      for (defEntry <- defs.values) {
        val rgb = (defEntry.red << 16) | (defEntry.green << 8) | defEntry.blue
        provinceColorToId(rgb) = defEntry.id
      }

      val provinceIdToStateMap = mutable.Map[Int, State]()
      val states: ObservableList[State] = State.observeStates

      for (state <- states.asScala) {
        for (province <- state.provinces.toList.asJava.asScala) {
          val id = province.id.get.asInstanceOf[Int]
          provinceIdToStateMap(id) = state
        }
      }

      val clickedState = getStateAtCanvasCoordinates(event.getX, event.getY,
        provinceColorToId.toMap, provinceIdToStateMap.toMap)
      if (clickedState != null) {
        logger.info(s"Right-clicked state: $clickedState")
        // add pdxEditor to scroll pane
        val pdxEditorPane = new PDXEditorPane(clickedState)
        pdxEditorPane.showSaveButton()
        pdxScrollPane.setContent(pdxEditorPane)
        pdxScrollPane.setVisible(true)
        selectedState = clickedState
        if (stateTable != null) stateTable.setStates(clickedState)
      } else {
        logger.info("Right-clicked on an undefined state area.")
        selectedState = null
        if (stateTable != null) stateTable.clearStates()
      }
    }
  }
}