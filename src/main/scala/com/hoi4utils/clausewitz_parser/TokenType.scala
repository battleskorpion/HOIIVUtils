package com.hoi4utils.clausewitz_parser

/**
 * clausewitz token types
 */
object TokenType extends Enumeration {
  type TokenType = Value
  val comment, symbol, operator, string, number, eof, unknown = Value
}
