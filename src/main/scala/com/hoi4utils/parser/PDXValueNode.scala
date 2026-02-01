package com.hoi4utils.parser

import com.hoi4utils.script.scripter.SimpleNodeScripter
import com.hoi4utils.shared.BoolType

class PDXValueNode[T <: PDXValueType]
(
  /** Tokens that occurred before the "core" of this node. */
  var leadingTrivia: Seq[Token] = Seq.empty,
  /** The main identifier token (if any). */
  var identifierToken: Option[Token] = None,
  /** The operator token (if any, e.g. "="). */
  var operatorToken: Option[Token] = None,
  /** The node's value. This may be a literal (String, Int, Double, Boolean),
   a list (block) of child nodes, or a Comment. */
  var rawValue: T,
  /** Tokens that came after the node's core. */
  var trailingTrivia: Seq[Token] = Seq.empty
) extends Node[T]:

  def this(value: T) =
    this(rawValue = value)

  def this(identifier: String, operator: String, value: T) =
    this(
      leadingTrivia = Seq.empty,
      identifierToken = Some(new Token(identifier, TokenType.symbol)),
      operatorToken = Some(new Token(operator, TokenType.operator)),
      rawValue = value,
      trailingTrivia = Seq.empty
    )

  override def asString: String = rawValue match
    case s: String   => s
    case i: Int      => i.toString
    case d: Double   => d.toString
    case b: Boolean  => b.toString

  def asBool(boolType: BoolType): Boolean = rawValue match
    case Some(s: String) => java.lang.Boolean.valueOf(s == boolType.trueResponse)
    case Some(b: Boolean) => java.lang.Boolean.valueOf(b)
    case _ =>
      // Try to pass the identifier token if available for better error context
      identifierToken match
        case Some(token) => throw new ParserException("Expected a Boolean or String for boolean conversion", token = Some(token))
        case None => throw new ParserException("Expected a Boolean or String for boolean conversion")
