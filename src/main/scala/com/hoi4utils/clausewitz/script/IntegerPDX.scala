package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue

import java.util
import java.util.List


class IntegerPDX extends AbstractPDX[Integer](pdxIdentifiers) {
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
    if (!valueIsInstanceOf(Int)) {
      throw new NodeValueTypeException(expression, "Number (as an Integer)")
    }
  }

  override def nodeEquals(other: PDXScript[_]): Boolean = {
    if (other.isInstanceOf[IntegerPDX]) return node.equals(other.getNode)
    false
  }
}
