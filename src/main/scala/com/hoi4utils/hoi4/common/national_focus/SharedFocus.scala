package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.*

import java.io.File

class SharedFocus extends Focus(PseudoSharedFocusTree(), pdxIdentifier = "shared_focus") {

  val offset = Offset()

  /* init */
  PseudoSharedFocusTree().addNewFocus(this)

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
