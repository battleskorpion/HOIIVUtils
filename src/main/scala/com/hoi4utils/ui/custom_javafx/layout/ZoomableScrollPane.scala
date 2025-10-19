package com.hoi4utils.ui.custom_javafx.layout

import javafx.geometry.{Bounds, Point2D, Pos}
import javafx.scene.{Group, Node}
import javafx.scene.control.ScrollPane
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.VBox

class ZoomableScrollPane(private val target: Node) extends ScrollPane:
  private var scaleValue: Double = 1.0
  private val zoomIntensity: Double = 0.02
  private val zoomNode: Node = Group(target)

  setContent(outerNode(zoomNode))
  setPannable(true)
  setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED)
  setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED)
  setFitToHeight(true)  // center
  setFitToWidth(true)   // center
  updateScale()

  private def outerNode(node: Node): Node =
    val outer = centeredNode(node)
    outer.setOnScroll { e =>
      // Only zoom if Ctrl is pressed, otherwise let normal scrolling happen
      if e.isControlDown then
        e.consume()
        onScroll(e.getDeltaY, Point2D(e.getX, e.getY))
    }
    outer

  private def centeredNode(node: Node): Node =
    val vBox = VBox(node)
    vBox.setAlignment(Pos.CENTER)
    vBox

  private def updateScale(): Unit =
    target.setScaleX(scaleValue)
    target.setScaleY(scaleValue)

  private def onScroll(wheelDelta: Double, mousePoint: Point2D): Unit =
    val zoomFactor = Math.exp(wheelDelta * zoomIntensity)

    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = getViewportBounds

    // calculate pixel offsets from [0, 1] range
    val valX = getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    val valY = getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)

    scaleValue = scaleValue * zoomFactor
    updateScale()
    layout() // refresh ScrollPane scroll positions & target bounds

    // convert target coordinates to zoomTarget coordinates
    val posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint))

    // calculate adjustment of scroll position (pixels)
    val adjustment = target.getLocalToParentTransform.deltaTransform(
      posInZoomTarget.multiply(zoomFactor - 1)
    )

    // convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    val updatedInnerBounds = zoomNode.getBoundsInLocal
    setHvalue((valX + adjustment.getX) / (updatedInnerBounds.getWidth - viewportBounds.getWidth))
    setVvalue((valY + adjustment.getY) / (updatedInnerBounds.getHeight - viewportBounds.getHeight))

  // Public methods for zoom controls
  def zoomIn(): Unit =
    onScroll(100.0, Point2D(getWidth / 2, getHeight / 2))

  def zoomOut(): Unit =
    onScroll(-100.0, Point2D(getWidth / 2, getHeight / 2))

  def resetZoom(): Unit =
    scaleValue = 1.0
    updateScale()

  def getZoomLevel: Double = scaleValue

  def setZoomLevel(zoom: Double): Unit =
    scaleValue = Math.max(0.1, Math.min(10.0, zoom)) // Clamp between 0.1 and 10
    updateScale()