package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

abstract class CollectionPDX[T <: PDXScript[?]](pdxSupplier: PDXSupplier[T], pdxIdentifiers: List[String])
  extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(pdxSupplier: PDXSupplier[T], pdxIdentifiers: String*) = {
    this(pdxSupplier, pdxIdentifiers.toList)
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    try add(expression)
    catch {
      case e: NodeValueTypeException =>
        throw new RuntimeException(e)
    }
  }

  override def loadPDX(expressions: Iterable[Node]): Iterable[Node] = {
    expressions match {
      case null => ListBuffer.empty
      case _ =>
        val remaining = ListBuffer.from(expressions)
        expressions.filter(this.isValidIdentifier).foreach((expression: Node) => {
          try {
            loadPDX(expression)
            remaining -= expression
          }
          catch {
            case e: UnexpectedIdentifierException =>
              System.err.println(e.getMessage)
          }
        })
        remaining
    }
  }

  override def equals(other: PDXScript[?]) = false // todo? well.

  override def value: Option[ListBuffer[T]] = {
    if (pdxList.isEmpty) None
    else Some(pdxList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit =
    val nodesToProcess = expression.$ match
      case nodes: ListBuffer[Node] => nodes
      case _: String => logger.error("String value found where a collection was expected: " + expression); ListBuffer.empty
      case _: Int => logger.error("Integer value found where a collection was expected: " + expression); ListBuffer.empty
      case _: Double => logger.error("Double value found where a collection was expected: " + expression); ListBuffer.empty
      case true => logger.error("Boolean value found where a collection was expected: " + expression); ListBuffer.empty
      case false => logger.error("Boolean value found where a collection was expected: " + expression); ListBuffer.empty

    nodesToProcess.foreach { node =>
      val childScript = pdxSupplier(node) match
        case Some(script) => script
        case None => throw new UnexpectedIdentifierException(node, this.getClass)
      childScript.loadPDX(node)
      pdxList += childScript
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


