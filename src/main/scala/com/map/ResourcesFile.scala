package com.map

import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node
import com.hoi4utils.script.{CollectionPDX, PDXSupplier}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

class ResourcesFile(var _resourcesFile: File) extends CollectionPDX[ResourceDef](ResourcesFile.pdxSupplier(), "resources") {
  if (!_resourcesFile.exists) {
    logger.error(s"Resources file does not exist: $_resourcesFile")
    throw new IllegalArgumentException(s"File does not exist: $_resourcesFile")
  }
  
  loadPDX(_resourcesFile)

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

object ResourcesFile extends PDXReadable with LazyLogging {

  private var _resourcesPDX: Option[ResourcesFile] = None

  def read(): Boolean =
    val resourcesFile = findResourcesFile()

    resourcesFile match
      case Some(file) =>
        _resourcesPDX = Some(ResourcesFile(file))
        true
      case None =>
        logger.error(s"In ${getClass.getSimpleName} - ${HOIIVFiles.HOI4.resources_file} is not a directory, " +
          s"or it does not exist (No resources file found).")
        false

  private def findResourcesFile(): Option[File] =
    val modFile = HOIIVFiles.Mod.resources_file
    val hoiFile = HOIIVFiles.HOI4.resources_file

    Option.when(modFile.exists && !modFile.isDirectory)(modFile)
      .orElse(Option.when(hoiFile.exists && hoiFile.isFile)(hoiFile))

  def clear(): Unit = {
    _resourcesPDX = None
  }

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

  def list: List[ResourceDef] = {
    _resourcesPDX match {
      case Some(resources) => resources.toList
      case None =>
        logger.warn("Tried to obtain resources list but valid Resources info not loaded.")
        List()
    }
  }

  override def name: String = {
    this.getClass.getSimpleName
  }
}