package com.hoi4utils.hoi4mod.history.countries

import com.hoi4utils.hoi4mod.localization.{Localizable, Property}

import scala.collection.mutable

class Character extends Localizable {
  private val id: String = null

  override def getLocalizableProperties: mutable.Map[Property, String] = mutable.Map(Property.NAME -> id)

  override def getLocalizableGroup: Iterable[? <: Localizable] = List(this) // todo improve
}

