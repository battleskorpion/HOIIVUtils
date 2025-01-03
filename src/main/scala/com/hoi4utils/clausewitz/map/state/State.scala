package com.hoi4utils.clausewitz.map.state

import com.hoi4utils.clausewitz.{HOIIVFile, HOIIVUtils}
import com.hoi4utils.clausewitz.data.country.CountryTag
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.clausewitz.code.ClausewitzDate
import com.hoi4utils.clausewitz.data.country.Country
import com.hoi4utils.clausewitz.data.country.CountryTagsManager
import com.hoi4utils.clausewitz.map.Owner
import com.hoi4utils.clausewitz.map.UndefinedStateIDException
import com.hoi4utils.clausewitz.map.buildings.Infrastructure
import com.hoi4utils.clausewitz.map.province.VictoryPoint
import com.hoi4utils.clausewitz.map.resources.Resources
import com.hoi4utils.clausewitz_parser.*
import org.jetbrains.annotations.NotNull

import scala.jdk.javaapi.CollectionConverters
import java.io.File
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}


/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 * Localizable: state name
 *
 * I apologize in advance.
 */
object State {
  /* static */
  private val states = new ListBuffer[State]

  /**
   * TODO: Make Demo
   */
  def read(): Unit = {
    if (HOIIVUtils.get("mod.path") == null) {
      System.err.println("Skipped State Reading")
      return
    }
    if (!HOIIVFile.mod_states_folder.exists || !HOIIVFile.mod_states_folder.isDirectory) {
      System.err.println("In State.java - " + HOIIVFile.mod_states_folder + " is not a directory, or etc.")
      return
    }
    if (HOIIVFile.mod_states_folder.listFiles == null || HOIIVFile.mod_states_folder.listFiles.length == 0) {
      System.out.println("No states found in " + HOIIVFile.mod_states_folder)
      return
    }
    // TODO: Good description of what this loop does
    for (stateFile <- HOIIVFile.mod_states_folder.listFiles) {
      if (stateFile.getName.endsWith(".txt")) new State(stateFile)
    }
  }

  def list: ListBuffer[State] = states

  def ownedStatesOfCountry(country: Country): ListBuffer[State] = ownedStatesOfCountry(country.countryTag)

  def ownedStatesOfCountry(tag: CountryTag): ListBuffer[State] = {
    val countryStates = new ListBuffer[State]
    countryStates.addAll(states.flatMap { state =>
      state.owner.get(ClausewitzDate.defaulty) match {
        case Some(owner) if owner.isCountry(tag) => Some(state)
        case _ => None
      }
    })
    countryStates
  }

  def infrastructureOfStates(states: ListBuffer[State]): Infrastructure = {
    var infrastructure = 0
    var population = 0
    var civilianFactories = 0
    var militaryFactories = 0
    var dockyards = 0
    var airfields = 0
    for (state <- states) {
      val stateData = state.getStateInfrastructure
      infrastructure += stateData.infrastructure
      population += stateData.population
      civilianFactories += stateData.civilianFactories
      militaryFactories += stateData.militaryFactories
      dockyards += stateData.navalDockyards
      airfields += stateData.airfields
    }
    new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, dockyards, 0, airfields)
  }

  // todo this is called and ran lots of times, optimize?
  def resourcesOfStates(states: ListBuffer[State]): Resources = {
    val resourcesOfStates = new Resources
    //		int aluminum = 0;
    //		int chromium = 0;
    //		int oil = 0;
    //		int rubber = 0;
    //		int steel = 0;
    //		int tungsten = 0;
    for (state <- states) {
      val resources = state.getResources
      resourcesOfStates.add(resources)
    }
    //return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
    System.out.println(resourcesOfStates.get("aluminum").amt)
    resourcesOfStates
  }

  def resourcesOfStates: Resources = resourcesOfStates(states)

  def numStates(country: CountryTag): Int = ownedStatesOfCountry(country).size

  def get(state_name: String): State = {
    for (state <- states) {
      if (state._name == state_name.trim) return state
    }
    null
  }

  def get(file: File): State = {
    for (state <- states) {
      if (state.stateFile == file) return state
    }
    null
  }

  /**
   * If the state represented by file is not in the list of states, creates the
   * new state.
   * If the state already exists, overwrites the state.
   *
   * @param file state file
   */
  def readState(file: File): Unit = {
    val tempState = new State(file, false)
    if (tempState.stateID < 1) {
      System.err.println("Error: Invalid state id for state " + tempState)
      return
    }
    for (state <- states) {
      if (state.stateID == tempState.stateID) {
        states -= (state)
        states.addOne(tempState)
        System.out.println("Modified state " + tempState)
        return
      }
    }
    // if state did not exist already in states
    states.addOne(tempState)
    System.out.println("Added state " + tempState)
  }

  /**
   * If the state represented by the file exists in states list, removes the state
   * from the states list
   *
   * @param file state file
   */
  def deleteState(file: File): Unit = {
    val tempState = new State(file, false)
    if (tempState.stateID < 1) {
      System.err.println("Error: Invalid state id for state " + tempState)
      return
    }
    for (state <- states) {
      if (state.stateID == tempState.stateID) {
        states -= (state)
        System.out.println("Removed state " + tempState)
        return
      }
    }
    System.out.println("Tried to delete state represented by file: " + "\n\t" + file + "\n\t" + "but state not found in states list")
  }

  def get(id: Int): State = {
    states.find((state: State) => state.stateID == id).orNull
  }

  def getStateDataFunctions(resourcePercentages: Boolean): ListBuffer[Function[State, ?]] = {
    val dataFunctions = new ListBuffer[Function[State, ?]]

    dataFunctions += ((c: State) => c.id)
    dataFunctions += ((c: State) => c.stateInfrastructure.population)
    dataFunctions += ((c: State) => c.stateInfrastructure.civMilRatio)
    dataFunctions += ((c: State) => c.stateInfrastructure.militaryFactories)
    dataFunctions += ((c: State) => c.stateInfrastructure.navalDockyards)
    dataFunctions += ((c: State) => c.stateInfrastructure.airfields)
    dataFunctions += ((c: State) => c.stateInfrastructure.civMilRatio)
    dataFunctions += ((c: State) => c.stateInfrastructure.popPerFactoryRatio)
    dataFunctions += ((c: State) => c.stateInfrastructure.popPerCivRatio)
    dataFunctions += ((c: State) => c.stateInfrastructure.popPerMilRatio)
    dataFunctions += ((c: State) => c.stateInfrastructure.popAirportCapacityRatio)
    /* todo better way to do this obv! plz fix :(
        with (wrapper function that returns either or depndent on resourcesPerfcentages boolean value ofc */
    // also if we're gonna have different resources able to load in down the line... it'll break this.
    if (resourcePercentages) {
      dataFunctions += ((s: State) => s.getResources.get("aluminum").amt)
      dataFunctions += ((s: State) => s.getResources.get("chromium").amt)
      dataFunctions += ((s: State) => s.getResources.get("oil").amt)
      dataFunctions += ((s: State) => s.getResources.get("rubber").amt)
      dataFunctions += ((s: State) => s.getResources.get("steel").amt)
      dataFunctions += ((s: State) => s.getResources.get("tungsten").amt)
    }
    else {
      dataFunctions += ((s: State) => s.getResources.get("aluminum").percentOfGlobal)
      dataFunctions += ((s: State) => s.getResources.get("chromium").percentOfGlobal)
      dataFunctions += ((s: State) => s.getResources.get("oil").percentOfGlobal)
      dataFunctions += ((s: State) => s.getResources.get("rubber").percentOfGlobal)
      dataFunctions += ((s: State) => s.getResources.get("steel").percentOfGlobal)
      dataFunctions += ((s: State) => s.getResources.get("tungsten").percentOfGlobal)
    }
    dataFunctions
  }

  def infrastructureOfCountries: ListBuffer[Infrastructure] = {
    val countryList = CountryTagsManager.getCountryTags.asScala
    val countriesInfrastructureList = new ListBuffer[Infrastructure]
    for (tag <- countryList) {
      countriesInfrastructureList.addOne(infrastructureOfCountry(tag))
    }
    countriesInfrastructureList
  }

  private def infrastructureOfCountry(tag: CountryTag) = infrastructureOfStates(ownedStatesOfCountry(tag))

  // ! todo test if working
  def resourcesOfCountries: ListBuffer[Resources] = {
    val countryList = CountryTagsManager.getCountryTags.asScala
    val countriesResourcesList = new ListBuffer[Resources]
    for (tag <- countryList) {
      countriesResourcesList.addOne(resourcesOfCountry(tag))
    }
    countriesResourcesList
  }

  def resourcesOfCountry(tag: CountryTag): Resources = resourcesOfStates(ownedStatesOfCountry(tag))

  protected def usefulData(data: String): Boolean = if (data.nonEmpty) if (data.trim.charAt(0) == '#') false
  else true
  else false
}

class State(private var stateFile: File, addToStatesList: Boolean) extends InfrastructureData with Localizable with Iterable[State] with Comparable[State] {
  private var stateID = 0
  private var _name: String = stateFile.getName.replace(".txt", "")
  final private var owner: mutable.Map[ClausewitzDate, Owner] =  new mutable.HashMap[ClausewitzDate, Owner]
  //! todo Finish state Category
  // private StateCategory stateCategory; 
  private var stateInfrastructure: Infrastructure = null
  private var resourcesData: Resources = null
  private var victoryPoints: ListBuffer[VictoryPoint] = new ListBuffer[VictoryPoint]

  /* init */
  readStateFile(stateFile)
  // add to states list
  if (addToStatesList) State.states.addOne(this)

  def this(stateFile: File) = {
    this(stateFile, true)
  }

  // todo simplify
  private def readStateFile(stateFile: File): Unit = {
    var infrastructure = 0
    var population = 0
    var civilianFactories = 0
    var militaryFactories = 0
    var dockyards = 0
    // ! todo something important
    // int navalPorts = 0; //has a province location
    var airfields = 0
    
    /* parse state data */
    val stateParser = new Parser(stateFile)
    // Expression exp = stateParser.expressions();
    val stateNode: Node = Try(stateParser.parse.find("state").get) match {
      case Success(node) => node
      case Failure(e: ParserException) => throw new RuntimeException(e)
      case Failure(_) => return
    }
    // id
    if (stateNode.contains("id")) stateID = stateNode.getValue("id").integer
    else {
      System.out.println(stateNode.$.toString)
      throw new UndefinedStateIDException(stateFile)
    }
    // population (manpower)
    if (stateNode.contains("manpower")) population = stateNode.getValue("manpower").integer // todo after here etc.
    // state category
    if (stateNode.contains("state_category")) {
    }
    /* buildings */
    if (stateNode.contains("history")) {
      val historyNode = stateNode.find("history").orNull.asInstanceOf[Node]
      var buildingsNode: Node = null
      if (historyNode.contains("buildings")) buildingsNode = historyNode.find("buildings").orNull.asInstanceOf[Node]
      // owner
      if (historyNode.contains("owner")) {
        // empty date constructor for default date
        historyNode.find("owner").get.$string match {
          case Some(ownerTag) => owner.put(ClausewitzDate.of, new Owner(new CountryTag(ownerTag)))
          case None => System.err.println("Warning: state owner not defined, " + stateFile.getName)
        }
      }
      else System.err.println("Warning: state owner not defined, " + stateFile.getName)
      if (buildingsNode == null) {
        System.err.println("Warning: buildings (incl. infrastructure) not defined in state, " + stateFile.getName)
        stateInfrastructure = null
      }
      else {
        // infrastructure
        if (buildingsNode.contains("infrastructure")) infrastructure = buildingsNode.getValue("infrastructure").integer // todo after here etc.
        // civilian factories
        if (buildingsNode.contains("industrial_complex")) civilianFactories = buildingsNode.getValue("industrial_complex").integer // todo after here etc.
        // military factories
        if (buildingsNode.contains("arms_factory")) militaryFactories = buildingsNode.getValue("arms_factory").integer // todo after here etc.
        // dockyards
        if (buildingsNode.contains("dockyard")) dockyards = buildingsNode.getValue("dockyard").integer // todo after here etc.
        // airfields
        if (buildingsNode.contains("air_base")) airfields = buildingsNode.getValue("air_base").integer // todo after here etc.
      }
      /* victory points */
      if (historyNode.contains("victory_points")) {
        val victoryPointsNode = historyNode.find("victory_points").orNull.asInstanceOf[Node]
        // todo bad code time
        val vpl = CollectionConverters.asJava(victoryPointsNode.toList)
        var vp: VictoryPoint = null
        if (vpl.size == 2) {
          vp = VictoryPoint.of(vpl.get(0).identifier.toDouble.toInt, vpl.get(1).identifier.toDouble.toInt)
          victoryPoints.addOne(vp)
        }
        else System.out.println("Warning: invalid victory point node in state, " + stateFile.getName)
      }
    }
    else {
      System.out.println("Warning: history not defined in state, " + stateFile.getName)
      System.out.println(stateNode.getValue.toString)
    }
    // resources
    resourcesData = findStateResources(stateNode)
    // data record
    stateInfrastructure = new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, dockyards, 0, airfields)
  }

  def findStateResources(stateNode: Node): Resources = {
    if (!stateNode.contains("resources")) return Resources()
    
    val resourcesNode = stateNode.find("resources").orNull.asInstanceOf[Node]

    // aluminum (aluminium bri'ish spelling)
    val aluminum: Int = resourcesNode.find("aluminium") match {
      case Some(al) => al.$intOrElse(0)
      case None => 0
    } 
    val chromium: Int = resourcesNode.find("chromium") match {
      case Some(ch) => ch.$intOrElse(0)
      case None => 0
    }
    val oil: Int = resourcesNode.find("oil") match {
      case Some(o) => o.$intOrElse(0)
      case None => 0
    }
    val rubber: Int = resourcesNode.find("rubber") match {
      case Some(r) => r.$intOrElse(0)
      case None => 0
    }
    val steel: Int = resourcesNode.find("steel") match {
      case Some(s) => s.$intOrElse(0)
      case None => 0
    }
    val tungsten: Int = resourcesNode.find("tungsten") match {
      case Some(t) => t.$intOrElse(0)
      case None => 0
    }
    
    new Resources(aluminum, chromium, oil, rubber, steel, tungsten)
  }

  def getStateInfrastructure: Infrastructure = stateInfrastructure

  def getResources: Resources = resourcesData

  override def toString: String = _name

  def getFile: File = stateFile

  override def getInfrastructureRecord: Infrastructure = stateInfrastructure

  def id: Int = stateID

  @NotNull override def iterator: Iterator[State] = State.states.iterator

  override def compareTo(@NotNull o: State): Int = Integer.compare(stateID, o.stateID)

  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] = mutable.Map(Property.NAME -> _name)

  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = State.states

  def name: String = _name
}
