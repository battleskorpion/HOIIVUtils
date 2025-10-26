package com.hoi4utils.script.shared

import com.hoi4utils.script.{DoublePDX, MultiPDX, PDXScript, StructuredPDX}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AIWillDoPDX extends StructuredPDX("ai_will_do"):
	final val base = new DoublePDX("base")
	final val factor = new DoublePDX("factor")
	final val add = new DoublePDX("add")
	final val modifier = new MultiPDX[AIWillDoModifierPDX](None, Some(() => new AIWillDoModifierPDX), "modifier")

	override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, modifier)

	override def getPDXTypeName: String = "AI Willingness"

class AIWillDoModifierPDX extends StructuredPDX("modifier"):
	final val base = new DoublePDX("base")
	final val factor = new DoublePDX("factor")
	final val add = new DoublePDX("add")
	// todo trigger block

	override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add)

	override def getPDXTypeName: String = "Modifier"
