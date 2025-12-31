package com.hoi4utils.ui.localization

import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, ToolBar}
import javafx.scene.layout.AnchorPane

import scala.compiletime.uninitialized

class FocusTreeLocalization2 extends HOIIVUtilsAbstractController2:

  @FXML private var ftlRoot: AnchorPane = uninitialized
  @FXML private var toolBar: ToolBar = uninitialized
  @FXML private var toolBar2: ToolBar = uninitialized
  @FXML private var versionLabel: Label = uninitialized
  @FXML private var saveButton: Button = uninitialized

  setFxmlFile("FocusTreeLocalization2.fxml")
  setTitle("Focus Tree Localization 2")
  
  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    versionLabel.setText(s"Version: ${HOIIVUtils.get("version")}")

  override def preSetup(): Unit = setupWindowControls(, toolBar, toolBar2)
