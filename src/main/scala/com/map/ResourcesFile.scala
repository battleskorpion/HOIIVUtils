package com.map

import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import com.hoi4utils.script.{CollectionPDX, PDXSupplier}
import com.map.ResourcesFile.resourcesFileErrors
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

class ResourcesFile(var _resourcesFile: File) extends CollectionPDX[ResourceDef](ResourcesFile.pdxSupplier(), "resources") {
  if (!_resourcesFile.exists) {
    logger.error(s"Resources file does not exist: $_resourcesFile")
    throw new IllegalArgumentException(s"File does not exist: $_resourcesFile")
  }
  
  loadPDX(_resourcesFile)

  /**
   * 
   * @param expression The expression to load.
   */
  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty) {
      expression.$ match {
        case l: ListBuffer[Node] =>
          try loadPDX(l)
          catch {
            case e: UnexpectedIdentifierException =>
              resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Unexpected identifier:\n  : ${e.getMessage}")
            case e: NodeValueTypeException =>
              resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Node value type exception:\n  : ${e.getMessage}")
            case e: IllegalStateException =>
              resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Illegal State exception:\n  : ${e.getMessage}")
          }
        case _ =>
          resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Expected a list of nodes, but found: ${expression.$}")
      }
    }
    try super.loadPDX(expression)
    catch {
      case e: UnexpectedIdentifierException =>
        resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Unexpected identifier:\n  : ${e.getMessage}")
      case e: NodeValueTypeException =>
        resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Node value type exception:\n  : ${e.getMessage}")
      case e: IllegalStateException =>
        resourcesFileErrors.addOne(s"[${getClass.getSimpleName}] Illegal State exception:\n  : ${e.getMessage}")
    }
  }

  override def getPDXTypeName: String = "Resources"
}

object ResourcesFile extends PDXReadable with LazyLogging {


  private var _resourcesPDX: Option[ResourcesFile] = None
  val resourcesFileErrors: ListBuffer[String] = ListBuffer.empty

  def read(): Boolean =
    findResourcesFile() match
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

  /**
   * Returns the list of resources defined in the resources file.
   * @throws IllegalStateException if the resources file has not been loaded yet.
   * @return a list of ResourceDef objects representing the resources.
   */
  @throws[IllegalStateException]
  def list: List[ResourceDef] = {
    _resourcesPDX match {
      case Some(resources) => resources.toList
      case None => throw new IllegalStateException("ResourcesFile has not been loaded yet.")
    }
  }

  override def name: String = {
    this.getClass.getSimpleName
  }
}