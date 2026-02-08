//package com.hoi4utils.databases.trigger
//
//import com.hoi4utils.script.{PDXScript, ScopedPDXScript}
//
//trait Trigger extends ScopedPDXScript with PDXScript[?, ?] with Cloneable {
//  
//	@throws[CloneNotSupportedException]
//	override def clone(): AnyRef = {
//		val clone = super.clone().asInstanceOf[Trigger]
//		// add stuff
//		clone
//	}
//}
