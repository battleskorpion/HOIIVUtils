package com.hoi4utils.clausewitz_parser

import java.util
import java.util.List
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream


object NodeStreamable {
  def of[T <: Node](stream: Stream[T]) = new NodeStream[T](stream)
}

trait NodeStreamable[NodeType <: Node] {
  def filter(predicate: Predicate[_ >: NodeType]): NodeStreamable[NodeType]

  def map[R <: Node](mapper: Function[_ >: NodeType, _ <: R]): NodeStreamable[R]

  def flatMap[R <: Node](mapper: Function[_ >: NodeType, _ <: NodeStreamable[R]]): NodeStreamable[R]

  def getStream: Stream[NodeType]

  def toList: util.List[NodeType]

  def forEach(action: Consumer[_ >: NodeType]): Unit

  def findFirst: NodeType

  def findFirst(predicate: Predicate[NodeType]): NodeType

  def findFirst(str: String): Node

  // todo filter name should filter.. a name.
  def filterName(str: String): NodeStreamable[NodeType]

  def filter(str: String): NodeStreamable[NodeType]

  def contains(str: String): Boolean = !filter(str).toList.isEmpty

  def anyMatch(predicate: Predicate[_ >: NodeType]): Boolean
}
