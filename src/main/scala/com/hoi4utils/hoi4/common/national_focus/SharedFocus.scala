package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.parser.Node
import com.hoi4utils.script.PDXError
import com.hoi4utils.script.datatype.StringPDX

import java.io.File

class SharedFocus extends Focus(PseudoSharedFocusTree(), pdxIdentifier = "shared_focus") {


	/* init */
	PseudoSharedFocusTree().addNewFocus(this)

	override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
		val pdxError = new PDXError(
			exception = exception,
			errorNode = node,
			file = if file != null then Some(file) else focusTree.focusFile,
			pdxScript = this
		).addInfo("focusId", id.str)
		focusErrors += pdxError

}
