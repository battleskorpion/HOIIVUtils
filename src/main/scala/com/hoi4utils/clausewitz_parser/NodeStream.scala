//package com.hoi4utils.clausewitz_parser
//
//import java.util
//import java.util.{ArrayList, List}
//import java.util.function.Consumer
//import java.util.function.Function
//import java.util.function.Predicate
//import java.util.stream.Collectors
//import java.util.stream.Stream
//
//import scala.collection.mutable.ListBuffer
//
//class NodeStream[T <: Node](var stream: Stream[T]) extends NodeStreamable[T] {
//  /**
//   * Creates a new NodeStream from a single node.
//   *
//   * If the node's value is an ArrayList, it will be flattened into the
//   * stream. If the node's value is a Node, it will be added to the stream.
//   *
//   * @param nodeToStream the node to stream
//   */
//  def this(nodeToStream: T) = {
//    this(Stream.of(nodeToStream))
//    // todo necessary etc.?
//    nodeToStream.$ match
//      case _: ListBuffer[?] => stream = stream.flatMap((node: T) => node.$.asInstanceOf[java.util.ArrayList[T]].stream)
//      case _: Node =>
//        stream = Stream.concat(stream, Stream.of(nodeToStream.$.asInstanceOf[T])) 
//      case _ =>
//  }
//
//  private def concat(stream1: Stream[T], stream2: Stream[T]) = Stream.concat(stream1, stream2)
//
//  private def concat(stream1: NodeStream[T], stream2: Stream[T]) = Stream.concat(stream1.getStream, stream2)
//
//  override def filter(predicate: Predicate[? >: T]): NodeStreamable[T] = {
//    stream = stream.filter(predicate)
//    this
//  }
//  
////  override def getStream: Stream[T] = stream
//
//  override def forEach(action: Consumer[? >: T]): Unit = {
//    stream.forEach(action)
//  }
//
//  override def findFirst: T = stream.findFirst.orElse(null.asInstanceOf[T])
//
//  override def findFirst(predicate: Predicate[T]): T = stream.filter(predicate) // Apply the predicate.findFirst.orElse(null)
//
//  override def findFirst(str: String): T = findFirst((node: T) => node.identifier != null && node.identifier == str)
//
//  override def filterName(str: String): NodeStreamable[T] = filter((node: T) => node.identifier != null && node.identifier == str)
//
//  override def filter(str: String): NodeStreamable[T] = filterName(str)
//}
