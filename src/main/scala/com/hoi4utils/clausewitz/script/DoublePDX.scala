package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import com.hoi4utils.ExpectedRange

class DoublePDX(pdxIdentifiers: List[String], range: ExpectedRange[Double] = ExpectedRange.ofDouble) extends AbstractPDX[Double](pdxIdentifiers) {
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
//    val value = expression.value
//    if (value.valueObject.isInstanceOf[Number]) obj = num.doubleValue
    this.node = Some(expression)
    expression.$ match {
      case _: Double =>
      case _ => throw new NodeValueTypeException(expression, "Number (as a Double)")
    }
  }
  
  override def set(value: Double): Unit = {
    if (this.node.nonEmpty) {
      this.node.get.setValue(value)
    }
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
  }

  def defaultRange: Boolean = range == ExpectedRange.ofDouble

  def minValue: Double = range.min

  def maxValue: Double = range.max

  def minValueNonInfinite: Double = range.minNonInfinite

  def maxValueNonInfinite: Double = range.maxNonInfinite

}
