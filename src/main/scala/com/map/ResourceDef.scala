package com.map

import com.hoi4utils.parser.Node
import com.hoi4utils.script.{DoublePDX, IntPDX, PDXScript, StructuredPDX}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private[map] class ResourceDef(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) {
  final var iconFrame = new IntPDX("icon_frame")
  final var cic = new DoublePDX("cic")
  final var convoys = new DoublePDX("convoys")

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(iconFrame, cic, convoys)
  }
}