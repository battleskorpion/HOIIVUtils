package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{PDXDecoder, PDXEntity, Registry}

import scala.reflect.ClassTag

class FocusTree extends Registry[Focus] with PDXEntity {

  val id = pdx[String]("id") required true
//  val country = pdx[FocusTreeCountry]
  val focuses = pdxList[Focus]("focus") required true
  val default = pdx[Boolean]("default")
  val resetOnCivilWar = pdx[Boolean]("reset_on_civil_war")
//  val continuousFocusPosition = pdx[PointPDX]("continuous_focus_position")
//  val initialShowPosition = pdx[InitialShowPosition]("initial_show_position")
//  val shortcut = pdx[Shortcut]("shortcut")
//  /** special handling */ 
//  val sharedFocuses = pdxList[Reference[SharedFocus]]("shared_focus")


  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
