package com.map

import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node
import com.hoi4utils.script.{CollectionPDX, PDXSupplier}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

object ResourcesFile extends PDXReadable with LazyLogging {

  private var _resourcesPDX: Option[ResourcesFile] = None

  def read(): Boolean = {
    var resourcesFile: Option[File] = None
    if (!HOIIVFiles.Mod.resources_file.exists || HOIIVFiles.Mod.resources_file.isDirectory) {
      if (HOIIVFiles.HOI4.resources_file.exists && HOIIVFiles.HOI4.resources_file.isFile) {
        resourcesFile = Some(HOIIVFiles.HOI4.resources_file)
      }
    } else {
      resourcesFile = Some(HOIIVFiles.Mod.resources_file)
    }

    resourcesFile match {
      case Some(file) =>
        _resourcesPDX = Some(new ResourcesFile(file))
        true
      case None =>
        logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.HOI4.resources_file} is not a directory, " +
          s"or it does not exist (No resources file found).")
        false
    }
  }

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

class ResourcesFile extends CollectionPDX[ResourceDef](ResourcesFile.pdxSupplier(), "resources") {
  private var _resourcesFile: Option[File] = None

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