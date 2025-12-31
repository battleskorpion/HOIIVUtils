package com.hoi4utils.ui.localization

import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.focusTreeErrors
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.javafx.scene.layout.ErrorIconPane
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, ProgressIndicator, ToggleButton, ToggleGroup, ToolBar}
import javafx.scene.layout.{AnchorPane, HBox, VBox}
import scalafx.geometry.Insets

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class FocusTreeLocalization2 extends HOIIVUtilsAbstractController2:

  @FXML private var ftlRoot: AnchorPane = uninitialized
  @FXML private var toolBar: ToolBar = uninitialized
  @FXML private var toolBar2: ToolBar = uninitialized
  @FXML private var versionLabel: Label = uninitialized
  @FXML private var saveButton: Button = uninitialized
  @FXML private var toggleGroup: ToggleGroup = uninitialized
  @FXML private var focusTreesCount: Label = uninitialized
  @FXML private var vbox: VBox = uninitialized
  @FXML private var welcome: ToggleButton = uninitialized
  @FXML private var progressIndicator: ProgressIndicator = uninitialized

  private var focusTreesToggleButtons: ListBuffer[ToggleButton] = ListBuffer.empty

  setFxmlFile("FocusTreeLocalization2.fxml")
  setTitle("Focus Tree Localization 2")

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    versionLabel.setText(s"Version: ${HOIIVUtils.get("version")}")

  override def preSetup(): Unit = setupWindowControls(ftlRoot, toolBar, toolBar2)

  private def populateFocusTreeSelection(): Unit =
    Some(FocusTreeManager.observeFocusTrees.sorted()).foreach(trees =>
      trees.forEach(someFocusTree =>
        val toggleButton = ToggleButton(someFocusTree.toString)
        focusTreesToggleButtons += toggleButton
        toggleButton.setToggleGroup(toggleGroup)
        toggleButton.setOnAction(_ => {
          if toggleButton.isSelected then
            // Manually deselect all other buttons
            focusTreesToggleButtons.foreach(btn =>
              if btn != toggleButton then
                btn.setSelected(false)
                welcome.setSelected(false)
            )
            welcome.setSelected(false)
//            loadFocusTreeView(someFocusTree)
          else
            toggleButton.setSelected(false)
            toggleButton.setSelected(true)
//            loadFocusTreeView(someFocusTree)
        })
        toggleButton.setPadding(Insets(5, 10, 5, 10))
        // Check if this focus tree has errors
        val hasErrors = focusTreeErrors.exists(_.focusTreeId == someFocusTree.id.str)
        if hasErrors then
          // Create HBox to hold toggle button and error icon
          val hbox = new HBox(5)
          hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT)
          hbox.getChildren.add(toggleButton)
          // Get the error count for this focus tree
          val errorCount = focusTreeErrors
            .find(_.focusTreeId == someFocusTree.id.str)
            .map(_.focusErrors.map(_.errors.size).sum)
            .getOrElse(0)
          // Create error icon pane
          val errorIcon = new ErrorIconPane(
            iconSize = 24,
            errorNumberCount = errorCount,
            onDoubleClick = Some(() => {
              // TODO: Open error details dialog or navigate to error list
              logger.info(s"Double-clicked error icon for ${someFocusTree.id.str}")
            }),
            tooltipText = Some(s"$errorCount error(s) in this focus tree")
          )
          errorIcon.build()
          hbox.getChildren.add(errorIcon)
          vbox.getChildren.add(hbox)
        else
          vbox.getChildren.add(toggleButton)
      )
      focusTreesCount.setText(s"Focus Trees: ${trees.size()}")
    )
    FocusTreeManager.sharedFocusFilesAsPseudoTrees.foreach(pseudoTree =>
      val toggleButton = ToggleButton(pseudoTree.toString)
      focusTreesToggleButtons += toggleButton
      toggleButton.setToggleGroup(toggleGroup)
      toggleButton.setOnAction(_ => {
        if toggleButton.isSelected then
          // Manually deselect all other buttons
          focusTreesToggleButtons.foreach(btn =>
            if btn != toggleButton then
              btn.setSelected(false)
              welcome.setSelected(false)
          )
          welcome.setSelected(false)
//          loadFocusTreeView(pseudoTree)
        else
          toggleButton.setSelected(false)
          toggleButton.setSelected(true)
//          loadFocusTreeView(pseudoTree)
      })
      toggleButton.setPadding(Insets(5, 10, 5, 10))
      // Check if this shared focus tree has errors
      val hasErrors = focusTreeErrors.exists(_.focusTreeId == s"[Shared Focuses] ${pseudoTree.id.str}")
      if hasErrors then
        // Create HBox to hold toggle button and error icon
        val hbox = new HBox(5)
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT)
        hbox.getChildren.add(toggleButton)
        // Get the error count for this shared focus tree
        val errorCount = focusTreeErrors
          .find(_.focusTreeId == s"[Shared Focuses] ${pseudoTree.id.str}")
          .map(_.focusErrors.map(_.errors.size).sum)
          .getOrElse(0)
        // Create error icon pane
        val errorIcon = new ErrorIconPane(
          iconSize = 24,
          errorNumberCount = errorCount,
          onDoubleClick = Some(() => {
            // TODO: Open error details dialog or navigate to error list
            logger.info(s"Double-clicked error icon for shared focuses ${pseudoTree.id.str}")
          }),
          tooltipText = Some(s"$errorCount error(s) in this shared focus file")
        )
        errorIcon.build()
        hbox.getChildren.add(errorIcon)
        vbox.getChildren.add(hbox)
      else
        vbox.getChildren.add(toggleButton)
    )
