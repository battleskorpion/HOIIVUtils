package com.hoi4utils.hoi4.history.countries

import com.hoi4utils.hoi4.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi4.common.technologies.Technology
import com.hoi4utils.hoi4.common.units.OrdersOfBattle
import com.hoi4utils.hoi4.history.countries.service.CountryService
import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi4.map.resource.Resource
import com.hoi4utils.hoi4.map.state.{InfrastructureData, State, StateService}
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.*
import com.typesafe.scalalogging.LazyLogging
import org.jetbrains.annotations.NotNull
import zio.{RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*

class CountryFile(countryErrors: ListBuffer[PDXFileError], stateService: StateService) extends StructuredPDX with HeadlessPDX with Comparable[CountryFile] with InfrastructureData {
  /* data */
  private var _countryTag: Option[CountryTag] = None

  private val oob = new ReferencePDX[OrdersOfBattle](() => OrdersOfBattle.list, "oob")
  private val defaultResearchSlots = 0 // default research slots as defined in history/countries file or similar
  private val countryFlags: Set[CountryFlag] = null
  private val capital = new ReferencePDX[State](() => stateService.list, "capital")
  private val stability = 0.0 // stability percentage defined from 0.0-1.0
  private val warSupport = 0.0 // war support percentage defined from 0.0-1.0
  private val startingTech: Set[Technology] = null // starting technology defined in history/countries file

  private var _file: Option[File] = None

  //private var _infrastructure: Infrastructure = null // infrastructure of all owned states
  //private var _resources: List[Resource] = List.empty // resources of all owned states

  /* default */
//  CountryFile.add(this)

  def this(countryTag: CountryTag, infrastructure: Infrastructure, resources: List[Resource])(countryErrors: ListBuffer[PDXFileError], stateService: StateService) = {
    this(countryErrors, stateService)
    this._countryTag = Some(countryTag)
    //    this._infrastructure = infrastructure
    //    this._resources = resources
  }

  def this(countryTag: CountryTag)(countryErrors: ListBuffer[PDXFileError], stateService: StateService) = {
    this(countryTag, new Infrastructure, List[Resource]())(countryErrors, stateService)
  }

  def this(file: File, countryTag: CountryTag)(countryErrors: ListBuffer[PDXFileError], stateService: StateService) = {
    this(countryTag)(countryErrors, stateService)
    if (!file.exists) {
      logger.error(s"Country file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
    _countryTag = Some(countryTag)
  }

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    ZIO.serviceWith[CountryService] { service =>
      given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)

      val pdxError = new PDXFileError(
        exception = exception,
        errorNode = node,
        pdxScript = this
      )
      service.countryErrors += pdxError
    }

  def setFile(file: File): Unit = {
    _file = Some(file)
  }

  def countryTag: CountryTag = _countryTag.getOrElse(CountryTag.NULL_TAG)

  def setCountryTag(countryTag: CountryTag): Unit = {
    this._countryTag = Some(countryTag)
  }

  override def getInfrastructureRecord: Infrastructure = zio.Unsafe.unsafe { implicit unsafe =>
    HOIIVUtils.getActiveRuntime.unsafe.run(infrastructure).getOrThrowFiberFailure()
  }

  //  def setInfrastructure(infrastructure: Infrastructure): Unit = {
  //    this._infrastructure = infrastructure
  //  }

  def infrastructure: URIO[StateService, Infrastructure] =
    ZIO.serviceWith[StateService] { stateService =>
      stateService.infrastructureOfCountry(this)
    }

  def resources: URIO[StateService, List[Resource]] =
    ZIO.serviceWith[StateService] { stateService =>
      stateService.resourcesOfCountry(this)
    }

  //  def setResources(resources: Nothing): Unit = {
  //    this._resources = resources
  //  }

  def aluminum: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("aluminium")).map(_.amt).sum)

  def chromium: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("chromium")).map(_.amt).sum)

  def oil: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("oil")).map(_.amt).sum)

  def rubber: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("rubber")).map(_.amt).sum)

  def steel: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("steel")).map(_.amt).sum)

  def tungsten: URIO[StateService, Double] =
    resources.map(_.filter(_.isValidID("tungsten")).map(_.amt).sum)

  private def tungstenPercentOfGlobal: URIO[StateService, Double] = {
    // todo this all (getting the resources) should be done a lil differently (more generically still.)
    for {
      stateService <- ZIO.service[StateService]
      resource <- tungsten
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("tungsten")).map(_.amt).sum
    } yield percentOfGlobal
  }

  private def steelPercentOfGlobal: URIO[StateService, Double] =
    for {
      stateService <- ZIO.service[StateService]
      resource <- steel
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("steel")).map(_.amt).sum
    } yield percentOfGlobal

  private def rubberPercentOfGlobal: URIO[StateService, Double] =
    for {
      stateService <- ZIO.service[StateService]
      resource <- rubber
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("rubber")).map(_.amt).sum
    } yield percentOfGlobal

  private def oilPercentOfGlobal: URIO[StateService, Double] =
    for {
      stateService <- ZIO.service[StateService]
      resource <- oil
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("oil")).map(_.amt).sum
    } yield percentOfGlobal

  private def chromiumPercentOfGlobal: URIO[StateService, Double] =
    for {
      stateService <- ZIO.service[StateService]
      resource <- chromium
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("chromium")).map(_.amt).sum
    } yield percentOfGlobal

  private def aluminumPercentOfGlobal: URIO[StateService, Double] = {
    for {
      stateService <- ZIO.service[StateService]
      resource <- aluminum
      percentOfGlobal = resource / stateService.resourcesOfStates.filter(_.isValidID("aluminium")).map(_.amt).sum
    } yield percentOfGlobal
  }

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
object CountryFile extends LazyLogging {

  def getDataFunctions(resourcePercentages: Boolean): URIO[StateService, Iterable[CountryFile => Int | String | Double]] =
    // Helper to run the effect using the local Runtime or Unsafe
    def resolve[T](effect: ZIO[StateService, ?, T]): T =
      zio.Unsafe.unsafe { implicit unsafe =>
        // We use the existing active runtime to block and get the value
        HOIIVUtils.getActiveRuntime.unsafe.run(effect).getOrThrowFiberFailure()
      }

    ZIO.serviceWith[StateService] { service =>
      val dataFunctions = ListBuffer[CountryFile => Int | String | Double]()

      dataFunctions += (c => c.name)
      dataFunctions += (c => resolve(c.infrastructure.map(_.population)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.civilianFactories)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.militaryFactories)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.navalDockyards)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.airfields)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.civMilRatio)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.popPerFactoryRatio)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.popPerCivRatio)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.popPerMilRatio)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.popAirportCapacityRatio)))
      dataFunctions += (c => resolve(c.infrastructure.map(_.popPerStateRatio(c.numOwnedStates))))
      // again, this resource code is not expandable. fix sometime :(
      if (resourcePercentages)
        dataFunctions += (c => resolve(c.aluminumPercentOfGlobal))
        dataFunctions += (c => resolve(c.chromiumPercentOfGlobal))
        dataFunctions += (c => resolve(c.oilPercentOfGlobal))
        dataFunctions += (c => resolve(c.rubberPercentOfGlobal))
        dataFunctions += (c => resolve(c.steelPercentOfGlobal))
        dataFunctions += (c => resolve(c.tungstenPercentOfGlobal))
      else
        dataFunctions += (c => resolve(c.aluminum))
        dataFunctions += (c => resolve(c.chromium))
        dataFunctions += (c => resolve(c.oil))
        dataFunctions += (c => resolve(c.rubber))
        dataFunctions += (c => resolve(c.steel))
        dataFunctions += (c => resolve(c.tungsten))
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
