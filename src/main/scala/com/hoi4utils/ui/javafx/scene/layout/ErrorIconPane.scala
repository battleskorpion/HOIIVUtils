package com.hoi4utils.ui.javafx.scene.layout

import javafx.scene.control.{Label, Tooltip}
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane

class ErrorIconPane(
                     val errorIcon: ImageView = new ImageView("/com/hoi4utils/ui/icons/error_icon.png"),
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
    errorIcon.setFitWidth(iconSize)
    errorIcon.setFitHeight(iconSize)
    AnchorPane.setTopAnchor(errorIcon, 0.0)
    AnchorPane.setLeftAnchor(errorIcon, 0.0)
    AnchorPane.setRightAnchor(errorIcon, 0.0)
    this.getChildren.add(errorIcon)
    if errorNumberCount != 0 then
      // error count below the icon
      val errorCountLabel = new Label(errorNumberCount.toString)
      this.getChildren.add(errorCountLabel)
      AnchorPane.setTopAnchor(errorCountLabel, iconSize + 5.0)
      AnchorPane.setLeftAnchor(errorCountLabel, 0.0)
      AnchorPane.setRightAnchor(errorCountLabel, 0.0)
    else
      AnchorPane.setBottomAnchor(errorIcon, 0.0)

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

