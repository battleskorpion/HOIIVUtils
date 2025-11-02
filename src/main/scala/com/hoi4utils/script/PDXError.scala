package com.hoi4utils.script

import com.hoi4utils.parser.Node

import java.io.File

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

  private def nodeContext = Map(
    "Node Identifier" -> errorNode.identifier.getOrElse("none"),
    "Node Value" -> Option(errorNode.$).map(_.toString).getOrElse("null"),
    "Node Type" -> Option(errorNode.$).map(_.getClass.getSimpleName).getOrElse("null")
  )

  override def toString: String =
    val fileInfo =
      file match
        case Some(f) => s"File: ${f.getAbsolutePath}\n"
    
    val nodeInfo =
      if errorNode != null then
        s"Node Context:\n" +
        nodeContext.map { case (k, v) => s"  $k: $v" }.mkString("\n") + "\n"

    val exceptionInfo =
      if exception != null then
        s"Exception Message: ${exception.getMessage}\n"

    val additionalInfoStr =
      if additionalInfo.nonEmpty then
        s"Additional Info:\n" +
        additionalInfo.map { case (k, v) => s"  $k: $v" }.mkString("\n") + "\n"

    s"${fileInfo}${nodeInfo}${exceptionInfo}${additionalInfoStr}"
