package com.hoi4utils.clausewitz_parser

import scala.reflect.ClassTag

//object NodeIterable {
//  def of[T <: Node](stream: Stream[T]) = new NodeStream[T](stream)
//}

// todo improve massively (use fromSpecific override instead of fromIterable workaround?)
trait NodeIterable[NodeType <: Node] extends Iterable[NodeType] {
  //def flatMap[R <: Node](mapper: Function[? >: NodeType, ? <: NodeStreamable[R]]): NodeStreamable[R]
  override def flatMap[B](f: NodeType => IterableOnce[B]): Iterable[B] = super.flatMap(f)

//  def getStream: Stream[NodeType]

  def fromIterable(iter: Iterable[NodeType]): NodeIterable[NodeType] =
    new NodeIterable[NodeType] {
      override def iterator: Iterator[NodeType] = iter.iterator
    }

  def find(str: String): Option[NodeType] = {
    find((node: NodeType) => node.identifier != null && node.identifier == str)
  }

  // Now filterName returns a NodeIterable instead of Iterable.
  def filterName(str: String): NodeIterable[NodeType] = {
    fromIterable(filter(node => node.identifier != null && node.identifier == str))
  }

  def filter(str: String): NodeIterable[NodeType] = {
    filterName(str)
  }

  // idk stuff was misbehaving had to do it this way
  def subFilter(str: String)(implicit ct: ClassTag[NodeType]): NodeIterable[NodeType] =
    fromIterable(
      this.flatMap { node =>
        node.filter(str).toSeq.collect {
          case found if ct.runtimeClass.isInstance(found) => found.asInstanceOf[NodeType]
        }
//        node.find(str).toSeq
      }
    )

  def contains(str: String): Boolean = filter(str).toList.nonEmpty

  def containsAll(strings: String*): Boolean = strings.forall(contains)

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
