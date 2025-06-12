package com.hoi4utils.script

import com.hoi4utils.exceptions.NodeValueTypeException
import com.hoi4utils.parser.Node

import scala.collection.mutable.ListBuffer

trait HeadlessPDX:
  self: StructuredPDX =>

  /** Overrides the default set behavior to ignore the identifier check.
   * This is useful for headless files where there is no top-level key.
   */
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit =
    // Skip identifier checking since headless files do not have a named header.
    node = Some(expression)
    expression.$ match
      case listBuffer: ListBuffer[Node] =>
        // Load each sub-PDXScript
        childScripts.foreach(_.loadPDX(listBuffer))
      case _ =>
        throw NodeValueTypeException(expression, "listBuffer", getClass)

// Commented alternative implementation using pattern matching for error handling:
//
// /** Optionally, override loadPDX if you want to further simplify handling of headless files.
//   * Here, we assume that a headless node does not have a name and can be processed directly.
//   */
// override def loadPDX(expression: Node): Unit =
//   scala.util.Try(set(expression)).recover {
//     case e: UnexpectedIdentifierException =>
//       println(s"Unexpected identifier in headless PDX script: ${e.getMessage}")
//     case e: NodeValueTypeException =>
//       println(s"Node value type error in headless PDX script: ${e.getMessage}")
//   }