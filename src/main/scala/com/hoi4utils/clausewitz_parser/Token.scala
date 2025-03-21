package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz_parser.TokenType.TokenType

import scala.util.matching.Regex

/**
 * Adapted partially from <a href="https://github.com/herbix/hoi4modutilities/blob/master/src/hoiformat/hoiparser.ts">hoiparser.ts</a>
 * from repo <a href="https://github.com/herbix/hoi4modutilities">herbix/hoi4modutilities</a>
 *
 */
object Token {
  val EOF_INDICATOR = "$"

  val tokenRegex: Map[TokenType, Regex] = Map(
    TokenType.comment -> Regex("#.*"), // Nullifies Comments  // prev: "#.*(?:[\r\n]|$)"

    TokenType.symbol -> Regex("(?:\\d+\\.)?[a-zA-Z_@\\[\\]][\\w:.@\\[\\]\\-?^/\\u00A0-\\u024F]*"), // Symbol

    TokenType.operator -> Regex("[={}<>;,]|>=|<=|!="),         // Seperates Operators

    TokenType.string -> Regex("\"(\\\\.|[^\"])*\""),           // Seperates Double Quotes

    //TokenType.number -> Regex("-?\\d*\\.\\d+|-?\\d+|0x\\d+"),  // Seperates Numbers

    TokenType.float -> "-?\\d*\\.\\d+".r,

    TokenType.int -> "-?(?:\\d+|0x[0-9a-fA-F]+)".r,

    TokenType.eof -> "\\$".r
  )
}

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

  /**
   * Determine token type based on token regex map
   */
  private def determineTokenType(value: String): TokenType = {
    for ((key, regex) <- Token.tokenRegex) {
      key match {
        case TokenType.int =>
          // For int tokens, ensure the entire value is matched
          regex.findFirstMatchIn(value) match {
            case Some(m) if m.start == 0 && m.end == value.length =>
              return key
            case _ => // not a complete match, continue
          }
        case _ =>
          // For other token types, accept a match anywhere
          if (regex.findFirstIn(value).isDefined)
            return key
      }
    }
    TokenType.unknown
  }

  def length: Int = this.value.length

  override def toString: String = this.value

  def isNumber: Boolean = TokenType.isNumeric(`type`)
}
