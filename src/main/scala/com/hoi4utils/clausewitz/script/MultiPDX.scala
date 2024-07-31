package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer
import java.util.function.Supplier
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

/**
 * PDX Script that can have multiple instantiation.
 * Example: multiple icon definitions in a focus.
 * The super PDXScript object will be a list of T objects.
 *
 * @param < T>
 */
class MultiPDX[T <: PDXScript[?]](var simpleSupplier: Option[() => T], var blockSupplier: Option[() => T], pdxIdentifiers: List[String])
  extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Iterable[T] {

  if (simpleSupplier.isEmpty && blockSupplier.isEmpty) throw new IllegalArgumentException("Both suppliers are null")

  def this(simpleSupplier: Option[() => T], blockSupplier: Option[() => T], pdxIdentifiers: String*) = {
    this(simpleSupplier, blockSupplier, pdxIdentifiers.toList)
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

  override def get(): Option[ListBuffer[T]] = super.get()

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
//    else try obj.add(value.valueObject.asInstanceOf[T])
//    catch {
//      case e: ClassCastException =>
//        throw new NodeValueTypeException(expression, e)
//    }
    node.$ match {
      case l: ListBuffer[T] =>
        val childScript = simpleSupplier.get.apply()  // todo fix
        childScript.loadPDX(expression)
        l.addOne(childScript)
      case _ =>
        // todo idk
        throw new NodeValueTypeException(expression, "list")
    }
  }

  def clear(): Unit = {
    node.$ match {
      case l: ListBuffer[T] => l.clear()
    }
  }

  override def isEmpty: Boolean = get().isEmpty

  override def iterator: Iterator[T] = get().iterator.flatten

  //  override def forEach(action: Consumer[? >: T]): Unit = {
  //    get().foreach(action)
  //  }
  override def foreach[U](f: T => U): Unit = super.foreach(f)

//  override def spliterator: Spliterator[T] = get().spliterator

  override def size: Int = get().size

//  def stream: Stream[T] = get().stream

  override def isUndefined: Boolean = node.isEmpty

  override def toScript: String = {
    val sb = new StringBuilder
    get() match {
      case Some(scripts) =>
        for (pdxScript <- scripts) {
          val str = pdxScript.toScript
          if (str != null) {
            sb.append(str)
          }
        }
        sb.toString()
      case None => ""
    }
  }

  // todo no. in general multi. would be more than one node.
  override def set(obj: ListBuffer[T]): Unit = {
    //
  }
}


