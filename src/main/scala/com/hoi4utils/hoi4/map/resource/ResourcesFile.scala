package com.hoi4utils.hoi4.map.resource

import com.hoi4utils.hoi4.map.resource.ResourcesFile
import com.hoi4utils.hoi4.map.resource.ResourcesFile.logger
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.Node
import com.hoi4utils.script.{CollectionPDX, PDXReadable, PDXSupplier}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

class ResourcesFile(file: File = null) extends CollectionPDX[ResourceDefinition](ResourcesFile.pdxSupplier(), "resources") with LazyLogging {
  private var _resourcesFile: Option[File] = None

  /* init */
  file match
    case null => // create empty resource
    case _ =>
      require(file.exists && file.isFile, s"Resource file $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)

  def setFile(file: File): Unit = {
    _resourcesFile = Some(file)
  }

  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty) {
      expression.$ match {
        case l: ListBuffer[Node] =>
          loadPDX(l)
        case _ =>
          logger.error("Error loading PDX script: " + expression)
      }
    }
    super.loadPDX(expression)
  }

  override def getPDXTypeName: String = "Resources"
}

object ResourcesFile extends PDXReadable with LazyLogging {
  //  private var _resources: List[Resource] = List()
  private var _resourcesPDX: Option[ResourcesFile] = None

  def read(): Boolean = primaryResourcesFile match
    case Some(file) =>
      _resourcesPDX = Some(new ResourcesFile(file))
      true
    case None =>
      logger.error(s"In ${this.getClass.getSimpleName} - Resources file is not a directory, " +
        s"or it does not exist (No resources file found).")
      false

  override def clear(): Unit = _resourcesPDX = None

  def pdxSupplier(): PDXSupplier[ResourceDefinition] = {
    new PDXSupplier[ResourceDefinition] {
      override def simplePDXSupplier(): Option[Node => Option[ResourceDefinition]] = {
        Some((expr: Node) => {
          Some(new ResourceDefinition(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[ResourceDefinition]] = {
        Some((expr: Node) => {
          Some(new ResourceDefinition(expr))
        })
      }
    }
  }

  def list: List[ResourceDefinition] = _resourcesPDX match
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

  private def hoi4ResourcesFileValid = HOIIVFiles.HOI4.resources_file.exists && HOIIVFiles.HOI4.resources_file.isFile

  private def modResourcesFileInvalid = !HOIIVFiles.Mod.resources_file.exists || HOIIVFiles.Mod.resources_file.isDirectory
}