package com.hoi4utils.ui.map

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.clausewitz.map.{Heightmap, ProvinceGenConfig, ProvinceGeneration, ProvinceMap}
import com.hoi4utils.ui.{HOIIVUtilsAbstractController, JavaFXUIManager}
import com.typesafe.scalalogging.LazyLogging
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.{Button, ProgressBar, TextField}
import javafx.scene.image.{PixelWriter, WritableImage}
import javafx.scene.layout.{AnchorPane, GridPane}

import javax.swing.JOptionPane
import java.awt.Color
import java.io.{File, IOException, InputStream}
import java.util.Objects
import java.util.function.BiFunction
import scala.compiletime.uninitialized

class MapGenerationController extends HOIIVUtilsAbstractController with LazyLogging {

  private var provinceGeneration: ProvinceGeneration = uninitialized
  private var config: ProvinceGenConfig = uninitialized
  private var heightmap: Heightmap = uninitialized

  @FXML var heightmapCanvas: Canvas = uninitialized
  @FXML var provinceCanvas: Canvas = uninitialized
  @FXML var heightmapTextField: TextField = uninitialized
  @FXML var browseHeightmapButton: Button = uninitialized
  @FXML var provinceGenerationButton: Button = uninitialized
  @FXML var provinceGridPane: GridPane = uninitialized
  @FXML var provinceAnchorPane: AnchorPane = uninitialized
  @FXML var provinceGenerationProgressBar: ProgressBar = uninitialized

  setFxmlResource("MapGeneration.fxml")
  setTitle("Map Generation")

  @FXML
  def initialize(): Unit = {
    try {
      val heightmapStream: InputStream = getClass.getResourceAsStream(Heightmap.DEFAULT)
      heightmap = new Heightmap(heightmapStream)
    } catch {
      case e: IOException =>
        logger.error(s"Default heightmap could not be loaded. ${Heightmap.DEFAULT}")
        throw new RuntimeException(e)
    }

    config = new ProvinceGenConfig(45, heightmap.width(), heightmap.height(), 4000, 0)
    provinceGeneration = new ProvinceGeneration(config)

    heightmapTextField.setText(Heightmap.DEFAULT)
    heightmapCanvas.setWidth(heightmap.width() / 4.0)
    heightmapCanvas.setHeight(heightmap.height() / 4.0)
    provinceCanvas.setWidth(heightmap.width() / 4.0)
    provinceCanvas.setHeight(heightmap.height() / 4.0)
    drawHeightmap()
  }

  private def drawHeightmap(): Unit = {
    val rgbFunction: BiFunction[Integer, Integer, Integer] =
      (x: Integer, y: Integer) => heightmap.height_xy_INT_RGB(x, y)
    drawImageOnCanvas(heightmapCanvas, heightmap.width(), heightmap.height(), rgbFunction)
  }

  private def drawImageOnCanvas(canvas: Canvas,
                                imgWidth: Int,
                                imgHeight: Int,
                                rgbSupplier: BiFunction[Integer, Integer, Integer]): Unit = {
    drawImageOnCanvas(canvas, imgWidth, imgHeight, rgbSupplier, 4)
  }

  private def drawImageOnCanvas(canvas: Canvas,
                                imgWidth: Int,
                                imgHeight: Int,
                                rgbSupplier: BiFunction[Integer, Integer, Integer],
                                zoom: Int): Unit = {
    val gc: GraphicsContext = canvas.getGraphicsContext2D

    val wImage = new WritableImage(imgWidth / zoom, imgHeight / zoom)
    val pixelWriter: PixelWriter = wImage.getPixelWriter

    for {
      y <- 0 until wImage.getHeight.toInt
      x <- 0 until wImage.getWidth.toInt
    } {
      val px = rgbSupplier.apply(x * zoom, y * zoom)
      val c = new Color(px)
      pixelWriter.setArgb(x, y, c.getRGB)
    }

    gc.drawImage(wImage, 0, 0)
  }

  @FXML
  def OnBrowseHeightmap(): Unit = {
    var file: File = null
    try {
      file = JavaFXUIManager.openChooser(
        browseHeightmapButton,
        new File(HOIIVUtils.get("hDir") + File.separator + "maps"),
        false
      )
      heightmap = new Heightmap(file)
    } catch {
      case _: IOException | _: IllegalArgumentException =>
        JOptionPane.showMessageDialog(
          null,
          s"Bad File Path.${file.getPath}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        logger.warn(s"Error: heightmap could not be loaded. Filepath selected: ${file.getPath}")
        heightmap = null
        return
    }

    // draw heightmap etc.
    heightmapTextField.setText(file.getPath) // todo- relative path maybe?
    drawHeightmap()
  }

  @FXML
  def onEnterHeightmap(): Unit = {
    var file: File = null
    try {
      file = new File(heightmapTextField.getText)
      if (file.exists()) {
        heightmap = new Heightmap(file)
      }
    } catch {
      case _: IOException | _: IllegalArgumentException =>
        JOptionPane.showMessageDialog(
          null,
          s"Bad File Path.${file.getPath}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        logger.warn(s"heightmap could not be loaded. Filepath selected: ${file.getPath}")
        heightmap = null
        return
    }
    drawHeightmap()
  }

  @FXML
  def onGenerateProvinces(): Unit = {
    provinceGenerationButton.setVisible(false)
    provinceGenerationProgressBar.setVisible(true)

    val task = new Task[Void] {
      override protected def call(): Void = {
        updateProgress(5, 100)
        provinceGeneration.generate(heightmap)

        updateProgress(99, 100)
        provinceGeneration.writeProvinceMap()

        drawProvinceMap()
        updateProgress(100, 100)

        null
      }
    }

    task.setOnSucceeded(_ => {
      provinceGenerationProgressBar.setVisible(false)
      provinceGenerationButton.setVisible(true)
    })

    provinceGenerationProgressBar.progressProperty().bind(task.progressProperty())

    new Thread(task).start()
  }

  def drawProvinceMap(): Unit = {
    val map: ProvinceMap = this.provinceGeneration.getProvinceMap
    val canvas: Canvas = this.provinceCanvas
    val width: Int = map.width()
    val height: Int = map.height()
    Objects.requireNonNull(map)
    val rgbFunction: BiFunction[Integer, Integer, Integer] =
      (x: Integer, y: Integer) => map.getRGB(x, y)
    this.drawImageOnCanvas(canvas, width, height, rgbFunction)
  }

  @FXML
  def onOpenProvinceGenSettingsWindow(): Unit = {
    new MapGenerationSettingsController().open(config)
  }
}