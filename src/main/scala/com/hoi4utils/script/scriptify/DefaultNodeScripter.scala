package com.hoi4utils.script.scriptify

import com.hoi4utils.parser.{Node, Token}
import dotty.tools.dotc.config.ScalaSettings.indent

import scala.collection.mutable.ListBuffer

object DefaultNodeScripter extends NodeScripter {

  /**
   *
   * @param sb String builder to append to
   * @param node
   * @param indent
   * @return
   */
  override protected def appendRhs(using sb: StringBuilder)(node: Node, indent: String): Unit =
    // append value
    node.rawValue match
      case Some(children: ListBuffer[Node]) =>
        if isNumberBlock(children) then appendNumberBlock(children)
        else appendChildrenBlock(node, indent, children)
      case Some(literal) => sb.append(literal.toString)
      case None =>
        if node.identifier.nonEmpty && node.operator.nonEmpty then sb.append("[null]")
        else () // explicitly do nothing, maybe log this? TODO
    // Append trailing trivia (e.g. comments that came after the node)
    appendTrivia(node.trailingTrivia, indent)

  /**
   * @inheritdoc
   */
  override def appendChildren(using sb: StringBuilder)(children: Iterable[Node], indent: String): Unit =
    for child <- children do
      // Recursively call toScript on each child with increased indent
      var childToScript = toScriptAccumulator(child, indent)
      if !childToScript.contains("\n") then childToScript = childToScript + "\n"
      sb.append(childToScript)
  // Ensure each child ends with a newline
  //if (!child.toScript(childIndent).endsWith("\n")) sb.append("\n")
  //if (sb.nonEmpty) sb.deleteCharAt(sb.length - 1)


}
