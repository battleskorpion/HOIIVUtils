package com.hoi4utils.hoi4.history.countries

import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.common.technologies.Technology
import com.hoi4utils.hoi4.common.units.OrdersOfBattle
import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi4.map.resource.Resource
import com.hoi4utils.hoi4.map.state.{InfrastructureData, State}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import com.typesafe.scalalogging.LazyLogging
import org.jetbrains.annotations.NotNull
import zio.{Task, ZIO}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*

class CountryFile extends StructuredPDX with HeadlessPDX with Comparable[CountryFile] with InfrastructureData {
  /* data */
  private var _countryTag: Option[CountryTag] = None

  private val oob = new ReferencePDX[OrdersOfBattle](() => OrdersOfBattle.list, "oob")
  private val defaultResearchSlots = 0 // default research slots as defined in history/countries file or similar
  private val countryFlags: Set[CountryFlag] = null
  private val capital = new ReferencePDX[State](() => State.list, "capital")
  private val stability = 0.0 // stability percentage defined from 0.0-1.0
  private val warSupport = 0.0 // war support percentage defined from 0.0-1.0
  private val startingTech: Set[Technology] = null // starting technology defined in history/countries file

  private var _file: Option[File] = None

  //private var _infrastructure: Infrastructure = null // infrastructure of all owned states
  //private var _resources: List[Resource] = List.empty // resources of all owned states

  /* default */
  CountryFile.add(this)

  def this(countryTag: CountryTag, infrastructure: Infrastructure, resources: List[Resource]) = {
    this()
    this._countryTag = Some(countryTag)
    //    this._infrastructure = infrastructure
    //    this._resources = resources
  }

  def this(countryTag: CountryTag) = {
    this(countryTag, new Infrastructure, List[Resource]())
  }

  def this(file: File, countryTag: CountryTag) = {
    this(countryTag)
    if (!file.exists) {
      logger.error(s"Country file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
    _countryTag = Some(countryTag)
  }

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this
    )
    CountryFile.countryErrors += pdxError

  def setFile(file: File): Unit = {
    _file = Some(file)
  }

  def countryTag: CountryTag = _countryTag.getOrElse(CountryTag.NULL_TAG)

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

  override def compareTo(@NotNull o: CountryFile): Int = _countryTag match {
    case Some(tag) => tag.compareTo(o.countryTag)
    case None => 0
  }

  override def toString: String = _countryTag.toString + " " + "[country tag]"

  override protected def childScripts: mutable.Seq[? <: PDXScript[?]] = {
    ListBuffer(oob, capital)
  }
}

// todo make country extend countrytag???? ehhhhh
// todo consider... implements infrastructure, resources?????
// todo localizable data?
object CountryFile extends LazyLogging with PDXReadable {
  override val cleanName: String = "Countries"

  private val countries = new ListBuffer[CountryFile]()
  var countryErrors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]

  def list: List[CountryFile] = countries.toList

  def read(): Task[Boolean] = {
    ZIO.succeed {
      if (!HOIIVFiles.Mod.country_folder.exists || !HOIIVFiles.Mod.country_folder.isDirectory) {
        logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.country_folder} is not a directory, or it does not exist.")
        false
      } else if (HOIIVFiles.Mod.country_folder.listFiles == null || HOIIVFiles.Mod.country_folder.listFiles.length == 0) {
        logger.warn(s"No focuses found in ${HOIIVFiles.Mod.country_folder}")
        false
      } else {
        // create focus trees from files
        val countryFiles = HOIIVFiles.Mod.country_folder.listFiles().filter(_.getName.endsWith(".txt"))
        countryFiles
          .map(f =>
            given ParsingContext(f)
            (f, CountryTag(f.getName.split(" ")(0), f))
          )
          .par
          .foreach { (f, tag) => new CountryFile(f, tag) }
        true
      }
    }
  }

  /**
   * Clears all countries and any other relevant values.
   */
  override def clear(): Task[Unit] = ZIO.succeed(countries.clear())

  /**
   * Adds a focus tree to the list of focus trees.
   *
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  def add(country: CountryFile): Iterable[CountryFile] = {
    countries += country
    countries
  }

  def getDataFunctions(resourcePercentages: Boolean): Iterable[CountryFile => ?] = {
    val dataFunctions = ListBuffer[CountryFile => ?]()

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
