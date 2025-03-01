package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

/**
 *
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * @tparam T
 */
trait PDXScript[T] {
  def set(obj: T): T

  /**
   * Set the node value to the given value.
   *
   * Obviously, if T != to the type of the value,
   * the new value may not be semantically correct. However, we need to allow this for
   * flexibility i.e. setting a PDX of type double with an int value, and this also matches
   * the underlying node class functionality.
   */
  def setNode(value: T | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit

  /**
   * Set the node value to the given expression.
   * @param expression
   * @throws
   */
  @throws[UnexpectedIdentifierException]
  def set(expression: Node): Unit

  /**
   * Get the value of the PDX script.
   * @return
   */
  def get(): Option[T]

  /**
   * Get the node of the PDX script.
   * @return
   */
  def getNode : Option[Node]

  /**
   * Load the PDX script represented by the given expression.
   * @param expression
   * @throws
   */
  @throws[UnexpectedIdentifierException]
  def loadPDX(expression: Node): Unit

  /**
   * Load the PDX script represented by the given expressions.
   * @param expressions
   */
  def loadPDX(expressions: Iterable[Node]): Unit

  //void loadPDX(@NotNull File file);//void loadPDX(@NotNull File file);

  /**
   * Checks if the given node matches any valid identifier for this PDX script.
   *
   * @param node The node to validate. Checks the name of the node against the list of valid identifiers.
   * @return True if the node matches a valid identifier, false otherwise.
   */
  def isValidIdentifier(node: Node): Boolean

  def clearNode(): Unit

  /**
   * Set the node value and any relevant pdx properties to null.
   */
  def setNull(): Unit
    
  def loadOrElse(exp: Node, value: T): Unit

  def toScript: String

  /**
   *
   * @param other
   * @return
   *
   * @note This method can not have a targetName annotation because, equals has to be a separate method, this
   *       method can't be overridden
   */
  final def == (other: PDXScript[?]): Boolean = this.equals(other)

  /**
   * Compare this PDXScript to another PDXScript.
   * @param other The other PDXScript to compare to.
   * @return True if the two PDXScripts are considered equal, false otherwise.
   */
  def equals(other: PDXScript[?]): Boolean

  /**
   * Get the value of the PDX script, or the given value if the PDX script is undefined or has an incompatible type. 
   * @param elseValue
   * @return
   */
  def getOrElse(elseValue: T): T
  
  def isUndefined: Boolean

  def isDefined: Boolean

  def getPDXIdentifier: String
}
