package com.hoi4utils.ui.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4.common.national_focus.{Focus, FocusTree}
import com.hoi4utils.ui.focus.FocusToggleButton.gfxFocusUnavailable
import com.hoi4utils.ui.javafx.scene.layout.ErrorIconPane
import com.typesafe.scalalogging.LazyLogging
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.*

class FocusToggleButton(private val _focus: Focus, prefW: Double, prefH: Double) extends ToggleButton with LazyLogging:

  private val focusIcon: Image = loadFocusIcon()
  private val cleanName: Label = Label(_focus.locName.getOrElse(_focus.id.str))

  setPrefSize(prefW, prefH)
  setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE)
  setMaxWidth(Double.MaxValue)

  getStyleClass.add("focus-toggle-button")
  setStyle("-fx-font-size: 14px; -fx-padding: 6px; -fx-background-color: transparent; -fx-border-color: transparent; -fx-text-background-color: white")

  if FocusToggleButton.gfxFocusUnavailable != null then
    val cleanNameBackGround = new ImageView(gfxFocusUnavailable)
    val stackPane = new StackPane(cleanNameBackGround, cleanName)
    stackPane.setAlignment(Pos.CENTER)

    // Add error icon if this focus has errors
    if _focus.focusErrors.nonEmpty then
      val errorIconPane = new ErrorIconPane(
        iconSize = 20,
        errorNumberCount = _focus.focusErrors.size,
        onDoubleClick = Some(() => {
          // TODO: Open error details for this focus
          logger.info(s"Error icon clicked for focus: ${_focus.id.str}")
        }),
        tooltipText = Some(s"${_focus.focusErrors.size} error(s) in this focus")
      )
      errorIconPane.build()

      // Prevent ALL mouse events from propagating to the focus button
      errorIconPane.setOnMousePressed(event => event.consume())
      errorIconPane.setOnMouseReleased(event => event.consume())
      errorIconPane.setOnMouseClicked(event => {
        event.consume()
        if event.getClickCount == 2 then
          // Double-click: show error details
          //todo add error details window
          logger.info(s"Double-clicked error icon for focus: ${_focus.id.str}")
        else
          // Single click: log or do nothing
          ()
      })
      errorIconPane.setOnMouseDragged(event => event.consume())
      errorIconPane.setOnMouseEntered(event => event.consume())
      errorIconPane.setOnMouseExited(event => event.consume())

      // Prevent the error icon from being transparent to mouse events
      errorIconPane.setMouseTransparent(false)

      // Position in top-right corner of the stackPane
      StackPane.setAlignment(errorIconPane, Pos.TOP_RIGHT)
      StackPane.setMargin(errorIconPane, new javafx.geometry.Insets(2, 2, 0, 0))

      stackPane.getChildren.add(errorIconPane)

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

object FocusToggleButton:
	private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")

	private def loadFocusUnavailableImage(focusUnavailablePath: String): Image =
		val inputStream =
			try getClass.getClassLoader.getResourceAsStream(focusUnavailablePath)
			catch
				case e: Exception =>
//					logger.error(s"Failed to load focus unavailable image from $focusUnavailablePath", e) // TODO: i forgor how to get a logger oops
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

