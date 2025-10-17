package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.collection.mutable.ListBuffer

/**
 * Concrete implementation for homogeneous PDX collections.
 *
 * CollectionPDX vs ListPDX:
 * - **ListPDX**: Simple supplier creates identical objects regardless of content
 * - **CollectionPDX**: Context-aware supplier can create different subtypes based on node content
 *
 * Use ListPDX when:
 * - Parsing collections where all elements are the same type
 * - Object creation is straightforward and context-independent
 * - You need a ready-to-use concrete class
 *
 * Example usage:
 * {{{
 * // Simple string collection
 * val removeIdeas = new ListPDX(() => new StringPDX(), "remove_ideas")
 *
 * // Collection of complex objects (all same type)
 * val uniformEvents = new ListPDX(() => new Event(), "events")
 * }}}
 *
 * @param simpleSupplier Function that creates new instances of T (content-agnostic)
 * @param pdxIdentifiers List of identifiers this collection can handle
 * @tparam T The type of objects this collection contains
 */
class ListPDX[T <: PDXScript[?]](var simpleSupplier: () => T, pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(simpleSupplier: () => T, pdxIdentifiers: String*) = {
    this(simpleSupplier, pdxIdentifiers.toList)
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: Node): Unit = loadPDXCollection(expression)
  
  override def equals(other: PDXScript[?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] = {
    if (pdxList.isEmpty) None
    else Some(pdxList)
  }

  /**
   * Implementation of addToCollection for ListPDX.
   * Uses simple supplier to create identical objects regardless of content.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def addToCollection(expression: Node): Unit = {
    expression.$ match {
      case l: ListBuffer[Node] =>
        for (childExpr <- l) {
          val childScript = useSupplierFunction(childExpr)
          childScript.loadPDX(childExpr)
          pdxList += childScript
        }
      case _ =>
        val childScript = useSupplierFunction(expression)
        childScript.loadPDX(expression)
        pdxList += childScript
    }
  }

  /**
   * Removes elements matching the predicate.
   * Also removes the corresponding node(s) from the underlying Node.
   */
  def removeIf(p: T => Boolean): ListBuffer[T] = {
    for (i <- pdxList.indices.reverse) {
      if (p(pdxList(i))) {
        pdxList.remove(i)
        node match {
          case Some(n) => n.remove(i)
          case None => // do nothing
        }
      }
    }
    pdxList
  }
  
  protected def useSupplierFunction(expression: Node): T = {
    simpleSupplier()
  }

  def clear(): Unit = {
    pdxList.clear()
    node.foreach { n =>
      n.$ match {
        case l: ListBuffer[?] => l.clear()
        case _ => // do nothing
      }
    }
  }

  override def isEmpty: Boolean = pdxList.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

  override def length: Int = pdxList.length

  override def apply(i: Int): T = pdxList(i)

  override def toList: List[T] = pdxList.toList

  override def isUndefined: Boolean = pdxList.forall(_.isUndefined) || pdxList.isEmpty

//  override def toScript: String = {
//    if (node.isEmpty || node.get.isEmpty) return null
//
////    val sb = new StringBuilder()
////    sb.append(node.get.identifier)
////    sb.append(" = {\n")
////    for (pdx <- get().get) {
////      sb.append('\t')
////      sb.append(pdx.toScript)
////    }
////    sb.toString
//    null
//  }

  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    val value = expression.$
    setNode(value)
  }

  // todo @skorp implement?
  override def set(obj: ListBuffer[T]): ListBuffer[T] = obj

  def headOrElse[U](default: U)(implicit ev: T <:< PDXScript[U]): U = {
    pdxList.headOption match {
      case Some(pdx) => ev(pdx).getOrElse(default)
      case None => default
    }
  }

  /**
   * Rebuilds the underlying Node tree from the current list of child PDXScript nodes.
   * Uses the abstracted collection node tree management from AbstractPDX.
   */
  override def updateNodeTree(): Unit = updateCollectionNodeTree(pdxList)

}
