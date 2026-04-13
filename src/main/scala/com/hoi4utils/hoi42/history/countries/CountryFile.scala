package com.hoi4utils.hoi42.history.countries

import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi4.map.state.InfrastructureData
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagRegistry, CountryTagService}
import com.hoi4utils.hoi42.common.technologies.Technology
import com.hoi4utils.hoi42.common.units.{OrdersOfBattle, OrdersOfBattleRegistry}
import com.hoi4utils.hoi42.map.resource.Resource
import com.hoi4utils.hoi42.map.state.*
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import com.hoi4utils.script2.{IDReferable, PDXDecoder, PDXEntity, Reference, Registry, RegistryMember}
import com.typesafe.scalalogging.LazyLogging
import org.jetbrains.annotations.NotNull
import zio.{RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*

class CountryFile(var countries: CountryFileRegistry, var file: Option[File]) extends PDXEntity with IDReferable[String] with RegistryMember[CountryFile](countries) {
  given Registry[CountryTag] = new CountryTagRegistry()
  given Registry[OrdersOfBattle] = new OrdersOfBattleRegistry()
  given Registry[State] = new StateRegistry()   // todo prob not good
  /* data */
  private var _countryTag: Option[CountryTag] = None

  val oob = pdx[Reference[OrdersOfBattle]]("oob")   //new ReferencePDX[String, OrdersOfBattle](() => OrdersOfBattle.list, "oob")
  val defaultResearchSlots = 0 // default research slots as defined in history/countries file or similar
  val countryFlags: Set[CountryFlag] = null
  val capital = pdx[Reference[State]]("capital") //new ReferencePDX[Int, State](() => stateService.list, "capital")
  val stability = 0.0 // stability percentage defined from 0.0-1.0
  val warSupport = 0.0 // war support percentage defined from 0.0-1.0
  val startingTech: Set[Technology] = null // starting technology defined in history/countries file

  private var _file: Option[File] = None

  //private var _infrastructure: Infrastructure = null // infrastructure of all owned states
  //private var _resources: List[Resource] = List.empty // resources of all owned states

  def countryTag: CountryTag = _countryTag.getOrElse(CountryTag.NULL_TAG)

  def setCountryTag(countryTag: CountryTag): Unit = {
    this._countryTag = Some(countryTag)
  }

  def name: String = _countryTag match {
    case Some(tag) => tag.toString
    case None => CountryTag.NULL_TAG.toString
  }

  private def numOwnedStates = 1 // todo;

  override def compareTo(@NotNull o: CountryFile): Int = _countryTag match {
    case Some(tag) => tag.$.compareTo(o.countryTag.$)
    case None => 0
  }

  override def toString: String = _countryTag.toString + " " + "[country tag]"
}

class CountryFileRegistry extends Registry[CountryFile] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}

