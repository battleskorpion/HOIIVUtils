package com.hoi4utils.script.scripter

import com.hoi4utils.parser.{Node, NodeValueType, Token}
import com.hoi4utils.script.scripter.DefaultNodeScripter.{appendChildrenBlock, appendLiteral, appendMissingValue, appendNumberBlock, appendTrivia, isNumberBlock, toScriptAcc}

import scala.collection.mutable.ListBuffer

trait NodeScripter {

  protected val nullStr = "[null]"

  protected def isAppendTrivia: Boolean = true

  protected def isAutoIndent: Boolean = true

  /**
   * Reconstructs the original file text by concatenating:
   * - leading trivia (whitespace/comments)
   * - the identifier and operator tokens
   * - the node's value (or nested children, if a block)
   * - trailing trivia
   */
  def toScript(node: Node): String = toScriptAcc(node, "")

    protected def toScriptAcc(node: Node, indent: String): String =
      given sb: StringBuilder = new StringBuilder

      appendTrivia(node.leadingTrivia, indent)
      appendPrefix(node, indent)
      appendRhs(node, indent)

      sb.toString()

  /**
   *
   * @param sb String builder to append to
   * @param node
   * @param indent
   * @return
   */
  protected def appendRhs(using sb: StringBuilder)(node: Node, indent: String): Unit =
    // append value
    node.rawValue match
      case Some(children: ListBuffer[Node]) =>
        if isNumberBlock(children) then appendNumberBlock(children)
        else appendChildrenBlock(node, indent, children)
      case Some(literal) => appendLiteral(literal)
      case None =>
        if node.identifier.nonEmpty && node.operator.nonEmpty then appendMissingValue()
        else () // explicitly do nothing, maybe log this? TODO
    // Append trailing trivia (e.g. comments that came after the node)
    appendTrivia(node.trailingTrivia, indent)

  /**
   * Append the identifier and operator (if any)
   * @param sb String builder to append to
   * @param node
   */
  protected def appendPrefix(using sb: StringBuilder)(node: Node, indent: String): Unit =
    node.identifier.foreach(id => sb.append(id).append(" "))
    node.operator.foreach(op => sb.append(op).append(" "))

  /**
   *
   * @param sb String builder to append to
   * @param children
   * @return
   */
  protected def appendNumberBlock(using sb: StringBuilder)(children: Iterable[Node]): Unit =
    sb.append("{ ")
    sb.append(children.map(_.asString).mkString(" "))
    sb.append(" }").append('\n')

  /**
   *
   * @param sb String builder to append to
   * @param node
   * @param indent
   * @param children
   * @return
   */
  protected def appendChildrenBlock(using sb: StringBuilder)
                                   (node: Node, indent: String, children: Iterable[Node]): Unit =
    val hasHeader = node.identifier.nonEmpty

    // For a block of child nodes, open a brace and newline
    appendChildScriptOpening(hasHeader)
    // append children
    val childIndent = if hasHeader && isAutoIndent then indent + "\t" else indent
    appendChildren(children, childIndent)
    appendChildScriptClosing(indent, hasHeader)

  /**
   *
   * @param sb String builder to append to
   * @param hasHeader
   * @return
   */
  protected def appendChildScriptOpening(using sb: StringBuilder)(hasHeader: Boolean): Unit =
    if hasHeader then sb.append("{\n")

  /**
   *
   * @param sb String builder to append to
   * @param indent
   * @param hasHeader
   * @return
   */
  protected def appendChildScriptClosing(using sb: StringBuilder)(indent: String, hasHeader: Boolean): Unit =
    if hasHeader then
      // Remove all trailing whitespace from the StringBuilder
      while sb.nonEmpty && sb.charAt(sb.length - 1).isWhitespace do
        sb.deleteCharAt(sb.length - 1)
      sb.append('\n').append(indent).append("}")
    else sb.append('\n')

  /**
   * @inheritdoc
   */
  protected def appendChildren(using sb: StringBuilder)(children: Iterable[Node], indent: String): Unit =
    for child <- children do
      sb.append(modifiedChildScript(toScriptAcc(child, indent)))

  protected def modifiedChildScript(childScript: String): String

  /**
   * Appends a literal node value
   * @param sb
   * @param literal
   */
  protected def appendLiteral(using sb: StringBuilder)(literal: NodeValueType): Unit =
    sb.append(literal.toString)

  /**
   * Appends trivia on its own line(s) (preserving comments/whitespace)
   *
   * @param sb String builder to append to
   * @param trivia
   * @param indent
   */
  protected def appendTrivia(using sb: StringBuilder)(trivia: Iterable[Token], indent: String): Unit =
    if (isAppendTrivia)
      for t <- trivia do sb.append(indent).append(t.value.replaceAll("\\t+", ""))

  protected def appendMissingValue(using sb: StringBuilder)(): Unit =
    sb.append(nullStr)

  protected def isNumberBlock(children: Iterable[Node]): Boolean =
    children.forall(n =>
      n.identifier.isEmpty &&
        n.operator.isEmpty &&
        n.rawValue.exists(_.isInstanceOf[Int | Double])
    )

}
