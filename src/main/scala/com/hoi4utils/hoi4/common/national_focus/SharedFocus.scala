package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.script.datatype.StringPDX

import java.io.File

class SharedFocus extends Focus(PseudoSharedFocusTree(), pdxIdentifier = "shared_focus") {


	/* init */
	PseudoSharedFocusTree().addNewFocus(this)


}
