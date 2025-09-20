package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

abstract class CollectionPDX[T <: PDXScript[?]](pdxSupplier: PDXSupplier[T], pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(pdxSupplier: PDXSupplier[T], pdxIdentifiers: String*) = {
    this(pdxSupplier, pdxIdentifiers.toList)
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def loadPDX(expression: Node): Unit = {
    add(expression)
  }

  override def equals(other: PDXScript[?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] = {
    if (pdxList.isEmpty) None
    else Some(pdxList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    //usingIdentifier(expression);  // could be any identifier based on T
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    expression.$ match {
      case l: ListBuffer[Node] =>
        for (childExpr <- l) {
          val childScript = useSupplierFunction(childExpr)
          childScript.loadPDX(childExpr)
          pdxList += childScript
        }
      case _ =>
        // todo idk   // double todo
        val childScript = useSupplierFunction(expression)
        childScript.loadPDX(expression)
        pdxList += childScript
    }
  }

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   *
   * @param pdxScript the PDXScript to add
   */
  @targetName("add")
  def +=(pdxScript: T): Unit = {
    pdxList += pdxScript
  }

  /**
   * Removes elements matching the predicate.
   * Also removes the corresponding node(s) from the underlying Node.
   */
  def removeIf(p: T => Boolean): ListBuffer[T] = {
    for (i <- pdxList.indices.reverse) { // iterate backwards for safe removal
      if (p(pdxList(i))) {
        pdxList.remove(i)
        node match {
          case Some(n) => n.remove(i)
          case None      => // do nothing
        }
      }
    }
    pdxList
  }

  protected def useSupplierFunction(expression: Node): T = {
    pdxSupplier(expression) match {
      case Some(s) => s
      case None => throw new UnexpectedIdentifierException(expression)
    }
  }

  /**
   * Clears the underlying collection and, if present, clears the corresponding node list.
   */
  def clear(): Unit = {
    pdxList.clear()
    node.foreach { n =>
      n.$ match {
        case l: ListBuffer[?] => l.clear()
        case _ =>
      }
    }
  }

  override def isEmpty: Boolean = pdxList.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)
  
  override def length: Int = size

  override def apply(i: Int): T = pdxList(i)
  
//  override def size: Int = value.size

  override def toList: List[T] = pdxList.toList

  override def isUndefined: Boolean = {
    pdxList.forall(_.isUndefined) || pdxList.isEmpty
  }

  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    val value = expression.$
    setNode(value)
  }

  override def set(obj: ListBuffer[T]): ListBuffer[T] = {
    obj
  }

  /**
   * Updates the underlying Node tree for collection types.
   * It gathers the child Node for each child script in pdxList and sets that as the rawValue.
   */
  override def updateNodeTree(): Unit = {
    // Rebuild the child nodes from each contained PDXScript.
    pdxList.foreach(_.updateNodeTree())
    val childNodes: ListBuffer[Node] = pdxList.flatMap(_.getNode)
    node match {
      case Some(n) =>
        n.setValue(childNodes)
      case None =>
        // If no node exists, create a new one using the child nodes.
        if(pdxList.nonEmpty) node = Some(new Node(pdxIdentifier, "=", childNodes))
        else node = None
    }
  }

  override def getPDXTypeName: String
}


