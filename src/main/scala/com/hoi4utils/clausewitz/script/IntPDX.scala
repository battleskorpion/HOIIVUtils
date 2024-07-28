package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue

class IntPDX(pdxIdentifiers: List[String]) extends AbstractPDX[Int](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = expression
    if (!valueIsInstanceOf[Int]) {
      throw new NodeValueTypeException(expression, "Number (as an Integer)")
    }
  }

  override def nodeEquals(other: PDXScript[?]): Boolean = {
    other match {
      case x: IntPDX => node.equals(x.node)
      case _ => false
    }
  }
}
