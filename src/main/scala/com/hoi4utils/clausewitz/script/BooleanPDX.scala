package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull

class BooleanPDX(pxdIdentifiers: List[String], final private var defaultValue: Boolean, final private var boolType: BoolType) extends AbstractPDX[Boolean](pdxIdentifiers) {

  def this(pdxIdentifier: String, defaultValue: Boolean, boolType: BoolType) = {
    this(pdxIdentifier, defaultValue, boolType)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    if (value.isInstanceOf[String]) obj = value.bool(boolType)
    else throw new NodeValueTypeException(expression, "String parsable as Bool matching enum + " + boolType.toString)
  }

  override def get(): Boolean = {
    val `val` = super.get()
    if (`val` == null) return defaultValue
    `val`
  }

  def objEquals(other: PDXScript[_]): Boolean = {
    if (other.isInstanceOf[BooleanPDX]) return obj.equals(pdx.get)
    false
  }

  def invert: Boolean = {
    set(!get)
    get()
  }
}
