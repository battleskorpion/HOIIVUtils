package com.hoi4utils.ui.javafx.scene.layout

import com.github.difflib.DiffUtils
import com.github.difflib.patch.{AbstractDelta, Chunk, DeltaType, Patch}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane

import java.util
import java.util.Collection
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

/**
 * DiffViewPane
 * A pane to display differences between two sets of strings.
 */
class DiffViewPane(private val leftTitle: String, private val rightTitle: String) extends AnchorPane:
  private val leftListView: ListView[String] = ListView[String]()
  private val rightListView: ListView[String] = ListView[String]()
  private var leftData: util.Collection[String] = uninitialized
  private var rightData: util.Collection[String] = uninitialized


  private val splitPane = SplitPane()
  this.getChildren.add(splitPane)
  AnchorPane.setTopAnchor(splitPane, 0.0)
  AnchorPane.setBottomAnchor(splitPane, 0.0)
  AnchorPane.setLeftAnchor(splitPane, 0.0)
  AnchorPane.setRightAnchor(splitPane, 0.0)

  private val leftPane = AnchorPane()
  private val rightPane = AnchorPane()
  splitPane.getItems.addAll(leftPane, rightPane)

  leftPane.getChildren.add(leftListView)
  rightPane.getChildren.add(rightListView)

  AnchorPane.setTopAnchor(leftListView, 0.0)
  AnchorPane.setBottomAnchor(leftListView, 0.0)
  AnchorPane.setLeftAnchor(leftListView, 0.0)
  AnchorPane.setRightAnchor(leftListView, 0.0)
  AnchorPane.setTopAnchor(rightListView, 0.0)
  AnchorPane.setBottomAnchor(rightListView, 0.0)
  AnchorPane.setLeftAnchor(rightListView, 0.0)
  AnchorPane.setRightAnchor(rightListView, 0.0)

  leftListView.setEditable(false)
  rightListView.setEditable(false)

  bindDiffViewScrolling()

  /**
   * Sets the data to be compared and displays the diff.
   *
   * @param originalData the original data
   * @param revisedData  the modified data
   */
  def setData(originalData: util.Collection[String], revisedData: util.Collection[String]): Unit =
    this.leftData = originalData
    this.rightData = revisedData
    displayDiff()

  private def bindDiffViewScrolling(): Unit =
    val leftSkinChangeListener: ChangeListener[Skin[?]] = new ChangeListener[Skin[?]]:
      override def changed(observable: ObservableValue[? <: Skin[?]], oldValue: Skin[?], newValue: Skin[?]): Unit =
        leftListView.skinProperty().removeListener(this)
        val leftScrollbar = getVerticalScrollBar(leftListView)
        if leftScrollbar == null then return
        leftScrollbar.setOpacity(0)
        val rightScrollbar = getVerticalScrollBar(rightListView)
        if rightScrollbar == null then
          rightListView.skinProperty().addListener((observableValue, oldSkin, newSkin) => {
            rightListView.skinProperty().removeListener(this)
            val rightScrollbar2 = getVerticalScrollBar(rightListView)
            if rightScrollbar2 != null then
              leftScrollbar.valueProperty().bindBidirectional(rightScrollbar2.valueProperty())
          })
        else
          leftScrollbar.valueProperty().bindBidirectional(rightScrollbar.valueProperty())

    leftListView.skinProperty().addListener(leftSkinChangeListener)

  /**
   * Display the diff between the left and right data.
   */
  private def displayDiff(): Unit =
    val leftLines = if leftData != null then leftData.asScala.toList else List.empty[String]
    val rightLines = if rightData != null then rightData.asScala.toList else List.empty[String]
    val patch: Patch[String] = DiffUtils.diff(leftLines.asJava, rightLines.asJava)

    val leftItems: ObservableList[String] = FXCollections.observableArrayList()
    val rightItems: ObservableList[String] = FXCollections.observableArrayList()

    var leftIndex = 0
    var rightIndex = 0

    for delta <- patch.getDeltas.asScala do
      val leftChunk: Chunk[String] = delta.getSource
      val rightChunk: Chunk[String] = delta.getTarget

      while leftIndex < leftChunk.getPosition do
        leftItems.add("  " + leftLines(leftIndex))
        rightItems.add("  " + rightLines(rightIndex))
        leftIndex += 1
        rightIndex += 1

      delta.getType match
        case DeltaType.DELETE =>
          for line <- leftChunk.getLines.asScala do
            leftItems.add("- " + line)
            rightItems.add("")
            leftIndex += 1

        case DeltaType.INSERT =>
          for line <- rightChunk.getLines.asScala do
            leftItems.add("")
            rightItems.add("+ " + line)
            rightIndex += 1

        case DeltaType.CHANGE =>
          var leftIndexDiff = -leftIndex
          var rightIndexDiff = -rightIndex
          for line <- leftChunk.getLines.asScala do
            leftItems.add("~ " + line)
            leftIndex += 1
          for line <- rightChunk.getLines.asScala do
            rightItems.add("~ " + line)
            rightIndex += 1
          leftIndexDiff += leftIndex
          rightIndexDiff += rightIndex
          if leftIndexDiff > rightIndexDiff then
            for _ <- 0 until (leftIndexDiff - rightIndexDiff) do
              rightItems.add("")
          else if rightIndexDiff > leftIndexDiff then
            for _ <- 0 until (rightIndexDiff - leftIndexDiff) do
              leftItems.add("")

    while leftIndex < leftLines.size do
      leftItems.add("  " + leftLines(leftIndex))
      leftIndex += 1
    while rightIndex < rightLines.size do
      rightItems.add("  " + rightLines(rightIndex))
      rightIndex += 1

    if leftTitle != null then
      leftItems.add(0, leftTitle)
    if rightTitle != null then
      rightItems.add(0, rightTitle)

    leftListView.setItems(leftItems)
    rightListView.setItems(rightItems)

    leftListView.setCellFactory(_ => DiffCell())
    rightListView.setCellFactory(_ => DiffCell())

  private def getVerticalScrollBar(scrollableNode: Node): ScrollBar =
    for node <- scrollableNode.lookupAll(".scroll-bar").asScala do
      node match
        case scrollBar: ScrollBar if scrollBar.getOrientation == Orientation.VERTICAL =>
          return scrollBar
        case _ =>
    null

  /**
   * Custom ListCell to style lines based on their diff status.
   */
  private class DiffCell extends ListCell[String]:
    override protected def updateItem(item: String, empty: Boolean): Unit =
      super.updateItem(item, empty)
      // i dont know why. but this has to be here. Putting it inside the if block is no good.
      getStyleClass.remove("diff-insert")
      getStyleClass.remove("diff-delete")
      getStyleClass.remove("diff-change")
      if empty || item == null then
        setText(null)
        setStyle(null)
      else
        setText(item)
        if item.startsWith("-") then
          //setStyle("-fx-background-color: lightcoral; -fx-font-family: monospace")
          getStyleClass.add("diff-delete")
          setStyle("-fx-font-family: monospace")
        else if item.startsWith("+") then
          //setStyle("-fx-background-color: lightgreen; -fx-font-family: monospace")
          getStyleClass.add("diff-insert")
          setStyle("-fx-font-family: monospace")
        else if item.startsWith("~") then
          getStyleClass.add("diff-change")
          setStyle("-fx-font-family: monospace")
        else
          setStyle("-fx-font-family: monospace")