package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

/**
 *
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * @tparam V the type of the value that this PDX script represents, e.g. String, Int, Double, ListBuffer[Node], etc.
 */
trait PDXScript[V] extends Cloneable with LazyLogging {
  
  def set(obj: V): V

  /**
   * Set the node value to the given value.
   *
   * Obviously, if T != to the type of the value,
   * the new value may not be semantically correct. However, we need to allow this for
   * flexibility i.e. setting a PDX of type double with an int value, and this also matches
   * the underlying node class functionality.
   */
  protected def setNode(value: V | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit

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
  def value: Option[V]

  /**
   * Get the node of the PDX script.
   * @return
   */
  def getNode : Option[Node]

  def getNodes: List[Node]

  /**
   * Load the PDX script represented by the given expression.
   * 
   * @param expression The expression to load.
   * @throws UnexpectedIdentifierException if the expression does not match any valid identifier for this PDX script.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  def loadPDX(expression: Node): Unit

  /**
   * Load the PDX script represented by the given expressions.
   * @param expressions
   */
  def loadPDX(expressions: Iterable[Node]): Iterable[Node]

  //void loadPDX(@NotNull File file);//void loadPDX(@NotNull File file);

  /**
   * Checks if the given node matches any valid identifier for this PDX script.
   *
   * @param node The node to validate. Checks the name of the node against the list of valid identifiers.
   * @return True if the node matches a valid identifier, false otherwise.
   */
  def isValidIdentifier(node: Node): Boolean

  /**
   * Checks if the given identifier matches any valid identifier for this PDX script.
   * 
   * @param identifier The identifier to validate.
   * @return True if the identifier matches a valid identifier, false otherwise.
   */
  def isValidID(identifier: String): Boolean = {
    pdxIdentifier.equals(identifier)
  }

  def clearNode(): Unit

  /**
   * Set the node value and any relevant pdx properties to null.
   */
  def setNull(): Unit
    
  def loadOrElse(exp: Node, value: V): Unit

  def updateNodeTree(): Unit
  
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
  def getOrElse(elseValue: V): V
  
  def isUndefined: Boolean

  def isDefined: Boolean

  def pdxIdentifier: String

  /**
   * A custom clone method for PDXScript.
   *
   * This performs a shallow clone (via super.clone) and then explicitly resets fields
   * that should remain shared between the original and the clone (for example, childScripts).
   */
  override def clone(): AnyRef = {
    val cloned = super.clone().asInstanceOf[PDXScript[V]]
    cloned
  }
}

object PDXScript {
  def allPDXFilesInDirectory(directory: File): List[File] = {
    if (directory.isFile) List(directory)
    else directory.listFiles().filter(_.isFile).filter(_.getName.endsWith(".txt")).toList
  }
  
}
