package com.hoi4utils.ui.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4.common.national_focus.{Focus, FocusTree}
import com.typesafe.scalalogging.LazyLogging
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class FocusToggleButton(private val _focus: Focus, prefW: Double, prefH: Double) extends ToggleButton with LazyLogging:

  private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")
  private val focusIcon: Image = loadFocusIcon()
  private val cleanName: Label = Label(_focus.locName.getOrElse(_focus.id.str))

  setPrefSize(prefW, prefH)
  setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
  setMaxWidth(Double.MaxValue)

  getStyleClass.add("focus-toggle-button")
  setStyle("-fx-font-size: 14px; -fx-padding: 6px; -fx-background-color: transparent; -fx-border-color: transparent;")

  if gfxFocusUnavailable != null then
    val cleanNameBackGround = new ImageView(gfxFocusUnavailable)
    val stackPane = new StackPane(cleanNameBackGround, cleanName)
    stackPane.setAlignment(Pos.CENTER)
    val iconView = new ImageView(focusIcon)
    val vbox = new VBox(-127, stackPane, iconView)
    vbox.setAlignment(Pos.CENTER) // Optional: center the items in the VBox
    setGraphic(vbox) // Set vbox, not stackPane!

  def setSize(width: Double, height: Double): Unit = setPrefSize(width, height)

  def setPreferredWidth(width: Double): Unit = setPrefWidth(width)

  def setPreferredHeight(height: Double): Unit = setPrefHeight(height)

  def applyCssStyle(css: String): Unit = setStyle(css)

  def setBackgroundImage(image: Image): Unit =
    if image != null then setGraphic(new ImageView(image)) else setGraphic(null)

  def setHelpTooltip(text: String): Unit = setTooltip(new Tooltip(text))

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

  private def loadFocusIcon(): Image = {
    _focus.getDDSImage match
      case Some(ddsImage) =>
        ddsImage
      case None =>
        logger.warn(s"No DDS image found for focus: ${_focus.id}")
        null
  }

  def focus: Focus = _focus

  def focusTree: FocusTree = _focus.focusTree
