package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

/**
 * Abstract base class for PDX collections that need polymorphic object creation.
 *
 * [[CollectionPDX]] vs [[ListPDX]]:
 * - CollectionPDX: Uses suppliers that can create different subtypes based on node content
 * - ListPDX: Uses simple suppliers that always create the same type regardless of content
 *
 * Use CollectionPDX when:
 * - You need to parse heterogeneous collections (e.g., different types of focuses, ideas, or modifiers)
 * - The type of object to create depends on the parsed content
 * - You want comprehensive error handling for unexpected identifiers
 *
 * Example usage:
 * {{{
 * // CollectionPDX - can handle different focus types based on content
 * class FocusCollection extends CollectionPDX[Focus](focusSupplier, "focus")
 * }}}
 *
 * @param pdxSupplier Function that creates objects based on the node content - enables polymorphism
 * @param pdxIdentifiers List of identifiers this collection can handle
 * @tparam T The base type of objects this collection contains
 */
abstract class CollectionPDX[T <: PDXScript[?]](pdxSupplier: PDXSupplier[T], pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(pdxSupplier: PDXSupplier[T], pdxIdentifiers: String*) = {
    this(pdxSupplier, pdxIdentifiers.toList)
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: Node): Unit = loadPDXCollection(expression)

  override def equals(other: PDXScript[?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] =
    if (pdxList.isEmpty) None
    else Some(pdxList)

  /**
   * Implementation of addToCollection for CollectionPDX.
   * Uses context-aware supplier to create appropriate PDXScript objects.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def addToCollection(expression: Node): Unit = expression.$ match
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

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   *
   * @param pdxScript the PDXScript to add
   */
  @targetName("add")
  def +=(pdxScript: T): Unit = pdxList += pdxScript

  /**
   * Removes elements matching the predicate.
   * Also removes the corresponding node(s) from the underlying Node.
   */
  def removeIf(p: T => Boolean): ListBuffer[T] =
    for
      i <- pdxList.indices.reverse
      if p(pdxList(i))
    do // iterate backwards for safe removal
      pdxList.remove(i)
      node.foreach(_.remove(i))
    pdxList

  protected def useSupplierFunction(expression: Node): T = pdxSupplier(expression) match
    case Some(s) => s
    case None => throw new UnexpectedIdentifierException(expression)


  /**
   * Clears the underlying collection and, if present, clears the corresponding node list.
   */
  def clear(): Unit =
    pdxList.clear()
    node.foreach { n =>
      n.$ match
        case l: ListBuffer[?] => l.clear()
        case _ =>
    }

  override def isEmpty: Boolean = pdxList.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

  override def length: Int = pdxList.length

  override def apply(i: Int): T = pdxList(i)

  override def toList: List[T] = pdxList.toList

  override def isUndefined: Boolean = pdxList.forall(_.isUndefined) || pdxList.isEmpty

  override def set(expression: Node): Unit =
    usingIdentifier(expression)
    this.node = Some(expression)
    val value = expression.$
    setNode(value)

  // todo @skorp implement?
  override def set(obj: ListBuffer[T]): ListBuffer[T] = obj

  /**
   * Updates the underlying Node tree for collection types.
   * Uses the abstracted collection node tree management from AbstractPDX.
   */
  override def updateNodeTree(): Unit = updateCollectionNodeTree(pdxList)

  override def getPDXTypeName: String
}


