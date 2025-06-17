package com.map

import com.hoi4utils.parser.Node
import com.hoi4utils.script.{DoublePDX, IntPDX, PDXScript, StructuredPDX}
import com.map.Resource.resourceErrors

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private[map] class ResourceDef(node: Node | String)
  extends StructuredPDX(
    node match {
      case s: String => s
      case n: Node => n.name
    }):
  final var iconFrame = new IntPDX("icon_frame")
  final var cic = new DoublePDX("cic")
  final var convoys = new DoublePDX("convoys")

  node match
    case n: Node => loadPDX(n, resourceErrors)
    case _ => // do nothing, node is a String

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(iconFrame, cic, convoys)
  }