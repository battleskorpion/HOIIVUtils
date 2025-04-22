package com.hoi4utils.script

import com.hoi4utils.parser.Node

import scala.collection.mutable.ListBuffer

trait PDXSupplier[T <: PDXScript[?]] {
  def apply(expression: Node): Option[T] = {
    (simplePDXSupplier(), blockPDXSupplier()) match {
      case (Some(s), None) => s.apply(expression)
      case (None, Some(b)) => b.apply(expression)
      case (Some(s), Some(b)) =>
        expression.$ match {
          case l: ListBuffer[Node] =>
            b.apply(expression)
          case _ => s.apply(expression)
        }
      case (None, None) => throw new RuntimeException("Both suppliers are null")
    }
  }

  def simplePDXSupplier(): Option[Node => Option[T]] = None

  def blockPDXSupplier(): Option[Node => Option[T]] = None
}
