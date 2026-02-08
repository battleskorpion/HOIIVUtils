//package com.hoi4utils.hoi4.common.national_focus
//
//import com.hoi4utils.hoi4.common.country_tags.CountryTagService
//import com.hoi4utils.hoi42.common.national_focus.FocusTree
//import zio.{URIO, ZIO}
//
//case class PseudoSharedFocusTree(manager: FocusTreeManager, countryTagService: CountryTagService) extends FocusTree()(manager, countryTagService) {
//  override def toString: String = s"[Shared Focuses] ${super.toString}"
//}
//
//object PseudoSharedFocusTree {
////	pseudoTreeHOI4ProjectMap: Map[]
//  // for now:
//  var trees: List[PseudoSharedFocusTree] = List()
//
//  def apply(): URIO[FocusTreeManager & CountryTagService, PseudoSharedFocusTree] = {
//    for {
//      manager <- ZIO.service[FocusTreeManager]
//      tagService <- ZIO.service[CountryTagService]
//      tree = trees.headOption match
//        case Some(tree) => tree
//        case None =>
//          val newTree = new PseudoSharedFocusTree(manager, tagService)
//          trees = newTree :: trees
//          newTree
//    } yield tree
////    ZIO.serviceWith[FocusTreeManager] { manager =>
////      trees.headOption match
////        case Some(tree) => tree
////        case None =>
////          val newTree = new PseudoSharedFocusTree(manager, countryTagService)
////          trees = newTree :: trees
////          newTree
////    }
//  }
//
//  def forFocuses(focuses: List[SharedFocus], id: String): URIO[FocusTreeManager & CountryTagService, PseudoSharedFocusTree] =
//    for {
//      manager <- ZIO.service[FocusTreeManager]
//      tagService <- ZIO.service[CountryTagService]
//      newTree =
//        val tree = new PseudoSharedFocusTree(manager, tagService)
//        tree.id @= id
//        focuses.foreach(tree.addNewFocus)
//        tree
//    } yield newTree
//
////    ZIO.serviceWith[FocusTreeManager] { manager =>
////      val newTree = new PseudoSharedFocusTree(manager, countryTagService)
////      newTree.id @= id
////      focuses.foreach(newTree.addNewFocus)
////      newTree
////    }
//}
