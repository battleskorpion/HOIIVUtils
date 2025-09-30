package com.hoi4utils.ui.focus_view

import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTreeFile}
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.pdxscript.PDXEditorController
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.{ContextMenu, MenuItem}
import scalafx.scene.control.*
import scalafx.scene.input.{MouseButton, MouseEvent}

/**
 * Simplified scroll pane wrapper around the new hybrid FocusTreeView.
 * This maintains the same interface as the original for compatibility.
 */
class FocusTreeScrollPane(private var _focusTree: Option[FocusTreeFile]) extends ScrollPane with LazyLogging:

  // Use the new hybrid focus tree view
  private val focusTreeView = new FocusTreeView(_focusTree)

  // Initialize scroll pane with the new view
  this.setContent(focusTreeView)
  this.fitToWidth = false
  this.fitToHeight = false

  // Callback for external editor opening (will be set by controller)
  private var externalEditorCallback: Option[(Focus, () => Unit) => Unit] = None

  // Set up the focus interaction callbacks
  focusTreeView.setOnFocusDoubleClick(openPDXEditor)
  focusTreeView.setOnFocusRightClick(handleFocusRightClick)
  focusTreeView.setOnRequestPanning(handlePanningRequest)

  setupScrollPaneHandlers()

  /**
   * Get/set the focus tree
   */
  def focusTree: Option[FocusTreeFile] = _focusTree

  def focusTree_=(focusTree: Option[FocusTreeFile]): Unit =
    _focusTree = focusTree
    focusTreeView.focusTree = focusTree

  def focusTree_=(focusTree: FocusTreeFile): Unit =
    this.focusTree = Some(focusTree)

  /**
   * Redraw the focus tree (delegates to the view)
   */
  def redrawFocusTree(): Unit =
    focusTreeView.redraw()

  /**
   * Toggle grid lines
   */
  def toggleGridLines(): Unit =
    focusTreeView.toggleGridLines()

  /**
   * Get selected focuses
   */
  def getSelectedFocuses: Set[Focus] =
    focusTreeView.getSelectedFocuses

  /**
   * Set the external editor callback (called by controller)
   */
  def setEditorCallback(callback: (Focus, () => Unit) => Unit): Unit =
    externalEditorCallback = Some(callback)

  /**
   * Handle double-click on a focus to open the PDX editor
   */
  private def openPDXEditor(focus: Focus): Unit =
    try
      externalEditorCallback match
        case Some(callback) =>
          callback(focus, () => redrawFocusTree())
        case None =>
          // Fallback to direct editor if no callback set
          val editor = PDXEditorController()
          editor.open(focus.asInstanceOf[PDXScript[?]], () => redrawFocusTree())
      logger.info(s"Opened PDX editor for focus: ${focus.id.getOrElse("unknown")}")
    catch
      case e: Exception =>
        logger.error(s"Failed to open PDX editor for focus: ${focus.id.getOrElse("unknown")}", e)

  /**
   * Handle right-click on a focus
   */
  private def handleFocusRightClick(focus: Focus, event: MouseEvent): Unit =
    val contextMenu = new ContextMenu()

    val editFocusItem = new MenuItem("Edit Focus")
    editFocusItem.setOnAction(_ => openPDXEditor(focus))

    val deleteFocusItem = new MenuItem("Delete Focus")
    deleteFocusItem.setOnAction(_ => handleDeleteFocus(focus))

    val setRelativeFocusItem = new MenuItem("Set Relative Focus")
    setRelativeFocusItem.setOnAction(_ => handleSetRelativeFocus(focus))

    contextMenu.getItems.addAll(editFocusItem, deleteFocusItem, setRelativeFocusItem)
    contextMenu.show(focusTreeView, event.getScreenX, event.getScreenY)

  /**
   * Handle deleting a focus
   */
  private def handleDeleteFocus(focus: Focus): Unit =
    _focusTree.foreach { tree =>
      // Remove from tree
      tree.focuses.remove(focus)
      redrawFocusTree()
      logger.info(s"Deleted focus: ${focus.id.getOrElse("unknown")}")
    }

  /**
   * Handle setting relative focus position
   */
  private def handleSetRelativeFocus(focus: Focus): Unit =
    logger.info(s"Set relative focus for: ${focus.id.getOrElse("unknown")}")
    // TODO: Implement relative focus setting dialog

  /**
   * Handle panning requests from the focus tree view
   */
  private def handlePanningRequest(enable: Boolean): Unit =
    this.setPannable(enable)
    if enable then
      logger.debug("Background drag panning enabled")
    else
      logger.debug("Background drag panning disabled")

  /**
   * Set up scroll pane specific handlers
   */
  private def setupScrollPaneHandlers(): Unit =
    // Setup middle mouse button panning
    this.setOnMousePressed { event =>
      if event.getButton == MouseButton.Middle then
        logger.debug("Middle mouse button pressed - enabling panning.")
        this.setPannable(true)
    }

    this.setOnMouseReleased { event =>
      if event.getButton == MouseButton.Middle then
        logger.debug("Middle mouse button released - disabling panning.")
        this.setPannable(false)
    }

  /**
   * Compatibility method - delegates to view
   */
  @FXML
  def selectClosestMatch(comboBox: ComboBox[FocusTreeFile], typedText: String): Unit =
    // This method was in the original but seems unrelated to the scroll pane
    // Keeping for compatibility but making it a no-op
    logger.debug(s"selectClosestMatch called with typedText: $typedText")