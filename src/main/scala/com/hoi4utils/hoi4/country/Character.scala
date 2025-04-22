package com.hoi4utils.hoi4.country

import com.hoi4utils.localization.{Localizable, Property}

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.jdk.javaapi.CollectionConverters

class Character extends Localizable {
  private val id: String = null

  override def getLocalizableProperties: mutable.Map[Property, String] = mutable.Map(Property.NAME -> id)

  override def getLocalizableGroup: Iterable[? <: Localizable] = List(this) // todo improve
}

