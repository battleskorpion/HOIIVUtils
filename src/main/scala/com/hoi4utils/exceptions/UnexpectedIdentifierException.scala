package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class UnexpectedIdentifierException(message: String) extends Exception(message) {

  def this(exp: Node) =
    this(s"Unexpected identifier: ${exp.name}")
}
