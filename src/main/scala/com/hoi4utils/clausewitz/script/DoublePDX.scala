package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue

import java.util
import java.util.List


class DoublePDX extends AbstractPDX[Double](pdxIdentifers) {
  def this(pdxIdentifiers: String) {
    this()
  }

  def this(PDXIdentifiers: String*) {
    this()
  }

  def this(pdxIdentifiers: util.List[String]) {
    this()
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.value
    if (value.valueObject.isInstanceOf[Number]) obj = num.doubleValue
    else throw new NodeValueTypeException(expression, "Number (as a Double)")
  }

  override def objEquals(other: PDXScript[_]): Boolean = {
    if (other.isInstanceOf[DoublePDX]) return obj.equals(pdx.get)
    false
  }
}
