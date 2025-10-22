package com.hoi4utils.ui.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTree}
import com.typesafe.scalalogging.LazyLogging
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class FocusToggleButton(private val _focus: Focus, cellSize: (Double, Double)) extends ToggleButton with LazyLogging:
  setPrefSize(cellSize._1, cellSize._2)
  setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
  setMaxWidth(Double.MaxValue)
  getStyleClass.add("focus-toggle-button")
  setStyle("-fx-font-size: 14px; -fx-padding: 6px; -fx-background-color: transparent; -fx-border-color: transparent;")

  val stackPane = new StackPane(
    ImageView(loadFocusUnavailableImage("focus_unavailable_bg.dds")),
    getCleanName
  )
  stackPane.setAlignment(Pos.CENTER)
  
  val vbox = new VBox(
    -127,
    stackPane,
    new ImageView(loadFocusImage)
  )
  vbox.setAlignment(Pos.CENTER)
  
  setGraphic(vbox)

  private def getCleanName = Label(_focus.locName.getOrElse(_focus.id.str))
  
  private def loadFocusImage: Image =
    _focus.getDDSImage match
      case Some(ddsImage) => ddsImage
      case None =>
        logger.warn(s"No DDS image found for focus: ${_focus.id}")
        null
  
  private def loadFocusUnavailableImage(focusUnavailablePath: String): Image =
    val inputStream =
      try getClass.getClassLoader.getResourceAsStream(focusUnavailablePath)
      catch
        case e: Exception =>
          logger.error(s"Failed to load focus unavailable image from $focusUnavailablePath", e)
          return null
    val buffer = new Array[Byte](inputStream.available)
    inputStream.read(buffer)
    inputStream.close()
    DDSReader.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match
        case Some(value) => value
        case None => return null,
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )

  def setHelpTooltip(text: String): Unit = setTooltip(new Tooltip(text))

  def focus: Focus = _focus

  def focusTree: FocusTree = focus.focusTree
