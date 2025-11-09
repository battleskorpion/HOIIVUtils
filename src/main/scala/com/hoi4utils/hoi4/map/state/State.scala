package com.hoi4utils.hoi4.map.state

import com.hoi4utils.*
import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.localization.*
import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi4.map.province.Province
import com.hoi4utils.hoi4.map.resource.Resource
import com.hoi4utils.hoi4.map.state
import com.hoi4utils.hoi4.map.state.InfrastructureData
import com.hoi4utils.hoi4.map.state.State.History
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.*
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.StringPDX
import com.hoi4utils.shared.{BoolType, ExpectedRange}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import org.jetbrains.annotations.NotNull

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters
import scala.util.boundary

/**
 * Represents a state in HOI4
 * @param stateFile state file
 * @param addToStatesList if true, adds the state to the list of states
 */
class State(addToStatesList: Boolean, file: File = null) extends StructuredPDX("state") with InfrastructureData
  with Localizable with Iterable[Province] with Comparable[State] with PDXFile with Referable with LazyLogging:

  final val stateID = new IntPDX("id")
  final val name = new StringPDX("name")
  /**
   * Resources in this state
   * Note: loading resources can sometimes fail due to malformed state files, so we catch exceptions here and log them
   */
  final val resources = new CollectionPDX[Resource](Resource(), "resources") {
    override def getPDXTypeName: String = "Resources"
  }
  final val history = new History()
  final val provinces =
    val loadNewProvince = () =>
      val p = new Province()
      Province.add(p)
      p
    new ListPDX[Province](loadNewProvince, "provinces")
  final val manpower = new IntPDX("manpower")
  final val buildingsMaxLevelFactor = new DoublePDX("buildings_max_level_factor")
  final val state_category = new StateCategory("state_category")
  final val local_supplies = new DoublePDX("local_supplies")
  final val impassible = new BooleanPDX("impassible", false, BoolType.YES_NO)

  private var _stateFile: Option[File] = None

  /* init */
  if addToStatesList then State.add(this)

  file match
    case null => // create empty state
    case _ =>
      require(file.exists && file.isFile, s"State $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    val pdxError = new PDXError(
      exception = exception,
      errorNode = node,
      file = if file != null then Some(file) else _stateFile,
      pdxScript = this
    ).addInfo("stateID", stateID.asOptionalString.getOrElse("unknown"))
    State.stateErrors += pdxError

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Seq[PDXScript[?]] =
    ListBuffer(stateID, name, resources, history, provinces, manpower, buildingsMaxLevelFactor,
      state_category, local_supplies, impassible)

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
//      logger.error(s"State ID not found in file: ${stateFile.getName}, ${stateNode.$}")
//      throw new UndefinedStateIDException(stateFile)
//    }
//  }
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
//        case None => logger.error(s"Warning: state owner not defined in ${stateFile.getName}")
//      }
//    } else {
//      logger.error(s"Warning: state owner not defined in ${stateFile.getName}")
//    }
//  }

//  private def extractVictoryPoints(historyNode: Node, stateFile: File): Unit = historyNode.find("victory_points") match {
//    case Some(victoryPointsNode) =>
//      val vpl = CollectionConverters.asJava(victoryPointsNode.toList)
//      if (vpl.size == 2) {
////        victoryPoints.addOne(VictoryPoint.of(vpl.get(0).identifier.toInt, vpl.get(1).identifier.toInt)) //TODO
//      } else {
//        logger.warn(s"Invalid victory point node in state: ${stateFile.getName}")
//      }
//    case None =>  logger.info(s"Victory points not defined in state: ${stateFile.getName}")
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

  def getStateInfrastructure: Infrastructure =
    new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, navalDockyards, navalPorts, airfields)

  def listResources: List[Resource] =
    resources.toList

  override def toString: String =
    name.value match
      case Some(n) => n
      case None => stateID.value match
        case Some(i) => i.toString
        case None => "[Unknown]"

  def stateFile: Option[File] = _stateFile

  def setFile(file: File): Unit =
    _stateFile = Some(file)

  /**
   * Todo fix java doc @ skorp
   * Returns the amount of the specified resource in this state.
   * If the resource is not found, returns 0.0.
   * @param name the name of the resource (e.g., "aluminium", "chromium", "oil", "rubber", "steel", "tungsten")
   * @return the amount of the specified resource in this state
   */
  def resourceAmount(name: String): Double = resources.find(_.pdxTypeIdentifier.equals(name)) match
    case Some(r) => r getOrElse 0
    case None => 0

  def owner(date: ClausewitzDate): Option[CountryTag] = history.owner.value

  def population: Int = manpower getOrElse 1

  def population_=(pop: Int): Unit = manpower @= pop

  def population_=?(pop: Int): Unit = manpower @=? pop

  def infrastructure: Int = history.buildings.infrastructure getOrElse 0

  def infrastructure_=(infrastructure: Int): Unit = history.buildings.infrastructure @= infrastructure

  def infrastructure_=?(infrastructure: Int): Unit = history.buildings.infrastructure @=? infrastructure

  def civilianFactories: Int = history.buildings.civilianFactories getOrElse 0

  def civilianFactories_=(factories: Int): Unit = history.buildings.civilianFactories @= factories

  def civilianFactories_=?(factories: Int): Unit = history.buildings.civilianFactories @=? factories

  def militaryFactories: Int = history.buildings.militaryFactories getOrElse 0

  def militaryFactories_=(factories: Int): Unit = history.buildings.militaryFactories @= factories

  def militaryFactories_=?(factories: Int): Unit = history.buildings.militaryFactories @=? factories

  def navalDockyards: Int = history.buildings.navalDockyards getOrElse 0

  def navalDockyards_=(dockyards: Int): Unit = history.buildings.navalDockyards @= dockyards

  def navalDockyards_=?(dockyards: Int): Unit = history.buildings.navalDockyards @=? dockyards

  def navalPorts: Int = history.buildings.navalDockyards getOrElse 0

  def navalPorts_=(ports: Int): Unit = history.buildings.navalDockyards @= ports

  def navalPorts_=?(ports: Int): Unit = history.buildings.navalDockyards @=? ports

  def airfields: Int = history.buildings.airBase getOrElse 0

  def airfields_=(airfields: Int): Unit = history.buildings.airBase @= airfields

  def airfields_=?(airfields: Int): Unit = history.buildings.airBase @=? airfields

  def civMilFactoryRatio: Double =
    if civilianFactories == 0 then 0.0
    else militaryFactories.toDouble / civilianFactories

  def populationFactoryRatio: Double =
    if civilianFactories == 0 then 0.0
    else population.toDouble / civilianFactories

  def populationCivFactoryRatio: Double =
    if civilianFactories == 0 then 0.0
    else population.toDouble / civilianFactories

  def populationMilFactoryRatio: Double =
    if militaryFactories == 0 then 0.0
    else population.toDouble / militaryFactories

  def populationAirCapacityRatio: Double =
    if airfields == 0 then 0.0
    else population.toDouble / airfields

  def infrastructureMaxLevel: Int =
    if buildingsMaxLevelFactor.getOrElse(0) == 0 then 0
    else (infrastructure * buildingsMaxLevelFactor.getOrElse(0)).toInt

  def resource(name: String): Resource = resources.find(_.pdxTypeIdentifier.equals(name)) match
    case Some(r) => r
    case None => new Resource(name, 0)

  def setResource(name: String, amt: Double): Unit = resources.find(_.pdxTypeIdentifier.equals(name)) match
    case Some(r) => r @= amt
    case None => new Resource(name, amt)

  override def getInfrastructureRecord: Infrastructure = getStateInfrastructure

  def id: String =
    stateID.value match
      case Some(i) => i.toString
      case None => "[Unknown]"

  override def compareTo(@NotNull o: State): Int = stateID.compareTo(o.stateID) match
    case Some(v) => v
    case None => throw new NullPointerException("State has no ID")

  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] =
    val id = this.stateID.value match
      case Some(i) => i.toString
      case None => ""
    mutable.Map(Property.NAME -> id)

  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = State.states

  override def iterator: Iterator[Province] = provinces.iterator

  override def getFile: Option[File] = _stateFile

  override def referableID: Option[String] = stateID.asOptionalString

/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 * Localizable: state name
 *
 * I apologize in advance.
 */
object State extends Iterable[State] with PDXReadable with LazyLogging:
  override val cleanName: String = "States"

  private val states = new ListBuffer[State]
  var stateErrors: ListBuffer[PDXError] = ListBuffer().empty

  def get(file: File): Option[State] =
    if file == null then return None
    if !states.exists(_.stateFile.contains(file)) then new State(true, file)
    states.find(_.stateFile.contains(file))

  def observeStates: ObservableList[State] =
    FXCollections.observableArrayList(CollectionConverters.asJava(states))

  /**
   * Creates States from reading files
   */
  def read(): Boolean =
    if !HOIIVFiles.Mod.states_folder.exists || !HOIIVFiles.Mod.states_folder.isDirectory then
      logger.error(s"In State.java - ${HOIIVFiles.Mod.states_folder} is not a directory, or it does not exist.")
      false
    else if HOIIVFiles.Mod.states_folder.listFiles == null || HOIIVFiles.Mod.states_folder.listFiles.isEmpty then
      logger.error(s"No states found in ${HOIIVFiles.Mod.states_folder}")
      false
    else
      HOIIVFiles.Mod.states_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach: f =>
        new State(true, f)
      true

  override def clear(): Unit =
    states.clear()

  def add(state: State): Iterable[State] =
    states += state
    states

  def list: List[State] = states.toList

  def get(id: Int): Option[State] =
    states.find(_.stateID @== id)

  def get(state_name: String): Option[State] =
    states.find(_.name @== state_name)

  def ownedStatesOfCountry(country: CountryFile): ListBuffer[State] = ownedStatesOfCountry(country.countryTag)

  def ownedStatesOfCountry(tag: CountryTag): ListBuffer[State] =
    states filter(state => state.owner(ClausewitzDate.defaulty).exists(_.equals(tag)))

  def infrastructureOfStates(states: ListBuffer[State]): Infrastructure =
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
  def resourcesOfStates(states: ListBuffer[State]): List[Resource] =
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

  /**
   * If the state represented by file is not in the list of states, creates the
   * new state.
   * If the state already exists, overwrites the state.
   *
   * @param file state file
   */
  def readState(file: File): Boolean =
    if file == null || !file.exists || file.isDirectory then
      logger.error(s"In State.java - ${file} is a directory, or it does not exist.")
      false
    else
      new State(true, file)
      true

  /**
   * If the state represented by the file exists in states list, removes the state
   * from the states list
   *
   * @param file state file
   */
  def removeState(file: File): Boolean = boundary:
    val tempState = new State(false)
    for state <- states do
      if state.stateID == tempState.stateID then
        states -= state
        logger.debug("Removed state " + tempState)
        boundary.break(true)
    false

  /**
   * TODO fix this java doc @ skorp
   * Returns a list of functions that return data about a state
   * @param resourcePercentages if <code>true</code>, returns resource percentages of global instead of resource amounts
   * @return list of functions that return data about a state
   */
  def getDataFunctions(resourcePercentages: Boolean = false): Iterable[State => ?] =
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

  def infrastructureOfCountries: ListBuffer[Infrastructure] =
    val countryList = CountryTag.countryTags
    val countriesInfrastructureList = new ListBuffer[Infrastructure]
    for tag <- countryList do
      countriesInfrastructureList.addOne(infrastructureOfCountry(tag))
    countriesInfrastructureList

  def infrastructureOfCountry(tag: CountryTag) = infrastructureOfStates(ownedStatesOfCountry(tag))

  def infrastructureOfCountry(country: CountryFile) = infrastructureOfStates(ownedStatesOfCountry(country))

  // ! todo test if working
  def resourcesOfCountries: List[List[Resource]] =
    val countryList = CountryTag.countryTags
    val countriesResourcesList = new ListBuffer[List[Resource]]
    for tag <- countryList do
      countriesResourcesList.addOne(resourcesOfCountry(tag))
    countriesResourcesList.toList

  def resourcesOfCountry(tag: CountryTag): List[Resource] = resourcesOfStates(ownedStatesOfCountry(tag))

  def resourcesOfCountry(country: CountryFile): List[Resource] = resourcesOfStates(ownedStatesOfCountry(country))

  protected def usefulData(data: String): Boolean = if data.nonEmpty then if data.trim.charAt(0) == '#' then false
  else true
  else false

  @NotNull override def iterator: Iterator[State] = states.iterator

  // todo fix structured effect block later when i can read
  class History(pdxIdentifier: String = "history", node: Node = null) extends StructuredPDX(pdxIdentifier):
    final val owner = new ReferencePDX[CountryTag](() => CountryTag.toList, "owner")
    final val controller = new ReferencePDX[CountryTag](() => CountryTag.toList, "controller")
    final val buildings = new BuildingsPDX
    final val victoryPoints = new MultiPDX[VictoryPointPDX](None, Some(() => new VictoryPointPDX), "victory_points")
    final val startDateScopes = new MultiPDX[StartDateScopePDX](None, Some(() => new StartDateScopePDX))
      with ProceduralIdentifierPDX(ClausewitzDate.validDate)

    /* constructor */
    if node != null then loadPDX(node)

    // Re-throw exceptions to bubble up to State
    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Seq[PDXScript[?]] =
      ListBuffer(owner, buildings, controller, victoryPoints, startDateScopes)

    override def getPDXTypeName: String = "History"

  class BuildingsPDX(node: Node = null) extends StructuredPDX("buildings"):
    final val infrastructure = new IntPDX("infrastructure", ExpectedRange.ofPositiveInt)
    final val civilianFactories = new IntPDX("industrial_complex", ExpectedRange.ofPositiveInt)
    final val militaryFactories = new IntPDX("arms_factory", ExpectedRange.ofPositiveInt)
    final val navalDockyards = new IntPDX("naval_base", ExpectedRange.ofPositiveInt)
    final val airBase = new IntPDX("air_base", ExpectedRange.ofPositiveInt)

    /* constructor */
    if node != null then loadPDX(node)

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Seq[PDXScript[?]] =
      ListBuffer(infrastructure, civilianFactories, militaryFactories, navalDockyards, airBase)

  class VictoryPointPDX extends ListPDX[IntPDX](() => new IntPDX(), "victory_points"):
    override def getPDXTypeName: String = "Victory Point"

  class StartDateScopePDX(date: ClausewitzDate = ClausewitzDate.defaulty) extends History() with ProceduralIdentifierPDX(ClausewitzDate.validDate):
    // Re-throw exceptions to bubble up to State (inherited from History which already overrides loadPDX)
    override def getPDXTypeName: String = "Start Date History"
