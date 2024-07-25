package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import java.util
import java.util.{ArrayList, List}
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

object Node {
  private val boolType: BoolType = null
}

class Node(private var identifier: String, private var operator: String, value: NodeValue, private var nameToken: Token, private var operatorToken: Token)
  extends NodeStreamable[Node] {

  this.value = if (value == null) new NodeValue
  else value
  final private var value: NodeValue = null

  def this(value: NodeValue) {
    this(null, null, value, null, null)
  }

  def this {
    this(null.asInstanceOf[NodeValue])
  }

  def this(value: util.ArrayList[Node]) {
    this(new NodeValue(value))
  }

  def name: String = identifier

  def value: NodeValue = value

  def valueObject: AnyRef = value.valueObject

  // no clue
  private[clausewitz_parser] def stream = {
    val nodeStream = new NodeStream[Node](this)
    nodeStream
  }

  override def getStream: Stream[Node] = stream.getStream

  override def filter(predicate: Predicate[_ >: Node]): NodeStreamable[Node] = new NodeStream[Node](this).filter(predicate)

  override def map[R <: Node](mapper: Function[_ >: Node, _ <: R]): NodeStreamable[R] = new NodeStream[Node](this).map(mapper)

  override def flatMap[R <: Node](mapper: Function[_ >: Node, _ <: NodeStreamable[R]]): NodeStreamable[R] = new NodeStream[Node](this).flatMap(mapper)

  override def toList: util.List[Node] = new NodeStream[Node](this).toList

  override def forEach(action: Consumer[_ >: Node]): Unit = {
    new NodeStream[Node](this).forEach(action)
  }

  override def findFirst: Node = new NodeStream[Node](this).findFirst

  override def findFirst(predicate: Predicate[Node]): Node = {
    val result = new NodeStream[Node](this).findFirst(predicate)
    if (result != null) result
    else new Node
  }

  override // note: was findFirstName, refactored
  def findFirst(str: String): Node = {
    val result = new NodeStream[Node](this).findFirst(str)
    if (result != null) result
    else new Node
  }

  override def filterName(str: String): NodeStreamable[Node] = new NodeStream[Node](this).filterName(str)

  override def filter(str: String): NodeStreamable[Node] = filterName(str)

  override def anyMatch(predicate: Predicate[_ >: Node]): Boolean = new NodeStream[Node](this).anyMatch(predicate)

  def getValue(id: String): NodeValue = findFirst(id).value

  def isParent: Boolean = value.isList

  def valueIsNull: Boolean = value.valueObject == null

  override def toString: String = identifier + operator + value.asString // todo

  def nameAsInteger: Int = identifier.toInt

  def nameEquals(s: String): Boolean = {
    if (identifier == null) return false
    identifier == s
  }
}