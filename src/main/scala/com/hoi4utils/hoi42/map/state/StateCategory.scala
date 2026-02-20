package com.hoi4utils.hoi42.map.state

import com.hoi4utils.script2.{IDReferable, NameReferable, PDXEntity}

class StateCategory extends PDXEntity with NameReferable[String] {
  val localBuildingSlots = pdx[Int]("local_building_slots")
//  val color = // color pdx
}
