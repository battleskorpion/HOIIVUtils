package com.hoi4utils.ui.focus

import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout._

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")
  private var focusGridColumns: Int = 0
  private var focusGridRows: Int = 0
  private var focusGridColumnsSize: Int = 250
  private var focusGridRowSize: Int = 250
  private var gridLines: Boolean = true
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"

  private var focusTreeList: ListBuffer[someFocusTree] = ListBuffer.empty

  @FXML var focusTree2: VBox = uninitialized
  @FXML var menuBar: MenuBar = uninitialized
  @FXML var focusTreeView: GridPane = uninitialized
  @FXML var mClose: Button = uninitialized
  @FXML var mSquare: Button = uninitialized
  @FXML var mMinimize: Button = uninitialized
  @FXML var focusSelection: ScrollPane = uninitialized
  @FXML var vbox: VBox = uninitialized
  @FXML var welcome: ToggleButton = uninitialized
  @FXML var toggleGroup: ToggleGroup = uninitialized

  private var toggleButtons: ListBuffer[ToggleButton] = ListBuffer.empty

  @FXML def initialize(): Unit =
    clear()
    welcome.setToggleGroup(toggleGroup)
    welcome.fire()
    populateFocusSelection()

  override def preSetup(): Unit = setupWindowControls(focusTree2, mClose, mSquare, mMinimize, menuBar)

  private def populateFocusSelection(): Unit =
    populateFocusTreeList
    focusTreeList.foreach(someFocusTree =>
      val toggleButton = ToggleButton(someFocusTree.name)
      toggleButtons += toggleButton
      toggleButton.setToggleGroup(toggleGroup)
      toggleButton.setOnAction(_ => loadFocusTreeView(someFocusTree))
      vbox.getChildren.add(toggleButton)
    )

  /** Clears the focus tree view in the middle */
  private def clear(): Unit =
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()
    focusTreeView.setGridLinesVisible(gridLines)

  private def loadWelcomeMessage(): Unit =
    clear()
    setCC()
    setRC()
    focusTreeView.add(Label(welcomeMessage), 0, 0)

  private def populateFocusTreeList: Unit =
    focusTreeList += someFocusTree("One", 10, 2, 3)
    focusTreeList += someFocusTree("Two", 42, 3, 2)
    focusTreeList += someFocusTree("Three", 7)

  class someFocusTree(var name: String, var testVar: Int = 0, var rows: Int = 1, var columns: Int = 1):
    var focuses: ListBuffer[someFocus] = ListBuffer.empty
    getFocuses
    private def getFocuses: Unit =
      // this is not a list of focuses lined up, cells will be empty except if the focus is present
      focuses += someFocus(s"${name}_Focus_1", 1, 2, 1)
      focuses += someFocus(s"${name}_Focus_2", 2, 1, 2)

  class someFocus(var name: String, var testVar: Int = 0, var positionX: Int = 1, var positionY: Int = 1):
    val anotherVar = s"Hello, Focus $name!"


  private def loadFocusTreeView(someFocusTree: someFocusTree): Unit =
    clear()
    for _ <- 0 until someFocusTree.columns do setCC()
    for _ <- 0 until someFocusTree.rows do setRC()

    if someFocusTree.focuses.isEmpty then
      focusTreeView.add(Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"), 0, 0)
    else
      var focusIndex = 0
      for row <- 0 until someFocusTree.rows do
        for column <- 0 until someFocusTree.columns do
          if focusIndex < someFocusTree.focuses.length then
            if someFocusTree.focuses(focusIndex).positionX == column + 1 && someFocusTree.focuses(focusIndex).positionY == row + 1 then
              val focus = someFocusTree.focuses(focusIndex)
              val focusLabel = Label(s"Focus: ${focus.name}\nTestVar: ${focus.testVar}\nAnotherVar: ${focus.anotherVar}\nPosition: (${focus.positionX}, ${focus.positionY})")
              focusLabel.setStyle("-fx-border-color: black; -fx-padding: 10px;")
              focusTreeView.add(focusLabel, column, row)
              focusIndex += 1


  private def setCC() = {
    val cc = new ColumnConstraints()
    cc.setMinWidth(focusGridColumnsSize)
    cc.setPrefWidth(focusGridColumnsSize)
    cc.setMaxWidth(focusGridColumnsSize)
    focusTreeView.getColumnConstraints.add(cc)
  }

  private def setRC() = {
    val rc = new RowConstraints()
    rc.setMinHeight(focusGridRowSize)
    rc.setPrefHeight(focusGridRowSize)
    rc.setMaxHeight(focusGridRowSize)
    focusTreeView.getRowConstraints.add(rc)
  }

  @FXML def handleWelcome(): Unit =
    clear()
    loadWelcomeMessage()
