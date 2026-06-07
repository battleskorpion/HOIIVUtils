package com.hoi4utils.hoi42.history.countries.service

import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi42.history.countries.{CountryFile, CountryFileRegistry}
import com.hoi4utils.hoi42.map.state.service.StateService
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.ZIOParser
import com.hoi4utils.script2.{PDXLoader, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import zio.{RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable.ListBuffer

trait CountryService extends CountryFileRegistry with PDXReadable.Default with LazyLogging {

  def add(country: CountryFile): Iterable[CountryFile]

  def readCountryFile(file: File): Task[CountryFile]
  def countries: Set[CountryFile]
  def list: Set[CountryFile]   // todo rename lols
  def infrastructure(country: CountryFile): URIO[StateService, Infrastructure]
  def removeCountryFile(file: File): Boolean
  def getDataFunctions(resourcePercentages: Boolean = false): Iterable[CountryFile => ?]

  override def clear(): Task[Unit] =
    super[CountryFileRegistry].clear()
}

object CountryService {
  val live: URLayer[CountryTagService & StateService, CountryService] = {
    ZLayer.fromFunction(CountryServiceImpl.apply)
  }
}

case class CountryServiceImpl(countryTagService: CountryTagService, stateService: StateService) extends CountryService {
  override val display: String = "Countries"

  /**
   * Creates [[CountryFile Countries]] from reading files.
   */
  override def read(): Task[Boolean] =
    def readCountryFiles(files: Seq[File], skipDuplicates: Boolean): Task[Seq[CountryFile]] =
      ZIO.foreach(files) { file => readCountryFile(file) }

    val folderMod = HOIIVFiles.Mod.country_folder

    if !folderMod.exists || !folderMod.isDirectory then
      ZIO.logError(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.states_folder} is not a directory, or it does not exist.")
        .as(false)
    else if folderMod.listFiles == null || folderMod.listFiles.isEmpty then
      ZIO.logWarning(s"No countries found in ${HOIIVFiles.Mod.states_folder}")
        .as(false)
    else
      val files = folderMod.listFiles().filter(_.getName.endsWith(".txt"))
      for {
        countries <- readCountryFiles(files, true)
        _ = countries.foreach(add)
      } yield true

  override def readCountryFile(file: File): Task[CountryFile] =
    for {
      node <- new ZIOParser(file).parse
      pdx <- ZIO.attempt {
        val loader = new PDXLoader[CountryFile]()
        val country = new CountryFile(this, Some(file))
        val errors = loader.load(node, country, country)
        if (errors.nonEmpty)
          println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
        country
      }
    } yield pdx

  override def add(country: CountryFile): Iterable[CountryFile] =
    this register country
    countries

  override def countries: Set[CountryFile] = referableEntities.toSet

  override def list: Set[CountryFile] = countries

  override def infrastructure(country: CountryFile): URIO[StateService, Infrastructure] =
    ZIO.serviceWith[StateService] { stateService =>
      stateService.infrastructureOfCountry(country)
    }

  /**
   * If the country represented by the file exists in country files list, removes the country file
   * from the country files list
   *
   * @param file country file
   */
  override def removeCountryFile(file: File): Boolean =
    val temp = new CountryFile(this, Some(file))
    countries.find(_.file.equals(file)).exists(country =>
      this deregister country
      ZIO.logDebug("Removed countryFile " + country)
      true
    )

  /**
   * TODO fix this java doc @ skorp
   * Returns a list of functions that return data about a country
   *
   * @param resourcePercentages if <code>true</code>, returns resource percentages of global instead of resource amounts
   * @return list of functions that return data about a state
   */
  override def getDataFunctions(resourcePercentages: Boolean = false): Iterable[CountryFile => ?] =
    val dataFunctions = ListBuffer[CountryFile => ?]()

    dataFunctions += (c => c.name)
    dataFunctions += (c => infrastructure(c).map(_.population))
    dataFunctions += (c => infrastructure(c).map(_.civilianFactories))
    dataFunctions += (c => infrastructure(c).map(_.militaryFactories))
    dataFunctions += (c => infrastructure(c).map(_.navalDockyards))
    dataFunctions += (c => infrastructure(c).map(_.airfields))
    dataFunctions += (c => infrastructure(c).map(_.civMilRatio))
    dataFunctions += (c => infrastructure(c).map(_.popPerFactoryRatio))
    dataFunctions += (c => infrastructure(c).map(_.popPerCivRatio))
    dataFunctions += (c => infrastructure(c).map(_.popPerMilRatio))
    dataFunctions += (c => infrastructure(c).map(_.popAirportCapacityRatio))
    dataFunctions += (c => infrastructure(c).map(_.popPerStateRatio(c.numOwnedStates)))
    // todo better way to do this obv! plz fix :( with (wrapper function that returns either or depndent on resourcesPerfcentages boolean value ofc
    // also if we're gonna have different resources able to load in down the line... it'll break this.
    if !resourcePercentages then
      //      dataFunctions += (s => s.resourceAmount("aluminium"))
      //      dataFunctions += (s => s.resourceAmount("chromium"))
      //      dataFunctions += (s => s.resourceAmount("oil"))
      //      dataFunctions += (s => s.resourceAmount("rubber"))
      //      dataFunctions += (s => s.resourceAmount("steel"))
      //      dataFunctions += (s => s.resourceAmount("tungsten"))
      // todo
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
    else
      //      dataFunctions += (s => s.resource("aluminium").percentOfGlobal)
      //      dataFunctions += (s => s.resource("chromium").percentOfGlobal)
      //      dataFunctions += (s => s.resource("oil").percentOfGlobal)
      //      dataFunctions += (s => s.resource("rubber").percentOfGlobal)
      //      dataFunctions += (s => s.resource("steel").percentOfGlobal)
      //      dataFunctions += (s => s.resource("tungsten").percentOfGlobal)
      // todo
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
      dataFunctions += (s => "TEMP - FIX")
    dataFunctions
}

