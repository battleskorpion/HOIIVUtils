package com.hoi4utils.hoi4.common.national_focus

import zio.{URIO, ZIO}

case class PseudoSharedFocusTree(manager: FocusTreeManager) extends FocusTree()(manager) {
  override def toString: String = s"[Shared Focuses] ${super.toString}"
}

object PseudoSharedFocusTree {
//	pseudoTreeHOI4ProjectMap: Map[]
  // for now:
  var trees: List[PseudoSharedFocusTree] = List()

  def apply(): URIO[FocusTreeManager, PseudoSharedFocusTree] =
    ZIO.serviceWith[FocusTreeManager] { manager =>
      trees.headOption match
        case Some(tree) => tree
        case None =>
          val newTree = new PseudoSharedFocusTree(manager)
          trees = newTree :: trees
          newTree
    }

  def forFocuses(focuses: List[SharedFocus], id: String): URIO[FocusTreeManager, PseudoSharedFocusTree] =
    ZIO.serviceWith[FocusTreeManager] { manager =>
      val newTree = new PseudoSharedFocusTree(manager)
      newTree.id @= id
      focuses.foreach(newTree.addNewFocus)
      newTree
    }
}
