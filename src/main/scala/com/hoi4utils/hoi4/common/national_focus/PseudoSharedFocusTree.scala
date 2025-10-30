package com.hoi4utils.hoi4.common.national_focus

case class PseudoSharedFocusTree() extends FocusTree {

}

object PseudoSharedFocusTree {
//	pseudoTreeHOI4ProjectMap: Map[]
	// for now:
	var trees: List[PseudoSharedFocusTree] = List()

	def apply(): PseudoSharedFocusTree = trees.headOption match
		case Some(tree) => tree
		case None =>
			val newTree = new PseudoSharedFocusTree()
			trees = newTree :: trees
			newTree

}
