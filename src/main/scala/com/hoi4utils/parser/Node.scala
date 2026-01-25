package com.hoi4utils.parser

import com.hoi4utils.script.scripter.SimpleNodeScripter
import com.hoi4utils.shared.BoolType

type NodeValueType = PDXValueType | Comment

// Consolidated Node class (no NodeValue) using rawValue directly.
class Node (
             /** Tokens that occurred before the "core" of this node. */
             var leadingTrivia: Seq[Token] = Seq.empty,
             /** The main identifier token (if any). */
             var identifierToken: Option[Token] = None,
             /** The operator token (if any, e.g. "="). */
             var operatorToken: Option[Token] = None,
             /** The node's value. This may be a literal (String, Int, Double, Boolean),
                 a list (block) of child nodes, or a Comment. */
             var rawValue: Option[NodeValueType] = None,
             /** Tokens that came after the node's core. */
             var trailingTrivia: Seq[Token] = Seq.empty
           ) extends NodeIterable[Node]:

  def this(value: NodeValueType) =
    this(rawValue = Some(value))

  def this(identifier: String, operator: String, value: NodeValueType) =
    this(
      leadingTrivia = Seq.empty,
      identifierToken = Some(new Token(identifier, TokenType.symbol)),
      operatorToken = Some(new Token(operator, TokenType.operator)),
      rawValue = Some(value),
      trailingTrivia = Seq.empty
    )

  // Convenience getters extracting raw string representations from tokens.
  def identifier: Option[String]    = identifierToken.map(_.value)
  def operator: Option[String]      = operatorToken.map(_.value)
  def value: Option[NodeValueType]  = rawValue
  def name: String                  = identifier.getOrElse("")

  /**
   * Converts the node's raw value to a String representation.
   */
  def asString: String = rawValue match
    case Some(s: String)   => s
    case Some(i: Int)      => i.toString
    case Some(d: Double)   => d.toString
    case Some(b: Boolean)  => b.toString
    case Some(list: Seq[Node]) =>
      val sb = new StringBuilder
      val scripter = SimpleNodeScripter
      sb.append("{")
      for i <- list.indices do
        sb.append(scripter.toScript(list(i)))
        if i < list.size - 1 then sb.append(" ")
      sb.append("}")
      sb.toString()
    case Some(n: Node)     => n.toString
    case Some(c: Comment)  => c.toString
    case None              => "[null]"
    case _                 => "[invalid type]"

  def asBool(boolType: BoolType): Boolean = rawValue match
    case Some(s: String) => java.lang.Boolean.valueOf(s == boolType.trueResponse)
    case Some(b: Boolean) => java.lang.Boolean.valueOf(b)
    case _ =>
      // Try to pass the identifier token if available for better error context
      identifierToken match
        case Some(token) => throw new ParserException("Expected a Boolean or String for boolean conversion", token = Some(token))
        case None => throw new ParserException("Expected a Boolean or String for boolean conversion")

  override def toString: String =
    //    val sb = new StringBuilder
    //    if (identifier.nonEmpty) sb.append(identifier.get).append(" ")
    //    if (operator.nonEmpty)   sb.append(operator.get).append(" ")
    //    sb.append(asString)
    //    sb.toString()
    asString

  // Helper methods to find child nodes (assuming NodeIterable provides find and findCaseInsensitive).
  def getValue(id: String): Node = find(id) match
    case Some(node) => node
    case None       => null

  def getValueCaseInsensitive(id: String): Node = findCaseInsensitive(id) match
    case Some(node) => node
    case None       => null

  /**
   * Sets the node's value.
   */
  def setValue(v: NodeValueType | Null): Unit = v match
    case null => rawValue = None
    case _    => rawValue = Some(v)

  def isParent: Boolean = rawValue match
    case Some(list: Seq[Node]) => true
    case _                            => false

  def valueIsNull: Boolean = rawValue.isEmpty

  override def isEmpty: Boolean = valueIsNull && identifier.isEmpty && operator.isEmpty

  def nameAsInteger: Int = identifier match
    case None    => 0
    case Some(s) => s.toInt

  def nameEquals(s: String): Boolean = identifier match
    case None    => s == null
    case Some(id) => id.equals(s)

  def setNull(): Unit = rawValue = None

  def clear(): Unit =
    identifierToken = None
    operatorToken = None
    rawValue = None
    
  def clearIfBlock(): Unit =
    rawValue match
      case s: NodeSeq => rawValue = Some(Seq.empty) 
      case _ => // do nothing

  def valueIsInstanceOf(clazz: Class[?]): Boolean = rawValue.exists(clazz.isInstance)

  // Shorthand methods using the raw value.
  def $ : PDXValueType | Null = rawValue match
    case Some(v: Seq[Node]) => v.filter(_.nonComment)
    case Some(v: String)           => v
    case Some(v: Int)              => v
    case Some(v: Double)           => v
    case Some(v: Boolean)          => v
    case Some(_: Comment)          => null
    case None                      => null

  def $value : NodeValueType | Null = rawValue.orNull

  override def iterator: Iterator[Node] = rawValue match
    case Some(l: Seq[Node]) => l.iterator
    case _ => List(this).iterator //Iterator.empty

  def $seq(): Option[Seq[Node]] = rawValue match
    case Some(s: Seq[Node]) => Some(s)
    case _ => None

  def $seqOrElse(x: Seq[Node]): Seq[Node] = rawValue match
    case Some(s: Seq[Node]) => s
    case _ => x

  def $string: Option[String] = $ match
    case s: String => Some(s)
    case _         => None

  def $stringOrElse(x: String): String = $ match
    case s: String => s
    case _         => x

  def $int: Option[Int] = $ match
    case i: Int => Some(i)
    case _      => None

  def $intOrElse(x: Int): Int = $ match
    case i: Int => i
    case _      => x

  def $double: Option[Double] = $ match
    case d: Double => Some(d)
    case _         => None

  def $doubleOrElse(x: Double): Double = $ match
    case d: Double => d
    case _         => x

  def $boolean: Option[Boolean] = $ match
    case b: Boolean => Some(b)
    case _          => None

  def $booleanOrElse(x: Boolean): Boolean = $ match
    case b: Boolean => b
    case _          => x

  def valueAsString: String = asString

  def start: Int = identifierToken.map(_.start).getOrElse(0)

  def remove(i: Int): Unit = $ match
    case l: Seq[Node] => 
      val updated = l.take(i) ++ l.drop(i + 1)
      setValue(updated)
    case _                   =>

  def isComment: Boolean = rawValue.exists:
    case _: Comment => true
    case _          => false

  def nonComment: Boolean = !isComment

  def isInt: Boolean = rawValue.exists:
    case _: Int => true
    case _      => false

  def isDouble: Boolean = rawValue.exists:
    case _: Double => true
    case _         => false

  def isString: Boolean = rawValue.exists:
    case _: String => true
    case _         => false

  def valueEquals(value: PDXValueType): Boolean = rawValue match
      case Some(v) => v == value
      case None    => false
