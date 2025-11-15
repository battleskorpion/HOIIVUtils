package com.hoi4utils.script

import com.hoi4utils.parser.{Node, ParsingContext}

import java.io.{File, PrintWriter, StringWriter}
import scala.collection.mutable.ListBuffer

/**
 * PDXError represents an error encountered while parsing or processing a script file
 * todo add more details later
 */
class PDXFileError(
                var exception: Exception = null,
                var errorNode: Node = null,
                var pdxScript: PDXScript[?] = null,
                var additionalInfo: Map[String, String] = Map.empty
              )(using context: ParsingContext):

  def file: File = context.file
  def line: Option[Int] = context.line
  def column: Option[Int] = context.column

  def addInfo(key: String, value: String): PDXFileError =
    additionalInfo = additionalInfo + (key -> value)
    this

  def addInfo(info: Map[String, String]): PDXFileError =
    additionalInfo = additionalInfo ++ info
    this

  /**
   * Returns the stack trace of the exception in standard format.
   * Format: "at package.Class.method(FileName.scala:lineNumber)"
   * This format is parseable for future clickable editor links (similar to console output).
   *
   * @return Stack trace as a string, or "No stack trace available" if no exception exists
   */
  def getStackTrace: String =
    if exception == null then
      "No stack trace available"
    else
      val stringWriter = new StringWriter()
      val printWriter = new PrintWriter(stringWriter)
      exception.printStackTrace(printWriter)
      printWriter.flush()
      stringWriter.toString

  private def nodeContext = Map(
    "Node Identifier" -> errorNode.identifier.getOrElse("none"),
    "Node Value" -> Option(errorNode.$).map(_.toString).getOrElse("null"),
    "Node Type" -> Option(errorNode.$).map(_.getClass.getSimpleName).getOrElse("null")
  )

  /**
   * Extracts token context from ParserException if available.
   * Similar to nodeContext but for token-based parser errors.
   */
  private def tokenContext: Map[String, String] = exception match
    case parserEx: com.hoi4utils.parser.ParserException if parserEx.token.isDefined =>
      val token = parserEx.token.get
      Map(
        "Token Value" -> token.value,
        "Token Type" -> token.`type`.toString,
        "Line" -> token.line.toString,
        "Column" -> token.column.toString,
        "Character Position" -> token.start.toString
      )
    case _ => Map.empty

  /**
   * Returns true if this error has token information from a ParserException
   */
  def hasTokenInfo: Boolean = exception match
    case parserEx: com.hoi4utils.parser.ParserException => parserEx.token.isDefined
    case _ => false

  /**
   * Gets the line number from either the token (if ParserException) or node
   */
  def getLine: Option[Int] = exception match
    case parserEx: com.hoi4utils.parser.ParserException if parserEx.token.isDefined =>
      Some(parserEx.token.get.line)
    case _ if errorNode != null && errorNode.identifierToken.isDefined =>
      Some(errorNode.identifierToken.get.line)
    case _ => None

  /**
   * Gets the column number from either the token (if ParserException) or node
   */
  def getColumn: Option[Int] = exception match
    case parserEx: com.hoi4utils.parser.ParserException if parserEx.token.isDefined =>
      Some(parserEx.token.get.column)
    case _ if errorNode != null && errorNode.identifierToken.isDefined =>
      Some(errorNode.identifierToken.get.column)
    case _ => None

  override def toString: String =
    // Special case: if this is just a placeholder message with no actual error data
    if exception == null && errorNode == null && pdxScript == null &&
       additionalInfo.size == 1 && additionalInfo.contains("message") then
      return additionalInfo("message")

    val parts = ListBuffer[String]()

    // Exception info first
    if exception != null then
      parts += s"${exception.getClass.getSimpleName}:"
      parts += s"\t${exception.getMessage}"
    else
      parts += "No Exception"

    // Token info (if ParserException with token)
    if tokenContext.nonEmpty then
      parts += "Token Info:"
      tokenContext.foreach { case (k, v) => parts += s"\t$k: $v" }

    // Node info
    if errorNode != null then
      parts += "Node Info:"
      nodeContext.foreach { case (k, v) => parts += s"\t$k: $v" }
    else if tokenContext.isEmpty then
      parts += "Node Info: None"

    // File path
    parts += s"File: ${file.getAbsolutePath}"

    // Class name (if pdxScript is set)
    if pdxScript != null then
      parts += pdxScript.getClass.getSimpleName
    else
      parts += "Class: None"

    // Additional info
    if additionalInfo.nonEmpty then
      additionalInfo.foreach { case (k, v) => parts += s"\t$k: $v" }
    else
      parts += "Additional Info: None"

    parts.mkString("\n")

/**
 * Wrapper for Focus errors - groups all errors for a single Focus
 */
class FocusErrorGroup(val focusId: String, val errors: ListBuffer[PDXFileError]):
  override def toString: String =
    if errors.isEmpty then
      s"\tFocus: $focusId\n\t\tNo errors"
    else
      val errorStrings = errors.map(_.toString.split("\n").map(line => s"\t\t$line").mkString("\n"))
      s"\tFocus: $focusId\n${errorStrings.mkString("\n")}"

/**
 * Wrapper for FocusTree errors - groups all focus errors for a tree
 */
class FocusTreeErrorGroup(val focusTreeId: String, val focusErrors: ListBuffer[FocusErrorGroup]):
  override def toString: String =
    // Special case: if this is a placeholder with empty errors, just show the message
    if focusErrors.isEmpty && focusTreeId == "No errors" then
      "No Problems Found"
    else if focusErrors.isEmpty then
      s"FocusTree: $focusTreeId\n\tNo focus errors"
    else
      s"FocusTree: $focusTreeId\n${focusErrors.mkString("\n")}"
