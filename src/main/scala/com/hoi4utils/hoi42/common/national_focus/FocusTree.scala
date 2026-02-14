package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{PDXDecoder, PDXEntity, Registry}

import scala.reflect.ClassTag

class FocusTree extends Registry[Focus] with PDXEntity {

  val id = pdx[String]("id") required true
  val focuses = pdxList[Focus]("focus")


  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
