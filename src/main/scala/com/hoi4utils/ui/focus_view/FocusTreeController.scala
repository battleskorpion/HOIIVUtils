package com.hoi4utils.ui.focus_view

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.extensions.*
import com.hoi4utils.hoi4.country.CountryTag
import com.hoi4utils.hoi4.focus.{FixFocus, Focus, FocusTree}
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.focus_view.FocusTreeController.updateLoadingStatus
import com.hoi4utils.ui.javafx_ui.image.JavaFXImageUtils
import com.hoi4utils.ui.pdxscript.{NewFocusTreeController, PDXEditorPane}
import com.hoi4utils.{HOIIVFiles, HOIIVUtils}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.{Point2D, Rectangle2D}
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

import java.io.*
import java.time.LocalDateTime
import java.util.Comparator
import java.util.function.Consumer
import javax.swing.JOptionPane
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

class FocusTreeController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("FocusTree.fxml")
  setTitle("Focus Tree View")

  // Constants
  private val FOCUS_X_SCALE: Int = 90
  private val CENTER_FOCUS_X: Int = FOCUS_X_SCALE / 2
  private val FOCUS_Y_SCALE: Int = 140
  private val CENTER_FOCUS_Y: Int = FOCUS_Y_SCALE / 2
  private val VISIBLE_DROPDOWN_ROW_COUNT: Int = 20
  private val X_OFFSET_FIX: Int = 30
  private val Y_OFFSET_FIX: Int = 40

  // FXML components
  @FXML private var focusTreeCanvas: Canvas = uninitialized
  @FXML private var focusTreeCanvasScrollPane: ScrollPane = uninitialized
  @FXML private var focusTreeDropdown: ComboBox[FocusTree] = uninitialized
  @FXML private var exportFocusTreeButton: Button = uninitialized
  @FXML private var focusTreeViewSplitPane: SplitPane = uninitialized
  @FXML var loadingLabel: Label = uninitialized
  @FXML var contentContainer: AnchorPane = uninitialized

  // State variables
  private var focusTree: Option[FocusTree] = None
  private var focusTooltipView: Option[Tooltip] = None
  private var focusDetailsFocus: Option[Focus] = None
  private var draggedFocus: Option[Focus] = None
  private var marqueeStartPoint: Option[Point2D] = None
  private var marqueeEndPoint: Option[Point2D] = None
  private val selectedFocuses: mutable.ListBuffer[Focus] = ListBuffer.empty
  private var gridLines: Boolean = false
  private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")
  private val initFailed = new SimpleBooleanProperty(false)

  /**
   * Called when the application is initialized.
   */
  @FXML
  def initialize(): Unit = {
    // Ensure FXML components are loaded first
    if (contentContainer == null || loadingLabel == null) {
      logger.error("FXML components not properly initialized")
      return
    }

    // Set initial UI state on FX thread
    contentContainer.setVisible(false)
    loadingLabel.setVisible(true)
    loadingLabel.setText("Beginning focus tree view initialization...")

    // Validate other required FXML components
    if (focusTreeDropdown == null || exportFocusTreeButton == null ||
      focusTreeCanvas == null || focusTreeCanvasScrollPane == null) {
      logger.error("Required FXML components are null")
      Platform.runLater(() => {
        loadingLabel.setText("Error: Required UI components not loaded")
        initFailed.set(true)
      })
      return
    }

    // Setup UI components that don't require background loading
    setupUIComponents()

    // Start background initialization
    val task = new javafx.concurrent.Task[InitializationResult] {
      override def call(): InitializationResult = {
        try {
          performBackgroundInitialization()
        } catch {
          case e: Exception =>
            logger.error("Background initialization failed", e)
            InitializationResult(success = false, error = Some(e.getMessage), focusTree = None, focusTrees = None)
        }
      }
    }

    task.setOnSucceeded(_ => {
      val result = task.getValue
      handleInitializationResult(result)
    })

    task.setOnFailed(_ => {
      logger.error("Initialization task failed", task.getException)
      Platform.runLater(() => {
        loadingLabel.setText(s"Initialization failed: ${task.getException.getMessage}")
        initFailed.set(true)
        showErrorDialog("Initialization Error", s"Failed to initialize: ${task.getException.getMessage}")
      })
    })

    task.setOnCancelled(_ => {
      logger.warn("Initialization was cancelled")
      Platform.runLater(() => {
        loadingLabel.setText("Initialization was cancelled")
        initFailed.set(true)
        showWarningDialog("Cancelled", "Focus Tree initialization was cancelled.")
      })
    })

    // Start the task in a new thread
    val thread = new Thread(task)
    thread.setDaemon(true)
    thread.start()
  }

  private def setupUIComponents(): Unit = {
    // Setup components that can be configured immediately
    focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"))
    focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT)

    // Setup scroll pane behavior for middle mouse button
    focusTreeCanvasScrollPane.setOnMousePressed { e =>
      if (e.getButton == MouseButton.MIDDLE) {
        logger.debug("Middle mouse button pressed - enabling panning.")
        focusTreeCanvasScrollPane.setPannable(true)
      }
    }

    focusTreeCanvasScrollPane.setOnMouseReleased { e =>
      if (e.getButton == MouseButton.MIDDLE) {
        logger.debug("Middle mouse button released - disabling panning.")
        focusTreeCanvasScrollPane.setPannable(false)
      }
    }

    exportFocusTreeButton.setOnAction(_ => handleExportFocusTreeButtonClick())
  }

  private def performBackgroundInitialization(): InitializationResult = {

    def updateStatus(status: String): Unit = {
      Platform.runLater(() => updateLoadingStatus(loadingLabel, status))
    }

    // Load available focus trees
    updateStatus("Loading focus trees...")
    val trees: ObservableList[FocusTree] = FocusTree.getFocusTrees.toObservableList

    if (trees == null) {
      updateStatus("Focus trees list is null. Ensure mod files are loaded correctly.")
      logger.error("Focus trees list is null. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("Focus trees list is null"), focusTree = None, focusTrees = None)
    }

    if (trees.isEmpty) {
      updateStatus("Focus trees list is empty. Ensure mod files are loaded correctly.")
      logger.warn("Focus trees list is empty. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("No focus trees found"), focusTree = None, focusTrees = Some(trees))
    }

    updateStatus(s"Focus trees loaded successfully: ${trees.size()} items")
    logger.info(s"Focus trees loaded successfully: ${trees.size()} items")

    // Try loading a specific focus tree
    updateStatus("Loading focus tree...")
    val loadedFocusTree = getFocusTree("SMA", "massachusetts.txt")

    loadedFocusTree match {
      case None =>
        updateStatus("No valid Focus Tree found. Ensure mod files are loaded correctly.")
        logger.error("Failed to load a valid focus tree. This may indicate an issue with mod loading.")
        return InitializationResult(success = false, error = Some("No valid Focus Tree found"), focusTree = None, focusTrees = Some(trees))

      case Some(tree) =>
        updateStatus("Focus tree loaded successfully.")
        logger.info(s"Loaded focus tree: $tree")

        // Fix localization
        updateStatus(s"Fixing localization for focus tree: $tree")
        Try(FixFocus.fixLocalization(tree)) match {
          case Success(_) =>
            updateStatus("Localization fix completed.")
            logger.debug("Localization fix completed.")
          case Failure(e) =>
            updateStatus(s"Failed to fix localization for focus tree: $tree")
            logger.error(s"Failed to fix localization for focus tree: $tree", e)
            return InitializationResult(success = false, error = Some(s"Failed to fix localization: ${e.getMessage}"), focusTree = Some(tree), focusTrees = Some(trees))
        }

        updateStatus("Initialization completed successfully. Drawing focus tree to canvas and displaying...")
        return InitializationResult(success = true, error = None, focusTree = Some(tree), focusTrees = Some(trees))
    }
  }

  private def handleInitializationResult(result: InitializationResult): Unit = {
    Platform.runLater(() => {
      if (!result.success) {
        initFailed.set(true)
        result.error.foreach { error =>
          logger.error("Initialization failed", error)
          showErrorDialog("Initialization Error", error)
        }
        return
      }

      logger.debug("Adding focus trees to dropdown...")
      // Setup dropdown with loaded trees
      result.focusTrees.foreach { trees =>
        focusTreeDropdown.setItems(trees)
        focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTree, String](_.toString))

        if (!trees.isEmpty) {
          focusTreeDropdown.getSelectionModel.select(0)
        }

        // Setup selection listener
        focusTreeDropdown.getSelectionModel.selectedItemProperty().addListener { (_, _, newValue) =>
          Option(newValue).foreach { tree =>
            logger.debug(s"Focus tree selected: $tree")
            focusTree = Some(tree)
            drawFocusTree()
          }
        }
      }

      // Set the loaded focus tree
      logger.debug("Setting focus tree...")
      result.focusTree.foreach { tree =>
        focusTree = Some(tree)
      }

      // Draw the focus tree
      logger.debug("Drawing focus tree on canvas...")
      drawFocusTree()
      logger.debug("Focus tree drawn successfully.")

      // Show completion message

      result.focusTree.foreach { tree =>

        logger.info(s"Loaded focuses: ${tree.focuses.size}")
        logger.info(s"Country: ${tree.country.value}")
        logger.info(s"Focus tree: $tree")


        // Optional: Show completion dialog
        showInfoDialog("Initialization Complete",
          s"Loaded focuses: ${tree.focuses.size}\n" +
            s"Loaded tree of country: ${tree.country.value}\n" +
            s"Focus tree: $tree")

      }

      // Switch to main content
      loadingLabel.setVisible(false)
      logger.debug("Switching to main content view...")
      contentContainer.setVisible(true)
      logger.info("FocusTreeController initialized successfully.")
    })
  }

  // Helper case class for initialization results
  private case class InitializationResult(
                                           success: Boolean,
                                           error: Option[String],
                                           focusTree: Option[FocusTree],
                                           focusTrees: Option[ObservableList[FocusTree]]
                                         )

  // Helper methods for dialogs
  private def showErrorDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
  }

  private def showWarningDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE)
  }

  private def showInfoDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
  }

  /**
   * Attempts to load a valid FocusTree from different sources.
   */
  private def getFocusTree(tag: String, filePath: String): Option[FocusTree] = {
    // Attempt to get the FocusTree by country tag
    val focusTreeOpt = FocusTree.get(CountryTag(tag))
      .orElse {
        // Attempt to load the FocusTree from a file
        val focusFile = new File(HOIIVFiles.Mod.focus_folder, filePath)
        if (focusFile.exists()) FocusTree.get(focusFile) else None
      }
      .orElse {
        // Attempt to find a valid FocusTree from the list of focus trees
        val focusTrees = FocusTree.listFocusTrees
        focusTrees.find(_.nonEmpty)
      }

    // Handle the result
    focusTreeOpt match {
      case Some(tree) =>
        logger.info(s"Found valid FocusTree: $tree")
        Some(tree)
      case None =>
        logger.error(s"No valid FocusTree found for tag: $tag or file: $filePath")
        None
    }
  }

  private def exportFocusTree(focusTree: FocusTree, path: String): Unit = {
    Try {
      val writer = new PrintWriter(path)
      try {
        writer.println(s"### Generated by HOI4Utils, time: ${LocalDateTime.now()} ###")
        writer.println(focusTree.toScript)
        logger.info(s"Exported focus tree $focusTree to $path")
      } finally {
        writer.close()
      }
    }.recover {
      case e: FileNotFoundException =>
        logger.error(s"Error exporting focus tree: ${e.getMessage}")
        showErrorDialog("Export Error", s"Error exporting focus tree: ${e.getMessage}")
    }
  }

  def getMinX: Int = focusTree.map(_.minX).getOrElse(0)

  def getMinY: Int = 0

  private def focusTreeViewLength(): Int = getMaxX - getMinX

  private def focusTreeViewHeight(): Int = getMaxY - getMinY

  private def focusTreeViewDimension(): Dimension = Dimension(focusTreeViewLength(), focusTreeViewHeight())

  private def getMaxX: Int = {
    focusTree.map(_.focuses.map(_.absoluteX).maxOption.getOrElse(10)).getOrElse(10)
  }

  private def getMaxY: Int = {
    focusTree.map(_.focuses.map(_.absoluteY).maxOption.getOrElse(10)).getOrElse(10)
  }

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
    JavaFXImageUtils.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match {
        case Some(value) => value
        case None => ???
      },
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )
  }

  // Maximum safe Canvas dimensions to avoid RTTexture issues
  private val MAX_CANVAS_WIDTH = HOIIVUtils.get("canvas.max.width").toDoubleOption.getOrElse(4000.0) // Stay safely under 8192 limit
  private val MAX_CANVAS_HEIGHT = HOIIVUtils.get("canvas.max.height").toDoubleOption.getOrElse(4000.0)

  // Thread-safe flag to prevent concurrent drawing operations
  @volatile private var isDrawing = false

  // Scale factors for when Canvas needs to be downscaled
  private var currentScaleX = 1.0
  private var currentScaleY = 1.0

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

    isDrawing = true

    try {
      // Calculate ideal dimensions
      val idealWidth = math.max(FOCUS_X_SCALE * (getMaxX - getMinX) + 2 * X_OFFSET_FIX, 800).toDouble
      val idealHeight = math.max(FOCUS_Y_SCALE * (getMaxY + 2), 600).toDouble

      if (idealWidth <= 0 || idealHeight <= 0) {
        logger.error(s"Invalid ideal Canvas dimensions: width=$idealWidth, height=$idealHeight")
        isDrawing = false
        return
      }

      // Apply maximum size constraints and calculate scaling
      val (actualWidth, actualHeight, scaleX, scaleY) = calculateSafeDimensions(idealWidth, idealHeight)

      logger.debug(s"Canvas dimensions - Ideal: ${idealWidth}x${idealHeight}, Actual: ${actualWidth}x${actualHeight}, Scale: ${scaleX}x${scaleY}")

      // Store current scale factors for drawing operations
      currentScaleX = scaleX
      currentScaleY = scaleY

      // Step 1: Update Canvas dimensions
      updateCanvasSize(actualWidth, actualHeight)

    } catch {
      case ex: Exception =>
        logger.error("Error in drawFocusTree setup", ex)
        isDrawing = false
    }
    
  }

  /**
   * Calculate safe Canvas dimensions that won't exceed texture limits
   */
  private def calculateSafeDimensions(idealWidth: Double, idealHeight: Double): (Double, Double, Double, Double) = {
    var actualWidth = idealWidth
    var actualHeight = idealHeight
    var scaleX = 1.0
    var scaleY = 1.0

    // Check if dimensions exceed maximum safe limits
    if (idealWidth > MAX_CANVAS_WIDTH) {
      scaleX = MAX_CANVAS_WIDTH / idealWidth
      actualWidth = MAX_CANVAS_WIDTH
      logger.warn(s"Canvas width ${idealWidth} exceeds maximum ${MAX_CANVAS_WIDTH}, scaling by ${scaleX}")
    }

    if (idealHeight > MAX_CANVAS_HEIGHT) {
      scaleY = MAX_CANVAS_HEIGHT / idealHeight
      actualHeight = MAX_CANVAS_HEIGHT
      logger.warn(s"Canvas height ${idealHeight} exceeds maximum ${MAX_CANVAS_HEIGHT}, scaling by ${scaleY}")
    }

    // Use uniform scaling to maintain aspect ratio if needed
    val uniformScale = math.min(scaleX, scaleY)
    if (uniformScale < 1.0) {
      actualWidth = idealWidth * uniformScale
      actualHeight = idealHeight * uniformScale
      scaleX = uniformScale
      scaleY = uniformScale
      logger.warn(s"Using uniform scaling factor: ${uniformScale}")
    }

    (actualWidth, actualHeight, scaleX, scaleY)
  }

  /**
   * Updates Canvas dimensions safely on the JavaFX thread
   */
  private def updateCanvasSize(width: Double, height: Double): Unit = {
    Platform.runLater(() => {
      try {
//        // Validate dimensions one more time before setting
//        if (width > MAX_CANVAS_WIDTH || height > MAX_CANVAS_HEIGHT) {
//          logger.error(s"Attempted to set Canvas size beyond safe limits: ${width}x${height}")
//          isDrawing = false
//          return
//        }

        focusTreeCanvas.setWidth(width)
        focusTreeCanvas.setHeight(height)

        // Step 2: Perform drawing after size update is complete
        Platform.runLater(() => {
          performDrawing(width, height)
        })

      } catch {
        case ex: Exception =>
          logger.error("Error updating Canvas size", ex)
          isDrawing = false
      }
    })
  }

  /**
   * Performs the actual drawing operations with proper scaling
   */
  private def performDrawing(expectedWidth: Double, expectedHeight: Double): Unit = {
    try {
      val gc2D = focusTreeCanvas.getGraphicsContext2D

      if (gc2D == null) {
        logger.error("GraphicsContext2D is null. Cannot draw focus tree.")
        isDrawing = false
        return
      }

      // Verify Canvas dimensions are valid and match expected values
      val actualWidth = focusTreeCanvas.getWidth
      val actualHeight = focusTreeCanvas.getHeight

      if (actualWidth <= 0 || actualHeight <= 0) {
        logger.error(s"Invalid actual Canvas dimensions: width=$actualWidth, height=$actualHeight")
        isDrawing = false
        return
      }

      if (actualWidth > MAX_CANVAS_WIDTH || actualHeight > MAX_CANVAS_HEIGHT) {
        logger.error(s"Canvas dimensions exceed safe limits: ${actualWidth}x${actualHeight}")
        isDrawing = false
        return
      }

      if (math.abs(actualWidth - expectedWidth) > 1.0 || math.abs(actualHeight - expectedHeight) > 1.0) {
        logger.warn(s"Canvas size mismatch. Expected: ${expectedWidth}x${expectedHeight}, Actual: ${actualWidth}x${actualHeight}")
      }

      // Apply scaling transformation if needed
      gc2D.save() // Save current transform state
      try {
        if (currentScaleX != 1.0 || currentScaleY != 1.0) {
          gc2D.scale(currentScaleX, currentScaleY)
          logger.debug(s"Applied scaling transformation: ${currentScaleX}x${currentScaleY}")
        }

        // Clear the canvas with a dark gray color
        gc2D.setFill(Color.DARKGRAY)
        gc2D.fillRect(0, 0, actualWidth / currentScaleX, actualHeight / currentScaleY)

        // Draw grid lines if enabled
        if (gridLines) drawGridLines(gc2D)

        // Draw focus tree content
        val focuses = focusTree.map(_.focuses).orNull
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
   */
  private def handleCanvasResize(): Unit = {
    focusTreeCanvas.widthProperty().addListener((obs, oldVal, newVal) => {
      val newWidth = newVal.doubleValue()
      if (newWidth > 0 && newWidth <= MAX_CANVAS_WIDTH && !isDrawing) {
        Platform.runLater(() => redrawFocusTree())
      } else if (newWidth > MAX_CANVAS_WIDTH) {
        logger.warn(s"Canvas width ${newWidth} exceeds maximum safe limit ${MAX_CANVAS_WIDTH}")
      }
    })

    focusTreeCanvas.heightProperty().addListener((obs, oldVal, newVal) => {
      val newHeight = newVal.doubleValue()
      if (newHeight > 0 && newHeight <= MAX_CANVAS_HEIGHT && !isDrawing) {
        Platform.runLater(() => redrawFocusTree())
      } else if (newHeight > MAX_CANVAS_HEIGHT) {
        logger.warn(s"Canvas height ${newHeight} exceeds maximum safe limit ${MAX_CANVAS_HEIGHT}")
      }
    })
  }

  /**
   * Initialization method to call when setting up the Canvas
   */
  def initializeFocusTreeCanvas(): Unit = {
    if (focusTreeCanvas != null) {
      handleCanvasResize()
      logger.debug("Focus tree Canvas initialized with maximum safe dimensions")
    } else {
      logger.error("Cannot initialize null focusTreeCanvas")
    }
  }

  /**
   * Check if Canvas dimensions are within safe limits
   */
  def validateCanvasDimensions(width: Double, height: Double): Boolean = {
    width > 0 && height > 0 && width <= MAX_CANVAS_WIDTH && height <= MAX_CANVAS_HEIGHT
  }

  def drawGridLines(gc: GraphicsContext): Unit = {
    val minX = getMinX
    val maxX = getMaxX
    val maxY = getMaxY

    gc.setStroke(Color.GRAY)
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

  def drawPrerequisites(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit = {
    gc2D.setStroke(Color.BLACK)
    gc2D.setLineWidth(3)

    val focusesWithPrereqs = focuses.filter(_.hasPrerequisites)
    val numFocuses = focusesWithPrereqs.size
    logger.debug(s"Drawing prerequisites for $numFocuses focuses...")

    focusesWithPrereqs.foreach { focus =>
      logger.debug(s"Drawing ${focusesWithPrereqs.size} prerequisite focus connections")
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

  def drawMutuallyExclusiveFocuses(gc2D: GraphicsContext, focuses: Seq[Focus], minX: Int): Unit = {
    gc2D.setStroke(Color.DARKRED)
    gc2D.setLineWidth(3)

    focuses.filter(_.isMutuallyExclusive).foreach { focus =>
      val mutuallyExclusiveFocuses = focus.mutuallyExclusiveList.asJava.asScala

      mutuallyExclusiveFocuses.foreach { mutexFocus =>
        val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val y1 = FOCUS_Y_SCALE * focus.absoluteY + (FOCUS_Y_SCALE / 1.6).toInt + Y_OFFSET_FIX
        val x2 = FOCUS_X_SCALE * (mutexFocus.absoluteX - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX
        val y2 = FOCUS_Y_SCALE * mutexFocus.absoluteY + (FOCUS_Y_SCALE / 1.6).toInt + Y_OFFSET_FIX

        // Draw a line between the two mutually exclusive focuses
        gc2D.strokeLine(x1, y1, x2, y2)

        // Draw circles at both ends
        gc2D.setFill(Color.DARKRED)
        gc2D.fillOval(x1 - 5, y1 - 5, 10, 10)
        gc2D.fillOval(x2 - 5, y2 - 5, 10, 10)
      }
    }
  }

  def drawFocus(gc2D: GraphicsContext, focus: Focus, minX: Int): Unit = {
    val isSelected = selectedFocuses.contains(focus)

    gc2D.setFill(Color.WHITE)
    val x1 = FOCUS_X_SCALE * (focus.absoluteX - minX) + X_OFFSET_FIX
    val y1 = focusToCanvasY(focus)
    val yAdj1 = (FOCUS_Y_SCALE / 2.2).toInt
    val yAdj2 = (FOCUS_Y_SCALE / 2) + 20

    // Focus name plate gfx (focus unavailable version)
    gfxFocusUnavailable match {
      case null =>
        logger.warn("Focus unavailable image is null, using default color fill.")
        gc2D.setFill(Color.GRAY)
        gc2D.fillRect(x1 - 32, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3)
      case img =>
        gc2D.drawImage(img, x1 - 32, y1 + yAdj1)
    }

    // Focus icon
    focus.getDDSImage.foreach(img => gc2D.drawImage(img, x1, y1))

    // Focus name text
    val locName = focus.localizationText(com.hoi4utils.localization.Property.NAME)
    val name = if (locName == "[null]" && focus.id.str.nonEmpty) focus.id.str else locName
    gc2D.fillText(name, x1 - 20, y1 + yAdj2)

    if (isSelected) {
      gc2D.setStroke(Color.YELLOW)
      gc2D.setLineWidth(2)
      gc2D.strokeRect(x1 - FOCUS_X_SCALE / 2.3, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3)
    }
  }

  def getFocusHover(p: Point2D): Option[Focus] = {
    focusTree.flatMap { tree =>
      val x = (p.getX / FOCUS_X_SCALE).toInt + tree.minX
      val y = (p.getY / FOCUS_Y_SCALE).toInt

      tree.focuses.find(_.hasAbsolutePosition(x, y))
    }
  }

  @FXML
  def selectClosestMatch(comboBox: ComboBox[FocusTree], typedText: String): Unit = {
    comboBox.getItems.asScala.foreach { item =>
      item.countryTag match {
        case Some(countryTag) if countryTag.toString.toLowerCase().startsWith(typedText.toLowerCase()) =>
          comboBox.getSelectionModel.select(item)
          comboBox.getEditor.setText(item.toString)
          return
        case _ => // Continue to next item
      }
    }
  }

  def addFocusTree(focusTree: FocusTree): Unit = {
    focusTreeDropdown.getItems.add(focusTree)
    focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTree, String](_.toString))
  }

  def viewFocusTree(focusTree: FocusTree): Unit = {
    logger.info(s"Viewing focus tree: $focusTree")
    focusTreeDropdown.getSelectionModel.select(focusTree)
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

  @FXML
  def toggleGridLines(): Unit = {
    gridLines = !gridLines
    drawFocusTree()
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

  // Event handlers
  @FXML
  def handleFocusTreeViewMouseMoved(e: MouseEvent): Unit = {
    val p = new Point2D(e.getX, e.getY)
    val focusTemp = getFocusHover(p)

    focusTemp match {
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
    }
  }

  @FXML
  def handleFocusTreeViewMousePressed(e: MouseEvent): Unit = {
    if (e.isPrimaryButtonDown) {
      selectedFocuses.clear()

      val internalX = ((e.getX - X_OFFSET_FIX) / FOCUS_X_SCALE).toInt + focusTree.map(_.minX).getOrElse(0)
      val internalY = ((e.getY - Y_OFFSET_FIX) / FOCUS_Y_SCALE).toInt

      draggedFocus = focusTree.flatMap(_.focuses.find(f => f.absoluteX == internalX && f.absoluteY == internalY))
      draggedFocus.foreach(focus => logger.info(s"Focus $focus selected"))
    } else if (e.isSecondaryButtonDown) {
      val contextMenu = new ContextMenu()
      val addFocusItem = new MenuItem("Add Focus")
      val newFocusTreeItem = new MenuItem("New Focus Tree")

      addFocusItem.setOnAction { _ =>
        logger.info("Adding focus via context menu")
        focusTree.foreach { tree =>
          val newFocus = new Focus(tree)
          tree.addNewFocus(newFocus)
          newFocus.setAbsoluteXY(canvasToFocusX(e.getX), canvasToFocusY(e.getY), false)
          newFocus.setID(tree.nextTempFocusID())
          openEditorWindow(newFocus, () => drawFocusTree())
        }
      }

      newFocusTreeItem.setOnAction { _ =>
        logger.info("Creating new focus tree via context menu")
        openNewFocusTreeWindow()
      }

      contextMenu.getItems.addAll(addFocusItem, newFocusTreeItem)
      contextMenu.show(focusTreeCanvas, e.getScreenX, e.getScreenY)
    }
  }

  @FXML
  def handleFocusTreeViewMouseDragged(e: MouseEvent): Unit = {
    if (e.isPrimaryButtonDown && draggedFocus.isDefined) {
      val newX = limitFocusMoveX(canvasToFocusX(e.getX))
      val newY = limitFocusMoveY(canvasToFocusY(e.getY))

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
      if (marqueeStartPoint.isEmpty) {
        marqueeStartPoint = Some(new Point2D(e.getX, e.getY))
      } else {
        marqueeEndPoint = Some(new Point2D(e.getX, e.getY))
      }
    }
  }

  def adjustFocusTreeViewport(prevDim: Dimension): Unit = {
    val dim = focusTreeViewDimension()
    if (!dim.equals(prevDim)) {
      val x = focusTreeCanvasScrollPane.getHvalue
      val y = focusTreeCanvasScrollPane.getVvalue
      val xRatio = x * prevDim.width / dim.width
      val yRatio = y * prevDim.height / dim.height
      focusTreeCanvasScrollPane.setHvalue(xRatio)
      focusTreeCanvasScrollPane.setVvalue(yRatio)
    }
  }

  @FXML
  def handleFocusTreeViewMouseReleased(@FXML e: MouseEvent): Unit = {
    if (draggedFocus.isDefined) {
      draggedFocus = None
    }

    if (marqueeStartPoint.isDefined && marqueeEndPoint.isDefined) {
      selectedFocuses.clear()
      focusTree.foreach { tree =>
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
    event.getButton match {
      case MouseButton.PRIMARY =>
        if (event.getClickCount == 2) {
          val clickedPoint = new Point2D(event.getX, event.getY)
          val clickedFocus = getFocusHover(clickedPoint)
          clickedFocus match {
            case None =>
              logger.info("No focus clicked.")
              JOptionPane.showMessageDialog(null, "No focus clicked.", "Info", JOptionPane.INFORMATION_MESSAGE)
            case Some(focus) =>
              logger.info(s"Focus clicked: $focus")
              openEditorWindow(focus, () => drawFocusTree())
          }
        }
      case MouseButton.SECONDARY =>
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

  @FXML
  def handleExportFocusTreeButtonClick(): Unit = {
    val path = "focusOutput.txt"
    logger.info(s"Exporting focus tree to $path")
    focusTree match {
      case Some(tree) =>
        exportFocusTree(tree, path)
      case None =>
        logger.error("No focus tree available to export.")
        JOptionPane.showMessageDialog(null, "No focus tree available to export.", "Error", JOptionPane.ERROR_MESSAGE)
        return
    }
    JOptionPane.showMessageDialog(null, s"Focus tree exported to $path", "Export Successful",
      JOptionPane.INFORMATION_MESSAGE)
  }

  //  @FXML
  //  def openEditorWindow(focus: Focus): Unit = {
  //    require(focus != null, "Focus cannot be null")
  //    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]]) // this is not necessarily redundant. DO NOT CHANGE
  //    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
  //    focusTreeViewSplitPane.getItems.add(pdxEditor)
  //  }


  def openEditorWindow(focus: Focus, onUpdate: Runnable): Unit = {
    require(focus != null, "Focus cannot be null")
    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]], onUpdate) // DO NOT CHANGE
    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
    focusTreeViewSplitPane.getItems.add(pdxEditor)
  }


  def openNewFocusTreeWindow(): Unit = {
    val newFocusTreeController = new NewFocusTreeController()
    newFocusTreeController.open(new Consumer[FocusTree] {
      override def accept(f: FocusTree): Unit = {
        addFocusTree(f)
        viewFocusTree(f)
      }
    })
  }
}

object FocusTreeController {
  def updateLoadingStatus(loadingLabel: Label, status: String): Unit = {
    Platform.runLater(() => {
      if (loadingLabel != null) {
        val currentText = loadingLabel.getText
        loadingLabel.setText(if (currentText.isEmpty) status else s"$currentText\n$status")
      }
    })
  }
}
