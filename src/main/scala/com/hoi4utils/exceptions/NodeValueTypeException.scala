package com.hoi4utils.exceptions

import com.hoi4utils.parser.Node


class NodeValueTypeException extends Exception {
  def this(exp: Node, clazz: Class[_]) {
    this()
    super ("[" + clazz.getName + "] Invalid node value type: " + exp.name)
  }

  def this(exp: Node, cause: Throwable, clazz: Class[_]) {
    this()
    super ("[" + clazz.getName + "] Invalid node value type: " + exp.name, cause)
  }

  def this(expression: Node, expected: String, clazz: Class[_]) {
    this()
    super ("[" + clazz.getName + "] Invalid node value type of expression: " + expression.name + ", Expected: " + expected)
  }
}