package com.hoi4utils.hoi4.map.resource

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, ParserException, ParsingContext}
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.DoublePDX
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

  def this(node: Node[?]) = {
    this(node.name)
    var file = None
    // TODO TODO
//    loadPDX(node, file)
  }

  def this(id: String, amt: Double) = {
    this(id)
    this.set(amt)
  }

  override def handlePDXError(exception: Exception = null, node: Node[?] = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this
    ).addInfo("resourceName", name)
    Resource.resourceErrors += pdxError

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
  var resourceErrors: ListBuffer[PDXFileError] = ListBuffer().empty

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
      override def simplePDXSupplier(): Option[Node[?] => Option[Resource]] = {
        Some((expr: Node[?]) => {
          Some(new Resource(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node[?] => Option[Resource]] = {
        Some((expr: Node[?]) => {
          Some(new Resource(expr))
        })
      }
    }
  }
}
