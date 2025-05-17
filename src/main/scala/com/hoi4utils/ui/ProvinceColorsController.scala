package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.clausewitz.map.gen.ColorGenerator
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import javafx.scene.paint.Color.{BLACK, rgb}
import scala.jdk.CollectionConverters._

class ProvinceColorsController extends HOIIVUtilsAbstractController with LazyLogger {
  setFxmlResource("ProvinceColors.fxml")
  setTitle("HOIIVUtils Province Colors")

  @FXML
  var idWindowName: Label = _
  @FXML
  private var colorInputField: TextField = _
  @FXML
  private var generateButton: Button = _
  @FXML
  private var statusLabel: Label = _
  @FXML
  private var progressIndicator: ProgressIndicator = _
  @FXML
  private var colorPreviewGrid: GridPane = _

  // Sliders for color ranges
  @FXML
  private var redMinSlider: Slider = _
  @FXML
  private var redMaxSlider: Slider = _
  @FXML
  private var greenMinSlider: Slider = _
  @FXML
  private var greenMaxSlider: Slider = _
  @FXML
  private var blueMinSlider: Slider = _
  @FXML
  private var blueMaxSlider: Slider = _

  // Labels for slider values
  @FXML
  private var minRedAmtLabel: Label = _
  @FXML
  private var maxRedAmtLabel: Label = _
  @FXML
  private var minGreenAmtLabel: Label = _
  @FXML
  private var maxGreenAmtLabel: Label = _
  @FXML
  private var minBlueAmtLabel: Label = _
  @FXML
  private var maxBlueAmtLabel: Label = _

  private var input: String = "65536"

  private val outputPath: String = HOIIVUtils.get("hDir") + File.separator + "Generated Province Colors.bmp"

  // Color constraints from old code
  private var redMin: Int = 0
  private var redMax: Int = 255
  private var greenMin: Int = 0
  private var greenMax: Int = 255
  private var blueMin: Int = 0
  private var blueMax: Int = 255

  // Setter methods
  def setRedMin(value: Int): Unit = redMin = value

  def setRedMax(value: Int): Unit = redMax = value

  def setGreenMin(value: Int): Unit = greenMin = value

  def setGreenMax(value: Int): Unit = greenMax = value

  def setBlueMin(value: Int): Unit = blueMin = value

  def setBlueMax(value: Int): Unit = blueMax = value

  @FXML
  def initialize(): Unit = {
    idWindowName.setText("Province Colors - Unique Color Generator")
    colorInputField.setText(input) // Set default input value
    progressIndicator.setVisible(false) // Hide progress indicator initially

    // Initialize sliders
    setupSliders()
  }

  /**
   * TODO: Fix Sliders to work with colorGenerator Example, if max red is 0 and min red is 0, then generate no red.
   * Sets up the sliders with listeners and initial values
   */
  private def setupSliders(): Unit = {
    // Initialize sliders with default values
    redMinSlider.setValue(redMin)
    redMaxSlider.setValue(redMax)
    greenMinSlider.setValue(greenMin)
    greenMaxSlider.setValue(greenMax)
    blueMinSlider.setValue(blueMin)
    blueMaxSlider.setValue(blueMax)

    // Initialize labels
    minRedAmtLabel.setText(redMin.toString)
    maxRedAmtLabel.setText(redMax.toString)
    minGreenAmtLabel.setText(greenMin.toString)
    maxGreenAmtLabel.setText(greenMax.toString)
    minBlueAmtLabel.setText(blueMin.toString)
    maxBlueAmtLabel.setText(blueMax.toString)

    // Add listeners to sliders
    redMinSlider.valueProperty().addListener((_, _, newValue) => {
      redMin = newValue.intValue()
      minRedAmtLabel.setText(redMin.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    redMaxSlider.valueProperty().addListener((_, _, newValue) => {
      redMax = newValue.intValue()
      maxRedAmtLabel.setText(redMax.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    greenMinSlider.valueProperty().addListener((_, _, newValue) => {
      greenMin = newValue.intValue()
      minGreenAmtLabel.setText(greenMin.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    greenMaxSlider.valueProperty().addListener((_, _, newValue) => {
      greenMax = newValue.intValue()
      maxGreenAmtLabel.setText(greenMax.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    blueMinSlider.valueProperty().addListener((_, _, newValue) => {
      blueMin = newValue.intValue()
      minBlueAmtLabel.setText(blueMin.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    blueMaxSlider.valueProperty().addListener((_, _, newValue) => {
      blueMax = newValue.intValue()
      maxBlueAmtLabel.setText(blueMax.toString)
      updateColorPreview(getNumColorsGenerate())
    })
  }

  /**
   * Gets the number of colors to generate from the input field
   * @return The number of colors to generate
   */
  private def getNumColorsGenerate(): Int = {
    try {
      var numColors = Integer.parseInt(colorInputField.getText)

      if (numColors <= 0) {
        statusLabel.setText("Error: Number of colors must be positive.")
        return 0
      }

      // Calculate the maximum possible colors within the RGB constraints
      val redRange = Math.max(1, redMax - redMin + 1)
      val greenRange = Math.max(1, greenMax - greenMin + 1)
      val blueRange = Math.max(1, blueMax - blueMin + 1)
      val maxPossibleColors = redRange.toLong * greenRange * blueRange

      // Check if exceeding maximum possible unique colors based on constraints
      if (numColors > maxPossibleColors) {
        numColors = maxPossibleColors.toInt
        statusLabel.setText(s"Warning: Limited to $numColors colors based on RGB ranges.")
        logger.warn(s"Requested more colors than possible with current RGB ranges. Limited to $numColors")
      }

      // Also check the absolute maximum (16.7 million)
      if (numColors > (1 << 24) - 1) {
        numColors = (1 << 24) - 1
        statusLabel.setText(s"Warning: Limited to maximum of $numColors unique colors.")
        logger.warn(s"Requested more colors than possible (24-bit RGB). Limited to $numColors")
      }

      numColors
    } catch {
      case e: NumberFormatException =>
        statusLabel.setText("Error: Please enter a valid number.")
        0
    }
  }

  @FXML
  private def handleColorInputField(): Unit = {
    input = colorInputField.getText
    val numColors = getNumColorsGenerate()

    if (numColors > 0) {
      // Update the color preview
      updateColorPreview(numColors)
      statusLabel.setText(s"Ready to generate BMP with $numColors unique colors.")
    }
  }

  /**
   * Handler for "Generate BMP" button click.
   */
  @FXML
  private def handleGenerateButton(): Unit = {
    val numColors = getNumColorsGenerate()

    if (numColors <= 0) {
      return // Error already displayed in getNumColorsGenerate
    }

    // Show progress indicator
    statusLabel.setText(s"Generating BMP with $numColors unique colors...")
    progressIndicator.setVisible(true)
    generateButton.setDisable(true)

    val colorGenerator = new ColorGenerator()

    // Run BMP generation on a background thread
    new Thread(() => {
      try {
        colorGenerator.generateColors(numColors, outputPath)

        // Update UI on completion
        Platform.runLater(() => {
          progressIndicator.setVisible(false)
          generateButton.setDisable(false)
          statusLabel.setText(s"BMP generated successfully: $outputPath")
          logger.info(s"Generated BMP with $numColors unique colors: $outputPath")
        })
      } catch {
        case e: Exception =>
          logger.error("Error generating BMP", e)
          Platform.runLater(() => {
            progressIndicator.setVisible(false)
            generateButton.setDisable(false)
            statusLabel.setText(s"Error: Failed to generate BMP - ${e.getMessage}")
            logger.error("Failed to generate BMP", e)
          })
      }
    }).start()
  }

  /**
   * TODO: Fix Preview
   * Updates the color preview in the UI with the specified number of unique colors.
   *
   * @param numColors Number of unique colors to display.
   */
  private def updateColorPreview(numColors: Int): Unit = {
    if (numColors <= 0) return

    colorPreviewGrid.getChildren.clear()

    val columns = Math.ceil(Math.sqrt(numColors)).toInt
    val boxSize = 8
    val previewLimit = Math.min(numColors, 1000)

    for (i <- 0 until previewLimit) {
      // Create a rectangle with the color
      val rect = new Rectangle(boxSize, boxSize)
      // TODO: Get some colors from colorGenerator to be preview here, somehow?
      val rColor = 0
      val gColor = 0
      val bColor = 0
      rect.setFill(rgb(rColor, gColor, bColor))
      rect.setStroke(BLACK)

      // Add the rectangle to the GridPane
      val row = i / columns
      val col = i % columns
      colorPreviewGrid.add(rect, col, row)
    }
  }
}
