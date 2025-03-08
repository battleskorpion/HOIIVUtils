package com.hoi4utils.clausewitz.data.country

import com.hoi4utils.clausewitz.data.technology.Technology
import com.hoi4utils.clausewitz.data.units.OrdersOfBattle
import com.hoi4utils.clausewitz.map.buildings.Infrastructure
import com.hoi4utils.clausewitz.map.state.{InfrastructureData, Resource, State}
import com.hoi4utils.clausewitz.script.ReferencePDX
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.jetbrains.annotations.NotNull

import scala.jdk.javaapi.CollectionConverters
import java.util
import java.util.function.Function
import scala.collection.mutable.ListBuffer


// todo make country extend countrytag???? ehhhhh
// todo consider... implements infrastructure, resources?????
// todo localizable data?
object Country {
  private val countries = new ListBuffer[Country]()

  def list: List[Country] = countries.toList
  
  def read(): Boolean = {
    // todo
    false
  }

  /**
   * Clears all countries and any other relevant values. 
   */
  def clear(): Unit = {
    countries.clear()
  }

  /**
   * Adds a focus tree to the list of focus trees.
   *
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  def add(country: Country): Iterable[Country] = {
    countries += country
    countries
  }

  def getDataFunctions(resourcePercentages: Boolean): Iterable[Country => ?] = {
    val dataFunctions = ListBuffer[Country => ?]()

    dataFunctions += (c => c.name)
    dataFunctions += (c => c._infrastructure.population)
    dataFunctions += (c => c._infrastructure.civilianFactories)
    dataFunctions += (c => c._infrastructure.militaryFactories)
    dataFunctions += (c => c._infrastructure.navalDockyards)
    dataFunctions += (c => c._infrastructure.airfields)
    dataFunctions += (c => c._infrastructure.civMilRatio)
    dataFunctions += (c => c._infrastructure.popPerFactoryRatio)
    dataFunctions += (c => c._infrastructure.popPerCivRatio)
    dataFunctions += (c => c._infrastructure.popPerMilRatio)
    dataFunctions += (c => c._infrastructure.popAirportCapacityRatio)
    dataFunctions += (c => c._infrastructure.popPerStateRatio(c.numOwnedStates))
    // again, this resource code is not expandable. fix sometime :(
    if (resourcePercentages) {
      dataFunctions += (c => c.aluminum)
      dataFunctions += (c => c.chromium)
      dataFunctions += (c => c.oil)
      dataFunctions += (c => c.rubber)
      dataFunctions += (c => c.steel)
      dataFunctions += (c => c.tungsten)
    }
    else {
      dataFunctions += (c => c.aluminumPercentOfGlobal)
      dataFunctions += (c => c.chromiumPercentOfGlobal)
      dataFunctions += (c => c.oilPercentOfGlobal)
      dataFunctions += (c => c.rubberPercentOfGlobal)
      dataFunctions += (c => c.steelPercentOfGlobal)
      dataFunctions += (c => c.tungstenPercentOfGlobal)
    }
    dataFunctions
  }

//  def loadCountries[T](list: List[T]): List[Country] = {
//    countryList.clear()
//
//    for (item <- list) {
//      countryList.add(new Country(item))
//    }
//
//    countryList
//  }

//  def loadCountries(countryTags: List[CountryTag], infrastructureList: List[Infrastructure],
//                    resourcesList: List[List[Resource]]): ObservableList[Country] = {
//    countries.clear()
//
//    val countryTagsIterator = countryTags.iterator
//    val infrastructureListIterator = infrastructureList.iterator
//    val resourcesListIterator = resourcesList.iterator
//
//    while (countryTagsIterator.hasNext) countries.add(new Country(countryTagsIterator.next, infrastructureListIterator.next, resourcesListIterator.next))
//    countries
//  }
//
//  def loadCountries(infrastructureList: List[Infrastructure], resourcesList: List[List[Resource]]): ObservableList[Country] = {
//    loadCountries(CountryTagsManager.getCountryTags, infrastructureList, resourcesList)
//  }

//  def loadCountries: ObservableList[Country] = loadCountries(CollectionConverters.asJava(State.infrastructureOfCountries), CollectionConverters.asJava(State.resourcesOfCountries))
}

class Country extends InfrastructureData with Comparable[Country] {
  private var _countryTag: Option[CountryTag] = None
  private var _infrastructure: Infrastructure = null // infrastructure of all owned states

  private var _resources: List[Resource] = List.empty // resources of all owned states

  private val oob_list: Set[OrdersOfBattle] = null // set of potential orders of battles defined in history/countries file (oob)

  private val defaultResearchSlots = 0 // default research slots as defined in history/countries file or similar

  private val countryFlags: Set[CountryFlag] = null
  private val capital = 0 // country capital as defined by applicable oob or unknown

  private val stability = .0 // stability percentage defined from 0.0-1.0

  private val warSupport = .0 // war support percentage defined from 0.0-1.0

  private val startingTech: Set[Technology] = null // starting technology defined in history/countries file

  def this(countryTag: CountryTag, infrastructure: Infrastructure, resources: List[Resource]) = {
    this()
    this._countryTag = Some(countryTag)
    this._infrastructure = infrastructure
    this._resources = resources
  }

  def this(countryTag: CountryTag) = {
    this(countryTag, new Infrastructure, List[Resource]())
  }

  def countryTag: CountryTag = _countryTag.getOrElse(CountryTag.NULL_TAG)

  def setCountryTag(countryTag: CountryTag): Unit = {
    this._countryTag = Some(countryTag)
  }

  override def getInfrastructureRecord: Infrastructure = _infrastructure

  def setInfrastructure(infrastructure: Infrastructure): Unit = {
    this._infrastructure = infrastructure
  }

  def resources: List[Resource] = _resources

  def setResources(resources: Nothing): Unit = {
    this._resources = resources
  }

  def aluminum: Double = _resources.filter(_.isValidID("aluminum")).map(_.amt).sum

  def chromium: Double = _resources.filter(_.isValidID("chromium")).map(_.amt).sum

  def oil: Double = _resources.filter(_.isValidID("oil")).map(_.amt).sum

  def rubber: Double = _resources.filter(_.isValidID("rubber")).map(_.amt).sum

  def steel: Double = _resources.filter(_.isValidID("steel")).map(_.amt).sum

  def tungsten: Double = _resources.filter(_.isValidID("tungsten")).map(_.amt).sum

  private def tungstenPercentOfGlobal = {
    tungsten / State.resourcesOfStates.filter(_.isValidID("tungsten")).map(_.amt).sum

    // todo this all (getting the resources) should be done a lil differently (more generically still.)
  }

  private def steelPercentOfGlobal = {
    // ! states resources and everything are correct, steel() etc must probably be returning 0.
    tungsten / State.resourcesOfStates.filter(_.isValidID("steel")).map(_.amt).sum
  }

  private def rubberPercentOfGlobal = tungsten / State.resourcesOfStates.filter(_.isValidID("rubber")).map(_.amt).sum

  private def oilPercentOfGlobal = tungsten / State.resourcesOfStates.filter(_.isValidID("oil")).map(_.amt).sum

  private def chromiumPercentOfGlobal = tungsten / State.resourcesOfStates.filter(_.isValidID("chromium")).map(_.amt).sum

  private def aluminumPercentOfGlobal = tungsten / State.resourcesOfStates.filter(_.isValidID("aluminum")).map(_.amt).sum

  def name: String = _countryTag.toString

  private def numOwnedStates = 1 // todo;

  override def compareTo(@NotNull o: Country): Int = _countryTag match {
    case Some(tag) => tag.compareTo(o.countryTag)
    case None => 0
  }

  override def toString: String = _countryTag.toString + " " + "[country tag]"
}
