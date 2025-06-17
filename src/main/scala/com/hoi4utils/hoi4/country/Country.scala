package com.hoi4utils.hoi4.country

import com.hoi4utils.clausewitz.data.country.CountryFlag
import com.hoi4utils.clausewitz.map.buildings.Infrastructure
import com.hoi4utils.clausewitz.map.state.InfrastructureData
import com.hoi4utils.hoi4.country.Country.countryErrors
import com.hoi4utils.hoi4.technology.Technology
import com.hoi4utils.hoi4.units.OrdersOfBattle
import com.hoi4utils.script.{HeadlessPDX, PDXScript, ReferencePDX, StructuredPDX}
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.map.{Resource, State}
import com.typesafe.scalalogging.LazyLogging
import org.jetbrains.annotations.NotNull

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Country
(
  file: File = null,
  countryTag: CountryTag = null,
  private var _infrastructure: Infrastructure = new Infrastructure,
  private var _resources: List[Resource] = List[Resource]()
) extends StructuredPDX with HeadlessPDX with Comparable[Country] with InfrastructureData {
  require(file != null, "File cannot be null")
  require(countryTag != null, "Country tag cannot be null")

  /* data */
  private val oob = new ReferencePDX[OrdersOfBattle](() => OrdersOfBattle.list, oob => oob.id.value, "oob")
  private val defaultResearchSlots = 0 // default research slots as defined in history/countries file or similar
  private val countryFlags: Set[CountryFlag] = null
  private val capital = new ReferencePDX[State](() => State.list, state => state.stateID.asSomeString, "capital")
  private val stability = 0.0 // stability percentage defined from 0.0-1.0
  private val warSupport = 0.0 // war support percentage defined from 0.0-1.0
  private val startingTech: Set[Technology] = null // starting technology defined in history/countries file

  //private var _infrastructure: Infrastructure = null // infrastructure of all owned states
  //private var _resources: List[Resource] = List.empty // resources of all owned states

  private var _file: Option[File] = None

  private var _countryTag: Option[CountryTag] = countryTag match
    case tag => Some(tag)
  
  /* load Country */
  file match
    case f if f.exists() && f.isFile =>
      loadPDX(f, countryErrors)
      _file = Some(f)
    case f if f != null && !f.exists() =>
      countryErrors += s"Country file ${f.getName} does not exist."
  
  countryErrors.addAll(
    getStructuredPDXBadNodesList match {
      case Some(errors) => errors.map(identity)
      case None => List.empty
    }
  )

  /* default */
  Country.add(this)

  def getCountryTag: CountryTag = _countryTag.getOrElse(CountryTag.NULL_TAG)

  def setCountryTag(countryTag: CountryTag): Unit = {
    this._countryTag = Some(countryTag)
  }

  override def getInfrastructureRecord: Infrastructure = infrastructure

//  def setInfrastructure(infrastructure: Infrastructure): Unit = {
//    this._infrastructure = infrastructure
//  }

  def infrastructure: Infrastructure = State.infrastructureOfCountry(this)

  def resources: List[Resource] = State.resourcesOfCountry(this)

//  def setResources(resources: Nothing): Unit = {
//    this._resources = resources
//  }

  def aluminum: Double = resources.filter(_.isValidID("aluminium")).map(_.amt).sum

  def chromium: Double = resources.filter(_.isValidID("chromium")).map(_.amt).sum

  def oil: Double = resources.filter(_.isValidID("oil")).map(_.amt).sum

  def rubber: Double = resources.filter(_.isValidID("rubber")).map(_.amt).sum

  def steel: Double = resources.filter(_.isValidID("steel")).map(_.amt).sum

  def tungsten: Double = resources.filter(_.isValidID("tungsten")).map(_.amt).sum

  private def tungstenPercentOfGlobal = {
    tungsten / State.resourcesOfStates.filter(_.isValidID("tungsten")).map(_.amt).sum

    // todo this all (getting the resources) should be done a lil differently (more generically still.)
  }

  private def steelPercentOfGlobal = steel / State.resourcesOfStates.filter(_.isValidID("steel")).map(_.amt).sum

  private def rubberPercentOfGlobal = rubber / State.resourcesOfStates.filter(_.isValidID("rubber")).map(_.amt).sum

  private def oilPercentOfGlobal = oil / State.resourcesOfStates.filter(_.isValidID("oil")).map(_.amt).sum

  private def chromiumPercentOfGlobal = chromium / State.resourcesOfStates.filter(_.isValidID("chromium")).map(_.amt).sum

  private def aluminumPercentOfGlobal = aluminum / State.resourcesOfStates.filter(_.isValidID("aluminium")).map(_.amt).sum

  def name: String = _countryTag match {
    case Some(tag) => tag.toString
    case None => CountryTag.NULL_TAG.toString
  }

  private def numOwnedStates = 1 // todo;

  override def compareTo(@NotNull o: Country): Int = _countryTag match {
    case Some(tag) => tag.compareTo(o.getCountryTag)
    case None => 0
  }

  override def toString: String = _countryTag.toString + " " + "[country tag]"

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(oob, capital)
  }
}

// todo make country extend countrytag???? ehhhhh
// todo consider... implements infrastructure, resources?????
// todo localizable data?
object Country extends LazyLogging with PDXReadable {

  private val countries = new ListBuffer[Country]()
  val countryErrors = new ListBuffer[String]()

  def getCountries: ListBuffer[Country] = countries

  def read(): Boolean = {
    if (!HOIIVFiles.Mod.country_folder.exists || !HOIIVFiles.Mod.country_folder.isDirectory) {
      logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.country_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.country_folder.listFiles == null || HOIIVFiles.Mod.country_folder.listFiles.length == 0) {
      logger.warn(s"No focuses found in ${HOIIVFiles.Mod.country_folder}")
      false
    } else {

      // create focus trees from files
      HOIIVFiles.Mod.country_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
        var tag = CountryTag.get(f.getName.split(" ")(0))
        new Country(f, tag)
      }
      true
    }
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
    dataFunctions += (c => c.infrastructure.population)
    dataFunctions += (c => c.infrastructure.civilianFactories)
    dataFunctions += (c => c.infrastructure.militaryFactories)
    dataFunctions += (c => c.infrastructure.navalDockyards)
    dataFunctions += (c => c.infrastructure.airfields)
    dataFunctions += (c => c.infrastructure.civMilRatio)
    dataFunctions += (c => c.infrastructure.popPerFactoryRatio)
    dataFunctions += (c => c.infrastructure.popPerCivRatio)
    dataFunctions += (c => c.infrastructure.popPerMilRatio)
    dataFunctions += (c => c.infrastructure.popAirportCapacityRatio)
    dataFunctions += (c => c.infrastructure.popPerStateRatio(c.numOwnedStates))
    // again, this resource code is not expandable. fix sometime :(
    if (resourcePercentages) {
      dataFunctions += (c => c.aluminumPercentOfGlobal)
      dataFunctions += (c => c.chromiumPercentOfGlobal)
      dataFunctions += (c => c.oilPercentOfGlobal)
      dataFunctions += (c => c.rubberPercentOfGlobal)
      dataFunctions += (c => c.steelPercentOfGlobal)
      dataFunctions += (c => c.tungstenPercentOfGlobal)
    }
    else {
      dataFunctions += (c => c.aluminum)
      dataFunctions += (c => c.chromium)
      dataFunctions += (c => c.oil)
      dataFunctions += (c => c.rubber)
      dataFunctions += (c => c.steel)
      dataFunctions += (c => c.tungsten)
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