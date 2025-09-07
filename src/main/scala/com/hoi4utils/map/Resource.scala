package com.hoi4utils.map

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
  
  def name: String = id

  //  def percentOfGlobal: Double = getOrElse(0) / State.resourcesOfStates.get(identifier).get
  def percentOfGlobal(implicit globalResources: List[Resource]): Double = globalResources.filter(r => r.isValidID(name)).map(_.getOrElse(0)).sum // TODO @ skorp 
  
  def amt: Double = getOrElse(0)
  
  def amt_=(value: Double): Unit = set(value)
}

object ResourcesFile extends PDXReadable {
  val logger: Logger = LogManager.getLogger(classOf[ResourcesFile])

  //  private var _resources: List[Resource] = List()
  private var _resourcesPDX: Option[ResourcesFile] = None

  def read(): Boolean = primaryResourcesFile match
    case Some(file) =>
      logger.info(s"Reading resources from ${file.getAbsolutePath}")
      _resourcesPDX = Some(new ResourcesFile(file))
      true
    case None =>
      logger.error(s"In ${this.getClass.getSimpleName} - Resources file is not a directory, " +
        s"or it does not exist (No resources file found).")
      false

  def pdxSupplier(): PDXSupplier[ResourceDef] = {
    new PDXSupplier[ResourceDef] {
      override def simplePDXSupplier(): Option[Node => Option[ResourceDef]] = {
        Some((expr: Node) => {
          Some(new ResourceDef(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[ResourceDef]] = {
        Some((expr: Node) => {
          Some(new ResourceDef(expr))
        })
      }
    }
  }

  def list: List[ResourceDef] = _resourcesPDX match
    case Some(resources) => resources.toList
    case None =>
      logger.warn("Tried to obtain resources list but valid Resources info not loaded.")
      List()
  
  override def name: String = {
    this.getClass.getSimpleName
  }

  // todo move both?
  def primaryResourcesFile: Option[File] = {
    if (modResourcesFileInvalid)
      if (hoi4ResourcesFileValid) Some(HOIIVFiles.HOI4.resources_file)
      else None
    else Some(HOIIVFiles.Mod.resources_file)
  }

  private def hoi4ResourcesFileValid =
    HOIIVFiles.HOI4.resources_file.exists && HOIIVFiles.HOI4.resources_file.isFile

  private def modResourcesFileInvalid =
    !HOIIVFiles.Mod.resources_file.exists || HOIIVFiles.Mod.resources_file.isDirectory

}

class ResourcesFile extends CollectionPDX[ResourceDef](ResourcesFile.pdxSupplier(), "resources") {
  private var _resourcesFile: Option[File] = None

  /* init */
  def this(file: File) = {
    this()
    if (!file.exists) {
      logger.error(s"Resources file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
  }

  def setFile(file: File): Unit = {
    _resourcesFile = Some(file)
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty) {
      expression.$ match {
        case l: ListBuffer[Node] =>
          loadPDX(l)
        case _ =>
          System.out.println("Error loading PDX script: " + expression)
      }
    }
    super.loadPDX(expression)
  }

  override def getPDXTypeName: String = "Resources"
}

private class ResourceDef(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) {
  final var iconFrame = new IntPDX("icon_frame")
  final var cic = new DoublePDX("cic")
  final var convoys = new DoublePDX("convoys")

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(iconFrame, cic, convoys)
  }

}