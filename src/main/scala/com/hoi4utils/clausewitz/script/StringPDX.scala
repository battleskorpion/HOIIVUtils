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
    this.node = Some(expression)
    if !this.node.get.$.isInstanceOf[String] then
      throw new NodeValueTypeException(this.node.get)
  }
  
  override def set(s: String): Unit = {
    this.node match {
      case Some(node) => node.setValue(s)
    }
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case other: StringPDX =>
        if (this.node.isEmpty || other.node.isEmpty) {
          return false
        }
        node.get.equals(other.node.get)
      case _ => false
    }
  }

  def nodeEquals(s: String): Boolean = {
    if (this.node.isEmpty) false
    else node.get.$.equals(s)
  }
}
