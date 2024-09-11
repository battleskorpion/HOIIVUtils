package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node

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

  override def simplePDXSupplier(expression: Node): Option[Node => Option[T]] = None

  override def blockPDXSupplier(expression: Node): Option[Node => Option[T]] = None
}
