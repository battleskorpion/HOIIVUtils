package com.hoi4utils.clausewitz_parser

import scala.reflect.ClassTag

//object NodeIterable {
//  def of[T <: Node](stream: Stream[T]) = new NodeStream[T](stream)
//}

trait NodeIterable[NodeType <: Node] extends Iterable[NodeType] {
  //def flatMap[R <: Node](mapper: Function[? >: NodeType, ? <: NodeStreamable[R]]): NodeStreamable[R]
  override def flatMap[B](f: NodeType => IterableOnce[B]): Iterable[B] = super.flatMap(f)

//  def getStream: Stream[NodeType]

  def find(str: String): Option[NodeType] = {
    find((node: NodeType) => node.identifier != null && node.identifier == str)
  }

  def filterName(str: String): Iterable[NodeType] = {
    filter((node: NodeType) => node.identifier != null && node.identifier == str)
  }

  def filter(str: String): Iterable[NodeType] = {
    filterName(str)
  }

  def contains(str: String): Boolean = filter(str).toList.nonEmpty

  def anyMatch(predicate: NodeType => Boolean): Boolean = {
    filter(predicate).toList.nonEmpty
  }

  /**
   * Retrieve the node’s value as type `T` if found and matching the type,
   * otherwise return `default`.
   */
  def valueOrElse[T: ClassTag](key: String, default: T): T = {
    find(key).flatMap { node =>
      node.$ match {
        case value: T => Some(value)
        case _        => None
      }
    }.getOrElse(default)
  }
  
}
