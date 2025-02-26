package com.hoi4utils.clausewitz.script

import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz_parser.{Node, NodeValue}

import scala.annotation.targetName

class DoublePDX(pdxIdentifiers: List[String], range: ExpectedRange[Double] = ExpectedRange.ofDouble) extends AbstractPDX[Double](pdxIdentifiers) with ValPDXScript[Double] {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  def this(pdxIdentifier: String) = {
    this(List(pdxIdentifier))
  }

  def this(pdxIdentifier: String, range: ExpectedRange[Double]) = {
    this(List(pdxIdentifier), range)
  }
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match {
      case _: Double =>
      case _ => throw new NodeValueTypeException(expression, "Number (as a Double)")
    }
  }
  
  override def set(value: Double): Double = {
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
    else
      this.node = Some(Node(NodeValue(value)))
    value
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

  override def get(): Option[Double] = {
    node.getOrElse(return None).$ match {
      case value: Double => Some(value)
      case value: Int => Some(value.toDouble)
      case _ => None
    }
  }

  override def getOrElse(default: Double): Double = {
    val value = node.getOrElse(return default).getValue
    value match
      case d: Double => d
      case i: Int => i.toDouble
      case _: String => ???
      case true => ???
      case false => ???
      case _: scala.collection.mutable.ListBuffer[com.hoi4utils.clausewitz_parser.Node] => ???
  }

  override def isDefaultRange: Boolean = range == ExpectedRange.ofDouble

  override def defaultRange: ExpectedRange[Double] = ExpectedRange.ofDouble

  override def minValue: Double = range.min

  override def maxValue: Double = range.max

  override def minValueNonInfinite: Double = range.minNonInfinite

  override def maxValueNonInfinite: Double = range.maxNonInfinite

  override def defaultValue: Double = 0.0

  @targetName("unaryPlus")
  def unary_+ : Double = this.get() match {
    case Some(value) => +value
    case None => 0.0
  }

  @targetName("unaryMinus")
  def unary_- : Double = this.get() match {
    case Some(value) => -value
    case None => -0.0
  }

  @targetName("plus")
  def +(other: Double): Double = this.get() match {
    case Some(value) => value + other
    case None => other
  }

  @targetName("minus")
  def -(other: Double): Double = this + (-other)

  @targetName("multiply")
  def *(other: Double): Double = this.get() match {
    case Some(value) => value * other
    case None => 0
  }

  @targetName("divide")
  def /(other: Double): Double = this.get() match {
    case Some(value) => value / other
    case None => 0
  }

}
