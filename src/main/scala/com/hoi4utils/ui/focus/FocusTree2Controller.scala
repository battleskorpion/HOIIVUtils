package com.hoi4utils.ui.focus

import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.*

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTree2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("FocusTree2.fxml")
  setTitle("Focus Tree Viewer 2: Electric Boogaloo")
  private var focusGridColumns: Int = 0
  private var focusGridRows: Int = 0
  private var focusGridColumnsSize: Int = 100
  private var focusGridRowSize: Int = 100
  private var gridLines: Boolean = true
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"

  private var focusTreeList: ListBuffer[someFocusTest] = ListBuffer.empty

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
    focusTreeList.foreach( focusName =>
      val toggleButton = ToggleButton(focusName.name)
      toggleButtons += toggleButton
      toggleButton.setToggleGroup(toggleGroup)
      toggleButton.setOnAction( _ =>
        clear()
        logger.info(s"Selected focus tree: $focusName")
        focusTreeView.getChildren.clear()
        loadFocusTreeView(focusName)
      )
      vbox.getChildren.add(toggleButton)
    )

  /** Clears the focus tree view in the middle */
  private def clear(): Unit =
    focusTreeView.getChildren.clear()
    focusTreeView.getColumnConstraints.clear()
    focusTreeView.getRowConstraints.clear()

  private def loadWelcomeMessage(): Unit =
    focusTreeView.setGridLinesVisible(gridLines)
    focusTreeView.getColumnConstraints.add(new ColumnConstraints(focusGridColumnsSize))
    focusTreeView.getRowConstraints.add(new RowConstraints(focusGridRowSize))
    focusTreeView.add(Label(welcomeMessage), 0, 1)
    focusTreeView.addColumn(100, Label("Column 2"))
    focusTreeView.addRow(100, Label("Row 2"))

  private def populateFocusTreeList: Unit =
    focusTreeList += someFocusTest("One")
    focusTreeList += someFocusTest("Two", 42)
    focusTreeList += someFocusTest("Three", 7)

  class someFocusTest(var name: String, var testVar: Int = 0):
    val anotherVar = s"Hello, Focus Tree $name!"

  private def loadFocusTreeView(focusName: someFocusTest): Unit =
    focusTreeView.setGridLinesVisible(gridLines)
    focusTreeView.getColumnConstraints.add(new ColumnConstraints(focusGridColumnsSize))
    focusTreeView.getRowConstraints.add(new RowConstraints(focusGridRowSize))
    focusTreeView.add(Label(s"Focus Tree: ${focusName.name}"), 0, 1)
    focusTreeView.addColumn(100, Label(s"Test Var: ${focusName.testVar}"))
    focusTreeView.addRow(100, Label(s"Another Var: ${focusName.anotherVar}"))

  @FXML def handleWelcome(): Unit =
    clear()
    loadWelcomeMessage()