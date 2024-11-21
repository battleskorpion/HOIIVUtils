package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import com.hoi4utils.ExpectedRange

import scala.annotation.targetName

class IntPDX(pdxIdentifiers: List[String], range: ExpectedRange[Int] = ExpectedRange.ofInt) extends AbstractPDX[Int](pdxIdentifiers) with ValPDXScript[Int] {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  def this(pdxIdentifier: String) = {
    this(List(pdxIdentifier))
  }

  def this(pdxIdentifier: String, range: ExpectedRange[Int]) = {
    this(List(pdxIdentifier), range)
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

  def set(value: Int): Int = {
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
    else
      this.node = Some(Node(NodeValue(value)))
    value
  }

  def set(other: IntPDX): Unit = {
    other.get() match {
      case Some(value) => this @= value
      case None => this.node = None
    }
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

  override def isDefaultRange: Boolean = range == ExpectedRange.ofInt

  override def defaultRange: ExpectedRange[Int] = ExpectedRange.ofInt

  override def minValue: Int = range.min

  override def maxValue: Int = range.max

  override def minValueNonInfinite: Int = range.minNonInfinite

  override def maxValueNonInfinite: Int = range.maxNonInfinite

  override def defaultValue: Int = 0

  @targetName("plus")
  def +(other: Int): Int = this.get() match {
    case Some(value) => value + other
    case None => other
  }

  @targetName("minus")
  def -(other: Int): Int = this + (-other)

  @targetName("multiply")
  def *(other: Int): Int = this.get() match {
    case Some(value) => value * other
    case None => 0
  }

  @targetName("divide")
  def /(other: Int): Int = this.get() match {
    case Some(value) => value / other
    case None => 0
  }
}
