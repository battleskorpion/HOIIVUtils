package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class NodeValueTypeException(message: String, cause: Throwable = null)
  extends Exception(message, cause)

object NodeValueTypeException:
  def apply(message: String): NodeValueTypeException =
    new NodeValueTypeException(message)

  def apply(node: Node): NodeValueTypeException =
    new NodeValueTypeException(s"Invalid node value type: ${node.name}")

  def apply(node: Node, expected: String, received: String): NodeValueTypeException =
    new NodeValueTypeException(s"Invalid node value type of expression: ${node.name},\n    Expected: $expected\n    Received: $received")