//package com.hoi4utils.script.shared
//
//import com.hoi4utils.script.datatype.DoublePDX
//import com.hoi4utils.script.seq.MultiPDX
//import com.hoi4utils.script.{PDXScript, StructuredPDX}
//
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//
//class AIWillDo extends StructuredPDX("ai_will_do"):
//	final val base = new DoublePDX("base")
//	final val factor = new DoublePDX("factor")
//	final val add = new DoublePDX("add")
//	final val modifier = new MultiPDX[AIWillDoModifier](None, Some(() => new AIWillDoModifier), "modifier")
//
//	override protected def childScripts: mutable.Seq[? <: PDXScript[?, ?]] = ListBuffer(base, factor, add, modifier)
//
//	override def getPDXTypeName: String = "AI Willingness"
//
//class AIWillDoModifier extends StructuredPDX("modifier"):
//	final val base = new DoublePDX("base")
//	final val factor = new DoublePDX("factor")
//	final val add = new DoublePDX("add")
//	// todo trigger block
//
//	override protected def childScripts: mutable.Seq[? <: PDXScript[?, ?]] = ListBuffer(base, factor, add)
//
//	override def getPDXTypeName: String = "Modifier"
