package com.hoi4utils.script

import com.hoi4utils.parser.{Node, PDXValueNode, SeqNode}

import scala.collection.mutable.ListBuffer

trait PDXSupplier[T <: PDXScript[?, ?]] {
  // TODO lol 
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

  def simplePDXSupplier(): Option[PDXValueNode[?] => Option[T]] = None

  def blockPDXSupplier(): Option[SeqNode => Option[T]] = None
}
