package com.hoi4utils.clausewitz_parser

/**
 * clausewitz token types
 */
object TokenType extends Enumeration {
  type TokenType = Value
  val comment, symbol, operator, string, `float`, `int`, eof, unknown = Value

  def isNumeric(tokenType: TokenType): Boolean = tokenType match {
    case `float` | `int` => true
    case _ => false
  }
}
