package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util
import java.util.{ArrayList, Iterator, List, Spliterator}
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Stream


/**
 * PDX Script that can have multiple instantiation.
 * Example: multiple icon definitions in a focus.
 * The super PDXScript object will be a list of T objects.
 *
 * @param < T>
 */
class MultiPDX[T <: PDXScript[_]] extends AbstractPDX[util.List[T]](pdxIdentifiers) with Iterable[T] {
  final protected var supplier: Supplier[T] = _

  def this(supplier: Supplier[T], pdxIdentifiers: String*) = {
    this()
    this.supplier = supplier
  }

  def this(supplier: Supplier[T], pdxIdentifiers: util.List[String]) = {
    this()
    this.supplier = supplier
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    try add(expression)
    catch {
      case e: NodeValueTypeException =>
        throw new RuntimeException(e)
    }
  }

  override def loadPDX(expressions: util.List[Node]): Unit = {
    if (expressions != null)
      expressions.stream.filter(this.isValidIdentifier).forEach((expression: Node) => {
        try loadPDX(expression)
        catch {
          case e: UnexpectedIdentifierException =>
            System.err.println(e.getMessage)
          //throw new RuntimeException(e);
        }

      })
  }

  override def nodeEquals(other: PDXScript[_]) = false // todo? well.

  override def get(): util.List[T] = super.get()

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.value
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
//    else try obj.add(value.valueObject.asInstanceOf[T])
//    catch {
//      case e: ClassCastException =>
//        throw new NodeValueTypeException(expression, e)
//    }
    node.$ match {
      case l: util.List[T] =>
        val childScript = supplier.get()
        childScript.loadPDX(expression)
        l.add(childScript)
      case _ =>
        // todo idk
        throw new NodeValueTypeException(expression, "list")
    }
  }

  def clear(): Unit = {
    node.$ match {
      case l: util.List[T] => l.clear()
    }
  }

  def isEmpty: Boolean = get().isEmpty

  override def iterator: util.Iterator[T] = get().iterator

  override def forEach(action: Consumer[_ >: T]): Unit = {
    get().forEach(action)
  }

  override def spliterator: Spliterator[T] = get().spliterator

  def size: Int = get().size

  def stream: Stream[T] = get().stream

  override def isUndefined: Boolean = obj.isEmpty

  override def toScript: String = {
    val sb = new StringBuilder
    val scripts = get()
    if (scripts == null) return null
    import scala.collection.JavaConversions._
    for (pdxScript <- scripts) {
      val str = pdxScript.toScript
      if (str == null) continue //todo: continue is not supported
      sb.append(str)
    }
    sb.toString
  }
}


