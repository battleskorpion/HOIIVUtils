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
    this.node = Some(expression)
    expression.$ match {
      case _: Double =>
      case _ => throw new NodeValueTypeException(expression, "Number (as a Double)")
    }
  }
  
  override def set(value: Double): Unit = {
    if (this.node.nonEmpty) {
      this.node.get.setValue(value)
    }
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case x: DoublePDX =>
        if (this.node.isEmpty || x.node.isEmpty) {
          return false
        }
        node.get.$.equals(x.node.get.$)
      case _ => false
    }
  }
}
