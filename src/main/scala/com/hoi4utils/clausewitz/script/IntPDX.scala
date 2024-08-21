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
    this.node = Some(expression)
    if (!valueIsInstanceOf[Int]) {
      throw new NodeValueTypeException(expression, "Number (as an Integer)")
    }
  }

  override def set(value: Int): Unit = {
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case x: IntPDX => node.equals(x.node)
      case _ => false
    }
  }

  override def get(): Option[Int] = {
    node.getOrElse(return None).$ match {
      case value: Int => Some(value)
      case value: Double => Some(value.toInt)
      case _ => None
    }
  }

  override def getOrElse(default: Int): Int = {
    val value = node.getOrElse(return default).getValue
    value match
      case i: Int => i
      case d: Double => d.toInt
  }
}
