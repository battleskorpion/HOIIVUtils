package com.hoi4utils.hoi4.technology

import com.hoi4utils.ClausewitzDate
import com.hoi4utils.localization.{Localizable, Property}
import org.jetbrains.annotations.NotNull

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.jdk.javaapi.CollectionConverters


class Technology extends Localizable {
  var id: String = "" 
  var technologyDate: Option[ClausewitzDate] = None
  var cost = .0 // research_cost

  var year = 0 // start_year

  var enabledEquipments: List[Equipment] = Nil
  var enabledEquipmentModules: List[EquipmentModule] = Nil
  var categories:List[TechCategory] = Nil
  var paths: List[TechPath] = Nil

  // ai_will_do
  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] = mutable.Map(Property.NAME -> id)

  // todo this can be more inclusive
  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = List(this)
}
