package com.hoi4utils.parser

class ParserException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(message: String) = {
    this(message, null)
  }

  def this(clazz: Class[?], str: String, file: String) = {
    this(s"[${clazz.getName}] Parser error: $str \n from pdx file: $file")
  }
}
