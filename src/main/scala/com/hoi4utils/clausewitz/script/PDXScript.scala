package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node

trait PDXScript[T] {
  def set(obj: T): Unit

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  def set(expression: Node): Unit

  def get(): Option[T]
  
  def getNode : Node

  @throws[UnexpectedIdentifierException]
  def loadPDX(expression: Node): Unit

  def loadPDX(expressions: Iterable[Node]): Unit

  //void loadPDX(@NotNull File file);//void loadPDX(@NotNull File file);

  def isValidIdentifier(node: Node): Boolean

  def setNull(): Unit

  def loadOrElse(exp: Node, value: T): Unit

  def toScript: String

  def equals(other: PDXScript[?]): Boolean

  def getOrElse(elseValue: T): T

  def isUndefined: Boolean

  def getPDXIdentifier: String
}
