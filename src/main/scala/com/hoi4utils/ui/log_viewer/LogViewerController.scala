package com.hoi4utils.ui.log_viewer

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.IOException
import java.nio.file.*

class LogViewerController extends HOIIVUtilsAbstractController {
  private val LOGGER: Logger = LogManager.getLogger(this.getClass)
  setFxmlResource("LogViewer.fxml")
  setTitle("Log Viewer")

  def initialize(): Unit = {
    LOGGER.info("Log Viewer initialized")
    println("Log Viewer initialized")
  }
}