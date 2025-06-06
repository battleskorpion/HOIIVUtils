package com.map

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object Resource {
  private var resourceIdentifiers = Array("aluminium", "chromium", "oil", "rubber", "steel", "tungsten") // default: aluminium, chromium, oil, rubber, steel, tungsten todo load in resources if modified.

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

/**
 * Represents a valid resource and its quantity. 
 * 
 * @param id 
 * @param amt  // quantity of resource being represented
 */
class Resource(id: String) extends DoublePDX(id) with PDXType[ResourceDef](id, () => ResourcesFile.list) {
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

  def sameResource(resource: Resource): Boolean = this.name == resource.name

  def sameResource(identifier: String): Boolean = this.name == name

//  def percentOfGlobal: Double = getOrElse(0) / State.resourcesOfStates.get(identifier).get

  def name: String = id
  
  
  def percentOfGlobal(implicit globalResources: List[Resource]): Double = {
    globalResources
      .filter(r => r.isValidID(name))
      .map(_.getOrElse(0))
      .sum
  }
  
  def amt: Double = getOrElse(0)
}