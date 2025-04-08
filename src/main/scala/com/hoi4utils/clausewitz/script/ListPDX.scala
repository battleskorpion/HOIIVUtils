package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.Node

import scala.collection.mutable.ListBuffer

/**
 *  where this is specifically USEFUL:
 *  ex:
 *  remove ideas can be a list of ideas where: 
 *  remove_ideas = {
 *      <idea_1>
 *      <idea_2>
 *      ...
 *  }
 *  
 *  where each <idea> is an idea string.
 */
class ListPDX[T <: PDXScript[?]](var simpleSupplier: () => T, pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Seq[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(simpleSupplier: () => T, pdxIdentifiers: String*) = {
    this(simpleSupplier, pdxIdentifiers.toList)
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
    if (expressions != null) {
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

  override def isUndefined: Boolean = {
    pdxList.forall(_.isUndefined) || pdxList.isEmpty
  }

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

  override def set(obj: ListBuffer[T]): ListBuffer[T] = {
    //
    obj
  }

  def headOrElse[U](default: U)(implicit ev: T <:< PDXScript[U]): U = {
    pdxList.headOption match {
      case Some(pdx) => ev(pdx).getOrElse(default)
      case None => default
    }
  }

  /**
   * Rebuilds the underlying Node tree from the current list of child PDXScript nodes.
   */
  override def updateNodeTree(): Unit = {
    pdxList.foreach(_.updateNodeTree())
    val childNodes: ListBuffer[Node] = pdxList.flatMap(_.getNode)
    node match {
      case Some(n) => n.setValue(childNodes)
      case None => 
        if (pdxList.nonEmpty) node = {
          if (pdxIdentifier.nonEmpty) Some(Node(pdxIdentifier, "=", childNodes))
          else Some(Node(childNodes))
        }
        else node = None
    }
  }

}
