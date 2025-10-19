package com.hoi4utils.hoi4mod.common.technologies

import com.hoi4utils.hoi4mod.localization.{Localizable, Property}
import com.hoi4utils.parser.ClausewitzDate
import com.hoi4utils.parser.ClausewitzDate
import org.jetbrains.annotations.NotNull

import scala.collection.mutable


class Technology extends Localizable {
	var id: String = ""
	var technologyDate: Option[ClausewitzDate] = None
	var cost = .0 // research_cost

	var year = 0 // start_year

	var enabledEquipments: List[Equipment] = Nil
	var enabledEquipmentModules: List[EquipmentModule] = Nil
	var categories: List[TechCategory] = Nil
	var paths: List[TechPath] = Nil

	// ai_will_do
	@NotNull override def getLocalizableProperties: mutable.Map[Property, String] = mutable.Map(Property.NAME -> id)

	// todo this can be more inclusive
	@NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = List(this)
}