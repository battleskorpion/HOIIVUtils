package com.hoi4utils.ui.menus

import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{GridPane, Pane}
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

import java.net.URL
import java.util.ResourceBundle
import scala.compiletime.uninitialized

class DragAndDrop extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("/com/hoi4utils/ui/menus/DragAndDrop.fxml")
  setTitle("HOIIVUtils Drag and Drop Window")

  @FXML private var grid: GridPane = uninitialized
  private var dragged: Rectangle = uninitialized
  private val cellSize = 100
  private val rows = 5
  private val cols = 5

  @FXML def initialize(): Unit =
    // Create the grid cells
    for row <- 0 until rows do
      for col <- 0 until cols do
        val cell = new Pane
        cell.setPrefSize(cellSize, cellSize)
        cell.setStyle("-fx-border-color: gray; -fx-background-color: #222;")
        grid.add(cell, col, row)
    // Add a draggable rectangle
    val rect = new Rectangle(cellSize - 10, cellSize - 10, Color.CORNFLOWERBLUE)
    val firstCell = grid.getChildren.get(0).asInstanceOf[Pane]
    firstCell.getChildren.add(rect)
    setupDragging(rect)

  private def setupDragging(rect: Rectangle): Unit =
    rect.addEventFilter(MouseEvent.MOUSE_PRESSED, e =>
      dragged = rect
      rect.setOpacity(0.7)
    )
    rect.addEventFilter(MouseEvent.MOUSE_DRAGGED, e =>
      rect.setTranslateX(e.getSceneX - cellSize / 2)
      rect.setTranslateY(e.getSceneY - cellSize / 2)
      rect.toFront()
    )
    rect.addEventFilter(MouseEvent.MOUSE_RELEASED, e =>
      if dragged != null then
        rect.setOpacity(1.0)
        val gridX = (e.getSceneX / cellSize).toInt
        val gridY = (e.getSceneY / cellSize).toInt
        if gridX >= 0 && gridX < cols && gridY >= 0 && gridY < rows then
          val targetCell = grid.getChildren.get(gridY * cols + gridX).asInstanceOf[Pane]
          targetCell.getChildren.setAll(rect)
          rect.setTranslateX(0)
          rect.setTranslateY(0)
        dragged = null
    )