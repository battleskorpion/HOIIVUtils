package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.{Node, NodeValue}
import org.jetbrains.annotations.{NotNull, Nullable}

import java.util.function.{Consumer, Supplier}
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

  override def get(): Option[ListBuffer[T]] = {
    if (pdxList.isEmpty) None
    else Some(pdxList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    expression.$ match {
      case l: ListBuffer[Node] =>
        val childScript = applySupplier(expression) // todo ehhh
        childScript.loadPDX(expression)
        pdxList.addOne(childScript)
      case _ =>
        // todo idk
        if (simpleSupplier.isEmpty) throw new NodeValueTypeException(expression, "list")
        val childScript = simpleSupplier.get.apply()
        childScript.loadPDX(expression)
        pdxList.addOne(childScript)
    }
  }

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   * @param pdxScript
   */
  @targetName ("add")
  def +=(pdxScript: T): Unit = {
    pdxList += pdxScript
  }

  def clear(): Unit = {
    if (node.nonEmpty) {
      node.get.$ match {
        case l: ListBuffer[T] => l.clear()
        case _ => 
      }
    }
    pdxList.clear()
  }

  override def isEmpty: Boolean = get().isEmpty

  override def iterator: Iterator[T] = pdxList.iterator

  //  override def forEach(action: Consumer[? >: T]): Unit = {
  //    get().foreach(action)
  //  }
  override def foreach[U](f: T => U): Unit = super.foreach(f)

//  override def spliterator: Spliterator[T] = get().spliterator

//  override def size: Int = get().getOrElse(ListBuffer.empty).size

  override def length: Int = pdxList.length

  override def apply(idx: Int): T = pdxList(idx)

  override def isUndefined: Boolean = {
    pdxList.forall(_.isUndefined)
  }

  override def toScript: String = {
    if (node.isEmpty || node.get.isEmpty) return null

    val sb = new StringBuilder()
    sb.append(node.get.identifier)
    sb.append(" = {\n")
    for (pdx <- pdxList) {
      sb.append('\t')
      sb.append(pdx.toScript)
    }
    sb.toString
  }

  // todo no. in general multi. would be more than one node.
  override def set(obj: ListBuffer[T]): ListBuffer[T] = {
    //
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
}


