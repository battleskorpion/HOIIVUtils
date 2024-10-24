package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer
import java.util.stream.Stream
import scala.collection.mutable.ListBuffer

// todo i do not like this class
abstract class CollectionPDX[T <: PDXScript[?]](pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Iterable[T] {
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
    import scala.jdk.CollectionConverters
    if (expressions != null)
      expressions.filter(this.isValidIdentifier).foreach((expression: Node) => {
        try loadPDX(expression)
        catch {
          case e: UnexpectedIdentifierException =>
            System.err.println(e.getMessage)
          //throw new RuntimeException(e);
        }

      })
  }

  override def equals(other: PDXScript[?]) = false // todo? well.

  override def get(): Option[ListBuffer[T]] = None//super.get() // todo

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    //usingIdentifier(expression);  // could be any identifier based on T
    val value = expression.$
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    node.get.$ match {
      case _: ListBuffer[Node] =>
//        val childScript = newChildScript(expression)
//        childScript.loadPDX(expression)
//        childScriptList.add(childScript)
      case _ =>
        // todo?
    }
  }

  protected def newChildScript(expression: Node): T = {
    // todo :(
//    val pdx = new T()
//    pdx.loadPDX(expression)
//    // todo this may be fine. if theres no runtime errors. leave this. yes the IDE is yelling.
//    //  the IDE thinks pdx is of type T. the compiler does not. ???
//    pdx.asInstanceOf[T]
    null.asInstanceOf[T]
  }

  def clear(): Unit = {
    if (node.nonEmpty) {
      node.get.$ match {
        case l: ListBuffer[T] =>
          l.clear()
        case _ =>
      }
    }
  }

  override def isEmpty: Boolean = get().isEmpty

  override def iterator: Iterator[T] = get().iterator.flatten   // todo idk

  override def foreach[U](f: T => U): Unit = super.foreach(f)

//  override def size: Int = {
//    get().size
//  }

  override def isUndefined: Boolean = node.isEmpty

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


