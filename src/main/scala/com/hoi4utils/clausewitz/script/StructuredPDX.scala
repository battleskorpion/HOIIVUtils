package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import java.util._


abstract class StructuredPDX extends AbstractPDX[util.List[Node]](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) {
    this()
  }

  def this(pdxIdentifiers: util.List[String]) {
    this()
  }

  protected def childScripts: util.Collection[_ <: PDXScript[_]]

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException] 
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    // then load each sub-PDXScript
    if (!value.isList) throw new NodeValueTypeException(expression, "list") // todo check through schema
//    import scala.collection.JavaConversions._
//    for (pdxScript <- obj) {
//      pdxScript.loadPDX(value.list)
//    }
  }

  override def loadPDX(expression: Node): Unit = {
    if (expression.name == null) {
      // todo check through schema? 
      if (expression.value.isList) loadPDX(expression.$)
      else System.out.println("Error loading PDX script: " + expression)
      return
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
  def getPDXProperty(identifier: String): PDXScript[_] = {
    import scala.collection.JavaConversions._
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
  def getPDXProperty(identifiers: util.List[String]): PDXScript[_] = {
    import scala.collection.JavaConversions._
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
  @SuppressWarnings(Array("unchecked")) def getPDXPropertyOfType[R](identifier: String): PDXScript[R] = {
    import scala.collection.JavaConversions._
    for (pdx <- childScripts) {
      if (pdx.getPDXIdentifier == identifier) return pdx.asInstanceOf[PDXScript[R]]
    }
    null
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXPropertyOfType[R](identifiers: util.List[String]): PDXScript[R] = {
    import scala.collection.JavaConversions._
    for (identifier <- identifiers) {
      val pdx = getPDXPropertyOfType(identifier)
      if (pdx != null) return pdx
    }
    null
  }

  def pdxProperties: util.Collection[_ <: PDXScript[_]] = {
    val scripts = childScripts
    if (scripts == null) return null
    Collections.unmodifiableCollection(scripts)
  }
}
