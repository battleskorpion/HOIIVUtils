package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.country_tags.*
import com.hoi4utils.script2.datatype.*
import com.hoi4utils.hoi42.common.*
import com.hoi4utils.script2.*
import com.hoi4utils.script2.PDXPropertyValueExtensions.*
import zio.{URIO, ZIO}

import java.io.File
import java.util.UUID

class PseudoSharedFocusTree(pseudoTreeRegistry: PseudoSharedFocusTreeRegistry, var file: Option[File])
  extends PDXEntity with FocusRegistry[SharedFocus] with IDReferable[String] with RegistryMember[PseudoSharedFocusTree](pseudoTreeRegistry) {

  given Registry[SharedFocus] = this 
  
  /** dummy focus tree ID */
  val id = pdx[String]("id") required true
  val sharedFocuses = pdxList[Reference[SharedFocus]]("shared_focus")

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]

  override def toString: String = s"[Shared Focuses] ${super.toString}"
}

object PseudoSharedFocusTree {
  def forFocuses(focuses: List[SharedFocus], id: String,
                 pseudoTreeRegistry: PseudoSharedFocusTreeRegistry,
                 file: Option[File]): URIO[FocusTreeService & CountryTagService, PseudoSharedFocusTree] =
    for {
      manager <- ZIO.service[FocusTreeService]
      tagService <- ZIO.service[CountryTagService]
      newTree =
        val tree = new PseudoSharedFocusTree(pseudoTreeRegistry, file)
        val treeId = if id == "" then s"unknown-${UUID.randomUUID()}" else id
        tree.id @= treeId
        tree ++= focuses 
        tree
    } yield newTree
}

class PseudoSharedFocusTreeRegistry extends Registry[PseudoSharedFocusTree] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}

