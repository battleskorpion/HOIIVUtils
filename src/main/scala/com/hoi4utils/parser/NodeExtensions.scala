package com.hoi4utils.parser

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType

import scala.reflect.ClassTag

trait NodeExtensions {
  extension (seqNode: SeqNode)
    def find(str: String): Option[Node[?]] =
      find(_.name.equals(str))

    def find(p: Node[?] => Boolean): Option[Node[?]] =
      seqNode.iterator.find(p)

    def findCaseInsensitive(str: String): Option[Node[?]] =
      find(_.name.equalsIgnoreCase(str))

    def filter(str: String): Iterable[Node[?]] = filterName(str)

    def filter(p: Node[?] => Boolean): Iterable[Node[?]] =
      seqNode.iterator.filter(p).toSeq

    def filterCaseInsensitive(str: String): Iterable[Node[?]] = filterNameCaseInsensitive(str)

    def filterName(str: String): Iterable[Node[?]] =
      filter(node => node.name.equals(str))

    def filterNameCaseInsensitive(str: String): Iterable[Node[?]] =
      filter(node => node.name.equalsIgnoreCase(str))

    //    // idk stuff was misbehaving had to do it this way
    //    def subFilter(str: String)(implicit ct: ClassTag[NodeType]): Iterable[NodeType] =
    //      iter.flatMap { node =>
    //        node.filter(str).toSeq.collect {
    //          case found if ct.runtimeClass.isInstance(found) => found.asInstanceOf[NodeType]
    //        }
    //        //        node.find(str).toSeq
    //      }

    //    def subFilterCaseInsensitive(str: String)(implicit ct: ClassTag[NodeType]): Iterable[NodeType] =
    //      iter.flatMap { node =>
    //        node.filterCaseInsensitive(str).toSeq.collect {
    //          case found if ct.runtimeClass.isInstance(found) => found.asInstanceOf[NodeType]
    //        }
    //        //        node.find(str).toSeq
    //      }

    //    // subFilter that takes a predicate.
    //    // Here we widen the predicate to a function Node => Boolean.
    //    // this is voodoo
    //    def subFilter(pred: NodeType => Boolean)(implicit ct: ClassTag[NodeType]): Iterable[NodeType] =
    //      // Widen the predicate to Node => Boolean:
    //      val widened: Node => Boolean = (n: Node) =>
    //        if (ct.runtimeClass.isInstance(n))
    //          pred(n.asInstanceOf[NodeType])
    //        else
    //          false
    //
    //      iter.flatMap { node =>
    //        // Call the filter method that accepts a Node => Boolean.
    //        // Explicitly annotate the lambda parameter type so the compiler picks the right overload.
    //        node.filter((n: Node) => widened(n)).toSeq.collect { case n: NodeType => n }
    //      }

    def contains(str: String): Boolean = filter(str).toList.nonEmpty

    def valueContains(str: String): Boolean =
      seqNode.filter((node: Node[?]) =>
        node.identifier.isDefined && node.valueAsString.equals(str)
      ).toList.nonEmpty

    def scriptContains(str: String): Boolean = contains(str) || valueContains(str)

    def containsCaseInsensitive(str: String): Boolean = filterCaseInsensitive(str).toList.nonEmpty

    def containsAll(strings: String*): Boolean = strings.forall(contains)

    def containsAllCaseInsensitive(strings: String*): Boolean = strings.forall(containsCaseInsensitive)

    def anyMatch(predicate: Node[?] => Boolean): Boolean =
      seqNode.filter(predicate).toList.nonEmpty

    /**
     * Retrieve the node’s value as type `T` if found and matching the type,
     * otherwise return `default`.
     */
    def valueOrElse[T: ClassTag](key: String, default: T): T =
      seqNode.find(key).flatMap { node =>
        node.$ match
          case value: T => Some(value)
          case _ => None
      }.getOrElse(default)

  //  extension (node: AnyNode)
  //    def find(str: String): Option[NodeType] =
  //      node.$ match
  //        case l: Iterable[NodeType] => l.find(str)
  //        case _ => if node.identifier.isDefined && node.name.equals(str) then Some(node) else None
  //
  //    def findCaseInsensitive(str: String): Option[NodeType] =
  //      node.$ match
  //        case l: Iterable[NodeType] => l.findCaseInsensitive(str)
  //        case _ => if node.identifier.isDefined && node.name.equalsIgnoreCase(str) then Some(node) else None
  //
  //    def filterName(str: String): Iterable[NodeType] =
  //      node.$ match
  //        case l: Iterable[NodeType] => l.filterName(str)
  //        case _ => if node.identifier.isDefined && node.name.equals(str) then Some(node) else None
  //
  //    def filterNameCaseInsensitive(str: String): Iterable[NodeType] =
  //      node.$ match
  //        case l: Iterable[NodeType] => l.filterNameCaseInsensitive(str)
  //        case _ => if node.identifier.isDefined && node.name.equalsIgnoreCase(str) then Some(node) else None
  //
  //    def filter(str: String): Iterable[NodeType] = filterName(str)
  //
  //    def filterCaseInsensitive(str: String): filterNameCaseInsensitive(str)
  //
  //    def contains(str: String): Boolean = find(str).isDefined

}
