package com.hoi4utils.parser

import com.hoi4utils.script.scripter.SimpleNodeScripter
import com.hoi4utils.shared.BoolType
import dotty.tools.sjs.ir.Trees.JSBinaryOp.&&

type NodeValueType = PDXValueType | NodeSeq
type NodeSeq = Seq[Node[?]]

trait Node[T <: NodeValueType]:
  /** Tokens that occurred before the "core" of this node. */
  var leadingTrivia: Seq[Token] = Seq.empty
  /** The main identifier token (if any). */
  var identifierToken: Option[Token] = None
  /** The operator token (if any, e.g. "="). */
  var operatorToken: Option[Token] = None
  /** The node's value. This may be a literal (String, Int, Double, Boolean),
   * a list (block) of child nodes, or a Comment. */
  var rawValue: T
  /** Tokens that came after the node's core. */
  var trailingTrivia: Seq[Token] = Seq.empty


  def identifier: Option[String] = identifierToken.map(_.value)
  def operator: Option[String] = operatorToken.map(_.value)
  def value: T = rawValue
  def name: String = identifier.getOrElse("")

  /**
   * Sets the node's value.
   */
  def setValue(v: T): Unit = rawValue = v

  // Shorthand methods using the raw value.
  def $: T = rawValue

  /**
   * Converts the node's raw value to a String representation.
   */
  def asString: String = rawValue match
    //    case Some(n: Node)     => n.toString
    //    case None              => "[null]"
    case _ => "[invalid type]"

  def start: Int = identifierToken.map(_.start).getOrElse(0)

  def isParent: Boolean = false

  def isEmpty: Boolean = false
  def nonEmpty: Boolean = !isEmpty

  def valueAsString: String = asString

  def nameAsInteger: Int = identifier match
    case None    => 0
    case Some(s) => s.toInt

  def nameEquals(s: String): Boolean = identifier match
    case None    => s == null
    case Some(id) => id.equals(s)

  override def toString: String = asString

