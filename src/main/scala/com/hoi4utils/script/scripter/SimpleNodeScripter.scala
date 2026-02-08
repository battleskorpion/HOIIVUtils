package com.hoi4utils.script.scripter

import com.hoi4utils.parser.{Node, NodeValueType, PDXValueType, Token}
import com.hoi4utils.parser.TokenType.comment

import scala.collection.mutable.ListBuffer

/**
 * Generates a canonical "pretty print" version.
 * Produces a normalized output rather than preserving every original space.
 */
object SimpleNodeScripter extends NodeScripter {

  /**
   * @inheritdoc
   */
  override protected def isAppendTrivia: Boolean = false

  /**
   * @inheritdoc
   */
  override protected def isAutoIndent: Boolean = false

  /**
   * @inheritdoc
   */
  override protected def appendLiteral(using sb: StringBuilder)(literal: PDXValueType): Unit =
    sb.append(literal).append('\n')

  /**
   * @inheritdoc
   */
  override protected def appendMissingValue(using sb: StringBuilder)(): Unit = {
    super.appendMissingValue()
    sb.append('\n')
  }

  /**
   * @inheritdoc
   */
  override protected def appendChildScriptOpening(using sb: StringBuilder)(hasHeader: Boolean): Unit =
    if hasHeader then sb.append("{\n\t")

  /**
   * @inheritdoc
   */
  override protected def appendChildScriptClosing(using sb: StringBuilder)(indent: String, hasHeader: Boolean): Unit =
    if hasHeader then sb.append(indent).append("}").append('\n')

  /**
   * @inheritdoc
   */
  override protected def appendChildren(using sb: StringBuilder)(children: Iterable[Node[?]], indent: String): Unit =
    super.appendChildren(children, indent)
    if sb.nonEmpty then sb.deleteCharAt(sb.length - 1)

  /**
   * @inheritdoc
   */
  override protected def modifiedChildScript(childScript: String): String =
    childScript.replace("\n", "\n\t")
}
