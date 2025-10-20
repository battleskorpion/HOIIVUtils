package com.hoi4utils.script

import com.hoi4utils.shared.ExpectedRange
import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.util.boundary

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
      case _: Int =>
      case _ =>throw new NodeValueTypeException(expression, "Number (as a Double)", this.getClass)
    }
  }
  
  override def set(value: Double): Double = {
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
    else
      this.node = Some(Node(value))
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

  override def value: Option[Double] = boundary {
    node.getOrElse(boundary.break(None)).$ match {
      case value: Double => Some(value)
      case value: Int => Some(value.toDouble)
      case null => None
      case _ =>
        logger.warn(s"Expected double value for pdx double")
        None
    }
  }

  /**
   * @inheritdoc
   */
  override def getOrElse(default: Double): Double = boundary {
    val value = node.getOrElse(boundary.break(default)).value
    value match {
      case Some(v) => v.match {
        case d: Double => d
        case i: Int => i.toDouble
        case _ =>
          logger.warn(s"Expected double value for pdx double, got ${value}")
          default
      }
      case None => default
    }
  }

  override def isDefaultRange: Boolean = range == ExpectedRange.ofDouble

  override def defaultRange: ExpectedRange[Double] = ExpectedRange.ofDouble

  override def minValue: Double = range.min

  override def maxValue: Double = range.max

  override def minValueNonInfinite: Double = range.minNonInfinite

  override def maxValueNonInfinite: Double = range.maxNonInfinite

  override def defaultValue: Double = 0.0

  @targetName("unaryPlus")
  def unary_+ : Double = this.value match {
    case Some(value) => +value
    case None => 0.0
  }

  @targetName("unaryMinus")
  def unary_- : Double = this.value match {
    case Some(value) => -value
    case None => -0.0
  }

  @targetName("plus")
  def +(other: Double): Double = this.value match {
    case Some(value) => value + other
    case None => other
  }

  @targetName("minus")
  def -(other: Double): Double = this + (-other)

  @targetName("multiply")
  def *(other: Double): Double = this.value match {
    case Some(value) => value * other
    case None => 0
  }

  @targetName("divide")
  def /(other: Double): Double = this.value match {
    case Some(value) => value / other
    case None => 0
  }
}
