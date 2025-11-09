package com.hoi4utils.script.datatype

import com.hoi4utils.script.{DoublePDX, IntPDX, PDXScript, StructuredPDX}

import scala.collection.mutable.ListBuffer

class PointPDX(pdxIdentifiers: List[String], xID: String = "x", yID: String = "y") extends StructuredPDX(pdxIdentifiers) {

	val x = IntPDX(xID)
	val y = IntPDX(yID)

	def this(pdxIdentifiers: String) =
		this(List(pdxIdentifiers))

	protected def childScripts: collection.mutable.Seq[? <: PDXScript[?]] = ListBuffer(x, y)

}
