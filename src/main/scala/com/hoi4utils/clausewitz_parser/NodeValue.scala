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
  private var _value: Option[String | Int | Double | Boolean | ListBuffer[Node]] = None

  def this(value: String | Int | Double | Boolean | ListBuffer[Node] | Null) = {
    this()
    // todo optim
    if (value != null) {
      _value = Some(value)
    } else {
      _value = None
    }
  }
  
  def string: String = _value match {
    case None => null
    case Some(str: String) => str
    case _ => throw new ParserException("Expected NodeValue value to be a string, value: " + _value)
  } 
  
  def stringOrElse(default: String): String = _value match {
    case None => default
    case Some(str: String) => str
    case _ => throw new ParserException("Expected NodeValue value to be a string, value: " + _value)
  }

  def integerOrElse(default: Int): Int = _value match {
    case None => default
    case Some(i: Int) => i
    case Some(d: Double) => d.intValue
    case _ => throw new ParserException("Expected NodeValue to be a Number or null, value: " + _value)
  } 

  def integer: Integer = _value match {
    case Some(i: Int) => i;
    case Some(d: Double) => d.intValue; 
    case _ => throw new ParserException("Expected NodeValue to be a Number, value: " + _value)
  }

  def rational: Double = _value match {
    case Some(d: Double) => d
    case Some(i: Int) => i.doubleValue
    // todo better error handling
    case _ => throw new ParserException("Expected NodeValue to be a Number, value: " + _value)
  }

  def bool(boolType: BoolType): Boolean = _value match {
    case Some(str: String) => str == boolType.trueResponse
    case Some(bool: Boolean) => bool
    case None => false
    case _ => throw new ParserException("Expected NodeValue to be interpretable as a Boolean, value: " + _value)
  }

  def list: ListBuffer[Node] = _value match {
    case Some(list: ListBuffer[Node]) => list
    case Some(node: Node) =>
      val list = new ListBuffer[Node]
      list.addOne(node)
      list
    case None => null
    case _ => throw new ParserException("Expected NodeValue to be an ArrayList<Node>, value: " + _value)
  } 

  def node: Node = _value match {
    case Some(n: Node) => n
    case None => null
    case _ => throw new ParserException("Expected NodeValue to be a Node, value: " + _value)
  } 

  def asString: String = _value match {
    case Some(s: String) => s
    case Some(i: Int) => i.toString
    case Some(d: Double) => d.toString
    case Some(l: ListBuffer[AnyVal]) =>
      val sb = new StringBuilder()
      sb.append("{")
      for (i <- l.indices) {
        sb.append(l(i))
        if (i < l.size - 1) sb.append(", ")
      }
      sb.append("}")
      sb.toString()
    case Some(n: Node) => n.toString
    case None => return "[null]"
    case _ => "[invalid type]"
  } 

  def isList: Boolean = _value.get.isInstanceOf[ListBuffer[?]]

  def isString: Boolean = _value.get.isInstanceOf[String]

  def isNumber: Boolean = _value.get.isInstanceOf[Number]
  // public boolean isBoolean() {
  // return value instanceof Boolean;
  // }
  // todo check allowables

  @targetName("plus")
  def +(other: NodeValue): NodeValue = (_value.get, other._value.get) match {
    case (str1: String, str2: String) =>
      new NodeValue(str1 + str2)
    case (num1: Int, num2: Int) =>
      new NodeValue(num1 + num2)
    case (num1: Double, num2: Double) =>
      new NodeValue(num1 + num2)
    case _ =>
      throw new ParserException(s"Cannot add NodeValues of types ${_value.get.getClass} and ${other._value.get.getClass}")
  }

  def value: Option[String | Int | Double | Boolean | ListBuffer[Node]] = {
    _value
  }
  
  def setValue (value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    value match {
      case null => _value = None
      case _ => _value = Some(value)
    }
  }

  def value_=(value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    setValue(value)
  }
  
  def valueIsInstanceOf(clazz: Class[?]): Boolean = clazz.isInstance(_value.get)
  
//  def $ (clazz: Class[_]): Boolean = valueIsInstanceOf(clazz)
}
