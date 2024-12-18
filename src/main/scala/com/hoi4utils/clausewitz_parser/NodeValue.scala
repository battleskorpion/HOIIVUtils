package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import scala.annotation.targetName

import scala.collection.mutable.ListBuffer

/**
 * stores string, number, ArrayList<Node>, boolean, or null
 */
// todo should be subclasseed to impl effect parameter?
final class NodeValue {
  private var value: String | Int | Double | Boolean | ListBuffer[Node] | Null = _

  def this(value: String | Int | Double | Boolean | ListBuffer[Node] | Null) = {
    this()
    this.value = value
  }
  
  def string: String = {
    if (value == null) return null
    value match
      case str: String => str
      case _ => throw new IllegalStateException("Expected NodeValue value to be a string, value: " + value)
  }

  def integerOrElse(default: Int): Int = {
    if (value == null) return default
    value match
      case i: Int => i
      case d: Double => d.intValue
      case _ => throw new IllegalStateException("Expected NodeValue to be a Number or null, value: " + value)
  }

  def integer: Integer = {
    value match {
      case i: Int => i;
      case d: Double => d.intValue; 
      case _ => throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value)
    }
  }

  def rational: Double = {
    value match {
      case d: Double => d
      case i: Int => i.doubleValue
      // todo better error handling
      case _ => throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value)
    }
  }

  def bool(boolType: BoolType): Boolean = {
    value match {
      case str: String => str == boolType.trueResponse
      case bool: Boolean => bool
      case null => false
      case _ => throw new IllegalStateException("Expected NodeValue to be interpretable as a Boolean, value: " + value)
    }
  }

  def list: ListBuffer[Node] = {
    value match
      case list: ListBuffer[Node] => list
      case node: Node =>
        val list = new ListBuffer[Node]
        list.addOne(node)
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

  def asString: String = {
    if (value == null) return "[null]"
    value match {
      case s: String => s
      case i: Int => i.toString
      case d: Double => d.toString
      case l: ListBuffer[AnyVal] =>
        val sb = new StringBuilder()
        sb.append("{")
        for (i <- l.indices) {
          sb.append(l(i))
          if (i < l.size - 1) sb.append(", ")
        }
        sb.append("}")
        sb.toString()
      case n: Node => n.toString
      case _ => "[invalid type]"
    }
  }

  def isList: Boolean = value.isInstanceOf[List[?]]

  def isString: Boolean = value.isInstanceOf[String]

  def isNumber: Boolean = value.isInstanceOf[Number]
  // public boolean isBoolean() {
  // return value instanceof Boolean;
  // }
  // todo check allowables

  @targetName("plus")
  def +(other: NodeValue): NodeValue = (value, other.value) match {
    case (str1: String, str2: String) =>
      new NodeValue(str1 + str2)
    case (num1: Int, num2: Int) =>
      new NodeValue(num1 + num2)
    case (num1: Double, num2: Double) =>
      new NodeValue(num1 + num2)
    case _ =>
      throw new IllegalStateException(s"Cannot add NodeValues of types ${value.getClass} and ${other.value.getClass}")
  }

  def getValue: String | Int | Double | Boolean | ListBuffer[Node] | Null = {
    value
  }
  
  def setValue (value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    this.value = value
  }

//  def value_=(value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
//    setValue(value)
//  }
  
  def valueIsInstanceOf(clazz: Class[?]): Boolean = clazz.isInstance(value)
  
//  def $ (clazz: Class[_]): Boolean = valueIsInstanceOf(clazz)
}
