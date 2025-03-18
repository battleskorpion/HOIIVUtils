package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.{Node, NodeValue}
import org.jetbrains.annotations.{NotNull, Nullable}

import java.util.function.Consumer
import java.util.stream.Stream
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

// todo i do not like this class
abstract class CollectionPDX[T <: PDXScript[?]](pdxSupplier: PDXSupplier[T], pdxIdentifiers: List[String])
  extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Iterable[T] {

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

  override def loadPDX(expressions: Iterable[Node]): Unit = {
    if (expressions != null) {
      expressions.filter(this.isValidIdentifier).foreach((expression: Node) => {
        try loadPDX(expression)
        catch {
          case e: UnexpectedIdentifierException =>
            System.err.println(e.getMessage)
          //throw new RuntimeException(e);
        }
      })
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
    //usingIdentifier(expression);  // could be any identifier based on T
    val value = expression.$
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

  protected def useSupplierFunction(expression: Node): T = {
    pdxSupplier(expression) match {
      case Some(s) => s
      case None => throw new UnexpectedIdentifierException(expression)
    }
  }

  def clear(): Unit = {
    if (node.nonEmpty) {
      node.get.$ match {
        case l: ListBuffer[T] => l.clear()
      }
    }
  }

  override def isEmpty: Boolean = pdxList.isEmpty

  override def iterator: Iterator[T] = pdxList.iterator // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

  override def size: Int = value.size

  override def toList: List[T] = pdxList.toList

  override def isUndefined: Boolean = {
    pdxList.forall(_.isUndefined) || pdxList.isEmpty
  }

  override def toScript: String = {
    if (node.isEmpty || node.get.isEmpty) return null

//    val sb = new StringBuilder()
//    sb.append(node.get.identifier)
//    sb.append(" = {\n")
//    for (pdx <- get().get) {
//      sb.append('\t')
//      sb.append(pdx.toScript)
//    }
//    sb.toString
    null  // todo
  }

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

  override def getPDXTypeName: String
}


