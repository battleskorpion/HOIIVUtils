package com.hoi4utils.script.seq

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, NodeSeq, NodeValueType, ParserException}
import com.hoi4utils.script.{PDXScript, PDXSupplier}

import java.io.File
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
abstract class CollectionPDX[T <: PDXScript[?, ?]](pdxSupplier: PDXSupplier[T], pdxIdentifiers: List[String]) extends SeqPDX[T](pdxIdentifiers) {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(pdxSupplier: PDXSupplier[T], pdxIdentifiers: String*) = {
    this(pdxSupplier, pdxIdentifiers.toList)
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: NodeType, file: Option[File]): Unit = loadPDXCollection(expression, file)

  // todo?
  protected def loadPDX(file: File): Unit =
    ()
    // TODO TODO
//    require(file.exists && file.isFile, s"File $file does not exist or is not a file.")
//    val pdxParser = new Parser(file)
//    try loadPDX(pdxParser.parse, Some(file))
//    catch case e: ParserException => handlePDXError(e, file = file)

  override def equals(other: PDXScript[?, ?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] =
    if (pdxList.isEmpty) None
    else Some(pdxList)

  /**
   * Implementation of addToCollection for CollectionPDX.
   * Uses context-aware supplier to create appropriate PDXScript objects.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def addToCollection(expression: Node[?], file: Option[File]): Unit = expression.$ match
    case l: NodeSeq =>
      () // TODO TODO
//      for (childExpr <- l) {
//        val childScript = useSupplierFunction(childExpr)
//        childScript.loadPDX(childExpr, file)
//        pdxList += childScript
//      }
    case _ =>
      () // TODO TODO
//      val childScript = useSupplierFunction(expression)
//      childScript.loadPDX(expression, file)
//      pdxList += childScript

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
    ListBuffer.empty // TODO TODO
//    for
//      i <- pdxList.indices.reverse
//      if p(pdxList(i))
//    do // iterate backwards for safe removal
//      pdxList.remove(i)
//      node.foreach(_.remove(i))
//    pdxList

  protected def useSupplierFunction(expression: Node[?]): T = pdxSupplier(expression) match
    case Some(s) => s
    case None => throw new UnexpectedIdentifierException(expression)

  /**
   * Clears the underlying collection and, if present, clears the corresponding node list.
   */
  def clear(): Unit =
    ()
    // TODO TODO
//    pdxList.clear()
//    node.foreach { n =>
//      n.$ match
//        case l: ListBuffer[?] => l.clear()
//        case _ =>
//    }

  override def isEmpty: Boolean = pdxList.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

  override def length: Int = pdxList.length

  override def apply(i: Int): T = pdxList(i)

  override def toList: List[T] = pdxList.toList

  override def isUndefined: Boolean = pdxList.forall(_.isUndefined) || pdxList.isEmpty

  override def set(expression: NodeType): Unit =
    () // TODO TODO
//    usingIdentifier(expression)
//    this.node = Some(expression)
//    val value = expression.$
//    setNode(value)

  // todo @skorp implement?
  override def set(obj: ListBuffer[T]): ListBuffer[T] = obj

//  /**
//   * Updates the underlying Node tree for collection types.
//   * Uses the abstracted collection node tree management from AbstractPDX.
//   */
//  override def updateNodeTree(): Unit = updateCollectionNodeTree(pdxList)

  override def getPDXTypeName: String
}


