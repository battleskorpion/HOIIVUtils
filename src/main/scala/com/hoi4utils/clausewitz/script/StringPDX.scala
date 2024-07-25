package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue

import java.util
import java.util.List


class StringPDX extends AbstractPDX[String](pdxIdentifiers) {
  def this(pdxIdentifiers: String) = {
    this()
  }

  def this(PDXIdentifiers: String*) = {
    this()
  }

  def this(pdxIdentifiers: util.List[String]) = {
    this()
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = expression
    if (!this.node.$.isInstanceOf[String]) {
      throw new NodeValueTypeException(this.node, classOf[String])
    }
  }

  override def nodeEquals(other: PDXScript[_]): Boolean = {
    other match
      case x: StringPDX => return node.equals(x.node)
      case _ =>
    false
  }

  def objEquals(s: String): Boolean = obj.equals(s)
}
