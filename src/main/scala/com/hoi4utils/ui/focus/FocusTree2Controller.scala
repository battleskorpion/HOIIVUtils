package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.FocusTreeFile
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.ObservableList
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
  private var focusGridColumnsSize: Int = 250
  private var focusGridRowSize: Int = 250
  private var gridLines: Boolean = true
  private val welcomeMessage: String = s"Welcome to the Focus Tree Viewer 2!"

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
    Some(FocusTreeFile.observeFocusTrees).foreach(trees =>
      trees.forEach(someFocusTree =>
        val toggleButton = ToggleButton(someFocusTree.toString)
        toggleButtons += toggleButton
        toggleButton.setToggleGroup(toggleGroup)
        toggleButton.setOnAction(_ => loadFocusTreeView(someFocusTree))
        vbox.getChildren.add(toggleButton)
      )
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

  private def loadFocusTreeView(someFocusTree: FocusTreeFile): Unit =
    clear()
    for _ <- 0 until someFocusTree.columns do setCC()
    for _ <- 0 until someFocusTree.rows do setRC()

    val focuses = someFocusTree.focuses
    focuses match
      case null =>
        focusTreeView.add(Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"), 0, 0)
        logger.warn("Focuses list is null, cannot draw focus tree.")
      case _ if focuses.isEmpty =>
        focusTreeView.add(Label(s"No focuses found in Focus Tree: ${someFocusTree.name}"), 0, 0)
        logger.warn("Focuses list is empty, nothing to draw.")
      case _ =>
        for row <- 0 until someFocusTree.rows do
          for column <- 0 until someFocusTree.columns do
            // Find a focus at this absolute position
            focuses.find(f => f.hasAbsolutePosition(column, row)) match
              case Some(focus) =>
                val focusButton = FocusToggleButton(focus.toString, focusGridColumnsSize, focusGridRowSize)
                focusTreeView.add(focusButton, column, row)
              case None =>
                // No focus at this position, optionally add placeholder
                ()




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
