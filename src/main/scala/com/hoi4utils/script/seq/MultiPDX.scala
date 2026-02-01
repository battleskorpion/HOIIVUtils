package com.hoi4utils.script.seq

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, NodeSeq, SeqNode}
import com.hoi4utils.script.PDXScript
import com.hoi4utils.script.seq.SeqPDX
import org.apache.poi.ss.formula.functions.T

import java.io.File
import scala.::
import scala.annotation.targetName
import scala.language.implicitConversions

/**
 * PDX Script that can have multiple instantiation.
 * Example: multiple icon definitions in a focus.
 * The super PDXScript object will be a list of T objects.
 *
 * @param simpleSupplier the supplier for simple PDXScript objects
 * @param blockSupplier  the supplier for block PDXScript objects
 * @param pdxIdentifiers the identifiers for the PDXScript objects
 */
class MultiPDX[T <: PDXScript[?, ?]](var simpleSupplier: Option[() => T], var blockSupplier: Option[() => T], pdxIdentifiers: List[String])
  extends SeqPDX[T](pdxIdentifiers){

  protected var pdxSeq: Seq[T] = Seq.empty

  def this(simpleSupplier: Option[() => T], blockSupplier: Option[() => T], pdxIdentifiers: String*) = {
    this(simpleSupplier, blockSupplier, pdxIdentifiers.toList)
  }

  def this(simpleSupplier: Option[() => T], blockSupplier: Option[() => T]) = {
    this(simpleSupplier, blockSupplier, List.empty)
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: NodeType, file: Option[File]): Unit = loadPDXCollection(expression, file)

  override def equals(other: PDXScript[?, ?]) = false // todo? well.

  override def value: Option[Seq[T]] = {
    if (pdxSeq.isEmpty) None
    else Some(pdxSeq)
  }

  /**
   * Implementation of addToCollection for MultiPDX.
   * Uses both simple and block suppliers based on node content.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def addToCollection(expression: Node[?], file: Option[File]): Unit =
    usingIdentifier(expression)
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    expression.$ match
      case l: NodeSeq =>
        val childScript = applySupplier(expression)
        childScript.loadPDX(expression, file)
        pdxSeq :+= childScript
      case _ =>
        if (simpleSupplier.isEmpty) throw new NodeValueTypeException(expression, "not a list", this.getClass)
        val childScript = simpleSupplier.get.apply()
        childScript.loadPDX(expression, file)
        pdxSeq :+= childScript

  def removeIf(p: T => Boolean): Seq[T] =
    val (toRemove, toKeep) = pdxSeq.partition(p)
    toRemove.foreach { item =>
      item.getNode.match {
        case s: SeqNode => s.clear()
        case _ => ()
      }
      item.clearNode()
    }
    pdxSeq = toKeep
    pdxSeq

  def filterInPlace(p: T => Boolean): Seq[T] =
    this.removeIf(p andThen(!_))

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   * @param pdxScript
   */
  @targetName("add")
  def +=(pdxScript: T): Unit =
    pdxSeq :+= pdxScript
    // TODO TODO add to node

  /**
   * Removes a PDXScript from the list of PDXScripts.
   * @param pdxScript
   */
  def -=(pdxScript: T): this.type =
    pdxSeq = pdxSeq.filterNot(_ == pdxScript)
    pdxScript.clearNode()
    this

  /**
   * Removes a PDXScript from the list of PDXScripts.
   * @param pdxScript
   * @note Java was *struggling* with 'this.type' return type. Use '-=' otherwise.
   * @return
   */
  def remove(pdxScript: T): Unit =
    this -= pdxScript

  def clear(): Unit =
    node match
      case s: SeqNode => s.clear()
      case _ => () 
    pdxSeq = Seq.empty

  override def isEmpty: Boolean = value.isEmpty

  override def iterator: Iterator[T] = pdxSeq.iterator

  override def foreach[U](f: T => U): Unit = pdxSeq.foreach(f)

  override def length: Int = pdxSeq.length

  override def apply(idx: Int): T = pdxSeq(idx)

  override def isUndefined: Boolean = pdxSeq.forall(_.isUndefined) || pdxSeq.isEmpty

  // todo no. in general multi. would be more than one node.
  override def set(obj: Seq[T]): Seq[T] = {
    obj
  }

  protected def applySupplier(expression: Node[?]): T =
    (simpleSupplier, blockSupplier) match
      case (Some(s), None) => s()
      case (None, Some(b)) => b()
      case (Some(s), Some(b)) =>
        expression.$ match
          case l: NodeSeq =>
            b()
          case _ => s()
      case (None, None) => throw new RuntimeException("Both suppliers are null")

  def applySomeSupplier(): T =
    (simpleSupplier, blockSupplier) match
      case (Some(s), None) => s()
      case (None, Some(b)) => b()
      case (Some(s), Some(b)) => s()
      case (None, None) => throw new RuntimeException("Both suppliers are null")

  def addNewPDX(): T =
    val pdx = applySomeSupplier()
    this += pdx
    pdx

  override def clearNode(): Unit =
    pdxSeq.foreach(_.clearNode())

  // TODO TOOD fix lol
  override def getNodes: List[Node[?]] =
    pdxSeq.flatMap(_.getNode).toList

  /**
   * Rebuilds the underlying Node tree for MultiPDX from the current collection of child nodes.
   * Uses the abstracted collection node tree management from AbstractPDX.
   */
  override def updateNodeTree(): Unit = updateCollectionNodeTree(pdxSeq)

}


