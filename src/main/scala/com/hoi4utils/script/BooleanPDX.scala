package com.hoi4utils.script

import com.hoi4utils.BoolType
import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

class BooleanPDX(pdxIdentifiers: List[String], final private var defaultValue: Boolean, final private var boolType: BoolType) extends AbstractPDX[Boolean](pdxIdentifiers) {

  def this(pdxIdentifier: String, defaultValue: Boolean, boolType: BoolType) = {
    this(List(pdxIdentifier), defaultValue, boolType)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit =
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match
      case _: Boolean => // No action needed, value is already Boolean
      case _: String =>
        val v = this.node.get.$.asInstanceOf[String]
        if boolType.matches(v) then set(boolType.parse(v))
        else throw NodeValueTypeException(expression, s"String parsable as Bool matching enum + ${boolType.toString}", s"${expression.$}")
      case _ =>
        throw NodeValueTypeException(expression, "Boolean or String", s"${expression.$}")
  
  override def set(value: Boolean): Boolean = {
    if (this.node.nonEmpty)
      this.node.foreach(_.setValue(value))
    else
      this.node = Some(Node(value))
    value
  }

  override def value: Option[Boolean] = {
    val v = super.value
    v.orNull match {
      case b: Boolean => Some(b)
      case null => None
    }
  }

  def $ : Boolean = {
    value.getOrElse(defaultValue)
  }

  def objEquals(other: PDXScript[?]): Boolean = {
    other match {
      case b: BooleanPDX => node.equals(b.node)
      case _ => false
    }
  }

  def invert: Boolean = {
    setNode(!this.$)
    this.$
  }

  override def toString: String = this.value.map(_.toString).getOrElse("")
}
