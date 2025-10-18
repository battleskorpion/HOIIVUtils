package com.hoi4utils.ui.focus

import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.*

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

  @FXML var focusTree2: VBox = uninitialized
  @FXML var menuBar: MenuBar = uninitialized
  @FXML var focusTreeView: GridPane = uninitialized
  @FXML var mClose: Button = uninitialized
  @FXML var mSquare: Button = uninitialized
  @FXML var mMinimize: Button = uninitialized
  @FXML var focusSelection: ScrollPane = uninitialized

  @FXML def initialize(): Unit =
    // add a button to focusSelection that is the load welcome message page
    populateFocusSelection()

  override def preSetup(): Unit = setupWindowControls(focusTree2, mClose, mSquare, mMinimize, menuBar)

  private def populateFocusSelection(): Unit =
    val vbox = new VBox()
    val button1 = new Button("Load Welcome Message")
    button1.setOnAction(_ => loadWelcomeMessage())
    vbox.getChildren.add(button1)
    focusSelection.setContent(vbox)

  private def loadWelcomeMessage(): Unit =
    focusTreeView.setGridLinesVisible(gridLines)
    focusTreeView.getColumnConstraints.add(new ColumnConstraints(focusGridColumnsSize))
    focusTreeView.getRowConstraints.add(new RowConstraints(focusGridRowSize))
    focusTreeView.add(Label(welcomeMessage), 0, 1)
    focusTreeView.addColumn(100, Label("Column 2"))
    focusTreeView.addRow(100, Label("Row 2"))