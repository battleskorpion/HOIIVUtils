package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.HOIIVUtils
import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.{Node, NodeValue}

import scala.collection.mutable.ListBuffer

abstract class StructuredPDX(pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[Node]](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  protected def childScripts: collection.mutable.Iterable[? <: PDXScript[?]]

  /**
   * Sets the current node to the provided expression and processes it based on its type.
   * If the expression is a ListBuffer of nodes, it will load each sub-PDXScript for processing.
   * Otherwise, an exception will be thrown indicating the mismatch in expected node value type.
   *
   * @param expression the node expression to set.
   * @throws UnexpectedIdentifierException if the identifier used in the expression is unexpected.
   * @throws NodeValueTypeException        if the expression is not of type ListBuffer[Node], but was expected to be.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match {
      case l: ListBuffer[Node] =>
        // then load each sub-PDXScript
        for (pdxScript <- childScripts) {
          pdxScript.loadPDX(l)
        }
      case _ =>
        throw new NodeValueTypeException(expression, "list")  // todo check through schema
    }
  }

  override def set(value: ListBuffer[Node]): ListBuffer[Node] = {
    // todo?
    super.setNode(value)
    value
  }
  
  // 
  
  override def loadPDX(expression: Node): Unit = {
    if (expression.name == null) {
      // todo check through schema?
      expression.$ match {
        case l: ListBuffer[Node] =>
          loadPDX(l)
        case _ =>
          System.out.println("Error loading PDX script: " + expression)
      }
    }
    try set(expression)
    catch {
      case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
        // System.out.println("Error loading PDX script:" + e.getMessage + "\n\t" + expression) // todo expression prints entire focus node
        System.out.println("Error loading PDX script:" + e.getMessage)
    }
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXProperty(identifier: String): Option[PDXScript[?]] = {
    for (pdx <- childScripts) {
      if (pdx.pdxIdentifier == identifier) return Some(pdx)
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXProperty(identifiers: List[String]): Option[PDXScript[?]] = {
    for (identifier <- identifiers) {
      val pdx = getPDXProperty(identifier)
      if (pdx.isDefined) return pdx
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXPropertyOfType[R](identifier: String): Option[PDXScript[R]] = {
    for (pdx <- childScripts) {
      pdx match {
        case pdxScript: PDXScript[R] =>
          if (pdxScript.pdxIdentifier == identifier) return Some(pdxScript)
        case _ =>
      }
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXPropertyOfType[R](identifiers: List[String]): Option[PDXScript[R]] = {
    for (identifier <- identifiers) {
      val pdx = getPDXPropertyOfType[R](identifier)
      if (pdx.isDefined) return pdx
    }
    None
  }

  def pdxProperties: Iterable[? <: PDXScript[?]] = {
    val scripts = childScripts
    scripts match {
      case null => null
      case _ => scripts
    }
  }

  override def toScript: String = {
    if (node.isEmpty || node.get.isEmpty) return null

//    val sb = new StringBuilder()
//    sb.append(node.get.identifier)
//    sb.append(" = {\n")
//    for (pdx <- childScripts) {
//      sb.append('\t')
//      sb.append(pdx.toScript)
//    }
//    sb.toString
    // favorable. more in-order as wanted/as was originally.
    node.get.toScript
  }
}
