package com.map

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node
import com.hoi4utils.script.CollectionPDX
import com.map.StateCategoryFile.stateCategoryFileErrors

import java.io.File
import scala.collection.mutable.ListBuffer

@throws[IllegalArgumentException]
class StateCategoryFile(file: File = null) extends CollectionPDX[StateCategoryDef](StateCategories.pdxSupplier(), "state_categories") {
  private var _stateCategoryFile: Option[File] = None

  file match {
    case f if f != null && f.exists() && f.isFile =>
      loadPDX(f, stateCategoryFileErrors)
      setFile(f)
    case f if f != null && !f.exists() =>
      throw new IllegalArgumentException(s"State Category file ${f.getName} does not exist.")
    case _ => // do nothing, file is null or not a valid file
  }

  def setFile(file: File): Unit = {
    _stateCategoryFile = Some(file)
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    super.loadPDX(expression)
  }

  override def getPDXTypeName: String = "State Category"
}

object StateCategoryFile:
  var stateCategoryFileErrors: ListBuffer[String] = ListBuffer.empty