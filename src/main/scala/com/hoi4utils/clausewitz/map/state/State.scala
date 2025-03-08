package com.hoi4utils.clausewitz.map.state

import com.hoi4utils.clausewitz.code.ClausewitzDate
import com.hoi4utils.clausewitz.data.country.{Country, CountryTag, CountryTagsManager}
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.clausewitz.map.{Owner, UndefinedStateIDException}
import com.hoi4utils.clausewitz.map.buildings.Infrastructure
import com.hoi4utils.clausewitz.map.province.Province
import com.hoi4utils.clausewitz.map.state.State.History
import com.hoi4utils.clausewitz.script.*
import com.hoi4utils.clausewitz.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.clausewitz_parser.*
import javafx.collections.{FXCollections, ObservableList}
import org.apache.logging.log4j.{LogManager, Logger}
import org.jetbrains.annotations.NotNull

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}
import scala.jdk.CollectionConverters.*
import scala.jdk.javaapi.CollectionConverters
import scala.util.{Failure, Success, Try}

/**
 * Represents a state in HOI4
 * @param stateFile state file
 * @param addToStatesList if true, adds the state to the list of states
 */
class State(addToStatesList: Boolean) extends StructuredPDX("state") with InfrastructureData with Localizable with Iterable[Province] with Comparable[State] {
  private val LOGGER: Logger = LogManager.getLogger(getClass)

  final val stateID = new IntPDX("id")
  final val name = new StringPDX("name")
  final val resources = new CollectionPDX[Resource](Resource(), "resources") {
    override def loadPDX(expr: Node): Unit = {
      try {
        super.loadPDX(expr)
      } catch {
        case e: Exception =>
          this.LOGGER.error(s"Error loading resources for state ${stateID.value.getOrElse("unknown")}: ${e.getMessage}")
      }
    }

    override def getPDXTypeName: String = "Resources"
  }
  final val history = new History
  final val provinces = {
    val loadNewProvince = () => {val p = new Province(); Province.add(p); p}
    new ListPDX[Province](loadNewProvince, "provinces")
  }
  final val manpower = new IntPDX("manpower")
  final val buildingsMaxLevelFactor = new DoublePDX("buildings_max_level_factor")
  final val state_category = new StateCategory("state_category")
  final val local_supplies = new DoublePDX("local_supplies")

  private var _stateFile: Option[File] = None

  /* init */
  if (addToStatesList) State.add(this)

  def this(addToStatesList: Boolean, file: File) = {
    this(addToStatesList)
    loadPDX(file)
    setFile(file)
  }

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(stateID, name, resources, history, provinces, manpower, buildingsMaxLevelFactor,
      state_category, local_supplies)
  }

//  private def parseStateNode(stateFile: File): Option[Node] = {
//    val stateParser = new Parser(stateFile)
//    Try(stateParser.parse.find("state").get) match {
//      case Success(node) => Some(node)
//      case Failure(e: ParserException) => throw new RuntimeException(e)
//      case Failure(_) => None
//    }
//  }
//
//  private def extractStateID(stateNode: Node, stateFile: File): Int = {
//    if (stateNode.contains("id")) {
//      stateNode.getValue("id").integer
//    } else {
//      LOGGER.error(s"State ID not found in file: ${stateFile.getName}, ${stateNode.$}")
//      throw new UndefinedStateIDException(stateFile)
//    }
//  }

//  // todo breh nah
//  private def extractPopulation(stateNode: Node): Int = {
//    stateNode.find("manpower").map(_.value match {
//      case Some(v) => v match {
//        case i: Int => i
//        case s: String => s.toIntOption.getOrElse(0) // Convert string to Int safely
//        case _ => 0
//      }
//      case None => 0
//    }).getOrElse(0)
//  }
//
//
//  private def extractStateCategory(stateNode: Node): Unit = {
//    if (stateNode.contains("state_category")) {
//      // TODO: Implement state category logic
//    }
//  }
//
//  private def extractOwner(historyNode: Node, stateFile: File): Unit = {
//    if (historyNode.contains("owner")) {
//      historyNode.find("owner").get.$string match {
//        case Some(ownerTag) => owner.put(ClausewitzDate.of, new Owner(new CountryTag(ownerTag)))
//        case None => LOGGER.error(s"Warning: state owner not defined in ${stateFile.getName}")
//      }
//    } else {
//      LOGGER.error(s"Warning: state owner not defined in ${stateFile.getName}")
//    }
//  }

//  private def extractVictoryPoints(historyNode: Node, stateFile: File): Unit = historyNode.find("victory_points") match {
//    case Some(victoryPointsNode) =>
//      val vpl = CollectionConverters.asJava(victoryPointsNode.toList)
//      if (vpl.size == 2) {
////        victoryPoints.addOne(VictoryPoint.of(vpl.get(0).identifier.toInt, vpl.get(1).identifier.toInt)) //TODO
//      } else {
//        LOGGER.warn(s"Invalid victory point node in state: ${stateFile.getName}")
//      }
//    case None =>  LOGGER.info(s"Victory points not defined in state: ${stateFile.getName}")
//  }


//  private def findStateResources(stateNode: Node): Resources = {
//    if (!stateNode.contains("resources")) return Resources()
//
//    val resourcesNode = stateNode.find("resources").orNull
//
//    // aluminum (aluminium bri'ish spelling)
//    val aluminum: Int = resourcesNode.find("aluminium") match {
//      case Some(al) => al.$intOrElse(0)
//      case None => 0
//    }
//    val chromium: Int = resourcesNode.find("chromium") match {
//      case Some(ch) => ch.$intOrElse(0)
//      case None => 0
//    }
//    val oil: Int = resourcesNode.find("oil") match {
//      case Some(o) => o.$intOrElse(0)
//      case None => 0
//    }
//    val rubber: Int = resourcesNode.find("rubber") match {
//      case Some(r) => r.$intOrElse(0)
//      case None => 0
//    }
//    val steel: Int = resourcesNode.find("steel") match {
//      case Some(s) => s.$intOrElse(0)
//      case None => 0
//    }
//    val tungsten: Int = resourcesNode.find("tungsten") match {
//      case Some(t) => t.$intOrElse(0)
//      case None => 0
//    }
//
//    new Resources(aluminum, chromium, oil, rubber, steel, tungsten)
//  }

  def getStateInfrastructure: Infrastructure = {
    new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, navalDockyards, navalPorts, airfields)
  }

  def listResources: List[Resource] = {
    resources.toList
  }

  override def toString: String = {
    name.value match {
      case Some(n) => n
      case None => stateID.value match {
        case Some(i) => i.toString
        case None => "[Unknown]"
      }
    }
  }

  def stateFile: Option[File] = _stateFile

  def setFile(file: File): Unit = {
    _stateFile = Some(file)
  }

  def resourceAmount(name: String): Double = resources.find(_.pdxTypeIdentifier.equals(name)) match {
    case Some(r) => r.getOrElse(0)
    case None => 0.0
  }
  
  def owner(date: ClausewitzDate): Option[CountryTag] = {
    history.owner.value match {
      case Some(owner) => Some(owner)
      case None => None
    }
  }

  def population: Int = manpower.getOrElse(1)

  def infrastructure: Int = history.buildings.infrastructure.getOrElse(0)

  def civilianFactories: Int = history.buildings.civilianFactories.getOrElse(0)

  def militaryFactories: Int = history.buildings.militaryFactories.getOrElse(0)

  def navalDockyards: Int = history.buildings.navalDockyards.getOrElse(0)

  def navalPorts: Int = history.buildings.navalDockyards.getOrElse(0)

  def airfields: Int = history.buildings.airBase.getOrElse(0)

  def civMilRatio: Double = {
    if (civilianFactories == 0) 0.0
    else militaryFactories.toDouble / civilianFactories
  }

  def popPerFactoryRatio: Double = {
    if (civilianFactories == 0) 0.0
    else population.toDouble / civilianFactories
  }

  def popPerCivRatio: Double = {
    if (civilianFactories == 0) 0.0
    else population.toDouble / civilianFactories
  }

  def popPerMilRatio: Double = {
    if (militaryFactories == 0) 0.0
    else population.toDouble / militaryFactories
  }

  def popAirportCapacityRatio: Double = {
    if (airfields == 0) 0.0
    else population.toDouble / airfields
  }

  def infrastructureMaxLevel: Int = {
    if (buildingsMaxLevelFactor.getOrElse(0) == 0) 0
    else (infrastructure * buildingsMaxLevelFactor.getOrElse(0)).toInt
  }

  def resource(name: String): Resource = resources.find(_.pdxTypeIdentifier.equals(name)) match {
    case Some(r) => r
    case None => new Resource(name, 0)
  }

  override def getInfrastructureRecord: Infrastructure = getStateInfrastructure

  override def compareTo(@NotNull o: State): Int = stateID.compareTo(o.stateID) match {
    case Some(v) => v
    case None => throw new NullPointerException("State has no ID")
  }

  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] = {
    val id = this.stateID.value match {
      case Some(i) => i.toString
      case None => ""
    }
    mutable.Map(Property.NAME -> id)
  }

  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = State.states

  override def iterator: Iterator[Province] = provinces.iterator
}

/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 * Localizable: state name
 *
 * I apologize in advance.
 */
object State extends Iterable[State] {
  private val LOGGER: Logger = LogManager.getLogger(getClass)
  /* static */
  private val states = new ListBuffer[State]

  def get(file: File): Option[State] = {
    if (file == null) return None
    if (!states.exists(_.stateFile.contains(file))) new State(true, file)
    states.find(_.stateFile.contains(file))
  }

  def observeStates: ObservableList[State] = {
    FXCollections.observableArrayList(CollectionConverters.asJava(states))
  }

  /**
   * Creates States from reading files
   */
  def read(): Boolean = {
    if (!HOIIVFiles.Mod.states_folder.exists || !HOIIVFiles.Mod.states_folder.isDirectory) {
      LOGGER.fatal(s"In State.java - ${HOIIVFiles.Mod.states_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.states_folder.listFiles == null || HOIIVFiles.Mod.states_folder.listFiles.isEmpty) {
      LOGGER.fatal(s"No states found in ${HOIIVFiles.Mod.states_folder}")
      false
    } else {
      LOGGER.info(s"Reading states from ${HOIIVFiles.Mod.states_folder}")

      HOIIVFiles.Mod.states_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
        new State(true, f)
      }
      true
    }
  }

  def clear(): Unit = {
    states.clear()
  }

  def add(state: State): Iterable[State] = {
    states += state
    states
  }

  def list: List[State] = states.toList

  def get(id: Int): Option[State] ={
    states.find(_.stateID @== id)
  }

  def get(state_name: String): Option[State] = {
    states.find(_.name @== state_name)
  }

  def ownedStatesOfCountry(country: Country): ListBuffer[State] = ownedStatesOfCountry(country.countryTag)

  def ownedStatesOfCountry(tag: CountryTag): ListBuffer[State] = {
    states filter(state => state.owner(ClausewitzDate.defaulty).exists(_.equals(tag)))
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
  def resourcesOfStates(states: ListBuffer[State]): List[Resource] = {
    //    val resourcesOfStates = new Resources
    //    for (state <- states) {
    //      val resources = state.getResources
    //      resourcesOfStates.add(resources)
    //    }
    //    //return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
    //    System.out.println(resourcesOfStates.get("aluminum").amt)
    //    resourcesOfStates
    if (states.isEmpty) Resource.newList()
    else {
      states
        .flatMap(_.listResources).groupBy(_.pdxTypeIdentifier).values.map { resources =>
        new Resource(resources.head.pdxTypeIdentifier, resources.map(_.getOrElse(0)).sum)
      }.toList
    }
  } 

  def resourcesOfStates: List[Resource] = resourcesOfStates(states)

  def numStates(country: CountryTag): Int = ownedStatesOfCountry(country).size

  /**
   * If the state represented by file is not in the list of states, creates the
   * new state.
   * If the state already exists, overwrites the state.
   *
   * @param file state file
   */
  def readState(file: File): Boolean = {
    if (file == null || !file.exists || file.isDirectory) {
      LOGGER.fatal(s"In State.java - ${file} is a directory, or it does not exist.")
      false
    } else {
      LOGGER.info(s"Reading state from ${file}")
      new State(true, file)
      true
    }
  }

  /**
   * If the state represented by the file exists in states list, removes the state
   * from the states list
   *
   * @param file state file
   */
  def removeState(file: File): Boolean = {
    val tempState = new State(false)
    for (state <- states) {
      if (state.stateID == tempState.stateID) {
        states -= state
        LOGGER.debug("Removed state " + tempState)
        return true
      }
    }
    false
  }

  def getDataFunctions(resourcePercentages: Boolean): Iterable[State => ?] = {
    val dataFunctions = ListBuffer[State => ?]()

    dataFunctions += (s => s.stateID)
    dataFunctions += (s => s.population)
    dataFunctions += (s => s.civMilRatio)
    dataFunctions += (s => s.militaryFactories)
    dataFunctions += (s => s.navalDockyards)
    dataFunctions += (s => s.airfields)
    dataFunctions += (s => s.civMilRatio)
    dataFunctions += (s => s.popPerFactoryRatio)
    dataFunctions += (s => s.popPerCivRatio)
    dataFunctions += (s => s.popPerMilRatio)
    dataFunctions += (s => s.popAirportCapacityRatio)
    /* todo better way to do this obv! plz fix :(
        with (wrapper function that returns either or depndent on resourcesPerfcentages boolean value ofc */
    // also if we're gonna have different resources able to load in down the line... it'll break this.
    if (resourcePercentages) {
      dataFunctions += (s => s.resourceAmount("aluminum"))
      dataFunctions += (s => s.resourceAmount("chromium"))
      dataFunctions += (s => s.resourceAmount("oil"))
      dataFunctions += (s => s.resourceAmount("rubber"))
      dataFunctions += (s => s.resourceAmount("steel"))
      dataFunctions += (s => s.resourceAmount("tungsten"))
    }
    else {
      val globalResources = states.flatMap(_.listResources)
      dataFunctions += (s => s.resource("aluminum").percentOfGlobal(globalResources))
      dataFunctions += (s => s.resource("chromium").percentOfGlobal(globalResources))
      dataFunctions += (s => s.resource("oil").percentOfGlobal(globalResources))
      dataFunctions += (s => s.resource("rubber").percentOfGlobal(globalResources))
      dataFunctions += (s => s.resource("steel").percentOfGlobal(globalResources))
      dataFunctions += (s => s.resource("tungsten").percentOfGlobal(globalResources))
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
  def resourcesOfCountries: List[List[Resource]] = {
    val countryList = CountryTagsManager.getCountryTags.asScala
    val countriesResourcesList = new ListBuffer[List[Resource]]
    for (tag <- countryList) {
      countriesResourcesList.addOne(resourcesOfCountry(tag))
    }
    countriesResourcesList.toList
  }

  def resourcesOfCountry(tag: CountryTag): List[Resource] = resourcesOfStates(ownedStatesOfCountry(tag))

  protected def usefulData(data: String): Boolean = if (data.nonEmpty) if (data.trim.charAt(0) == '#') false
  else true
  else false

  @NotNull override def iterator: Iterator[State] = states.iterator

  class History extends StructuredPDX("history") {
    final val owner = new ReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), "owner")
    final val buildings = new BuildingsPDX
//    final MultiPDX[VictoryPoint] victoryPoints = new MultiPDX(None, Some())
    //final val addCoreOf = new MultiReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), List("add_core_of")) // TODO

    def this(node: Node) = {
      this()
      loadPDX(node)
    }

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
      ListBuffer(owner, buildings)
    }
  }

  class BuildingsPDX extends StructuredPDX("buildings") {
    final val infrastructure = new IntPDX("infrastructure")
    final val civilianFactories = new IntPDX("industrial_complex")
    final val militaryFactories = new IntPDX("arms_factory")
    final val navalDockyards = new IntPDX("naval_base")
    final val airBase = new IntPDX("air_base")

    def this(node: Node) = {
      this()
      loadPDX(node)
    }

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
      ListBuffer(infrastructure, civilianFactories, militaryFactories, navalDockyards, airBase)
    }
  }
}