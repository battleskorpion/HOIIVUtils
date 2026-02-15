package com.hoi4utils.hoi42.common.map.state

import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagRegistry}
import com.hoi4utils.hoi42.common.map.province.Province
import com.hoi4utils.hoi42.common.map.resource.Resource
import com.hoi4utils.script2.*

class State(var states: StateRegistry) extends PDXEntity with IDReferable[Int] with RegistryMember[State](states):
  given Registry[CountryTag] = new CountryTagRegistry()
  val id = pdx[Int]("id") required true
  val name = pdx[String]("name")
  val resources = pdxList[Resource]("resources")
  val history = pdx[History]("history")
  val provinces = pdxList[Province]("provinces")
  val manpower = pdx[Int]("manpower")
  val buildingsMaxLevelFactor = pdx[Double]("buildings_max_level_factor")
  val stateCategory = pdx[StateCategory]("state_category")
  val localSupplies = pdx[Double]("local_supplies")
  val impassible = pdx[Boolean]("impassible") // TODO BoolType.YES_NO for validator

  override def idProperty: PDXProperty[Int] = id 

object State { }

class StateRegistry extends Registry[State] {

  override def idDecoder: PDXDecoder[Int] = summon[PDXDecoder[Int]]
}

class History(using Registry[CountryTag]) extends PDXEntity:

  val owner = pdx[Reference[CountryTag]]("owner")
  val controller = pdx[Reference[CountryTag]]("controller")
  val buildings = pdx[Buildings]("buildings")
  val victoryPoints = pdxList[Int]("victory_points") // todo todo
//    val startDateScopes = pdxList[StartDateScope]()   // TODO special....

class Buildings extends PDXEntity:
  val infrastructure = pdx[Int]("infrastructure")
  val civilianFactories = pdx[Int]("industrial_complex")
  val militaryFactories = pdx[Int]("arms_factory")
  val navalDockyards = pdx[Int]("naval_base")
  val airBase = pdx[Int]("air_base")

//  class StartDateScope extends History with ProceduralIdentifier

