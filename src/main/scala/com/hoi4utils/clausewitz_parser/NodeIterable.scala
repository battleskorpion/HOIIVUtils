package com.hoi4utils.clausewitz_parser

import java.util
import java.util.List
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream


object NodeIterable {
  def of[T <: Node](stream: Stream[T]) = new NodeStream[T](stream)
}

trait NodeIterable[NodeType <: Node] extends Iterable[NodeType] {
  //def flatMap[R <: Node](mapper: Function[? >: NodeType, ? <: NodeStreamable[R]]): NodeStreamable[R]
  override def flatMap[B](f: NodeType => IterableOnce[B]): Iterable[B] = super.flatMap(f)

//  def getStream: Stream[NodeType]

  def find(str: String): Node = {
    findFirst((node: NodeType) => node.identifier != null && node.identifier == str)
  }

  // todo filter name should filter.. a name.
  def filterName(str: String): NodeIterable[NodeType] = {
    filter((node: NodeType) => node.identifier != null && node.identifier == str)
  }

  def filter(str: String): NodeIterable[NodeType] = {
    filterName(str)
  }

  def contains(str: String): Boolean = filter(str).toList.nonEmpty

  def anyMatch(predicate: NodeType => Boolean): Boolean = {
    filter(predicate).toList.nonEmpty
  }
}
