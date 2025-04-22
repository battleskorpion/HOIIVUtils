package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node


class UnexpectedIdentifierException extends Exception {
  def this(message: String) {
    this()
    super (message)
  }

  def this(exp: Node) {
    this()
    super ("Unexpected identifier: " + exp.name)
  }
}
