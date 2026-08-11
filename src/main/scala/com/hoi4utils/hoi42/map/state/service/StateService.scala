package com.hoi4utils.hoi42.map.state.service

import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi42.history.countries.CountryFile
import com.hoi4utils.hoi42.map.resource.Resource
import com.hoi4utils.hoi42.map.state.StateExtensions.*
import com.hoi4utils.hoi42.map.state.{State, StateRegistry}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.NodeExtensions.getTyped
import com.hoi4utils.parser.{ClausewitzDate, NodeSeq, ZIOParser}
import com.hoi4utils.script.PDXFileError
import com.hoi4utils.script2.PDXPropertyValueExtensions.*
import com.hoi4utils.script2.{PDXLoader, PDXReadable}
import javafx.collections.{FXCollections, ObservableList}
import zio.{Task, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters


trait StateService extends StateRegistry with PDXReadable.Default {

//  def get(file: File): URIO[CountryTagService, Option[State]] // if add rename
  def add(state: State): Iterable[State]

  def readState(file: File): Task[State]
  def states: Set[State]
  def list: Set[State]      // todo rename lols
  def find(id: Int): Option[State]
  def find(state_name: String): Option[State]
  def observeStates: ObservableList[State]
  def ownedStatesOfCountry(country: CountryFile): Set[State]
  def ownedStatesOfCountry(tag: CountryTag): Set[State]
  def infrastructureOfStates(states: Iterable[State]): Infrastructure
  def resourcesOfStates(states: Iterable[State]): Set[Resource]
  def resourcesOfStates: Set[Resource]
  def numStates(country: CountryTag): Int
  implicit def globalResources: Set[Resource]
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
   * Creates [[State States]] from reading files.
   */
  def read(): Task[Boolean] =
    def readStates(files: Seq[File], skipDuplicates: Boolean): Task[Seq[State]] =
      for {
        results <- ZIO.foreach(files) { file =>
          readState(file).foldZIO(
            err => ZIO.logWarning(s"Parse failed for ${file.getName}: $err").as(None),
            pdx => ZIO.some(pdx)
          )
        }
      } yield results.flatten


    if !HOIIVFiles.Mod.states_folder.exists || !HOIIVFiles.Mod.states_folder.isDirectory then
      ZIO.logError(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.states_folder} is not a directory, or it does not exist.")
        .as(false)
    else if HOIIVFiles.Mod.states_folder.listFiles == null || HOIIVFiles.Mod.states_folder.listFiles.isEmpty then
      ZIO.logWarning(s"No states found in ${HOIIVFiles.Mod.states_folder}")
        .as(false)
    else
      val files = HOIIVFiles.Mod.states_folder.listFiles().filter(_.getName.endsWith(".txt"))
      for {
        _      <- ZIO.logDebug("Starting readStates...")
        states <- readStates(files, true)
        _ <- ZIO.logDebug(s"Read ${files.length} state files in ${HOIIVFiles.Mod.states_folder}")

        (validStates, invalidStates) = states.partition(_.referableID.isDefined)
        _ <- ZIO.when(invalidStates.nonEmpty) {
          ZIO.logWarning(s"Skipping ${invalidStates.size} state file(s) missing an ID: " +
            invalidStates.map(_.file.map(_.getName).getOrElse("unknown")).mkString(", "))
        }

        _ <- ZIO.attempt {
          validStates.foreach(add)
        }.tapErrorCause { cause =>
          ZIO.logErrorCause(s"[FATAL] Failed to add valid states to registry", cause)
        }
      } yield true

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

  override def readState(file: File): Task[State] =
    for {
      node <- new ZIOParser(file).parse
      pdx <- ZIO.attempt {
        val loader = new PDXLoader[State]()
        val state = new State(this, Some(file))
        val pdxNode = node.getTyped[NodeSeq]("state")
        val errors = loader.load(pdxNode, state, state)
        if (errors.nonEmpty)
          println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
        state
      }
    } yield pdx

  override def add(state: State): Iterable[State] =
    this register state
    states

  override def states: Set[State] = referableEntities.toSet

  override def list: Set[State] = states

  override def find(id: Int): Option[State] =
    states.find(_.stateID @== id)

  override def find(state_name: String): Option[State] =
    states.find(_.name @== state_name)

  override def observeStates: ObservableList[State] =
    FXCollections.observableArrayList(CollectionConverters.asJava(states))

  override def ownedStatesOfCountry(country: CountryFile): Set[State] = ownedStatesOfCountry(country.countryTag)

  override def ownedStatesOfCountry(tag: CountryTag): Set[State] =
    states filter (state => state.owner(ClausewitzDate.defaulty).exists(_.equals(tag)))

  override def infrastructureOfStates(states: Iterable[State]): Infrastructure =
    states.map(s => s.stateInfrastructure)
      .reduce((s1, s2) => Infrastructure.combine(s1, s2))

  // todo this is called and ran lots of times, optimize?
  override def resourcesOfStates(states: Iterable[State]): Set[Resource] =
//    if states.isEmpty then Resource.newList()
//    else
//      states
//        .flatMap(_.listResources).groupBy(_.pdxTypeIdentifier).values.map: resources =>
//          new Resource(resources.head.pdxTypeIdentifier, resources.map(_.getOrElse(0)).sum)
//        .toList
    // todo
    Set.empty

  def resourcesOfStates: Set[Resource] = resourcesOfStates(states)

  def numStates(country: CountryTag): Int = ownedStatesOfCountry(country).size

  implicit def globalResources: Set[Resource] = states.flatMap(_.resources.$)

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
  override def removeState(file: File): Boolean =
    val tempState = new State(this, Some(file))
    states.find(_.stateID @== tempState.stateID).exists(state =>
      this deregister state
      ZIO.logDebug("Removed state " + state)
      true
    )

  /**
   * TODO fix this java doc @ skorp
   * Returns a list of functions that return data about a state
   *
   * @param resourcePercentages if <code>true</code>, returns resource percentages of global instead of resource amounts
   * @return list of functions that return data about a state
   */
  override def getDataFunctions(resourcePercentages: Boolean = false): Iterable[State => ?] =
    val dataFunctions = ListBuffer[State => ?]()

    dataFunctions += (s => s.stateID.getOrElse("[Unknown]"))
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

  override def infrastructureOfCountries: Seq[Infrastructure] =
    val countryList = countryTagService.countryTags
    val countriesInfrastructureList = new ListBuffer[Infrastructure]
    for tag <- countryList do
      countriesInfrastructureList.addOne(infrastructureOfCountry(tag))
    countriesInfrastructureList.toSeq

  override def infrastructureOfCountry(tag: CountryTag): Infrastructure = infrastructureOfStates(ownedStatesOfCountry(tag))

  override def infrastructureOfCountry(country: CountryFile): Infrastructure = infrastructureOfStates(ownedStatesOfCountry(country))

  // ! todo test if working
  override def resourcesOfCountries: URIO[CountryTagService, List[Set[Resource]]] =
    for {
      tagService <- ZIO.service[CountryTagService]
      list =
        val countryList = tagService.countryTags
        val countriesResourcesList = new ListBuffer[Set[Resource]]
        for tag <- countryList do countriesResourcesList.addOne(resourcesOfCountry(tag))
        countriesResourcesList.toList
    } yield list

  override def resourcesOfCountry(tag: CountryTag): Set[Resource] = resourcesOfStates(ownedStatesOfCountry(tag))

  override def resourcesOfCountry(country: CountryFile): Set[Resource] = resourcesOfStates(ownedStatesOfCountry(country))

  protected def usefulData(data: String): Boolean =
    if data.nonEmpty then
      if data.trim.charAt(0) == '#' then false
      else true
    else false
}


