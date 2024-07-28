package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue


class DoublePDX(pdxIdentifiers: List[String]) extends AbstractPDX[Double](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
//    val value = expression.value
//    if (value.valueObject.isInstanceOf[Number]) obj = num.doubleValue
    this.node = expression
    node.$ match {
      case _: Double =>
      case _ => throw new NodeValueTypeException(expression, "Number (as a Double)")
    }
  }

  override def nodeEquals(other: PDXScript[?]): Boolean = {
    if (other.isInstanceOf[DoublePDX]) return node.$.equals(other.get())
    false
  }
}
