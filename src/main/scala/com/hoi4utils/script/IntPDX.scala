package com.hoi4utils.script

import com.hoi4utils.shared.ExpectedRange
import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.util.boundary

class IntPDX(pdxIdentifiers: List[String], range: ExpectedRange[Int] = ExpectedRange.ofInt) extends AbstractPDX[Int](pdxIdentifiers) 
  with ValPDXScript[Int]:
  
  def this(pdxIdentifiers: String*) =
    this(pdxIdentifiers.toList)

  def this(pdxIdentifier: String) =
    this(List(pdxIdentifier))

  def this(pdxIdentifier: String, range: ExpectedRange[Int]) =
    this(List(pdxIdentifier), range)

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit =
    usingIdentifier(expression)
    this.node = Some(expression)
    if (!valueIsInstanceOf[Int | Integer]) then
      throw new NodeValueTypeException(expression, "Number (as an Integer)", this.getClass)

  def set(value: Int): Int =
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
    else
      this.node = Some(Node(value))
    value

  def set(other: IntPDX): Unit =
    other.value match
      case Some(value) => this @= value
      case None => this.node = None

  override def equals(other: PDXScript[?]): Boolean =
    other match
      case x: IntPDX => node.equals(x.node)
      case _ => false

  override def value: Option[Int] = boundary:
    node.getOrElse(boundary.break(None)).$ match
      case value: Int => Some(value)
      case value: Double => Some(value.toInt)
      case null => None
      case _ =>
        logger.warn(s"Expected integer value for pdx int")
        None

  /**
   * @inheritdoc
   */
  override infix def getOrElse(default: Int): Int = boundary {
    val value = node.getOrElse(boundary.break(default)).value
    value match
      case Some(v) => v match
        case i: Int => i
        case d: Double => d.toInt
        case _ =>
          logger.warn(s"Expected integer value for pdx int, got $value")
          default
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
  override def unary_+ : Int = this.value match
    case Some(v) => +v
    case None => 0

  @targetName("unaryMinus")
  override def unary_- : Int = this.value match
    case Some(v) => -v
    case None => 0

  @targetName("plus")
  override def +(other: Int): Int = this.value match
    case Some(v) => v + other
    case None => other

  @targetName("minus")
  override def -(other: Int): Int = this + (-other)

  @targetName("multiply")
  override def *(other: Int): Int = this.value match
    case Some(v) => v * other
    case None => 0

  @targetName("divide")
  override def /(other: Int): Int = this.value match
    case Some(v) => v / other
    case None => 0
  
  def asSomeString: Option[String] = Some(asString)
