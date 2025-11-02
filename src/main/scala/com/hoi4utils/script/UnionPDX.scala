package com.hoi4utils.script

abstract class UnionPDX[T <: PDXScript[?]](pdxIdentifiers: List[String]) {

	def this(pdxIdentifier: String) = {
		this(List(pdxIdentifier))
	}

	protected def schemas: Seq[() => T]

	
}
