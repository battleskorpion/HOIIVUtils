package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
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
class MultiPDX[T <: PDXScript[?]](var simpleSupplier: Option[() => T], var blockSupplier: Option[() => T], pdxIdentifiers: List[String])
  extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty
  
  def this(simpleSupplier: Option[() => T], blockSupplier: Option[() => T], pdxIdentifiers: String*) = {
    this(simpleSupplier, blockSupplier, pdxIdentifiers.toList)
  }

  def this(simpleSupplier: Option[() => T], blockSupplier: Option[() => T]) = {
    this(simpleSupplier, blockSupplier, List.empty)
  }
  
  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    try {
      add(expression)
    } catch {
      case e: NodeValueTypeException =>
        logger.error("Error loading PDX script: " + e.getMessage + "\n\t" + expression)
        // For MultiPDX, preserve the node by storing the raw expression.
        node = Some(expression)
    }
  }

  override def loadPDX(expressions: Iterable[Node]): Iterable[Node] = {
    if (expressions != null) {
      val remaining = ListBuffer.from(expressions)
      expressions.filter(this.isValidIdentifier).foreach((expression: Node) => {
        try {
          loadPDX(expression)
          remaining -= expression
        }
        catch {
          case e: UnexpectedIdentifierException =>
            logger.error(e.getMessage)
          //throw new RuntimeException(e);
        }
      })
      remaining
    } else {
      ListBuffer.empty
    }
  }

  override def equals(other: PDXScript[?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] = {
    if (pdxList.isEmpty) None
    else Some(pdxList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    usingIdentifier(expression)
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    expression.$ match {
      case l: ListBuffer[Node] =>
        val childScript = applySupplier(expression) // todo ehhh
        childScript.loadPDX(expression)
        pdxList.addOne(childScript)
      case _ =>
        // todo idk
        if (simpleSupplier.isEmpty) throw new NodeValueTypeException(expression, "not a list", this.getClass)
        val childScript = simpleSupplier.get.apply()
        childScript.loadPDX(expression)
        pdxList.addOne(childScript)
    }
  }

  def removeIf(p: T => Boolean): ListBuffer[T] = {
    for (i <- pdxList.indices.reverse) {
      if (p(pdxList(i))) {
        pdxList(i).getNode match {
          case Some(node) => node.clear()
          case _          => // do nothing
        }
        pdxList(i).clearNode()
        pdxList.remove(i)
      }
    }
    pdxList
  }

  def filterInPlace(p: T => Boolean): ListBuffer[T] = {
    this.removeIf(p andThen(!_))
  }

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   * @param pdxScript
   */
  @targetName("add")
  def +=(pdxScript: T): Unit = {
    pdxList += pdxScript
    // TODO TODO add to node
  }

  /**
   * Removes a PDXScript from the list of PDXScripts.
   * @param pdxScript
   */
  def -=(pdxScript: T): this.type = {
    val index = pdxList.indexOf(pdxScript)
    pdxList -= pdxScript
    pdxScript.clearNode()
    this
  }

  /**
   * Removes a PDXScript from the list of PDXScripts.
   * @param pdxScript
   * @note Java was *struggling* with 'this.type' return type. Use '-=' otherwise.
   * @return
   */
  def remove(pdxScript: T): Unit = {
    this -= pdxScript
  }

  def clear(): Unit = {
    node.foreach { n =>
      n.$ match {
        case l: ListBuffer[T] => l.clear()
        case _                => // do nothing
      }
    }
    pdxList.clear()
  }

  override def isEmpty: Boolean = value.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator

  override def foreach[U](f: T => U): Unit = pdxList.foreach(f)

  override def length: Int = pdxList.length

  override def apply(idx: Int): T = pdxList(idx)

  override def isUndefined: Boolean = pdxList.forall(_.isUndefined) || pdxList.isEmpty

  // todo no. in general multi. would be more than one node.
  override def set(obj: ListBuffer[T]): ListBuffer[T] = {
    obj
  }

  protected def applySupplier(expression: Node): T = {
    (simpleSupplier, blockSupplier) match {
      case (Some(s), None) => s()
      case (None, Some(b)) => b()
      case (Some(s), Some(b)) =>
        expression.$ match {
          case l: ListBuffer[Node] =>
            b()
          case _ => s()
        }
      case (None, None) => throw new RuntimeException("Both suppliers are null")
    }
  }

  def applySomeSupplier(): T = {
    (simpleSupplier, blockSupplier) match {
      case (Some(s), None) => s()
      case (None, Some(b)) => b()
      case (Some(s), Some(b)) => s()
      case (None, None) => throw new RuntimeException("Both suppliers are null")
    }
  }

  def addNewPDX(): T = {
    val pdx = applySomeSupplier()
    this += pdx
    pdx
  }

  override def clearNode(): Unit = {
    pdxList.foreach(_.clearNode())
  }

  override def getNodes: List[Node] = {
    pdxList.flatMap(_.getNode).toList
  }

  /**
   * Rebuilds the underlying Node tree for MultiPDX from the current collection of child nodes.
   */
  override def updateNodeTree(): Unit = {
    pdxList.foreach(_.updateNodeTree())
    val childNodes: ListBuffer[Node] = pdxList.flatMap(_.getNode)
    node match {
      case Some(n) => n.setValue(childNodes)
      case None => 
        if (childNodes.nonEmpty) node = Some(new Node(childNodes))
        else node = None
    }
  }

}


