package com.hoi4utils.hoi42.map.state

import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi42.map.resource.Resource
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{ClausewitzDate, ZIOParser}
import com.hoi4utils.script.PDXFileError
import com.hoi4utils.script2.{PDXLoader, PDXReadable}
import javafx.collections.{FXCollections, ObservableList}
import zio.{Task, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable.ListBuffer


trait StateService extends StateRegistry with PDXReadable {

  def get(file: File): URIO[CountryTagService, Option[State]]
  def add(state: State): Iterable[State]

  def list: Set[State]      // todo rename lols
  def get(id: Int): Option[State]
  def get(state_name: String): Option[State]
  def observeStates: ObservableList[State]
  def ownedStatesOfCountry(country: CountryFile): Seq[State]
  def ownedStatesOfCountry(tag: CountryTag): Seq[State]
  def infrastructureOfStates(states: Iterable[State]): Infrastructure
  def resourcesOfStates(states: Iterable[State]): Set[Resource]
  def resourcesOfStates: List[Resource]
  def numStates(country: CountryTag): Int
  implicit def globalResources: Set[Resource]
  def readState(file: File): Boolean
  def removeState(file: File): Boolean
  def getDataFunctions(resourcePercentages: Boolean = false): Iterable[State => ?]
  def infrastructureOfCountries: Seq[Infrastructure]
  def infrastructureOfCountry(tag: CountryTag): Infrastructure
  def infrastructureOfCountry(country: CountryFile): Infrastructure
  def resourcesOfCountries: URIO[CountryTagService, Seq[Set[Resource]]]
  def resourcesOfCountry(tag: CountryTag): Set[Resource]
  def resourcesOfCountry(country: CountryFile): Set[Resource]

  override def clear(): Task[Unit] =
    super[StateRegistry].clear()
}

object StateService {
  val live: URLayer[CountryTagService, StateService] =
    ZLayer.fromFunction(StateServiceImpl.apply)
}

case class StateServiceImpl(countryTagService: CountryTagService) extends StateService {
  override val display: String = "States"

  /**
   * Creates States from reading files
   */
  def read(): Task[Boolean] =
    def readStates(files: Seq[File], skipDuplicates: Boolean): Task[Seq[State]] = {
      ZIO.foreach(files) { file =>
        for {
          node <- new ZIOParser(file).parse
          pdx <- ZIO.attempt {
            val loader = new PDXLoader[State]()
            val state = new State(this, Some(f))
            val errors = loader.load(node, state, state)
            if (errors.nonEmpty) {
              println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
            }
            state
          }
        } yield pdx
      }
    }

    if !HOIIVFiles.Mod.states_folder.exists || !HOIIVFiles.Mod.states_folder.isDirectory then
      ZIO.logError(s"In State.java - ${HOIIVFiles.Mod.states_folder} is not a directory, or it does not exist.")
      ZIO.succeed(false)
    else if HOIIVFiles.Mod.states_folder.listFiles == null || HOIIVFiles.Mod.states_folder.listFiles.isEmpty then
      ZIO.logError(s"No states found in ${HOIIVFiles.Mod.states_folder}")
      ZIO.succeed(false)
    else
      val files = HOIIVFiles.Mod.states_folder.listFiles().filter(_.getName.endsWith(".txt"))
      for {
        states <- readStates(files, true)
        _ = states.foreach(add)
      } yield true

  override def states: Set[State] = referableEntities.toSet

  // todo this should exist im being lazy
//  override def get(file: File): URIO[CountryTagService, Option[State]] =
//    for {
//      countryTagService <- ZIO.service[CountryTagService]
//      state =
//        if file == null then None
//        else if !states.exists(_.stateFile.contains(file)) then {
//          val newState = new State(file)(countryTagService)
//          add(newState)
//          Some(newState)
//        } else states.find(_.stateFile.contains(file))
//    } yield state

  override def add(state: State): Iterable[State] =
    this register state
    states

  override def list: Set[State] = states.toSet

  override def get(id: Int): Option[State] =
    states.find(_.stateID @== id)

  override def get(state_name: String): Option[State] =
    states.find(_.name @== state_name)

  override def observeStates: ObservableList[State] =
    FXCollections.observableArrayList(CollectionConverters.asJava(states))

  override def ownedStatesOfCountry(country: CountryFile): ListBuffer[State] = ownedStatesOfCountry(country.countryTag)

  override def ownedStatesOfCountry(tag: CountryTag): ListBuffer[State] =
    states filter (state => state.owner(ClausewitzDate.defaulty).exists(_.equals(tag)))

  override def infrastructureOfStates(states: ListBuffer[State]): Infrastructure =
    var infrastructure = 0
    var population = 0
    var civilianFactories = 0
    var militaryFactories = 0
    var dockyards = 0
    var airfields = 0
    for state <- states do
      val stateData = state.getStateInfrastructure
      infrastructure += stateData.infrastructure
      population += stateData.population
      civilianFactories += stateData.civilianFactories
      militaryFactories += stateData.militaryFactories
      dockyards += stateData.navalDockyards
      airfields += stateData.airfields
    new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, dockyards, 0, airfields)

  // todo this is called and ran lots of times, optimize?
  override def resourcesOfStates(states: ListBuffer[State]): List[Resource] =
    //    val resourcesOfStates = new Resources
    //    for (state <- states) {
    //      val resources = state.getResources
    //      resourcesOfStates.add(resources)
    //    }
    //    //return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
    //    logger.debug(resourcesOfStates.get("aluminum").amt)
    //    resourcesOfStates
    if states.isEmpty then Resource.newList()
    else
      states
        .flatMap(_.listResources).groupBy(_.pdxTypeIdentifier).values.map: resources =>
          new Resource(resources.head.pdxTypeIdentifier, resources.map(_.getOrElse(0)).sum)
        .toList

  def resourcesOfStates: List[Resource] = resourcesOfStates(states)

  def numStates(country: CountryTag): Int = ownedStatesOfCountry(country).size

  implicit def globalResources: List[Resource] = states.flatMap(_.listResources).toList

  // todo idk if this is still needed seems duplicateish as well
//  /**
//   * If the state represented by file is not in the list of states, creates the
//   * new state.
//   * If the state already exists, overwrites the state.
//   *
//   * @param file state file
//   */
//  override def readState(file: File): Boolean =
//    if file == null || !file.exists || file.isDirectory then
//      ZIO.logError(s"In State.java - ${file} is a directory, or it does not exist.")
//      false
//    else
//      val state = new State(file)(countryTagService)
//      add(state)
//      true

  /**
   * If the state represented by the file exists in states list, removes the state
   * from the states list
   *
   * @param file state file
   */
  override def removeState(file: File): Boolean = boundary:
    val tempState = new State()(countryTagService)
    for state <- states do
      if state.stateID == tempState.stateID then
        states -= state
        ZIO.logDebug("Removed state " + tempState)
        boundary.break(true)
    false

  /**
   * TODO fix this java doc @ skorp
   * Returns a list of functions that return data about a state
   *
   * @param resourcePercentages if <code>true</code>, returns resource percentages of global instead of resource amounts
   * @return list of functions that return data about a state
   */
  override def getDataFunctions(resourcePercentages: Boolean = false): Iterable[State => ?] =
    val dataFunctions = ListBuffer[State => ?]()

    dataFunctions += (s => s.id)
    dataFunctions += (s => s.population)
    dataFunctions += (s => s.civilianFactories)
    dataFunctions += (s => s.militaryFactories)
    dataFunctions += (s => s.navalDockyards)
    dataFunctions += (s => s.airfields)
    dataFunctions += (s => s.civMilFactoryRatio)
    dataFunctions += (s => s.populationFactoryRatio)
    dataFunctions += (s => s.populationCivFactoryRatio)
    dataFunctions += (s => s.populationMilFactoryRatio)
    dataFunctions += (s => s.populationAirCapacityRatio)
    // todo better way to do this obv! plz fix :( with (wrapper function that returns either or depndent on resourcesPerfcentages boolean value ofc
    // also if we're gonna have different resources able to load in down the line... it'll break this.
    if !resourcePercentages then
      dataFunctions += (s => s.resourceAmount("aluminium"))
      dataFunctions += (s => s.resourceAmount("chromium"))
      dataFunctions += (s => s.resourceAmount("oil"))
      dataFunctions += (s => s.resourceAmount("rubber"))
      dataFunctions += (s => s.resourceAmount("steel"))
      dataFunctions += (s => s.resourceAmount("tungsten"))
    else
      dataFunctions += (s => s.resource("aluminium").percentOfGlobal)
      dataFunctions += (s => s.resource("chromium").percentOfGlobal)
      dataFunctions += (s => s.resource("oil").percentOfGlobal)
      dataFunctions += (s => s.resource("rubber").percentOfGlobal)
      dataFunctions += (s => s.resource("steel").percentOfGlobal)
      dataFunctions += (s => s.resource("tungsten").percentOfGlobal)
    dataFunctions

  override def infrastructureOfCountries: ListBuffer[Infrastructure] =
    val countryList = countryTagService.countryTags
    val countriesInfrastructureList = new ListBuffer[Infrastructure]
    for tag <- countryList do
      countriesInfrastructureList.addOne(infrastructureOfCountry(tag))
    countriesInfrastructureList

  override def infrastructureOfCountry(tag: CountryTag): Infrastructure = infrastructureOfStates(ownedStatesOfCountry(tag))

  override def infrastructureOfCountry(country: CountryFile): Infrastructure = infrastructureOfStates(ownedStatesOfCountry(country))

  // ! todo test if working
  override def resourcesOfCountries: URIO[CountryTagService, List[List[Resource]]] =
    for {
      tagService <- ZIO.service[CountryTagService]
      list =
        val countryList = tagService.countryTags
        val countriesResourcesList = new ListBuffer[List[Resource]]
        for tag <- countryList do countriesResourcesList.addOne(resourcesOfCountry(tag))
        countriesResourcesList.toList
    } yield list

  override def resourcesOfCountry(tag: CountryTag): List[Resource] = resourcesOfStates(ownedStatesOfCountry(tag))

  override def resourcesOfCountry(country: CountryFile): List[Resource] = resourcesOfStates(ownedStatesOfCountry(country))

  protected def usefulData(data: String): Boolean = if data.nonEmpty then if data.trim.charAt(0) == '#' then false
  else true
  else false
}
