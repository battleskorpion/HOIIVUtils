package com.hoi4utils.ui.javafx.scene.layout

import javafx.scene.control.{Label, Tooltip}
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane

class ErrorIconPane(
                     val errorIcon: ImageView = new ImageView("icons/error_icon.png"),
                     val iconSize: Double = 512,
                     val errorNumberCount: Integer = 0,
                     val onDoubleClick: Option[() => Unit] = None,
                     val tooltipText: Option[String] = None
                   ) extends AnchorPane:

  def show(): Unit =
    build()
    this.setVisible(true)
    this.setManaged(true)
    this.setMouseTransparent(false)

  def build(): Unit =
    // Set the pane to match toggle button height (prevent vertical spacing issues)
    this.setPrefHeight(iconSize)
    this.setMaxHeight(iconSize)

    // Configure icon for better quality
    errorIcon.setFitWidth(iconSize)
    errorIcon.setFitHeight(iconSize)
    errorIcon.setPreserveRatio(true)
    errorIcon.setSmooth(true)

    // Position icon on the left, vertically centered
    AnchorPane.setTopAnchor(errorIcon, 0.0)
    AnchorPane.setLeftAnchor(errorIcon, 0.0)
    AnchorPane.setBottomAnchor(errorIcon, 0.0)
    this.getChildren.add(errorIcon)

    if errorNumberCount != 0 then
      // error count to the right of the icon
      val errorCountLabel = new Label(errorNumberCount.toString)
      errorCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;")
      this.getChildren.add(errorCountLabel)

      // Position label to the right of the icon, vertically centered
      AnchorPane.setLeftAnchor(errorCountLabel, iconSize + 3.0)
      AnchorPane.setTopAnchor(errorCountLabel, (iconSize - 14.0) / 2.0) // Center vertically (approximate font height)

      // Set pane width to accommodate icon + spacing + label (estimate ~20px for 1-2 digit numbers)
      this.setPrefWidth(iconSize + 3.0 + 20.0)
    else
      // Just the icon width
      this.setPrefWidth(iconSize)

    // Set up double-click handler if provided
    onDoubleClick.foreach { handler =>
      this.setOnMouseClicked(event =>
        if event.getClickCount == 2 then handler()
      )
    }

    // Set up tooltip if provided
    tooltipText.foreach { text =>
      Tooltip.install(this, new Tooltip(text))
    }

  def hide(): Unit =
    this.getChildren.clear()
    this.setVisible(false)
    this.setManaged(false)
    this.setMouseTransparent(true)

