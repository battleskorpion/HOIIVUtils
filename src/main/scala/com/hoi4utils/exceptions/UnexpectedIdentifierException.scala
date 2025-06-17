package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class UnexpectedIdentifierException(message: String, exception: Exception = null)
  extends Exception(message, exception)

object UnexpectedIdentifierException:
  def apply(node: Node): UnexpectedIdentifierException =
    new UnexpectedIdentifierException(s"Unexpected identifier: ${node.name}", null)

  def apply(node: Node, message: String): UnexpectedIdentifierException =
    new UnexpectedIdentifierException(s"Unexpected identifier: ${node.name},\nMessage: $message", null)