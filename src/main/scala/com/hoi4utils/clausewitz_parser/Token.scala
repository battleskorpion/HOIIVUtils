package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz_parser.TokenType.TokenType

import scala.collection.mutable
import scala.collection.mutable.LinkedHashMap
import scala.util.matching.Regex

object Token {
  val EOF_INDICATOR = "$"

  // im being lazy and need ordered
  val tokenRegex: mutable.LinkedHashMap[TokenType, Regex] = mutable.LinkedHashMap(
    TokenType.comment -> "#.*".r, // Nullifies Comments  // prev: "#.*(?:[\r\n]|$)"

    //TokenType.number -> Regex("-?\\d*\\.\\d+|-?\\d+|0x\\d+"),  // Seperates Numbers

    TokenType.string -> "\"(\\\\.|[^\"])*\"".r,           // Seperates Double Quotes

    TokenType.operator -> "[={}<>;,]|>=|<=|!=".r,         // Seperates Operators

    TokenType.float -> "-?\\d*\\.\\d+".r,

    TokenType.int -> "-?(?:\\d+|0x[0-9a-fA-F]+)".r,

    TokenType.symbol -> "[A-Za-z0-9_:\\.@\\[\\]\\-?^/\\u00A0-\\u024F]+".r, // Symbol

    TokenType.eof -> "\\$".r,

    TokenType.whitespace -> "\\s+".r,         // <-- NEW
  )
}

/**
 * Token class remains mostly the same; it will now recognize whitespace 
 * as a distinct TokenType if it matches the regex above.
 */
class Token {
  var value: String = _
  var `type`: TokenType = _
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
    for ((key, regex) <- Token.tokenRegex) {
      key match {
        case TokenType.int | TokenType.float =>
          // For int tokens, ensure the entire value is matched
          regex.findFirstMatchIn(value) match {
            case Some(m) if m.start == 0 && m.end == value.length => return key
            case _ => // not a complete match, keep looking
          }
        case _ =>
          // For other token types, accept a match anywhere
          if (regex.findFirstIn(value).isDefined) return key
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
        this.value == that.value && this.`type` == that.`type` && this.start == that.start
      case _ => false
    }
  }
}
