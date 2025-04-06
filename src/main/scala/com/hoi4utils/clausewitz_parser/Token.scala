package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz_parser.TokenType.TokenType
import scala.util.matching.Regex

object Token {
  val EOF_INDICATOR = "$"

  val tokenRegex: Map[TokenType, Regex] = Map(
    TokenType.whitespace -> "\\s+".r,         // <-- NEW
    
    TokenType.comment -> "#.*".r, // Nullifies Comments  // prev: "#.*(?:[\r\n]|$)"
    
    TokenType.operator -> "[={}<>;,]|>=|<=|!=".r,         // Seperates Operators

    //TokenType.number -> Regex("-?\\d*\\.\\d+|-?\\d+|0x\\d+"),  // Seperates Numbers

    TokenType.float -> "-?\\d*\\.\\d+".r,

    TokenType.int -> "-?(?:\\d+|0x[0-9a-fA-F]+)".r,

    TokenType.symbol -> "[A-Za-z0-9_:\\.@\\[\\]\\-?^/\\u00A0-\\u024F]+".r, // Symbol

    TokenType.string -> "\"(\\\\.|[^\"])*\"".r,           // Seperates Double Quotes

    TokenType.eof -> "\\$".r
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
        case TokenType.int =>
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
}
