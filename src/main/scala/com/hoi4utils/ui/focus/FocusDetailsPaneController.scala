package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.Focus
import com.hoi4utils.ui.pdxscript.PDXEditorPane
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, ProgressIndicator, ScrollPane}
import javafx.scene.layout.{AnchorPane, HBox, VBox}

import scala.compiletime.uninitialized

class FocusDetailsPaneController extends LazyLogging:

  @FXML var rootAnchorPane: AnchorPane = uninitialized
  @FXML var contentVBox: VBox = uninitialized
  @FXML var titleLabel: Label = uninitialized
  @FXML var loadingIndicator: ProgressIndicator = uninitialized
  @FXML var editorScrollPane: ScrollPane = uninitialized
  @FXML var editorContainer: VBox = uninitialized
  @FXML var placeholderLabel: Label = uninitialized
  @FXML var actionButtonsHBox: HBox = uninitialized
  @FXML var saveButton: Button = uninitialized
  @FXML var revertButton: Button = uninitialized

  private var currentLoadTask: Option[Task[PDXEditorPane]] = None
  private var currentFocus: Option[Focus] = None

  @FXML
  def initialize(): Unit =
    if saveButton != null then
      saveButton.setOnAction(_ => handleSave())
    if revertButton != null then
      revertButton.setOnAction(_ => handleRevert())

  def loadFocus(focus: Focus): Unit =
    cancelCurrentTask()

    currentFocus = Some(focus)
    titleLabel.setText(s"Focus: ${focus.id}")

    loadingIndicator.setVisible(true)
    placeholderLabel.setVisible(false)

    val loadTask = new Task[PDXEditorPane]() {
      override def call(): PDXEditorPane = {
        logger.info(s"Loading PDXEditor for focus: ${focus.id}")

        val editorPane = new PDXEditorPane(
          focus.asInstanceOf[com.hoi4utils.script.PDXScript[?]],
          () => onFocusUpdate()
        )

        editorPane
      }
    }

    loadTask.setOnSucceeded(_ => {
      currentLoadTask match
        case Some(task) if task == loadTask =>
          Platform.runLater(() => {
            try {
              val editorPane = loadTask.getValue

              editorContainer.getChildren.clear()
              editorContainer.getChildren.add(editorPane)

              actionButtonsHBox.setVisible(true)

              logger.info(s"Successfully loaded editor for focus: ${focus.id}")
            } catch {
              case e: Exception =>
                logger.error(s"Error displaying editor for focus: ${focus.id}", e)
                showError(s"Failed to load focus details: ${e.getMessage}")
            } finally {
              loadingIndicator.setVisible(false)
              currentLoadTask = None
            }
          })
        case _ =>
          logger.info("Task succeeded but was already replaced")
          loadingIndicator.setVisible(false)
    })

    loadTask.setOnCancelled(_ => {
      logger.info(s"Load cancelled for focus: ${focus.id}")
      loadingIndicator.setVisible(false)
      currentLoadTask = None
    })

    loadTask.setOnFailed(_ => {
      logger.error(s"Failed to load focus: ${focus.id}", loadTask.getException)
      Platform.runLater(() => {
        showError(s"Failed to load focus: ${loadTask.getException.getMessage}")
        loadingIndicator.setVisible(false)
      })
      currentLoadTask = None
    })

    currentLoadTask = Some(loadTask)
    val thread = new Thread(loadTask)
    thread.setDaemon(true)
    thread.start()

  def clear(): Unit =
    cancelCurrentTask()
    currentFocus = None
    editorContainer.getChildren.clear()
    titleLabel.setText("Focus Details")
    placeholderLabel.setVisible(true)
    actionButtonsHBox.setVisible(false)
    loadingIndicator.setVisible(false)

  private def onFocusUpdate(): Unit =
    currentFocus.foreach(focus => {
      logger.info(s"Focus updated: ${focus.id}")
    })

  private def handleSave(): Unit =
    currentFocus.foreach(focus => {
      logger.info(s"Saving focus: ${focus.id}")
      // Implement save logic here
    })

  private def handleRevert(): Unit =
    currentFocus.foreach(focus => {
      logger.info(s"Reverting focus: ${focus.id}")
      loadFocus(focus)
    })

  private def showError(message: String): Unit =
    editorContainer.getChildren.clear()
    val errorLabel = new Label(message)
    errorLabel.setStyle("-fx-text-fill: red;")
    editorContainer.getChildren.add(errorLabel)
    placeholderLabel.setVisible(false)

  private def cancelCurrentTask(): Unit =
    currentLoadTask match
      case Some(task) if task.isRunning =>
        logger.info("Cancelling current load task")
        task.cancel()
        currentLoadTask = None
      case Some(task) =>
        currentLoadTask = None
      case None =>