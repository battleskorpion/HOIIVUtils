package com.hoi4utils.parser

import com.hoi4utils.parser.TokenType.TokenType

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.util.matching.Regex

object Token {
  val EOF_INDICATOR = "$"

  // im being lazy and need ordered
  val tokenRegex: mutable.LinkedHashMap[TokenType, Regex] = mutable.LinkedHashMap(
    TokenType.comment -> "#.*".r,
    TokenType.string -> "\"(\\\\.|[^\"])*\"".r,
    TokenType.operator -> "[={}<>;,]|>=|<=|!=".r,
    TokenType.float -> "-?\\d*\\.\\d+".r,
    TokenType.int -> "-?(?:\\d+|0x[0-9a-fA-F]+)".r,
    TokenType.symbol -> "[A-Za-z0-9_:\\.@\\[\\]\\-?^/\\u00A0-\\u024F]+".r,
    TokenType.eof -> "\\$".r,
    TokenType.whitespace -> "\\s+".r,
  )

  private val floatPattern = tokenRegex(TokenType.float)
  private val intPattern = tokenRegex(TokenType.int)
}

/**
 * Token class remains mostly the same; it will now recognize whitespace 
 * as a distinct TokenType if it matches the regex above.
 */
class Token {
  var value: String = uninitialized
  var `type`: TokenType = uninitialized
  var start = 0

  def this(value: String, start: Int, `type`: TokenType) = {
    this()
    this.value = value
    this.start = start
    this.`type` = `type`
  }

  def this(value: String, start: Int) = {
    this()
    this.value = value
    this.start = start
    this.`type` = determineTokenType(value)
  }

  private def determineTokenType(value: String): TokenType = {
    if (value.length == 1) {
      value.charAt(0) match {
        case '=' | '{' | '}' | '<' | '>' | ';' | ',' => return TokenType.operator
        case '$' => return TokenType.eof
        case _ => // continue to regex matching
      }
    }

    if (value.length == 2) {
      value match {
        case ">=" | "<=" | "!=" => return TokenType.operator
        case _ => // continue to regex matching
      }
    }

    // Check numbers first with direct pattern matching (avoiding loop overhead)
    Token.intPattern.findFirstMatchIn(value) match {
      case Some(m) if m.start == 0 && m.end == value.length => return TokenType.int
      case _ =>
    }

    Token.floatPattern.findFirstMatchIn(value) match {
      case Some(m) if m.start == 0 && m.end == value.length => return TokenType.float
      case _ =>
    }

    // Check remaining types in order of frequency (adjust based on your data)
    for ((tokenType, regex) <- Token.tokenRegex) {
      tokenType match {
        case TokenType.int | TokenType.float => // Already checked above
        case _ =>
          if (regex.findFirstIn(value).isDefined) return tokenType
      }
    }

    TokenType.unknown
  }

  def length: Int = this.value.length
  
  override def toString: String = this.value
  
  def isNumber: Boolean = TokenType.isNumeric(`type`)

  override def equals(other: Any): Boolean = {
    other match {
      case that: Token =>
        this.start == that.start &&
          this.`type` == that.`type` &&
          this.value == that.value  // String comparison last as it's most expensive
      case _ => false
    }
  }
}
