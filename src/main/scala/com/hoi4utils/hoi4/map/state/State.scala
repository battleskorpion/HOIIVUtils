package com.hoi4utils.hoi4.map.state

import com.hoi4utils.*
import com.hoi4utils.hoi4.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.localization.*
import com.hoi4utils.hoi4.map.buildings.Infrastructure
import com.hoi4utils.hoi4.map.province.Province
import com.hoi4utils.hoi4.map.resource.Resource
import com.hoi4utils.hoi4.map.state
import com.hoi4utils.hoi4.map.state.InfrastructureData
import com.hoi4utils.hoi4.map.state.State.History
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.*
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.StringPDX
import com.hoi4utils.shared.{BoolType, ExpectedRange}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import org.jetbrains.annotations.NotNull
import zio.{Task, URIO, ZIO}

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
class State(file: File = null)(countryTagService: CountryTagService) extends StructuredPDX("state") with InfrastructureData
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
  final val history = new History()(countryTagService)
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

  file match
    case null => // create empty state
    case _ =>
      require(file.exists && file.isFile, s"State $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this
    ).addInfo("stateID", stateID.asOptionalString.getOrElse("unknown"))
    val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
    }
    stateService.stateErrors += pdxError

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

  @NotNull override def localizableProperties: Map[Property, String] =
    val id = this.stateID.value match
      case Some(i) => i.toString
      case None => ""
    Map(Property.NAME -> id)

  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] =
    val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
    }
    stateService.list

  override def iterator: Iterator[Province] = provinces.iterator

  override def getFile: Option[File] = _stateFile

  override def referableID: Option[String] = stateID.asOptionalString

/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 * Localizable: state name
 *
 * I apologize in advance.
 */
object State extends LazyLogging:

  // TODO for java compatibility
  def observeStates: ObservableList[State] =
    val stateService: StateService = zio.Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[StateService]).getOrThrowFiberFailure()
    }
    stateService.observeStates

  // todo fix structured effect block later when i can read
  class History(pdxIdentifier: String = "history", node: Node = null, file: Option[File] = None)(countryTagService: CountryTagService) extends StructuredPDX(pdxIdentifier):
    final val owner = new ReferencePDX[CountryTag](() => countryTagService.list, "owner")
    final val controller = new ReferencePDX[CountryTag](() => countryTagService.list, "controller")
    final val buildings = new BuildingsPDX
    final val victoryPoints = new MultiPDX[VictoryPointPDX](None, Some(() => new VictoryPointPDX), "victory_points")
    final val startDateScopes = new MultiPDX[StartDateScopePDX](None, Some(() => new StartDateScopePDX()(countryTagService)))
      with ProceduralIdentifierPDX(ClausewitzDate.validDate)

    /* constructor */
    if node != null then loadPDX(node, file)

    // Re-throw exceptions to bubble up to State
    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Seq[PDXScript[?]] =
      ListBuffer(owner, buildings, controller, victoryPoints, startDateScopes)

    override def getPDXTypeName: String = "History"

  class BuildingsPDX(node: Node = null, file: Option[File] = None) extends StructuredPDX("buildings"):
    final val infrastructure = new IntPDX("infrastructure", ExpectedRange.ofPositiveInt)
    final val civilianFactories = new IntPDX("industrial_complex", ExpectedRange.ofPositiveInt)
    final val militaryFactories = new IntPDX("arms_factory", ExpectedRange.ofPositiveInt)
    final val navalDockyards = new IntPDX("naval_base", ExpectedRange.ofPositiveInt)
    final val airBase = new IntPDX("air_base", ExpectedRange.ofPositiveInt)

    /* constructor */
    if node != null then loadPDX(node, file)

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Seq[PDXScript[?]] =
      ListBuffer(infrastructure, civilianFactories, militaryFactories, navalDockyards, airBase)

  class VictoryPointPDX extends ListPDX[IntPDX](() => new IntPDX(), "victory_points"):
    override def getPDXTypeName: String = "Victory Point"

  class StartDateScopePDX(date: ClausewitzDate = ClausewitzDate.defaulty)(countryTagService: CountryTagService) extends History()(countryTagService) with ProceduralIdentifierPDX(ClausewitzDate.validDate):
    // Re-throw exceptions to bubble up to State (inherited from History which already overrides loadPDX)
    override def getPDXTypeName: String = "Start Date History"
