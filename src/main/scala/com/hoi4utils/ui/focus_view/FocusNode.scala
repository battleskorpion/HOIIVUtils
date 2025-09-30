package com.hoi4utils.ui.focus_view

import com.hoi4utils.hoi4mod.common.national_focus.Focus
import com.hoi4utils.hoi4mod.localization.Property
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.{Button, Tooltip}
import javafx.scene.input.MouseButton
import scalafx.scene.input.MouseEvent

/**
 * Invisible clickable node positioned over rendered focus graphics.
 * Handles all mouse interactions for a specific focus.
 */
class FocusNode(val focus: Focus, private val onDoubleClick: Focus => Unit, private val onRightClick: (Focus, MouseEvent) => Unit)
  extends Button with LazyLogging:

  // Make button invisible but still clickable
  setStyle("-fx-background-color: transparent; -fx-border-color: transparent;")
  setOpacity(0.0) // Invisible but still receives mouse events

  // Set up tooltip
  updateTooltip()

  // Set up mouse event handlers
  setOnMouseClicked { event =>
    event.getButton match
      case MouseButton.PRIMARY if event.getClickCount == 2 =>
        logger.debug(s"Double-clicked focus: ${focus.id.getOrElse("unknown")}")
        onDoubleClick(focus)
      case MouseButton.SECONDARY =>
        logger.debug(s"Right-clicked focus: ${focus.id.getOrElse("unknown")}")
        onRightClick(focus, new MouseEvent(event))
      case _ => // Ignore other click types
  }

  /**
   * Update the tooltip content based on current focus data
   */
  def updateTooltip(): Unit =
    val details = focus.toScript
    val tooltip = new Tooltip(details)
    setTooltip(tooltip)

  /**
   * Position this node at the correct location for the focus
   */
  def updatePosition(focusX: Double, focusY: Double, focusWidth: Double, focusHeight: Double): Unit =
    setLayoutX(focusX - focusWidth / 2)
    setLayoutY(focusY - focusHeight / 2)
    setPrefWidth(focusWidth)
    setPrefHeight(focusHeight)
    setMinWidth(focusWidth)
    setMinHeight(focusHeight)
    setMaxWidth(focusWidth)
    setMaxHeight(focusHeight)

  /**
   * Show selection highlight
   */
  def setSelected(selected: Boolean): Unit =
    if selected then
      setStyle("-fx-background-color: rgba(255, 255, 0, 0.3); -fx-border-color: yellow; -fx-border-width: 2px;")
      setOpacity(0.3)
    else
      setStyle("-fx-background-color: transparent; -fx-border-color: transparent;")
      setOpacity(0.0)

  /**
   * Show hover highlight
   */
  def setHovered(hovered: Boolean): Unit =
    if hovered && getOpacity == 0.0 then // Only show hover if not selected
      setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: white; -fx-border-width: 1px;")
      setOpacity(0.1)
    else if !hovered && getOpacity <= 0.1 then // Remove hover if not selected
      setStyle("-fx-background-color: transparent; -fx-border-color: transparent;")
      setOpacity(0.0)

  override def toString: String = s"FocusNode(${focus.id.getOrElse("unknown")})"