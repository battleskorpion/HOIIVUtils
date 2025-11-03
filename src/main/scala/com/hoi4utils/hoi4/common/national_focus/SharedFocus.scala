package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.script.{PDXScript, StructuredPDX, TriggerPDX}
import com.hoi4utils.script.datatype.{PointPDX, StringPDX}

import java.io.File

class SharedFocus extends Focus(PseudoSharedFocusTree(), pdxIdentifier = "shared_focus") {

	val offset = Offset()

	/* init */
	PseudoSharedFocusTree().addNewFocus(this)

	class Offset extends PointPDX("offset") {
		val trigger = TriggerPDX()

		override def childScripts: collection.mutable.Iterable[? <: PDXScript[?]] = super.childScripts ++ List(trigger)
	}

}
