package com.hoi4utils.hoi4mod.map.resource

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, ParserException}
import com.hoi4utils.script.*
import com.typesafe.scalalogging.LazyLogging
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Represents a valid resource and its quantity.
 *
 * @param id
 * @param amt  // quantity of resource being represented
 */
class Resource(id: String) extends DoublePDX(id) with PDXType[ResourceDefinition](id, () => ResourcesFile.list) {
  /* init */
  require(isValidPDXTypeIdentifier(id), s"Invalid resource identifier: $id. Expected one of: ${Resource.resourceIdentifiers.mkString(", ")}")

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  def this(id: String, amt: Double) = {
    this(id)
    this.set(amt)
  }

  override def handleNodeValueTypeError(node: Node, exception: Exception): Unit = {
    val msg = s"In ${this.getClass.getSimpleName} - Error parsing resource '${node.name}': ${exception.getMessage}"
    Resource.resourceErrors += msg
    super.handleNodeValueTypeError(node, exception)
  }

  override def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit =
    val msg = s"In ${this.getClass.getSimpleName} - Unexpected identifier '${node.identifier.getOrElse("none")}' in resource '${node.name}': ${exception.getMessage}"
    Resource.resourceErrors += msg
    super.handleUnexpectedIdentifier(node, exception)

  override def handleParserException(file: File, exception: Exception): Unit =
    val msg = s"In ${this.getClass.getSimpleName} - Error parsing resource in file '${file.getPath}': ${exception.getMessage}"
    Resource.resourceErrors += msg
    super.handleParserException(file, exception)

  infix def isResource(resource: Resource): Boolean = this.name == resource.name

  infix def isResource(identifier: String): Boolean = this.name == name

  def name: String = id

  //  def percentOfGlobal: Double = getOrElse(0) / State.resourcesOfStates.get(identifier).get
  def percentOfGlobal(using globalResources: List[Resource]): Double =
    val global = globalAmt
    if global == 0.0 then 0.0 else amt / global

  def globalAmt(using globalResources: List[Resource]): Double =
    globalResources.view
      .filter(_ isResource name)
      .map (_.amt)
      .sum

  def amt: Double = getOrElse(0)

  def amt_=(value: Double): Unit = set(value)
}

object Resource {
  private var resourceIdentifiers = Array("aluminium", "chromium", "oil", "rubber", "steel", "tungsten") // default: aluminium, chromium, oil, rubber, steel, tungsten todo load in resources if modified.
  var resourceErrors: ListBuffer[String] = ListBuffer().empty

  private def setResourceIdentifiers(identifiers: Array[String]): Unit = {
    Resource.resourceIdentifiers = identifiers
  }

  /**
   * Returns a list of all valid resources.
   * @return List of all valid resources.
   */
  def newList(): List[Resource] = {
    resourceIdentifiers.map(name => new Resource(name)).toList
  }
  
  def apply(): PDXSupplier[Resource] = {
    new PDXSupplier[Resource] {
      override def simplePDXSupplier(): Option[Node => Option[Resource]] = {
        Some((expr: Node) => {
          Some(new Resource(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[Resource]] = {
        Some((expr: Node) => {
          Some(new Resource(expr))
        })
      }
    }
  }
}