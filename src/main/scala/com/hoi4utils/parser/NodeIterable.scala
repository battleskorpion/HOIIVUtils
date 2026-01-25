package com.hoi4utils.parser

import scala.reflect.ClassTag

//object NodeIterable {
//  def of[T <: Node](stream: Stream[T]) = new NodeStream[T](stream)
//}

// todo improve massively (use fromSpecific override instead of fromIterable workaround?)
trait NodeIterable[NodeType <: Node] extends Iterable[NodeType]:
  //def flatMap[R <: Node](mapper: Function[? >: NodeType, ? <: NodeStreamable[R]]): NodeStreamable[R]
  override def flatMap[B](f: NodeType => IterableOnce[B]): Iterable[B] = super.flatMap(f)

//  def getStream: Stream[NodeType]

  def fromIterable(iter: Iterable[NodeType]): NodeIterable[NodeType] =
    new NodeIterable[NodeType]:
      override def iterator: Iterator[NodeType] = iter.iterator

  def find(str: String): Option[NodeType] =
    find((node: NodeType) =>
      node.$ match
        case l: List[Node] =>
          l.exists(_.name.equals(str))
        case _ =>
          node.identifier.isDefined && node.name.equals(str)
    )

  def findCaseInsensitive(str: String): Option[NodeType] =
    find((node: NodeType) =>
      node.$ match
        case l: List[Node] =>
          l.exists(_.name.equalsIgnoreCase(str))
        case _ =>
          node.identifier.isDefined && node.name.equalsIgnoreCase(str)
    )

  // Now filterName returns a NodeIterable instead of Iterable.
  def filterName(str: String): NodeIterable[NodeType] =
    fromIterable(filter(node => node.identifier.isDefined && node.name.equals(str)))

  def filterNameCaseInsensitive(str: String): NodeIterable[NodeType] =
    fromIterable(filter(node => node.identifier.isDefined && node.name.equalsIgnoreCase(str)))

  def filter(str: String): NodeIterable[NodeType] = filterName(str)

  def filterCaseInsensitive(str: String): NodeIterable[NodeType] = filterNameCaseInsensitive(str)

  override def filter(pred: NodeType => Boolean): NodeIterable[NodeType] =
    fromIterable(super.filter(pred))

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

  def subFilterCaseInsensitive(str: String)(implicit ct: ClassTag[NodeType]): NodeIterable[NodeType] =
    fromIterable(
      this.flatMap { node =>
        node.filterCaseInsensitive(str).toSeq.collect {
          case found if ct.runtimeClass.isInstance(found) => found.asInstanceOf[NodeType]
        }
        //        node.find(str).toSeq
      }
    )

  // subFilter that takes a predicate.
  // Here we widen the predicate to a function Node => Boolean.
  // this is voodoo
  def subFilter(pred: NodeType => Boolean)(implicit ct: ClassTag[NodeType]): NodeIterable[NodeType] =
    // Widen the predicate to Node => Boolean:
    val widened: Node => Boolean = (n: Node) =>
      if (ct.runtimeClass.isInstance(n))
        pred(n.asInstanceOf[NodeType])
      else
        false

    fromIterable(
      this.flatMap { node =>
        // Call the filter method that accepts a Node => Boolean.
        // Explicitly annotate the lambda parameter type so the compiler picks the right overload.
        node.filter((n: Node) => widened(n)).toSeq.collect { case n: NodeType => n }
      }
    )

  def filterHead(str: String): NodeType = filterName(str).head
  
  def filterHeadOption(str: String): Option[NodeType] = filterName(str).headOption

  def contains(str: String): Boolean = filter(str).toList.nonEmpty

  def valueContains(str: String): Boolean =
    filter((node: NodeType) =>
      node.$ match
        case l: List[Node] =>
          l.exists(_.valueAsString.equals(str))
        case _ =>
          node.identifier.isDefined && node.valueAsString.equals(str)
    ).toList.nonEmpty

  def scriptContains(str: String): Boolean = contains(str) || valueContains(str)

  def containsCaseInsensitive(str: String): Boolean = filterCaseInsensitive(str).toList.nonEmpty

  def containsAll(strings: String*): Boolean = strings.forall(contains)

  def containsAllCaseInsensitive(strings: String*): Boolean = strings.forall(containsCaseInsensitive)

  def anyMatch(predicate: NodeType => Boolean): Boolean =
    filter(predicate).toList.nonEmpty

  /**
   * Retrieve the node’s value as type `T` if found and matching the type,
   * otherwise return `default`.
   */
  def valueOrElse[T: ClassTag](key: String, default: T): T =
    find(key).flatMap { node =>
      node.$ match
        case value: T => Some(value)
        case _        => None
    }.getOrElse(default)
