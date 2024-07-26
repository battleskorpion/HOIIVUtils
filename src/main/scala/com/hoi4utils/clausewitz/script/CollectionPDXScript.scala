package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer
import java.util.stream.Stream

// todo i do not like this class
abstract class CollectionPDXScript[T <: PDXScript[?]](pdxIdentifiers: String*) extends AbstractPDX[ListBuffer[T]](pdxIdentifiers) with Iterable[T] {
  def this(pdxIdentifiers: List[String]) = {
    this(pdxIdentifiers*)
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    try add(expression)
    catch {
      case e: NodeValueTypeException =>
        throw new RuntimeException(e)
    }
  }

  override def loadPDX(expressions: List[Node]): Unit = {
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

  override def nodeEquals(other: PDXScript[?]) = false // todo? well.

  override def get(): ListBuffer[T] = super.get()

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  protected def add(expression: Node): Unit = {
    //usingIdentifier(expression);  // could be any identifier based on T
    val value = expression.value
    // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
    // then load each sub-PDXScript
    node.$ match {
      case _: ListBuffer[T] =>
        val childScript = newChildScript(expression)
        childScript.loadPDX(expression)
        childScriptList.add(childScript)
      case _ =>
        // todo?
    }
  }

  protected def newChildScript(expression: Node): T

  def clear(): Unit = {
    node.$.clear
  }

  def isEmpty: Boolean = get().isEmpty

  override def iterator: Iterator[T] = get().iterator

  override def forEach(action: Consumer[? >: T]): Unit = {
    get().foreach(action)
  }

//  override def spliterator: java.util.Spliterator[T] = get().spliterator

  def size: Int = get().size

//  def stream: Stream[T] = get().stream

  override def isUndefined: Boolean = node.isEmpty

  override def toScript: String = {
    val sb = new StringBuilder
    val scripts = get()
    if (scripts == null) return null
    for (pdxScript <- scripts) {
      val str = pdxScript.toScript
      if (str == null) continue //todo: continue is not supported
      sb.append(str)
    }
    sb.toString
  }
}


