package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, PDXValueNode}
import com.hoi4utils.script.datatype.ValPDXScript
import com.hoi4utils.shared.ExpectedRange

import scala.annotation.targetName
import scala.util.boundary

class IntPDX(pdxIdentifiers: List[String], range: ExpectedRange[Int] = ExpectedRange.ofInt) extends AbstractPDX[Int, Int](pdxIdentifiers)
  with ValPDXScript[Int]:

  def this(pdxIdentifiers: String*) =
    this(pdxIdentifiers.toList)

  def this(pdxIdentifier: String) =
    this(List(pdxIdentifier))

  def this(pdxIdentifier: String, range: ExpectedRange[Int]) =
    this(List(pdxIdentifier), range)

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: PDXValueNode[Int]): Unit =
    if pdxIdentifiers.nonEmpty then usingIdentifier(expression)
    this.node = Some(expression)

  def set(value: Int): Int =
    if (this.node.nonEmpty)
      this.node.get.setValue(value)
    else
      this.node = Some(PDXValueNode(value))
    value

  def set(other: IntPDX): Unit =
    other.value match
      case Some(value) => this @= value
      case None => this.node = None

  override def equals(other: PDXScript[?, ?]): Boolean =
    other match
      case x: IntPDX => node.equals(x.node)
      case _ => false

  override def value: Option[Int] = node.map(_.$) 

  /**
   * @inheritdoc
   */
  override infix def getOrElse(default: Int): Int = node match
    case Some(n) =>
      n.$.toInt
    case _ => default

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

  def asOptionalString: Option[String] = value match
    case Some(v) => Some(v.toString)
    case None => None
