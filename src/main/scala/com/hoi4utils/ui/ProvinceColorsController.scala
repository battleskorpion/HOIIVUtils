package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.clausewitz.map.gen.ColorGenerator
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import javafx.scene.paint.Color.{BLACK, rgb}

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.compiletime.uninitialized

case class IntRange(var min: Int, var max: Int)

class ProvinceColorsController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("ProvinceColors.fxml")
  setTitle("HOIIVUtils Province Colors")

  @FXML
  var idWindowName: Label = uninitialized
  @FXML
  private var colorInputField: TextField = uninitialized
  @FXML
  private var generateButton: Button = uninitialized
  @FXML
  private var statusLabel: Label = uninitialized
  @FXML
  private var progressIndicator: ProgressIndicator = uninitialized
  @FXML
  private var colorPreviewGrid: GridPane = uninitialized

  // Sliders for color ranges
  @FXML
  private var redMinSlider: Slider = uninitialized
  @FXML
  private var redMaxSlider: Slider = uninitialized
  @FXML
  private var greenMinSlider: Slider = uninitialized
  @FXML
  private var greenMaxSlider: Slider = uninitialized
  @FXML
  private var blueMinSlider: Slider = uninitialized
  @FXML
  private var blueMaxSlider: Slider = uninitialized

  // Labels for slider values
  @FXML
  private var minRedAmtLabel: Label = uninitialized
  @FXML
  private var maxRedAmtLabel: Label = uninitialized
  @FXML
  private var minGreenAmtLabel: Label = uninitialized
  @FXML
  private var maxGreenAmtLabel: Label = uninitialized
  @FXML
  private var minBlueAmtLabel: Label = uninitialized
  @FXML
  private var maxBlueAmtLabel: Label = uninitialized

  private var input: String = "65536"

  private val outputPath: String = HOIIVUtils.get("hDir") + File.separator + "Generated Province Colors.bmp"

  // Color constraints from old code
  private var redRange   = IntRange(0, 255)
  private var greenRange = IntRange(0, 255)
  private var blueRange  = IntRange(0, 255)

  // Setter methods
  def setRedMin(value: Int): Unit = redRange.min = value

  def setRedMax(value: Int): Unit = redRange.max = value

  def setGreenMin(value: Int): Unit = greenRange.min = value

  def setGreenMax(value: Int): Unit = greenRange.max = value

  def setBlueMin(value: Int): Unit = blueRange.min = value

  def setBlueMax(value: Int): Unit = blueRange.max = value

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
    redMinSlider.setValue(redRange.min)
    redMaxSlider.setValue(redRange.max)
    greenMinSlider.setValue(greenRange.min)
    greenMaxSlider.setValue(greenRange.max)
    blueMinSlider.setValue(blueRange.min)
    blueMaxSlider.setValue(blueRange.max)

    // Initialize labels
    minRedAmtLabel.setText(redRange.min.toString)
    maxRedAmtLabel.setText(redRange.max.toString)
    minGreenAmtLabel.setText(greenRange.min.toString)
    maxGreenAmtLabel.setText(greenRange.max.toString)
    minBlueAmtLabel.setText(blueRange.min.toString)
    maxBlueAmtLabel.setText(blueRange.max.toString)

    // Add listeners to sliders
    redMinSlider.valueProperty().addListener((_, _, newValue) => {
      redRange.min = newValue.intValue()
      minRedAmtLabel.setText(redRange.max.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    redMaxSlider.valueProperty().addListener((_, _, newValue) => {
      redRange.max = newValue.intValue()
      maxRedAmtLabel.setText(redRange.max.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    greenMinSlider.valueProperty().addListener((_, _, newValue) => {
      greenRange.min = newValue.intValue()
      minGreenAmtLabel.setText(greenRange.min.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    greenMaxSlider.valueProperty().addListener((_, _, newValue) => {
      greenRange.max = newValue.intValue()
      maxGreenAmtLabel.setText(greenRange.max.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    blueMinSlider.valueProperty().addListener((_, _, newValue) => {
      blueRange.min = newValue.intValue()
      minBlueAmtLabel.setText(blueRange.min.toString)
      updateColorPreview(getNumColorsGenerate())
    })

    blueMaxSlider.valueProperty().addListener((_, _, newValue) => {
      blueRange.max = newValue.intValue()
      maxBlueAmtLabel.setText(blueRange.max.toString)
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
      val redMax = Math.max(1, redRange.max - redRange.min + 1)
      val greenMax = Math.max(1, greenRange.max - greenRange.min + 1)
      val blueMax = Math.max(1, blueRange.max - blueRange.min + 1)
      val maxPossibleColors = redMax.toLong * greenMax * blueMax

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
