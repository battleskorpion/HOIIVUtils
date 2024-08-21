package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

//object Node {
//  private val boolType: BoolType = null
//}

class Node(protected[clausewitz_parser] var _identifier: String, protected[clausewitz_parser] var _operator: String,
           protected[clausewitz_parser] var nodeValue: NodeValue, protected[clausewitz_parser] var nameToken: Token,
           protected[clausewitz_parser] var operatorToken: Token)
  extends NodeIterable[Node] {

  if (nodeValue == null) nodeValue = new NodeValue
  
  def this(identifier: String, operator: String, value: String | Int | Double | Boolean | ListBuffer[Node] | Null) = {
    this(identifier, operator, new NodeValue(value), null, null)
  }
  
  def this(value: NodeValue) = {
    this(null, null, value, null, null)
  }

  def this() = {
    this(null.asInstanceOf[NodeValue])
  }

  def this(value: ListBuffer[Node]) = {
    this(new NodeValue(value))
  }

  def name: String = identifier

  def getValue: String | Int | Double | Boolean | ListBuffer[Node] | Null = nodeValue.getValue

  //def getValue(id:String): NodeValue = find(id).nodeValue
  def getValue(id: String): NodeValue = {
    val value = find(id)
    value match {
      case Some(node) => node.nodeValue
      case None => null
    }
  }

  def setValue(value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    this.nodeValue.setValue(value)
  }

  def isParent: Boolean = nodeValue.isList

  def valueIsNull: Boolean = this.$ == null
  
  override def isEmpty: Boolean = {
    valueIsNull && identifier == null && operator == null
  }

  /**
   * Returns a string representation of the individual node.
   * @return
   */
  override def toString: String = {
    if (!isEmpty)
      identifier + operator + nodeValue.asString
    else
      super.toString()
  }

  def nameAsInteger: Int = identifier.toInt

  def nameEquals(s: String): Boolean = {
    if (identifier == null) return false
    identifier == s
  }
  
  def setNull(): Unit = nodeValue = new NodeValue
  
  def valueIsInstanceOf(clazz: Class[?]): Boolean = nodeValue.valueIsInstanceOf(clazz)
  
  def $ : String | Int | Double | Boolean | ListBuffer[Node] | Null = getValue

  def identifier: String = _identifier

  def identifier_= (identifier: String): Unit = {
    _identifier = identifier
  }

  def operator: String = _operator

  def operator_= (operator: String): Unit = {
    _operator = operator
  }

  override def iterator: Iterator[Node] = {
    this.$ match {
      case l: ListBuffer[Node] => l.iterator
      case _ =>List(this).iterator
    }
  }

  def $list(): Option[ListBuffer[Node]] = {
    this.$ match {
      case l: ListBuffer[Node] => Some(l)
      case _ => None
    }
  }

  def $string: String = {
    this.$ match {
      case s: String => s
      case _ => null
    }
  }
  
  def $integer: Integer = {
    this.$ match {
      case i: Int => i
      case _ => null
    }
  }

  def valueAsString: String = {
    nodeValue.asString
  }
}