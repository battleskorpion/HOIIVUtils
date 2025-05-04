package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

class NodeValueTypeException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(message: String) =
    this(message, null)

  def this(exp: Node, clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type: ${exp.name}")

  def this(exp: Node, cause: Throwable, clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type: ${exp.name}", cause)

  def this(exp: Node, expected: String, clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type of expression: ${exp.name}, Expected: $expected")
}
