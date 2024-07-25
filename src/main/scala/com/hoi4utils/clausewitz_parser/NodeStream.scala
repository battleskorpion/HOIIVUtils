package com.hoi4utils.clausewitz_parser

import java.util
import java.util.{ArrayList, List}
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream


class NodeStream[T <: Node](var stream: Stream[T]) extends NodeStreamable[T] {
  /**
   * Creates a new NodeStream from a single node.
   *
   * If the node's value is an ArrayList, it will be flattened into the
   * stream. If the node's value is a Node, it will be added to the stream.
   *
   * @param nodeToStream the node to stream
   */
  def this(nodeToStream: T) {
    this(Stream.of(nodeToStream))
    // todo necessary etc.?
    if (nodeToStream.valueObject.isInstanceOf[util.ArrayList[_]]) stream = stream.flatMap((node: T) => node.valueObject.asInstanceOf[util.ArrayList[T]].stream) // ! Unchecked cast
    else if (nodeToStream.valueObject.isInstanceOf[Node]) {
      // stream = concat(stream, ((NodeType) nodeToStream.value()).stream());
      stream = Stream.concat(stream, Stream.of(nodeToStream.valueObject.asInstanceOf[T])) // ! Unchecked cast

    }
  }

  private def concat(stream1: Stream[T], stream2: Stream[T]) = Stream.concat(stream1, stream2)

  private def concat(stream1: NodeStream[T], stream2: Stream[T]) = Stream.concat(stream1.getStream, stream2)

  override def filter(predicate: Predicate[_ >: T]): NodeStreamable[T] = {
    stream = stream.filter(predicate)
    this
  }

  override def map[R <: Node](mapper: Function[_ >: T, _ <: R]) = new NodeStream[R](stream.map(mapper))

  override def flatMap[R <: Node](mapper: Function[_ >: T, _ <: NodeStreamable[R]]) = new NodeStream[R](stream.flatMap((item: T) => mapper.apply(item).getStream))

  override def getStream: Stream[T] = stream

  override def toList: util.List[T] = stream.collect(Collectors.toList)

  override def forEach(action: Consumer[_ >: T]): Unit = {
    stream.forEach(action)
  }

  override def findFirst: T = stream.findFirst.orElse(null)

  override def findFirst(predicate: Predicate[T]): T = stream.filter(predicate) // Apply the predicate.findFirst.orElse(null)

  override def findFirst(str: String): T = findFirst((node: T) => node.identifier != null && node.identifier == str)

  override def filterName(str: String): NodeStreamable[T] = filter((node: T) => node.identifier != null && node.identifier == str)

  override def filter(str: String): NodeStreamable[T] = filterName(str)

  override def anyMatch(predicate: Predicate[_ >: T]): Boolean = stream.anyMatch(predicate)
}
