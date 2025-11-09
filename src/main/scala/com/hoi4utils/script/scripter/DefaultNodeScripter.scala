package com.hoi4utils.script.scripter

import com.hoi4utils.parser.{Comment, Node, NodeValueType, Token}
import dotty.tools.dotc.config.ScalaSettings.indent

import scala.collection.mutable.ListBuffer

object DefaultNodeScripter extends NodeScripter {

  /**
   * @inheritdoc
   */
  override def appendPrefix(using sb: StringBuilder)(node: Node, indent: String): Unit =
    node.identifier.foreach { id =>
      sb.append(indent).append(id)
      node.operator.foreach(op => sb.append(" ").append(op))
      sb.append(" ") // separate id/op from the value
    }

  // Ensure each child ends with a newline
  //if (!child.toScript(childIndent).endsWith("\n")) sb.append("\n")
  //if (sb.nonEmpty) sb.deleteCharAt(sb.length - 1)

  override protected def modifiedChildScript(childScript: String): String =
    if childScript.contains("\n") then childScript
    else childScript + "\n"
}
