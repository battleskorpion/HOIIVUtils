package com.map

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.parser.Node
import com.hoi4utils.script.PDXSupplier
import com.typesafe.scalalogging.LazyLogging
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import scala.collection.mutable.ListBuffer

/* * StateCategories.scala
 * This file is part of the HOI4 Map Editor project.
 * It provides functionality to read and manage state categories from files.
 */
object StateCategories extends LazyLogging:

  private val _stateCategoryFiles: ListBuffer[StateCategoryFile] = ListBuffer()
  var stateCategoriesErrors: ListBuffer[String] = ListBuffer().empty

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
        stateCategoriesErrors.addOne(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.HOI4.resources_file} is not a directory, " +
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