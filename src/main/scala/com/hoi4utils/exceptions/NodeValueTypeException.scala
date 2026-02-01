package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node

/**
 * Exception thrown when a node's value type does not match the expected type.
 *
 * @param message The detail message.
 * @param cause   The cause of the exception.
 */
class NodeValueTypeException(message: String, cause: Throwable) extends Exception(message, cause):

  def this(message: String) =
    this(message, null)

  def this(exp: Node[?], clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type: ${exp.name}")

  def this(exp: Node[?], cause: Throwable, clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type: ${exp.name}", cause)

  def this(exp: Node[?], expected: String, clazz: Class[?]) =
    this(s"[${clazz.getName}] Invalid node value type of expression: ${exp.name}, Expected: $expected")
