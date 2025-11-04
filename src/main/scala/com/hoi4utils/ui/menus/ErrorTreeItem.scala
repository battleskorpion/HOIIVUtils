package com.hoi4utils.ui.menus

import com.hoi4utils.script.{FocusErrorGroup, FocusTreeErrorGroup, PDXError}

import java.io.File

/**
 * Wrapper class for TreeView items in the error list.
 * Provides a hierarchical structure for displaying errors similar to IntelliJ's project view.
 *
 * @param displayText The text to display in the tree node
 * @param itemType The type of item (File, FocusTree, Focus, or Error)
 * @param pdxError Optional PDXError for leaf nodes
 * @param children Child items for expandable nodes
 */
class ErrorTreeItem(
                     val displayText: String,
                     val itemType: ErrorTreeItemType,
                     val pdxError: Option[PDXError] = None,
                     val children: List[ErrorTreeItem] = List.empty
                   ):

  override def toString: String = displayText

/**
 * Enum for different types of tree items in the error hierarchy
 */
enum ErrorTreeItemType:
  case FileGroup      // Top-level: file name with error count
  case FocusTreeGroup // Top-level: focus tree with error count
  case FocusGroup     // Mid-level: individual focus with error count
  case ErrorDetail    // Leaf: expandable error with details
  case ErrorSection   // Sub-leaf: individual section within an error (exception, stack trace, etc.)

/**
 * Factory methods for creating ErrorTreeItem instances
 */
object ErrorTreeItem:

  /**
   * Creates a file group node with all errors from that file
   */
  def fromFileGroup(file: File, errors: List[PDXError]): ErrorTreeItem =
    val fileName = file.getName
    val errorCount = errors.size
    val displayText = s"$fileName ($errorCount ${if errorCount == 1 then "error" else "errors"})"

    val errorChildren = errors.map(fromPDXError)

    new ErrorTreeItem(displayText, ErrorTreeItemType.FileGroup, None, errorChildren)

  /**
   * Creates a focus tree group node with all focus errors
   */
  def fromFocusTreeErrorGroup(focusTreeGroup: FocusTreeErrorGroup): ErrorTreeItem =
    val errorCount = focusTreeGroup.focusErrors.map(_.errors.size).sum
    val displayText = s"${focusTreeGroup.focusTreeId} ($errorCount ${if errorCount == 1 then "error" else "errors"})"

    val focusChildren = focusTreeGroup.focusErrors.map(fromFocusErrorGroup).toList

    new ErrorTreeItem(displayText, ErrorTreeItemType.FocusTreeGroup, None, focusChildren)

  /**
   * Creates a focus group node with all errors for that focus
   */
  def fromFocusErrorGroup(focusGroup: FocusErrorGroup): ErrorTreeItem =
    val errorCount = focusGroup.errors.size
    val displayText = s"${focusGroup.focusId} ($errorCount ${if errorCount == 1 then "error" else "errors"})"

    val errorChildren = focusGroup.errors.map(fromPDXError).toList

    new ErrorTreeItem(displayText, ErrorTreeItemType.FocusGroup, None, errorChildren)

  /**
   * Creates an error detail node with expandable sections
   */
  def fromPDXError(error: PDXError): ErrorTreeItem =
    // Get summary text for the error (first line or exception message)
    val summary = if error.exception != null then
      s"${error.exception.getClass.getSimpleName}: ${error.exception.getMessage}"
    else if error.additionalInfo.contains("message") then
      error.additionalInfo("message")
    else
      "Error"

    // Create child sections for expandable details
    val sections = List(
      createExceptionSection(error),
      createNodeInfoSection(error),
      createFileSection(error),
      createStackTraceSection(error),
      createAdditionalInfoSection(error)
    ).flatten

    new ErrorTreeItem(summary, ErrorTreeItemType.ErrorDetail, Some(error), sections)

  /**
   * Creates the exception section if available
   */
  private def createExceptionSection(error: PDXError): Option[ErrorTreeItem] =
    if error.exception != null then
      val exceptionText = s"${error.exception.getClass.getSimpleName}: ${error.exception.getMessage}"
      Some(new ErrorTreeItem(s"Exception: $exceptionText", ErrorTreeItemType.ErrorSection, Some(error)))
    else
      None

  /**
   * Creates the node info section if available
   */
  private def createNodeInfoSection(error: PDXError): Option[ErrorTreeItem] =
    if error.errorNode != null then
      val nodeText = s"Identifier: ${error.errorNode.identifier.getOrElse("none")}, " +
                     s"Value: ${Option(error.errorNode.$).map(_.toString).getOrElse("null")}"
      Some(new ErrorTreeItem(s"Node: $nodeText", ErrorTreeItemType.ErrorSection, Some(error)))
    else
      None

  /**
   * Creates the file section if available
   */
  private def createFileSection(error: PDXError): Option[ErrorTreeItem] =
    error.file.map { f =>
      new ErrorTreeItem(s"File: ${f.getName}", ErrorTreeItemType.ErrorSection, Some(error))
    }

  /**
   * Creates the stack trace section if available
   */
  private def createStackTraceSection(error: PDXError): Option[ErrorTreeItem] =
    if error.exception != null then
      // Create a node that will show "Stack Trace (click to expand)"
      Some(new ErrorTreeItem("Stack Trace (expandable)", ErrorTreeItemType.ErrorSection, Some(error)))
    else
      None

  /**
   * Creates the additional info section if available
   */
  private def createAdditionalInfoSection(error: PDXError): Option[ErrorTreeItem] =
    if error.additionalInfo.nonEmpty && !(error.additionalInfo.size == 1 && error.additionalInfo.contains("message")) then
      val infoText = error.additionalInfo.map { case (k, v) => s"$k: $v" }.mkString(", ")
      Some(new ErrorTreeItem(s"Info: $infoText", ErrorTreeItemType.ErrorSection, Some(error)))
    else
      None
