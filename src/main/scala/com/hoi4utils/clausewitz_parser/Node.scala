package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import dotty.tools.sjs.ir.Trees.JSUnaryOp.!

import scala.collection.mutable.ListBuffer

// Consolidated Node class (no NodeValue) using rawValue directly.
class Node (
            // Tokens that occurred before the “core” of this node.
            var leadingTrivia: ListBuffer[Token] = ListBuffer(),
            // The main identifier token (if any)
            var identifierToken: Option[Token] = None,
            // The operator token (if any, e.g. "=")
            var operatorToken: Option[Token] = None,
            // The node’s value. This may be a literal (String, Int, Double, Boolean),
            // a list (block) of child nodes, or a Comment.
            var rawValue: Option[String | Int | Double | Boolean | ListBuffer[Node] | Comment] = None,
            // Tokens that came after the node’s core.
            var trailingTrivia: ListBuffer[Token] = ListBuffer()
          ) extends NodeIterable[Node] {

  def this(value: String | Int | Double | Boolean | ListBuffer[Node] | Comment) =
    this(rawValue = Some(value))

  def this(identifier: String, operator: String, value: String | Int | Double | Boolean | ListBuffer[Node] | Comment) =
    this(
      leadingTrivia = ListBuffer(),
      identifierToken = Some(new Token(identifier, -1, TokenType.symbol)),
      operatorToken = Some(new Token(operator, -1, TokenType.operator)),
      rawValue = Some(value),
      trailingTrivia = ListBuffer()
    )

  // Convenience getters extracting raw string representations from tokens.
  def identifier: Option[String] = identifierToken.map(_.value)
  def operator: Option[String]   = operatorToken.map(_.value)
  def value: Option[String | Int | Double | Boolean | ListBuffer[Node] | Comment] = rawValue
  def name: String = identifier.getOrElse("")

  /**
   * Converts the node’s raw value to a String representation.
   */
  def asString: String = rawValue match {
    case Some(s: String)   => s
    case Some(i: Int)      => i.toString
    case Some(d: Double)   => d.toString
    case Some(b: Boolean)  => b.toString
    case Some(list: ListBuffer[Node]) =>
      val sb = new StringBuilder
      sb.append("{")
      for (i <- list.indices) {
        sb.append(list(i).toScriptSimple)
        if (i < list.size - 1) sb.append(" ")
      }
      sb.append("}")
      sb.toString()
    case Some(n: Node)     => n.toString
    case Some(c: Comment)  => c.toString
    case None              => "[null]"
    case _                 => "[invalid type]"
  }

  def asBool(boolType: BoolType): Boolean = rawValue match {
    case Some(s: String) => java.lang.Boolean.valueOf(s == boolType.trueResponse)
    case Some(b: Boolean) => java.lang.Boolean.valueOf(b)
    case _ => throw new ParserException("Expected a Boolean or String for boolean conversion")
  }

  /**
   * Reconstructs the original file text by concatenating:
   * - leading trivia (whitespace/comments)
   * - the identifier and operator tokens
   * - the node’s value (or nested children, if a block)
   * - trailing trivia
   */
  def toScript(indent: String = ""): String = {
    val sb = new StringBuilder

    // Append all leading trivia on its own line(s) (preserving comments/whitespace)
    for (t <- leadingTrivia) {
      // replace all whitespace
      sb.append(indent).append(t.value.replaceAll("\\t+", ""))
    }

    // Append the identifier and operator (if any)
    identifier.foreach { id =>
      sb.append(indent).append(id)
      operator.foreach(op => sb.append(" ").append(op))
      sb.append(" ") // separate identifier/operator from the value
    }

    /* value */
    rawValue match {
      case Some(children: ListBuffer[Node]) =>
        if (children.forall(_.identifier.isEmpty) &&
          children.forall(_.operator.isEmpty) &&
          children.forall(n => n.rawValue.exists {
            case _: Int | _: Double => true
            case _ => false
          })) {
          sb.append("{ ")
          sb.append(children.map(_.asString).mkString(" "))
          sb.append(" }").append('\n')
        } else {
          // For a block of child nodes, open a brace and newline
          if (identifier.nonEmpty) sb.append("{\n")
          // Increase indent for children
          val childIndent = {
            if (identifier.nonEmpty) indent + "\t"
            else indent
          }
          for (child <- children) {
            // Recursively call toScript on each child with increased indent
            var childToScript = child.toScript(childIndent)
            if (!childToScript.contains("\n")) childToScript = childToScript + "\n"
            sb.append(childToScript)
            // Ensure each child ends with a newline
            //if (!child.toScript(childIndent).endsWith("\n")) sb.append("\n")
          }
          //if (sb.nonEmpty) sb.deleteCharAt(sb.length - 1)
          if (identifier.nonEmpty) {
            // Remove all trailing whitespace from the StringBuilder
            while (sb.nonEmpty && sb.charAt(sb.length - 1).isWhitespace) {
              sb.deleteCharAt(sb.length - 1)
            }
            sb.append('\n').append(indent).append("}")
          }
          else sb.append('\n')
        }
      case Some(v) =>
        // For a literal value, simply append its string form
        sb.append(v.toString)
      case None =>
        if (identifier.nonEmpty && operator.nonEmpty)
          sb.append("[null]")
    }

    // Append trailing trivia (e.g. comments that came after the node)
    for (t <- trailingTrivia) {
      sb.append(indent).append(t.value.replaceAll("\\t+", ""))
    }
    sb.toString()
  }

  def toScript: String = toScript("")

  /**
   * Generates a canonical “pretty print” version.
   * This method produces a normalized output rather than preserving every original space.
   */
  def toScriptSimple: String = {
    val sb = new StringBuilder
    if (identifier.nonEmpty) sb.append(identifier.get).append(" ")
    if (operator.nonEmpty)   sb.append(operator.get).append(" ")

    rawValue match {
      case Some(children: ListBuffer[Node]) =>
        // Special handling if the block is a list of numbers.
        if (children.forall(_.identifier.isEmpty) &&
          children.forall(_.operator.isEmpty) &&
          children.forall(n => n.rawValue.exists {
            case _: Int | _: Double => true
            case _ => false
          })) {
          sb.append("{ ")
          sb.append(children.map(_.asString).mkString(" "))
          sb.append(" }").append('\n')
        } else {
          if (identifier.nonEmpty) sb.append("{\n\t")
          for (child <- children) {
            var sScript = child.toScriptSimple
            if (sScript != null && sScript.nonEmpty) {
              // Add an extra tab to subsequent lines.
              sScript = sScript.replace("\n", "\n\t")
              sb.append(sScript)
            }
          }
          if (sb.nonEmpty) sb.deleteCharAt(sb.length - 1)
          if (identifier.nonEmpty) sb.append("}").append('\n')
        }
      case Some(v) =>
        sb.append(asString).append('\n')
      case None =>
        if (identifier.nonEmpty && operator.nonEmpty)
          sb.append(identifier.get).append(" ").append(operator.get).append(" [null]").append('\n')
    }
    sb.toString()
  }

  override def toString: String = {
//    val sb = new StringBuilder
//    if (identifier.nonEmpty) sb.append(identifier.get).append(" ")
//    if (operator.nonEmpty)   sb.append(operator.get).append(" ")
//    sb.append(asString)
//    sb.toString()
    asString
  }

  // Helper methods to find child nodes (assuming NodeIterable provides find and findCaseInsensitive).
  def getValue(id: String): Node = {
    find(id) match {
      case Some(node) => node
      case None       => null
    }
  }

  def getValueCaseInsensitive(id: String): Node = {
    findCaseInsensitive(id) match {
      case Some(node) => node
      case None       => null
    }
  }

  /**
   * Sets the node’s value.
   */
  def setValue(v: String | Int | Double | Boolean | ListBuffer[Node] | Comment | Null): Unit = {
    v match {
      case null => rawValue = None
      case _    => rawValue = Some(v)
    }
  }

  def isParent: Boolean = rawValue match {
    case Some(list: ListBuffer[Node]) => true
    case _                            => false
  }

  def valueIsNull: Boolean = rawValue.isEmpty

  override def isEmpty: Boolean = valueIsNull && identifier.isEmpty && operator.isEmpty

  def nameAsInteger: Int = identifier match {
    case None    => 0
    case Some(s) => s.toInt
  }

  def nameEquals(s: String): Boolean = identifier match {
    case None    => s == null
    case Some(id) => id.equals(s)
  }

  def setNull(): Unit = rawValue = None

  def clear(): Unit = {
    identifierToken = None
    operatorToken = None
    rawValue = None
  }

  def valueIsInstanceOf(clazz: Class[?]): Boolean = rawValue.exists(clazz.isInstance)

  // Shorthand methods using the raw value.
  def $ : String | Int | Double | Boolean | ListBuffer[Node] | Null = rawValue match {
    case Some(v: ListBuffer[Node]) => v.filter(_.nonComment)
    case Some(v: String)           => v
    case Some(v: Int)              => v
    case Some(v: Double)           => v
    case Some(v: Boolean)          => v
    case Some(_: Comment)          => null
    case _                         => null
  }

  def $value : String | Int | Double | Boolean | ListBuffer[Node] | Comment | Null = rawValue.orNull

  override def iterator: Iterator[Node] = rawValue match {
    case Some(l: ListBuffer[Node]) => l.iterator
    case _ => List(this).iterator //Iterator.empty
  }

  def $list(): Option[ListBuffer[Node]] = $ match {
    case l: ListBuffer[Node] => Some(l)
    case _                   => None
  }

  def $listOrElse(x: ListBuffer[Node]): ListBuffer[Node] = $ match {
    case l: ListBuffer[Node] => l
    case _                   => x
  }

  def $string: Option[String] = $ match {
    case s: String => Some(s)
    case _         => None
  }

  def $stringOrElse(x: String): String = $ match {
    case s: String => s
    case _         => x
  }

  def $int: Option[Int] = $ match {
    case i: Int => Some(i)
    case _      => None
  }

  def $intOrElse(x: Int): Int = $ match {
    case i: Int => i
    case _      => x
  }

  def $double: Option[Double] = $ match {
    case d: Double => Some(d)
    case _         => None
  }

  def $doubleOrElse(x: Double): Double = $ match {
    case d: Double => d
    case _         => x
  }

  def $boolean: Option[Boolean] = $ match {
    case b: Boolean => Some(b)
    case _          => None
  }

  def $booleanOrElse(x: Boolean): Boolean = $ match {
    case b: Boolean => b
    case _          => x
  }

  def valueAsString: String = asString

  def start: Int = identifierToken.map(_.start).getOrElse(0)

  def remove(i: Int): Unit = $ match {
    case l: ListBuffer[Node] => l.remove(i)
    case _                   =>
  }

  def isComment: Boolean = rawValue.exists {
    case _: Comment => true
    case _          => false
  }

  def nonComment: Boolean = !isComment

  def isInt: Boolean = rawValue.exists {
    case _: Int => true
    case _      => false
  }

  def isDouble: Boolean = rawValue.exists {
    case _: Double => true
    case _         => false
  }

  def isString: Boolean = rawValue.exists {
    case _: String => true
    case _         => false
  }
  
  def valueEquals(value: String | Int | Double | Boolean | ListBuffer[Node]): Boolean = {
    rawValue match {
      case Some(v) => v == value
      case None     => false
    }
  }
}
