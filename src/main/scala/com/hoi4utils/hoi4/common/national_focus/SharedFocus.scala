package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.*
import zio.ZIO

import java.io.File

class SharedFocus(focusTree: FocusTree) extends Focus(focusTree, pdxIdentifier = "shared_focus") {

  val offset = Offset()

  /* init */
  for {
    pseudoTree <- PseudoSharedFocusTree()
    _ <- ZIO.succeed(pseudoTree.addNewFocus(this))
  } yield ()

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this
    ).addInfo("focusId", id.str)
    focusErrors += pdxError

  class Offset extends PointPDX("offset") {
    val trigger = TriggerPDX()

    override def childScripts: collection.mutable.Seq[? <: PDXScript[?]] = super.childScripts ++ List(trigger)
  }

}
