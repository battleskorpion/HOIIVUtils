package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull

import java.util
import java.util.List


class BooleanPDX extends AbstractPDX[Boolean](pdxIdentifiers) {
  final private var boolType: BoolType = undefined
  /**
   * this is separate to obj because
   * if we want to clear the value of obj, we would still return this value by default.
   */
  final private var defaultValue = false

  def this(pdxIdentifiers: String, defaultValue: Boolean, boolType: BoolType) = {
    this()
    this.boolType = boolType
    this.defaultValue = defaultValue
  }

  def this(pdxIdentifiers: util.List[String], defaultValue: Boolean, boolType: BoolType) = {
    this()
    this.boolType = boolType
    this.defaultValue = defaultValue
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.value
    if (value.valueObject.isInstanceOf[String]) obj = value.bool(boolType)
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
