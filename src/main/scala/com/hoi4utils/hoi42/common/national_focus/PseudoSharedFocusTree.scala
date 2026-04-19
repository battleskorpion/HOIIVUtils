package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.script2.PDXDecoder
import com.hoi4utils.script2.PDXPropertyValueExtensions.* 
import zio.{URIO, ZIO}

class PseudoSharedFocusTree(pseudoTreeRegistry: PseudoSharedFocusTreeRegistry) extends FocusTree(pseudoTreeRegistry) {
  override def toString: String = s"[Shared Focuses] ${super.toString}"
}

object PseudoSharedFocusTree {
  def forFocuses(focuses: List[SharedFocus], id: String, 
                 pseudoTreeRegistry: PseudoSharedFocusTreeRegistry): URIO[FocusTreeService & CountryTagService, PseudoSharedFocusTree] =
    for {
      manager <- ZIO.service[FocusTreeService]
      tagService <- ZIO.service[CountryTagService]
      newTree =
        val tree = new PseudoSharedFocusTree(pseudoTreeRegistry) 
        tree.id @= id
        focuses.foreach(tree.addNewFocus) // TODO unused expression without side effects? 
        tree
    } yield newTree
}

class PseudoSharedFocusTreeRegistry extends FocusTreeRegistry {
  
  override def idDecoder: PDXDecoder[Int] = summon[PDXDecoder[Int]]
}
  
