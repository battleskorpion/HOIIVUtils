package com.map

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.parser.Node
import com.hoi4utils.script.PDXSupplier
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import scala.collection.mutable.ListBuffer


object StateCategories {
  val logger: Logger = LogManager.getLogger(classOf[this.type])

  private val _stateCategoryFiles: ListBuffer[StateCategoryFile] = ListBuffer()

  def read(): Unit = {
    clear()

    var stateCategoryDirectory: Option[File] = None
    if (!HOIIVFiles.Mod.state_category_dir.exists || !HOIIVFiles.Mod.state_category_dir.isDirectory) {
      if (HOIIVFiles.HOI4.state_category_dir.exists && HOIIVFiles.HOI4.state_category_dir.isDirectory) {
        stateCategoryDirectory = Some(HOIIVFiles.HOI4.state_category_dir)
      }
    } else {
      stateCategoryDirectory = Some(HOIIVFiles.Mod.state_category_dir)
    }

    stateCategoryDirectory match {
      case Some(dir) =>
        //_resourcesPDX = Some(new Resources(dir))
        stateCategoryDirectory.filter(_.getName.endsWith(".txt")).foreach { f =>
          _stateCategoryFiles += new StateCategoryFile(f)
        }
      case None =>
        logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.HOI4.resources_file} is not a directory, " +
          s"or it does not exist (No resources file found).")
    }
  }

  def pdxSupplier(): PDXSupplier[StateCategoryDef] = {
    new PDXSupplier[StateCategoryDef] {
      override def simplePDXSupplier(): Option[Node => Option[StateCategoryDef]] = {
        Some((expr: Node) => {
          Some(new StateCategoryDef(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[StateCategoryDef]] = {
        Some((expr: Node) => {
          Some(new StateCategoryDef(expr))
        })
      }
    }
  }

  def list: List[StateCategoryDef] = {
    _stateCategoryFiles.flatMap(_.toList).toList
  }

  def clear(): Unit = {
    _stateCategoryFiles.clear()
  }
}