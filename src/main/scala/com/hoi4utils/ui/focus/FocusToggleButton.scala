package com.hoi4utils.ui.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.ui.custom_javafx.image.ScalaFXImageUtils
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.*
import javafx.scene.layout.*
import scalafx.scene.image.Image
import javafx.scene.image.ImageView

class FocusToggleButton(var name: String = "No Name", prefW: Double = 200, prefH: Double = 40) extends ToggleButton with LazyLogging:
  private var focus = name
  private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")

  // initial sizing and style
  setPrefSize(prefW, prefH)
  setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
  setMaxWidth(Double.MaxValue)

  // Apply custom style class for subdued colors
  getStyleClass.add("focus-toggle-button")
  setStyle("-fx-font-size: 14px; -fx-padding: 6px;")

  // set the DDS image as a graphic (ScalaFX Image -> JavaFX Image via delegate)
  if gfxFocusUnavailable != null then
    setGraphic(new ImageView(gfxFocusUnavailable.delegate))

  // convenience methods to change settings at runtime
  def setSize(width: Double, height: Double): Unit =
    setPrefSize(width, height)

  def setPreferredWidth(width: Double): Unit = setPrefWidth(width)

  def setPreferredHeight(height: Double): Unit = setPrefHeight(height)

  def applyCssStyle(css: String): Unit = setStyle(css)

  def setBackgroundImage(image: Image): Unit =
    if image != null then setGraphic(new ImageView(image.delegate)) else setGraphic(null)

  def setHelpTooltip(text: String): Unit = setTooltip(new Tooltip(text))

  // existing loader (unchanged behavior)
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
    ScalaFXImageUtils.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match
        case Some(value) => value
        case None => return null,
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )