package com.hoi4utils.clausewitz_parser

import java.util
import java.util.{HashMap, Map}
import java.util.regex.Pattern


/**
 * Adapted partially from <a href="https://github.com/herbix/hoi4modutilities/blob/master/src/hoiformat/hoiparser.ts">hoiparser.ts</a>
 * from repo <a href="https://github.com/herbix/hoi4modutilities">herbix/hoi4modutilities</a>
 *
 */
object Token {
  val tokenRegex = new util.HashMap[TokenType, Pattern]
  val EOF_INDICATOR = "$"

  try tokenRegex.put(TokenType.comment, Pattern.compile("#.*")) // Nullifies Comments  // prev: "#.*(?:[\r\n]|$)"

  tokenRegex.put(TokenType.symbol, Pattern.compile("(?:\\d+\\.)?[a-zA-Z_@\\[\\]][\\w:.@\\[\\]\\-?^/\\u00A0-\\u024F]*")) // Symbol

  tokenRegex.put(TokenType.operator, Pattern.compile("[={}<>;,]|>=|<=|!=")) // Seperates Operators

  tokenRegex.put(TokenType.string, Pattern.compile("\"(\\\\.|[^\"])*\"")) // Seperates Double Quotes

  tokenRegex.put(TokenType.number, Pattern.compile("-?\\d*\\.\\d+|-?\\d+|0x\\d+")) // Seperates Numbers

  tokenRegex.put(TokenType.eof, Pattern.compile(EOF_INDICATOR))

}

class Token {
  var value: String = null
  var `type`: TokenType = null
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
    import scala.collection.JavaConversions._
    for (entry <- Token.tokenRegex.entrySet) {
      if (entry.getValue.matcher(value).matches) return entry.getKey
    }
    TokenType.unknown
  }

  def length: Int = this.value.length

  override def toString: String = this.value
}
