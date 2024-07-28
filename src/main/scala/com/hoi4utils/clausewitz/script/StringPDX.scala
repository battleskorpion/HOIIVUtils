package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue


class StringPDX(pdxIdentifiers: List[String]) extends AbstractPDX[String](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = expression
    if !this.node.$.isInstanceOf[String] then
      throw new NodeValueTypeException(this.node)
  }

  override def nodeEquals(other: PDXScript[?]): Boolean = {
    other match
      case x: StringPDX => node.equals(x.node)
      case _ => false
  }

  def nodeEquals(s: String): Boolean = node.$.equals(s)
}
