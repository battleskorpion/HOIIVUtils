package com.hoi4utils.hoi4.common.national_focus

case class PseudoSharedFocusTree() extends FocusTree {
	override def toString: String = s"[Shared Focuses] ${super.toString}"
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

	def forFocuses(focuses: List[SharedFocus], id: String): PseudoSharedFocusTree = {
		val newTree = new PseudoSharedFocusTree()
		newTree.id @= id
		focuses.foreach(newTree.addNewFocus)
		newTree
	}
}
