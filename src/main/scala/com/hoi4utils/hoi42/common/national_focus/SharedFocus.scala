package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.PDXDecoder
import com.hoi4utils.script2.datatype.PointPDX
import zio.ZIO

class SharedFocus(focusTree: FocusTree) extends Focus(focusTree) {
  val offset = pdx[Offset]("offset")

  /* init */
  // todo might have to do stuff later
//  for {
//    pseudoTree <- PseudoSharedFocusTree()
//    _ <- ZIO.succeed(pseudoTree.addNewFocus(this))
//  } yield ()

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}

class Offset extends PointPDX {
  //  val trigger = pdx[TriggerPDX]("trigger")
}
