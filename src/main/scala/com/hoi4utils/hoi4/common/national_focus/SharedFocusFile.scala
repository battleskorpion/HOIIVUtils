package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import dotty.tools.sjs.ir.Trees.JSBinaryOp.&&

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SharedFocusFile(_file: Option[File])(manager: FocusTreeManager) extends StructuredPDX with HeadlessPDX with PDXFile {

  /* PDX attributes */
  val sharedFocuses: MultiPDX[SharedFocus] = MultiPDX[SharedFocus](None, Some(() => SharedFocus(PseudoSharedFocusTree(manager))), "shared_focus")

  // File-level errors (parse errors, etc.)
  var fileErrors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]


  /* init */
  _file match
    case Some(f) =>
      require(f.exists && f.isFile, s"Shared focuses file $f does not exist or is not a file.")
      loadPDX(f)
      // Collect errors from all shared focuses in this file
      collectAndRegisterSharedFocusErrors()
    case None =>

  def this(file: File)(manager: FocusTreeManager) = this(Some(file))(manager)

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this
    )
    fileErrors += pdxError

  /**
   * Collects all shared focus errors and registers them with FocusTreeManager
   */
  def collectAndRegisterSharedFocusErrors(): Unit =
    val focusErrorGroups = ListBuffer[FocusErrorGroup]()

    // Add file-level errors as a special "File Errors" group
    if fileErrors.nonEmpty then
      focusErrorGroups += new FocusErrorGroup("File Errors", fileErrors)

    // Add errors from individual shared focuses
    sharedFocuses.foreach { focus =>
      if focus.focusErrors.nonEmpty then
        focusErrorGroups += new FocusErrorGroup(focus.id.str, focus.focusErrors)
    }

    // If there are any errors, register them
    if focusErrorGroups.nonEmpty then
      val fileName = _file.map(_.getName).getOrElse("Shared Focuses")
      val treeErrorGroup = new com.hoi4utils.script.FocusTreeErrorGroup(s"[Shared Focuses] $fileName", focusErrorGroups)
      manager.focusTreeErrors += treeErrorGroup


  override protected def childScripts: mutable.Seq[? <: PDXScript[?]] = ListBuffer(sharedFocuses)

  override def getFile: Option[File] = _file

}
