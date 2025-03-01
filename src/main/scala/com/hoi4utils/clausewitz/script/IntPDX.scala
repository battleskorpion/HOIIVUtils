package com.hoi4utils.clausewitz.script

import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz_parser.{Node, NodeValue}

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
      case null => None
      case _ => 
        LOGGER.warn(s"Expected integer value for pdx int")
        None
    }
  }

  /**
   * @inheritdoc
   */
  override def getOrElse(default: Int): Int = {
    val value = node.getOrElse(return default).value
    value match
      case Some(v) => v match {
        case i: Int => i
        case d: Double => d.toInt
        case _ => 
          LOGGER.warn(s"Expected integer value for pdx int, got $value")
          default
      }
      case None => default
  }

  override def isDefaultRange: Boolean = range == ExpectedRange.ofInt

  override def defaultRange: ExpectedRange[Int] = ExpectedRange.ofInt

  override def minValue: Int = range.min

  override def maxValue: Int = range.max

  override def minValueNonInfinite: Int = range.minNonInfinite

  override def maxValueNonInfinite: Int = range.maxNonInfinite

  override def defaultValue: Int = 0

  @targetName("unaryPlus")
  def unary_+ : Int = this.get() match {
    case Some(value) => +value
    case None => 0
  }

  @targetName("unaryMinus")
  def unary_- : Int = this.get() match {
    case Some(value) => -value
    case None => 0
  }

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
