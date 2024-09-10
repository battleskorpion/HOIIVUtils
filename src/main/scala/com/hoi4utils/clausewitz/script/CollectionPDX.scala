package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer
import java.util.stream.Stream
import scala.collection.mutable.ListBuffer

// todo i do not like this class
abstract class CollectionPDX[T <: PDXScript[?]](pdxIdentifiers: List[String])
  extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Iterable[T] {

  protected var pdxList: ListBuffer[T] = ListBuffer.empty

  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

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
    //usingIdentifier(expression);  // could be any identifier based on T
    val value = expression.$
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    expression.$ match {
      case _: ListBuffer[Node] =>
        val childScript = newChildScript(expression)
        childScript.loadPDX(expression)
        pdxList += childScript
      case _ =>
        // todo idk
        val childScript = newChildScript(expression)
        childScript.loadPDX(expression)
        pdxList += childScript
    }
  }

  protected def newChildScript(expression: Node): T = {
    // todo :D
    // what if instead of this class, we saw the effects and then declared them
  }

  def clear(): Unit = {
    if (node.nonEmpty) {
      node.get.$ match {
        case l: ListBuffer[T] => l.clear()
      }
    }
  }

  override def isEmpty: Boolean = get().isEmpty

  override def iterator: Iterator[T] = get().iterator.flatten   // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

  override def size: Int = get().size

  override def isUndefined: Boolean = super.isUndefined

  override def toScript: String = {
    val sb = new StringBuilder
    get() match {
      case Some(scripts) =>
        for (pdxScript <- scripts) {
          val str = pdxScript.toScript
          if (str != null)
            sb.append(str)
        }
        sb.toString()
      case None => ""
    }
  }

  override def set(obj: ListBuffer[T]): Unit = {
    //
  }
}


