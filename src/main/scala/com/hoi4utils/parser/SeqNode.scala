package com.hoi4utils.parser

import com.hoi4utils.script.scripter.SimpleNodeScripter

class SeqNode
(
  /** Tokens that occurred before the "core" of this node. */
  var leadingTrivia: Seq[Token] = Seq.empty,
  /** The main identifier token (if any). */
  var identifierToken: Option[Token] = None,
  /** The operator token (if any, e.g. "="). */
  var operatorToken: Option[Token] = None,
  /** The node's value. This may be a literal (String, Int, Double, Boolean),
   a list (block) of child nodes, or a Comment. */
  var rawValue: NodeSeq,
  /** Tokens that came after the node's core. */
  var trailingTrivia: Seq[Token] = Seq.empty
) extends Node[NodeSeq]:

  def this(value: NodeSeq) =
    this(rawValue = value)

  def this(identifier: String, operator: String, value: NodeSeq) =
    this(
      leadingTrivia = Seq.empty,
      identifierToken = Some(new Token(identifier, TokenType.symbol)),
      operatorToken = Some(new Token(operator, TokenType.operator)),
      rawValue = value,
      trailingTrivia = Seq.empty
    )

  // use extension methods
//  // Helper methods to find child nodes (assuming NodeIterable provides find and findCaseInsensitive).
//  def getValue(id: String): Node[?] = find(id) match
//    case Some(node) => node
//    case None => null
//
//  def getValueCaseInsensitive(id: String): Node[?] = findCaseInsensitive(id) match
//    case Some(node) => node
//    case None => null

  override def asString: String =
    val sb = new StringBuilder
    val scripter = SimpleNodeScripter
    sb.append("{")
    for i <- rawValue.indices do
      sb.append(scripter.toScript(rawValue(i)))
      if i < rawValue.size - 1 then sb.append(" ")
    sb.append("}")
    sb.toString()

  override def isParent: Boolean = true

  def clear(): Unit =
    rawValue = Seq.empty

//  override def $: NodeSeq = rawValue.filter(_.nonComment) // todo obsolete? 

  override def iterator: Iterator[Node[?]] =
    rawValue.iterator

  def remove(i: Int): Unit =
    val updated = $.take(i) ++ $.drop(i + 1)
    setValue(updated)

  def removeInclComments(i: Int): Unit =
    val updated = $.take(i) ++ $.drop(i + 1)
    setValue(updated)




