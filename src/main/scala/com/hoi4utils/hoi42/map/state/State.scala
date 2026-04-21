package com.hoi4utils.hoi42.map.state

import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagRegistry}
import com.hoi4utils.hoi42.map.province.Province
import com.hoi4utils.hoi42.map.resource.Resource
import com.hoi4utils.parser.ClausewitzDate
import com.hoi4utils.script2.*
import com.hoi4utils.script2.PDXPropertyValueExtensions.*

import java.io.File

// todo on the file will turn into trait thingy
class State(var states: StateRegistry, var file: Option[File]) extends PDXEntity with IDReferable[Int] with RegistryMember[State](states):
  given Registry[CountryTag] = new CountryTagRegistry()

  val stateID: PDXProperty[Int] = pdx[Int]("id")
  val name = pdx[String]("name")
  val resources = pdxList[Resource]("resources") default Resource.NONE.toList
  val history = pdx[History]("history")
  val provinces = pdxList[Province]("provinces")
  val manpower = pdx[Int]("manpower")
  val buildingsMaxLevelFactor = pdx[Double]("buildings_max_level_factor")
  val stateCategory = pdx[StateCategory]("state_category")
  val localSupplies = pdx[Double]("local_supplies")
  val impassible = pdx[Boolean]("impassible") // TODO BoolType.YES_NO for validator

  override def idProperty: PDXProperty[Int] = stateID

  def owner(date: ClausewitzDate): Option[CountryTag] = history.flatMapRef(_.owner)

  def buildings: Option[Buildings] = history ~> (_.buildings)

  def population: Int = manpower getOrElse 1
  def population_=(pop: Int): Unit = manpower @= pop
  def population_=?(pop: Int): Unit = manpower @=? pop

  def infrastructure: Int = buildings / (_.infrastructure) getOrElse 0
  def infrastructure_=(infrastructure: Int): Unit = buildings / (_.infrastructure) @= infrastructure
  def infrastructure_=?(infrastructure: Int): Unit = (buildings / (_.infrastructure)) @=? infrastructure

  def civilianFactories: Int = buildings / (_.civilianFactories) getOrElse 0
  def civilianFactories_=(factories: Int): Unit = buildings / (_.civilianFactories) @= factories
  def civilianFactories_=?(factories: Int): Unit = (buildings / (_.civilianFactories)) @=? factories

  def militaryFactories: Int = buildings / (_.militaryFactories) getOrElse 0
  def militaryFactories_=(factories: Int): Unit = buildings / (_.militaryFactories) @= factories
  def militaryFactories_=?(factories: Int): Unit = (buildings / (_.militaryFactories)) @=? factories

  def navalDockyards: Int = buildings / (_.navalDockyards) getOrElse 0
  def navalDockyards_=(dockyards: Int): Unit = buildings / (_.navalDockyards) @= dockyards
  def navalDockyards_=?(dockyards: Int): Unit = (buildings / (_.navalDockyards)) @=? dockyards

  def navalPorts: Int = buildings / (_.navalDockyards) getOrElse 0
  def navalPorts_=(ports: Int): Unit = buildings / (_.navalDockyards) @= ports
  def navalPorts_=?(ports: Int): Unit = (buildings / (_.navalDockyards)) @=? ports

  def airfields: Int = buildings / (_.airBase) getOrElse 0
  def airfields_=(airfields: Int): Unit = buildings / (_.airBase) @= airfields
  def airfields_=?(airfields: Int): Unit = (buildings / (_.airBase)) @=? airfields

  def stateInfrastructure: Infrastructure =
    new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, navalDockyards, navalPorts, airfields)

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

