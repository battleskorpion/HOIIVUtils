package com.hoi4utils.script

import com.hoi4utils.parser.Node

import java.io.File
import scala.collection.mutable.ListBuffer

/**
 * PDXError represents an error encountered while parsing or processing
 * todo add more details later
 */
class PDXError(
                var exception: Exception = null,
                var errorNode: Node = null,
                var pdxScript: PDXScript[?] = null,
                var file: Option[File] = None,
                var additionalInfo: Map[String, String] = Map.empty
              ):

  def addInfo(key: String, value: String): PDXError =
    additionalInfo = additionalInfo + (key -> value)
    this

  def addInfo(info: Map[String, String]): PDXError =
    additionalInfo = additionalInfo ++ info
    this

  private def nodeContext = Map(
    "Node Identifier" -> errorNode.identifier.getOrElse("none"),
    "Node Value" -> Option(errorNode.$).map(_.toString).getOrElse("null"),
    "Node Type" -> Option(errorNode.$).map(_.getClass.getSimpleName).getOrElse("null")
  )

  override def toString: String =
    // Special case: if this is just a placeholder message with no actual error data
    if exception == null && errorNode == null && file.isEmpty && pdxScript == null &&
       additionalInfo.size == 1 && additionalInfo.contains("message") then
      return additionalInfo("message")

    val parts = ListBuffer[String]()

    // Exception info first
    if exception != null then
      parts += s"${exception.getClass.getSimpleName}:"
      parts += s"\t${exception.getMessage}"
    else
      parts += "No Exception"

    // Node info
    if errorNode != null then
      parts += "Node Info:"
      nodeContext.foreach { case (k, v) => parts += s"\t$k: $v" }
    else
      parts += "Node Info: None"

    // File path
    file match
      case Some(f) => parts += s"File: ${f.getAbsolutePath}"
      case None => parts += "File: None"

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
class FocusErrorGroup(val focusId: String, val errors: ListBuffer[PDXError]):
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
