//package com.hoi4utils.hoi4.map.state
//
//import com.hoi4utils.exceptions.UnexpectedIdentifierException
//import com.hoi4utils.parser.Node
//import com.hoi4utils.script.seq.CollectionPDX
//
//import java.io.File
//
//class StateCategoryFile(file: File = null) extends CollectionPDX[StateCategoryDefinition](StateCategories.pdxSupplier(), "state_categories") {
//  private var _stateCategoryFile: Option[File] = None
//
//  /* init */
//  file match
//    case null => // create empty StateCategoryFile
//    case _ =>
//      require(file.exists && file.isFile, s"StateCategoryFile $file does not exist or is not a file.")
//      loadPDX(file)
//      setFile(file)
//
//  def setFile(file: File): Unit = {
//    _stateCategoryFile = Some(file)
//  }
//
//  @throws[UnexpectedIdentifierException]
//  override def loadPDX(expression: Node[?], file: Option[File]): Unit = {
//    // TODO TODO 
//    () 
////    super.loadPDX(expression, file)
//  }
//
//  override def getPDXTypeName: String = "State Category"
//}
