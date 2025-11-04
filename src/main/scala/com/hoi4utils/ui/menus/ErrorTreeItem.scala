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
 * @param filePath Optional file path for "Open File" button
 * @param isStackTraceLine Whether this is a clickable stack trace line
 */
class ErrorTreeItem(
                     val displayText: String,
                     val itemType: ErrorTreeItemType,
                     val pdxError: Option[PDXError] = None,
                     val children: List[ErrorTreeItem] = List.empty,
                     val filePath: Option[File] = None,
                     val isStackTraceLine: Boolean = false
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
    val fullPath = file.getAbsolutePath
    val displayText = s"File: ($errorCount ${if errorCount == 1 then "error" else "errors"}) \"$fullPath\""

    val errorChildren = errors.map(fromPDXError)

    new ErrorTreeItem(displayText, ErrorTreeItemType.FileGroup, None, errorChildren, Some(file))

  /**
   * Creates a focus tree group node with all focus errors
   */
  def fromFocusTreeErrorGroup(focusTreeGroup: FocusTreeErrorGroup): ErrorTreeItem =
    val errorCount = focusTreeGroup.focusErrors.map(_.errors.size).sum

    // Extract focus tree ID and file path from focusTreeId
    // Format might be "[Shared Focuses] filename" or just "focus_tree_id"
    val (treeType, treeId, filePath) = if focusTreeGroup.focusTreeId.startsWith("[Shared Focuses]") then
      ("Shared Focus", focusTreeGroup.focusTreeId.replace("[Shared Focuses]", "").trim, focusTreeGroup.focusTreeId)
    else if focusTreeGroup.focusTreeId.startsWith("Focus Tree:") then
      ("Focus Tree", focusTreeGroup.focusTreeId.replace("Focus Tree:", "").trim, focusTreeGroup.focusTreeId)
    else
      ("Focus Tree", focusTreeGroup.focusTreeId, focusTreeGroup.focusTreeId)

    val displayText = s"$treeType: ($errorCount ${if errorCount == 1 then "error" else "errors"}) $treeId"

    val focusChildren = focusTreeGroup.focusErrors.flatMap(_.errors).map(fromPDXError).toList

    new ErrorTreeItem(displayText, ErrorTreeItemType.FocusTreeGroup, None, focusChildren, None)

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
    // Exception name as the main node
    val exceptionName = if error.exception != null then
      s"${error.exception.getClass.getSimpleName}:"
    else
      "Error:"

    // Build children in order: message, token, node, additional info, stack trace
    val children = List(
      createExceptionMessageSection(error),
      createTokenDetailSection(error),
      createNodeDetailSection(error),
      createAdditionalInfoDetailSection(error),
      createStackTraceDetailSection(error)
    ).flatten

    new ErrorTreeItem(exceptionName, ErrorTreeItemType.ErrorDetail, Some(error), children, error.file)

  /**
   * Creates the exception message node
   */
  private def createExceptionMessageSection(error: PDXError): Option[ErrorTreeItem] =
    if error.exception != null && error.exception.getMessage != null then
      Some(new ErrorTreeItem(error.exception.getMessage, ErrorTreeItemType.ErrorSection, Some(error)))
    else
      None

  /**
   * Creates the token detail section with child nodes for each token property
   */
  private def createTokenDetailSection(error: PDXError): Option[ErrorTreeItem] =
    error.exception match
      case parserEx: com.hoi4utils.parser.ParserException if parserEx.token.isDefined =>
        val token = parserEx.token.get
        val tokenLabel = s"Token: ${token.value}"

        // Create child nodes for each token detail
        val tokenDetails = List(
          new ErrorTreeItem(s"Token Value: ${token.value}", ErrorTreeItemType.ErrorSection, Some(error)),
          new ErrorTreeItem(s"Token Type: ${token.`type`}", ErrorTreeItemType.ErrorSection, Some(error)),
          new ErrorTreeItem(s"Line: ${token.line}", ErrorTreeItemType.ErrorSection, Some(error)),
          new ErrorTreeItem(s"Column: ${token.column}", ErrorTreeItemType.ErrorSection, Some(error)),
          new ErrorTreeItem(s"Character Position: ${token.start}", ErrorTreeItemType.ErrorSection, Some(error))
        )

        Some(new ErrorTreeItem(tokenLabel, ErrorTreeItemType.ErrorSection, Some(error), tokenDetails))
      case _ => None

  /**
   * Creates the node detail section with child nodes for each node property
   */
  private def createNodeDetailSection(error: PDXError): Option[ErrorTreeItem] =
    if error.errorNode != null then
      val nodeLabel = s"Node: ${error.errorNode.identifier.getOrElse("(no identifier)")}"

      // Create child nodes for each node detail
      val nodeDetails = List(
        new ErrorTreeItem(s"Node Identifier: ${error.errorNode.identifier.getOrElse("none")}", ErrorTreeItemType.ErrorSection, Some(error)),
        new ErrorTreeItem(s"Node Value: ${Option(error.errorNode.$).map(_.toString).getOrElse("null")}", ErrorTreeItemType.ErrorSection, Some(error)),
        new ErrorTreeItem(s"Node Type: ${Option(error.errorNode.$).map(_.getClass.getSimpleName).getOrElse("null")}", ErrorTreeItemType.ErrorSection, Some(error))
      )

      Some(new ErrorTreeItem(nodeLabel, ErrorTreeItemType.ErrorSection, Some(error), nodeDetails))
    else
      None

  /**
   * Creates additional info section with child nodes for each info entry
   */
  private def createAdditionalInfoDetailSection(error: PDXError): Option[ErrorTreeItem] =
    if error.additionalInfo.nonEmpty && !(error.additionalInfo.size == 1 && error.additionalInfo.contains("message")) then
      val filteredInfo = error.additionalInfo.filterNot(_._1 == "message")

      if filteredInfo.nonEmpty then
        // Create child nodes for each additional info entry
        val infoDetails = filteredInfo.map { case (key, value) =>
          new ErrorTreeItem(s"$key: $value", ErrorTreeItemType.ErrorSection, Some(error))
        }.toList

        Some(new ErrorTreeItem("Additional Info:", ErrorTreeItemType.ErrorSection, Some(error), infoDetails))
      else
        None
    else
      None

  /**
   * Creates the stack trace section with clickable child nodes for each stack trace line
   */
  private def createStackTraceDetailSection(error: PDXError): Option[ErrorTreeItem] =
    if error.exception != null then
      val stackTrace = error.getStackTrace

      if stackTrace != "No stack trace available" then
        // Parse stack trace lines
        val stackTraceLines = stackTrace.split("\n").toList.filter(_.trim.nonEmpty)

        // Create clickable child nodes for each stack trace line
        val stackTraceDetails = stackTraceLines.map { line =>
          new ErrorTreeItem(line.trim, ErrorTreeItemType.ErrorSection, Some(error), List.empty, None, isStackTraceLine = true)
        }

        Some(new ErrorTreeItem("Stack Trace:", ErrorTreeItemType.ErrorSection, Some(error), stackTraceDetails))
      else
        None
    else
      None
