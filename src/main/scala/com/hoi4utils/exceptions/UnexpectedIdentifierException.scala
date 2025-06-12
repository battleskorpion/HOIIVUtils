package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class UnexpectedIdentifierException(message: String) extends Exception(message) {

  def this(exp: Node, clazz: Class[?]) =
    this(s"Unexpected identifier: ${exp.name}, in class \"${clazz.getSimpleName}\". ")

  def this(exp: Node, clazz: Class[?], referencePDXIdentifiers: String) =
    this(s"Unexpected identifier: ${exp.name}, in class \"${clazz.getSimpleName}\" with valid identifiers: $referencePDXIdentifiers. ")

  def this(e: Exception, clazz: Class[?], file: String) =
    this(s"Unexpected identifier in class \"${clazz.getSimpleName}\" while reading file: $file. ${e.getMessage} \n ${e.getStackTrace.mkString("\n")}")
}