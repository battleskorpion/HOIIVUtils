package com.hoi4utils.ui

import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.stage.{FileChooser, Stage}

import java.io.{FileWriter, OutputStream, PrintStream}
import java.net.URL
import java.util.ResourceBundle
import scala.compiletime.uninitialized

/**
 * Controller for the Log Viewer window.
 * Redirects logger to a TextArea and provides options to clear and save the log.
 */
class LogViewerController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("LogViewer.fxml")
  setTitle("Log Viewer")
  @FXML private var consoleOutput: TextArea = uninitialized

  def initialize(): Unit = {
    logger.info("Log Viewer initialized")
  }

  def initialize(location: URL, resources: ResourceBundle): Unit = {
  }

  /**
   * Called when the clear button is pressed
   */
  @FXML
  def clearConsole(): Unit = {
    consoleOutput.clear()
  }

  /**
   * Called when the save to file button is pressed
   */
  @FXML
  def saveToFile(): Unit = {
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Save Console Output")
    fileChooser.getExtensionFilters.add(
      new FileChooser.ExtensionFilter("Text Files", "*.txt")
    )

    val stage = consoleOutput.getScene.getWindow.asInstanceOf[Stage]
    val file = fileChooser.showSaveDialog(stage)

    if (file != null) {
      try {
        val writer = new FileWriter(file)
        try {
          writer.write(consoleOutput.getText)
        } finally {
          writer.close()
        }
      } catch {
        case e: Exception =>
          logger.error(s"Error saving file: ${e.getMessage}")
      }
    }
  }
}