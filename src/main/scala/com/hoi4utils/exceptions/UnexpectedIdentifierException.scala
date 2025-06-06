package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class UnexpectedIdentifierException(message: String) extends Exception(message) {

  def this(exp: Node, clazz: Class[?]) =
    this(s"Unexpected identifier: ${exp.name}, in class \"${clazz.getSimpleName}\". ")

  def this(exp: Node, clazz: Class[?], referencePDXIdentifiers: String) =
    this(s"Unexpected identifier: ${exp.name}, in class \"${clazz.getSimpleName}\" with valid identifiers: $referencePDXIdentifiers. ")
}