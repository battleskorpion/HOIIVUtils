package com.hoi4utils.clausewitz_parser

class ParserException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) {
    this(message, null)
  }
  
  def this(cause: Throwable) {
    this(null, cause)
  }
}
