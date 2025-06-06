package com.map

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node
import com.hoi4utils.script.CollectionPDX

import java.io.File

class StateCategoryFile extends CollectionPDX[StateCategoryDef](StateCategories.pdxSupplier(), "state_categories") {
  private var _stateCategoryFile: Option[File] = None

  /* init */
  def this(file: File) = {
    this()
    if (!file.exists) {
      logger.error(s"State Category file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
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
