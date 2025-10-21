package com.hoi4utils.ui.parser

import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.layout.VBox

import scala.compiletime.uninitialized

class ParserViewerController2 extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("/com/hoi4utils/ui/parser/ParserViewer2.fxml")
  setTitle("Parser Viewer 2")
  
  @FXML var root: VBox = uninitialized

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()

  override def preSetup(): Unit = setupWindowControls(root)
