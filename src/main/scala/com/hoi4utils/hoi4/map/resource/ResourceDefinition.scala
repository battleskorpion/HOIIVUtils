package com.hoi4utils.hoi4.map.resource

import com.hoi4utils.parser.Node
import com.hoi4utils.script.{DoublePDX, IntPDX, PDXScript, StructuredPDX}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private class ResourceDefinition(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) {
  final var iconFrame = new IntPDX("icon_frame")
  final var cic = new DoublePDX("cic")
  final var convoys = new DoublePDX("convoys")

  def this(node: Node) = {
    this(node.name)
    val file = None
    loadPDX(node, file)
  }

  override protected def childScripts: mutable.Seq[? <: PDXScript[?]] = {
    ListBuffer(iconFrame, cic, convoys)
  }

}
