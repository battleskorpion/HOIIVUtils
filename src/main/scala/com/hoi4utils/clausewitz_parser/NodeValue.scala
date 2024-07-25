package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import java.util
import java.util.{ArrayList, Arrays}
import scala.annotation.targetName


/**
 * stores string, number, ArrayList<Node>, SymbolNode, or null
 */
// todo should be subclasseed to impl effect parameter?
final class NodeValue {
  private var value: String | Int | Double | Boolean | util.ArrayList[Node] = _

  def this(value: String | Int | Double | Boolean | util.ArrayList[Node]) {
    this()
    this.value = value
  }

  def valueObject: AnyRef = value

  def string: String = {
    value match
      case str: String => str
      case n: Null => null
      case _ => throw new IllegalStateException("Expected NodeValue value to be a string, value: " + value)
  }

  def integerOrElse(i: Int): Int = {
    if (value == null) return i
    value match
      case number: Number =>
        if (value.isInstanceOf[Integer]) value
        else number.intValue
      case _ => throw new IllegalStateException("Expected NodeValue to be a Number or null, value: " + value)
  }

  def integer: Integer = {
    if (value.isInstanceOf[Integer]) return value

    throw new IllegalStateException("Expected NodeValue to be a Number or null, value: " + value)
    // todo better error handling
  }

  def rational: Double = {
    if (value.isInstanceOf[Double]) return value
    // todo better error handling
    throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value)
  }

  def bool(boolType: BoolType): Boolean = {
//    if (value.isInstanceOf[String]) return value == boolType.trueResponse
    if (value.isInstanceOf[Boolean]) return value
    if (value == null) return false
    // todo better error handling
    throw new IllegalStateException("Expected NodeValue to be interpretable as a Boolean, value: " + value)
  }

  def list: util.ArrayList[Node] = {
    value match
      case list: util.ArrayList[Node] => list
      case node: Node =>
        val list = new util.ArrayList[Node]
        list.add(node)
        list
      case null => null
      case _ => throw new IllegalStateException("Expected NodeValue to be an ArrayList<Node>, value: " + value)
  }

  def node: Node = {
    value match
      case n: Node => return n
      case _ =>
    if (value == null) return null
    throw new IllegalStateException("Expected NodeValue to be a Node, value: " + value)
  }

  def asString: String = value match {
    case s: String => s
    case i: Int => Int.toString(i)
    case d: Double => Double.toString(d)
    case n: Number => Long.toString(n.longValue)
    case l: util.ArrayList[AnyVal] => util.Arrays.toString(l.toArray)
    case n: Node => n.toString
    case null: Null => "[null]"
    case _ => "[invalid type]"
  }

  def isList: Boolean = value.isInstanceOf[List[_]]

  def isString: Boolean = value.isInstanceOf[String]

  def isNumber: Boolean = value.isInstanceOf[Number]
  // public boolean isBoolean() {
  // return value instanceof Boolean;
  // }
  // todo check allowables
  
  @targetName("plus")
  def +(other: NodeValue): NodeValue = {
    value match
      case str: String if other.value.isInstanceOf[String] =>
        new NodeValue(str + other.value.asInstanceOf[String])
      case num: Int if other.value.isInstanceOf[Int] =>
        new NodeValue(value.asInstanceOf[Int].intValue + other.value.asInstanceOf[Int].intValue)
      case num: Double if other.value.isInstanceOf[Double] =>
        new NodeValue(value.asInstanceOf[Double].doubleValue + other.value.asInstanceOf[Double].doubleValue)
      case _ => throw new IllegalStateException("Cannot add NodeValues of types " + value.getClass + " and " + other.value.getClass)
  }
  
  def setValue (value: String | Int | Double | Boolean | util.ArrayList[Node]): Unit = {
    this.value = value
  }
}
