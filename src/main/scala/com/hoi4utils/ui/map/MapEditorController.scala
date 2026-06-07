package com.hoi4utils.ui.map

import com.hoi4utils.hoi4.map.province.DefinitionCSV
import com.hoi4utils.hoi42.map.province.Province
import com.hoi4utils.hoi42.map.state.State
import com.hoi4utils.hoi42.map.state.service.StateService
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController
import com.hoi4utils.ui.countries.StateTable
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ScrollPane
import javafx.scene.control.Slider
import javafx.scene.control.SplitPane
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import zio.ZIO

import scala.jdk.javaapi.CollectionConverters
import java.io.File
import java.util
import java.util.function.{DoubleFunction, ToIntFunction}
import scala.collection.immutable.Map
import scala.compiletime.uninitialized

class MapEditorController extends HOIIVUtilsAbstractController with LazyLogging {

  @FXML private val mapCanvas: Canvas = uninitialized
  @FXML private val zoomSlider: Slider = uninitialized
  @FXML private val pdxScrollPane: ScrollPane = uninitialized
  @FXML private val mapEditorSplitPane: SplitPane = uninitialized

  private var mapImage: Option[Image] = None
  // Keep the original province map image (with definition colors) for hover lookup.
  private var originalMapImage: Option[Image] = None
  private var zoomFactor = 1.0
  private var showBuildingsTable = false
  // Tooltip to display state info on hover.
  final private val stateTooltip = new Tooltip
  private var stateTable: StateTable = uninitialized
  private var selectedState: Option[State] = None // currently selected state
  private val selectedProvince: Option[Province] = None // currently selected province

  /* init */
  setFxmlFile("MapEditor.fxml")
  setTitle("Map Editor")

  @FXML def initialize(): Unit =
    // Try loading a default province map image (adjust the path as needed)
    try {
      val file = HOIIVFiles.Mod.province_map_file
      if (file.exists) {
        mapImage = Some(new Image(file.toURI.toString))
        originalMapImage = mapImage // store original

        logger.info("Default province map loaded: " + file.getAbsolutePath)
      }
      else logger.warn("Default province map not found at " + file.getAbsolutePath)
    } catch {
      case e: Exception =>
        logger.error("Error loading default province map image", e)
    }
    zoomSlider.setValue(1.0)
    pdxScrollPane.setVisible(false)
    drawMap()

  private def drawMap(): Unit =
    if (mapImage == null) return
    mapImage match
      case Some(img) =>
        val width = img.getWidth * zoomFactor
        val height = img.getHeight * zoomFactor
        mapCanvas.setWidth(width)
        mapCanvas.setHeight(height)
        val gc = mapCanvas.getGraphicsContext2D
        gc.clearRect(0, 0, width, height)
        gc.drawImage(img, 0, 0, width, height)
      case None => ()

  /**
   * Helper function that calculates and returns the state at the given canvas coordinates.
   *
   * @param canvasX              X coordinate on the canvas.
   * @param canvasY              Y coordinate on the canvas.
   * @param provinceColorToId    Mapping from province RGB to province id.
   * @param provinceIdToStateMap Mapping from province id to State.
   * @return the State at the given coordinates, or null if none.
   */
  private def getStateAtCanvasCoordinates(canvasX: Double, canvasY: Double, provinceColorToId: Map[Integer, Integer], provinceIdToStateMap: Map[Integer, State]): Option[State] =
    originalMapImage match
      case Some(originalImg) =>
        // Convert canvas coordinates to original image coordinates.
        val origX = (canvasX / zoomFactor).toInt
        val origY = (canvasY / zoomFactor).toInt
        if (origX < 0 || origY < 0 || origX >= originalImg.getWidth || origY >= originalImg.getHeight) return null
        val origReader = originalImg.getPixelReader
        val origPixelColor = origReader.getColor(origX, origY)
        val pr = (origPixelColor.getRed * 255).toInt
        val pg = (origPixelColor.getGreen * 255).toInt
        val pb = (origPixelColor.getBlue * 255).toInt
        val origRgb = (pr << 16) | (pg << 8) | pb
        val provinceId = provinceColorToId.get(origRgb)
        provinceId match
          case Some(id) => provinceIdToStateMap.get(id)
          case None => None
      case None => None 

  /**
   * Helper to render the map coloured by some per‐state integer metric.
   *
   * @param metricFn extracts the metric (e.g. civ or mil factories) from a State
   * @param colorFn  maps a normalized [0–1] value to a Color
   */
  private def viewByStateMetric(metricFn: State => Int, colorFn: DoubleFunction[Color]): Unit =
    originalMapImage match
      case Some(originalImg) =>
        val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
          HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
        }
        // load province → ID
        val defFile = HOIIVFiles.Mod.definition_csv_file
        if (!defFile.exists) return
        val defs = DefinitionCSV.load(defFile)
        val provinceColorToId = new util.HashMap[Integer, Integer]
        for 
          d <- defs.values
        do 
          val rgb = (d.red << 16) | (d.green << 8) | d.blue
          provinceColorToId.put(rgb, d.id) 
        // load states and compute max metric
        val states = stateService.states
        val max = states.map(metricFn).maxOption.getOrElse(1)
        // build province→stateColor map
        val provinceIdToColor = new scala.collection.mutable.HashMap[Int, Color]
        for 
          s <- states
        do 
          val `val` = metricFn.apply(s)
          val norm = `val` / max.toDouble
          val c = colorFn.apply(norm)
          
          for 
            p <- s.provinces
          do 
            p.identifier match
              case Some(id) => provinceIdToColor.put(id, c)

        // recolour every pixel
        val w = originalImg.getWidth.toInt
        val h = originalImg.getHeight.toInt
        val newImg = new WritableImage(w, h)
        val rdr = originalImg.getPixelReader
        val wtr = newImg.getPixelWriter
        for (y <- 0 until h) {
          for (x <- 0 until w) {
            val oc = rdr.getColor(x, y)
            val pr = (oc.getRed * 255).toInt
            val pg = (oc.getGreen * 255).toInt
            val pb = (oc.getBlue * 255).toInt
            val pid = provinceColorToId.get((pr << 16) | (pg << 8) | pb)
            val colorToSet: Color = if (pid != null && provinceIdToColor.contains(pid)) provinceIdToColor.getOrElse(pid, oc) else oc
            wtr.setColor(x, y, colorToSet)
          }
        }
        mapImage = Some(newImg)
        drawMap()
        mapCanvas.setOnMouseMoved(null) // no tooltip for these views
      case None => () 

  @FXML private[map] def onLoadProvinceMap(): Unit =
    val fileChooser = new FileChooser
    fileChooser.setTitle("Load Province Map")
    fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("Bitmap Images", "*.bmp", "*.png", "*.jpg"))
    val file = fileChooser.showOpenDialog(mapCanvas.getScene.getWindow)
    if (file != null) try {
      mapImage = Some(new Image(file.toURI.toString)) 
      originalMapImage = mapImage // update original as well

      logger.info("Loaded province map: " + file.getAbsolutePath)
      drawMap()
    } catch {
      case e: Exception =>
        logger.error("Failed to load image", e)
    }

  @FXML private[map] def mapToPNG(): Unit =
    mapImage match
      case Some(mapImg) =>
        val fileChooser = new FileChooser
        fileChooser.setTitle("Save Map as PNG")
        fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter("PNG Images", "*.png"))
        val file = fileChooser.showSaveDialog(mapCanvas.getScene.getWindow)
        if (file != null) try {
          val writableImage = new WritableImage(mapImg.getWidth.toInt, mapImg.getHeight.toInt)
          val pixelWriter = writableImage.getPixelWriter
          pixelWriter.setPixels(0, 0, mapImg.getWidth.toInt, mapImg.getHeight.toInt, mapImg.getPixelReader, 0, 0)
          javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null), "png", file)
          logger.info("Map saved to: " + file.getAbsolutePath)
        } catch {
          case e: Exception =>
            logger.error("Failed to save map image", e)
        }
      case None =>
        logger.warn("No map image to save.")

  @FXML private[map] def onViewByProvince(): Unit = {
    logger.info("Switching view mode to Province.")
    // Remove any existing mouse handler.
    mapCanvas.setOnMouseMoved(null)
    drawMap()
  }

  /**
   * Implements the state view using the definitions CSV.
   * For each pixel in the province map, we:
   *   1. Look up its RGB value (from the original image) in the definitions mapping to determine the province id.
   *      2. Use a mapping from province id to State (built from loaded states) to get a state color.
   *      3. Recolor the pixel with the state color (if available), or leave it unchanged otherwise.
   *      Also, install a mouse moved event handler that shows a tooltip with the state name under the mouse.
   */
  @FXML private[map] def onViewByState(): Unit =
    logger.info("Switching view mode to State.")
    if (originalMapImage == null) {
      logger.warn("No province map image loaded.")
      return
    }
    // Load definitions CSV. Assume HOIIVFiles.Mod.definition_csv_file exists.
    val defFile = HOIIVFiles.Mod.definition_csv_file
    if (!defFile.exists) {
      logger.warn("Definitions file not found: " + defFile.getAbsolutePath)
      return
    }
    // Load the province definitions from CSV (Scala object)
    val defs = DefinitionCSV.load(defFile)
    // Build a mapping from province RGB (as in the original image) to province id.
    val provinceColorToId = new util.HashMap[Integer, Integer]
    for (`def` <- defs.values) {
      val rgb = (`def`.red << 16) | (`def`.green << 8) | `def`.blue
      provinceColorToId.put(rgb, `def`.id)
    }
    // Build a mapping from province id to a state color and a mapping to the state.
    val provinceIdToStateColor = new util.HashMap[Integer, Color]
    val provinceIdToStateMap = new util.HashMap[Integer, State]
    val states = stateService.observeStates
    for (state <- states) {
      // Assign a random color for each state.
      val stateColor = Color.hsb(Math.random * 360, 0.5, 0.9)
      // Assume state.provinces() returns an Iterable<Province> (convert Scala collection to Java)
      for (province <- CollectionConverters.asJava(state.provinces.toList)) {
        val id = province.id.get.asInstanceOf[Integer]
        provinceIdToStateColor.put(id, stateColor)
        provinceIdToStateMap.put(id, state)
      }
    }
    // Create a new WritableImage by recoloring the original image.
    val width = originalMapImage.getWidth.toInt
    val height = originalMapImage.getHeight.toInt
    val newImage = new WritableImage(width, height)
    val reader = originalMapImage.getPixelReader
    val writer = newImage.getPixelWriter
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val origColor = reader.getColor(x, y)
        val r = (origColor.getRed * 255).toInt
        val g = (origColor.getGreen * 255).toInt
        val b = (origColor.getBlue * 255).toInt
        val pixelRgb = (r << 16) | (g << 8) | b
        val provinceId = provinceColorToId.get(pixelRgb)
        if (provinceId != null) {
          val stateColor = provinceIdToStateColor.get(provinceId)
          if (stateColor != null) writer.setColor(x, y, stateColor)
          else writer.setColor(x, y, origColor)
        }
        else writer.setColor(x, y, origColor)
      }
    }
    // Update the displayed image.
    mapImage = newImage
    drawMap()
    // Install a mouse moved handler to update a tooltip with the state name.
    mapCanvas.setOnMouseMoved((event: MouseEvent) => {
      val s = getStateAtCanvasCoordinates(event.getX, event.getY, provinceColorToId, provinceIdToStateMap)
      if (s != null) {
        stateTooltip.setText("State: " + s.toString)
        stateTooltip.show(mapCanvas, event.getScreenX + 10, event.getScreenY + 10)
      }
      else stateTooltip.hide()

    })

  @FXML private[map] def onViewByStrategicRegion(): Unit =
    MapEditorController.logger.info("Switching view mode to Strategic Region.")
    // TODO: Implement strategic region view rendering.
    // Remove tooltip handler if active.
    mapCanvas.setOnMouseMoved(null)
    drawMap()

  @FXML private[map] def onViewByCivFactories(): Unit =
    MapEditorController.logger.info("Switching view mode to Civilian Factories.")
    viewByStateMetric(State.civilianFactories, // extract civilianFactories
      (norm: Double) => Color.hsb(120, 0.8, 0.3 + 0.7 * norm) // greenish scale, brighter = more factories)
    )

  @FXML private[map] def onViewByMilFactories(): Unit =
    MapEditorController.logger.info("Switching view mode to Military Factories.")
    viewByStateMetric(State.militaryFactories, // extract militaryFactories
      (norm: Double) => Color.hsb(0, 0.8, 0.3 + 0.7 * norm) // reddish scale, brighter = more factories)
    )

  @FXML private[map] def onZoomIn(): Unit =
    zoomFactor *= 1.2
    zoomSlider.setValue(zoomFactor)
    drawMap()

  @FXML private[map] def onZoomOut(): Unit =
    zoomFactor /= 1.2
    zoomSlider.setValue(zoomFactor)
    drawMap()

  @FXML private[map] def onResetZoom(): Unit =
    zoomFactor = 1.0
    zoomSlider.setValue(zoomFactor)
    drawMap()

  @FXML private[map] def onZoomSliderReleased(event: MouseEvent): Unit =
    zoomFactor = zoomSlider.getValue
    drawMap()

  @FXML private[map] def onToggleBuildingsTable(): Unit =
    showBuildingsTable = !showBuildingsTable
    if (showBuildingsTable) {
      val buildingsTable = new Pane
      val content = new StateTable
      buildingsTable.getChildren.add(content)
      buildingsTable.setId("buildingsTable")
      mapEditorSplitPane.getItems.add(buildingsTable)
      buildingsTable.setVisible(true)
      //if (selectedState != null) content.setStates(selectedState); // TODO FIX IN SCALA
      //else content.clearStates();
      stateTable = content
    }
    else {
      val buildingsTable = mapEditorSplitPane.lookup("#buildingsTable")
      if (buildingsTable != null) mapEditorSplitPane.getItems.remove(buildingsTable)
      stateTable = null
    }

  /**
   * Mouse click handler that calls getStateAtCanvasCoordinates and processes the clicked state.
   * This method is meant to be used as a right-click handler.
   *
   * @param event the mouse event.
   */
  @FXML private[map] def onCanvasMouseClick(event: MouseEvent): Unit =
    if (event.getButton eq MouseButton.PRIMARY) {
      // For simplicity, we rebuild the mappings as in onViewByState().
      // In a production version, consider caching these maps.
      val defFile = HOIIVFiles.Mod.definition_csv_file
      if (!defFile.exists) {
        MapEditorController.logger.warn("Definitions file not found: " + defFile.getAbsolutePath)
        return
      }
      val scalaDefs = DefinitionCSV.load(defFile)
      val defs = CollectionConverters.asJava(scalaDefs)
      val provinceColorToId = new util.HashMap[Integer, Integer]
      import scala.collection.JavaConversions._
      for (`def` <- defs.values) {
        val rgb = (`def`.red << 16) | (`def`.green << 8) | `def`.blue
        provinceColorToId.put(rgb, `def`.id)
      }
      val provinceIdToStateMap = new util.HashMap[Integer, State]
      val states = stateService.observeStates
      import scala.collection.JavaConversions._
      for (state <- states) {
        import scala.collection.JavaConversions._
        for (province <- CollectionConverters.asJava(state.provinces.toList)) {
          val id = province.id.get.asInstanceOf[Integer]
          provinceIdToStateMap.put(id, state)
        }
      }
      val clickedState = getStateAtCanvasCoordinates(event.getX, event.getY, provinceColorToId, provinceIdToStateMap)
      if (clickedState != null) {
        MapEditorController.logger.info("Right-clicked state: " + clickedState)
        // add pdxEditor to scroll pane
        val pdxEditorPane = new PDXEditorPane(clickedState)
        pdxEditorPane.showSaveButton
        pdxScrollPane.setContent(pdxEditorPane)
        pdxScrollPane.setVisible(true)
        this.selectedState = clickedState
      }
      else {
        MapEditorController.logger.info("Right-clicked on an undefined state area.")
        this.selectedState = null
        if (stateTable != null) stateTable.clearStates()
      }
    }
}
