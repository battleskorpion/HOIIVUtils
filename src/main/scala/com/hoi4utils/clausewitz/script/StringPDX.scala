package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.{Node}

import scala.annotation.targetName


class StringPDX(pdxIdentifiers: List[String]) extends AbstractPDX[String](pdxIdentifiers)
  with Comparable[StringPDX] {
  
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    this.node.get.$ match {
      case _: String =>
      case _ => throw new NodeValueTypeException(this.node.get, this.getClass)
    }
  }
  
  override def set(s: String): String = {
    this.node match {
      case Some(node) => node.setValue(s)
      case None => this.node = Some(Node(pdxIdentifiers.head, "=", s))
    }
    s
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case other: StringPDX =>
        if (this.node.isEmpty || other.node.isEmpty) {
          return false
        }
        node.get.equals(other.node.get)
      case _ => false
    }
  }

  def nodeEquals(s: String): Boolean = {
    this.node.nonEmpty && node.get.$.equals(s)
  }

  override def compareTo(o: StringPDX): Int = {
    (this.value, o.value) match {
      case (Some(str), Some(o)) => str.compareTo(o)
      case (Some(str), None) => 1
      case (None, Some(o)) => -1
      case (None, None) => 0
    }
  }

  def str: String = {
    value.getOrElse("")
  }

  override def toString: String = {
    this.node match {
      case Some(node) => node.toString
      case None => "StringPDX[identifiers: " + pdxIdentifiers.mkString(", ") + "]"
    }
  }

  /**
   * Checks the value of the script is equal to the given value.
   *
   * @param other
   * @return
   */
  @targetName("getEquals")
  def @==(other: String): Boolean = value match {
    case Some(v) => v.equals(other)
    case None => false
  }

  /**
   * Checks if the value of the script is not equal to the given value.
   *
   * @param other
   * @return
   */
  @targetName("getNotEquals")
  def @!=(other: String): Boolean = !(this @== other)

  /**
   * Sets the value of the script to the given value.
   *
   * @param other
   */
  def @=(other: String): Unit = set(other)

  /**
   * Sets the value of the script to the value of the given script.
   *
   * @param other
   */
  def @=(other: PDXScript[String]): Unit = other.value match {
    case Some(v) => set(v)
    case None => setNull()
  }
}
