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

class LogViewerController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("LogViewer.fxml")
  setTitle("Log Viewer")

  def initialize(): Unit = {
    logger.info("Log Viewer initialized")
  }

  @FXML private var consoleOutput: TextArea = uninitialized

  private var originalOut: PrintStream = uninitialized
  private var customOut: ConsoleOutputStream = uninitialized

  def initialize(location: URL, resources: ResourceBundle): Unit = {
    // Store the original System.out
    originalOut = System.out

    // Create our custom output stream and redirect System.out to it
    customOut = new ConsoleOutputStream(consoleOutput)
    System.setOut(new PrintStream(customOut, true))

    // Test output
    println("Console initialized")
    println("This text should appear in the JavaFX console window")
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
          System.err.println(s"Error saving file: ${e.getMessage}")
      }
    }
  }

  /**
   * Should be called when the application closes
   */
  def shutdown(): Unit = {
    // Restore the original System.out
    if (originalOut != null) {
      System.setOut(originalOut)
    }
  }

  /**
   * Custom OutputStream that writes to the TextArea
   */
  private class ConsoleOutputStream(textArea: TextArea) extends OutputStream {
    private val buffer = new StringBuilder()

    override def write(b: Int): Unit = {
      // Write to the original stdout
      if (originalOut != null) {
        originalOut.write(b)
      }

      // Process for display in the TextArea
      if (b == '\n') {
        // Update the TextArea on the JavaFX application thread
        val text = buffer.toString()
        Platform.runLater(() => {
          textArea.appendText(text + "\n")
          // Auto-scroll to the bottom
          textArea.positionCaret(textArea.getText.length)
        })
        buffer.clear()
      } else {
        buffer.append(b.toChar)
      }
    }
  }
}