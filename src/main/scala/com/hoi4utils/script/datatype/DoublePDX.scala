//package com.hoi4utils.script.datatype
//
//import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
//import com.hoi4utils.parser.{Node, PDXValueNode}
//import com.hoi4utils.script.{AbstractPDX, PDXScript}
//import com.hoi4utils.script.datatype.ValPDXScript
//import com.hoi4utils.shared.ExpectedRange
//
//import scala.annotation.targetName
//import scala.util.boundary
//
//class DoublePDX(pdxIdentifiers: List[String], range: ExpectedRange[Double] = ExpectedRange.ofDouble) extends AbstractPDX[Double, Double | Int](pdxIdentifiers) with ValPDXScript[Double, Double | Int] {
//  def this(pdxIdentifiers: String*) =
//    this(pdxIdentifiers.toList)
//
//  def this(pdxIdentifier: String) =
//    this(List(pdxIdentifier))
//
//  def this(pdxIdentifier: String, range: ExpectedRange[Double]) =
//    this(List(pdxIdentifier), range)
//
//  @throws[UnexpectedIdentifierException]
//  @throws[NodeValueTypeException]
//  override def set(expression: Node[Double | Int]): Unit =
//    usingIdentifier(expression)
//    expression.$ match
//      case d: Double => this.node = Some(new PDXValueNode[Double | Int](d))
//      case i: Int => this.node = Some(new PDXValueNode[Double | Int](i))
//
//
//  override def set(value: Double): Double =
//    node match
//      case Some(n) => n.setValue(value)
//      case None => this.node = Some(PDXValueNode[Double | Int](value))
//    value
//
//  override def equals(other: PDXScript[?, ?]): Boolean = other match
//    case x: DoublePDX =>
//      if (this.node.isEmpty || x.node.isEmpty) {
//        return false
//      }
//      node.get.$.equals(x.node.get.$)
//    case _ => false
//
//  override def value: Option[Double] = node.map(_.$) match
//    case d: Double => Some(d)
//    case i: Int => Some(i.toDouble)
//    case _ => None
//
//  /**
//   * @inheritdoc
//   */
//  override infix def getOrElse(default: Double): Double = value match
//    case Some(v) => v
//    case _ => default
//
//  override def isDefaultRange: Boolean = range == ExpectedRange.ofDouble
//
//  override def defaultRange: ExpectedRange[Double] = ExpectedRange.ofDouble
//
//  override def minValue: Double = range.min
//
//  override def maxValue: Double = range.max
//
//  override def minValueNonInfinite: Double = range.minNonInfinite
//
//  override def maxValueNonInfinite: Double = range.maxNonInfinite
//
//  override def defaultValue: Double = 0.0
//
//  @targetName("unaryPlus")
//  def unary_+ : Double = this.value match {
//    case Some(value) => +value
//    case None => 0.0
//  }
//
//  @targetName("unaryMinus")
//  def unary_- : Double = this.value match {
//    case Some(value) => -value
//    case None => -0.0
//  }
//
//  @targetName("plus")
//  def +(other: Double): Double = this.value match {
//    case Some(value) => value + other
//    case None => other
//  }
//
//  @targetName("minus")
//  def -(other: Double): Double = this + (-other)
//
//  @targetName("multiply")
//  def *(other: Double): Double = this.value match {
//    case Some(value) => value * other
//    case None => 0
//  }
//
//  @targetName("divide")
//  def /(other: Double): Double = this.value match {
//    case Some(value) => value / other
//    case None => 0
//  }
//}
