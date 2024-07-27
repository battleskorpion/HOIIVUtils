package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import scala.collection.mutable.ListBuffer

abstract class StructuredPDX(pdxIdentifiers: String*) extends AbstractPDX[ListBuffer[Node]](pdxIdentifiers*) {
  def this(pdxIdentifiers: List[String]) = {
    this(pdxIdentifiers*)
  }

  protected def childScripts: ListBuffer[? <: PDXScript[?]]

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    // then load each sub-PDXScript
    if (!value.isInstanceOf[ListBuffer]) throw new NodeValueTypeException(expression, "list") // todo check through schema
//    import scala.collection.JavaConversions._
//    for (pdxScript <- obj) {
//      pdxScript.loadPDX(value.list)
//    }
  }

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
        System.out.println("Error loading PDX script:" + e.getMessage + "\n\t" + expression)
    }
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXProperty(identifier: String): PDXScript[?] = {
    import scala.collection.BuildFrom.buildFromString
    for (pdx <- childScripts) {
      if (pdx.getPDXIdentifier == identifier) return pdx
    }
    null
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXProperty(identifiers: List[String]): PDXScript[?] = {
    for (identifier <- identifiers) {
      val pdx = getPDXProperty(identifier)
      if (pdx != null) return pdx
    }
    null
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXPropertyOfType[R](identifier: String): PDXScript[R] = {
    import scala.collection.BuildFrom.buildFromString
    for (pdx <- childScripts) {
      pdx match {
        case pdxScript: PDXScript[R] =>
          if (pdxScript.getPDXIdentifier == identifier) return pdxScript
        case _ =>
      }
    }
    null.asInstanceOf[PDXScript[R]]
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXPropertyOfType[R](identifiers: List[String]): PDXScript[R] = {
    for (identifier <- identifiers) {
      val pdx = getPDXPropertyOfType(identifier)
      pdx match {
        case pdxScript: PDXScript[R] =>
          return pdxScript
        case _ =>
      }
    }
    null
  }

  def pdxProperties: Iterable[? <: PDXScript[?]] = {
    val scripts = childScripts
    scripts match {
      case null => null
      case _ => scripts
    }
  }
}
