package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.data.country.CountryTag
import com.hoi4utils.clausewitz.data.idea.Idea
import com.hoi4utils.clausewitz.exceptions.InvalidParameterException
import com.hoi4utils.clausewitz.map.province.Province
import com.hoi4utils.clausewitz.map.state.*
import com.hoi4utils.clausewitz.script.*
import com.hoi4utils.clausewitz_parser.Node

import java.io.{File, IOException}
import java.nio.file.{Files, StandardCopyOption}
import java.sql.*
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters
import scala.util.{Try, Using}
import language.experimental.namedTuples

object EffectDatabase {
  try {
    Class.forName("org.sqlite.JDBC")
  } catch {
    case e: ClassNotFoundException =>
      throw new RuntimeException(e)
  }

  private val edb: String = "databases/effects.db"
  private var _connection: Connection = _
  private var _effects: List[Effect] = List()

  def init(): Unit = {
    try {
      val url = getClass.getClassLoader.getResource(edb)
      if (url == null) throw new SQLException(s"Unable to find '$edb'")

      val tempFile = File.createTempFile("effects", ".db")
      tempFile.deleteOnExit()

      Using(url.openStream) { inputStream =>
        Files.copy(inputStream, tempFile.toPath, StandardCopyOption.REPLACE_EXISTING)
      }.recover {
        case e: Exception =>
          println(s"An error occurred: ${e.getMessage}")
      }

      _connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath)
      _effects = loadEffects()
    } catch {
      case e@(_: IOException | _: SQLException) =>
        e.printStackTrace()
    }
  }

  def apply(): PDXSupplier[Effect] = {
    new PDXSupplier[Effect] {
      override def simplePDXSupplier(): Option[Node => Option[SimpleEffect]] = {
        Some((expr: Node) => {
          _effects.filter(_.isInstanceOf[SimpleEffect])
              .find(_.pdxIdentifier == expr.name)
              .map(_.clone().asInstanceOf[SimpleEffect])
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[BlockEffect]] = {
        Some((expr: Node) => {
          _effects.filter(_.isInstanceOf[BlockEffect])
              .find(_.pdxIdentifier == expr.name)
              .map(_.clone().asInstanceOf[BlockEffect])
        })
      }
    }
  }

  def effects: List[Effect] = _effects

  def close(): Unit = {
    try {
      if (_connection != null) _connection.close()
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }

  private def loadEffects(): List[Effect] = {
    val loadedEffects = new ListBuffer[Effect]
    val retrieveSQL = "SELECT * FROM effects"
    try {
      val retrieveStatement = _connection.prepareStatement(retrieveSQL)
      val resultSet = retrieveStatement.executeQuery
      while (resultSet.next) {
        val pdxIdentifier = resultSet.getString("identifier")
        val supportedScopes_str = resultSet.getString("supported_scopes")
        val supportedTargets_str = resultSet.getString("supported_targets")
        val requiredParametersFull_str = resultSet.getString("required_parameters_full")
        val requiredParametersSimple_str = resultSet.getString("required_parameters_simple")

        val supportedScopes = parseEnumSet(supportedScopes_str)

        if (!(requiredParametersFull_str == null && requiredParametersSimple_str == null)) {
          if (supportedScopes.isEmpty) {
            throw new InvalidParameterException("Invalid scope definition: " + supportedScopes_str)
          }

          val requiredParametersFull = Option(requiredParametersFull_str)
          val requiredParameterSimple = Option(requiredParametersSimple_str)

          val effects = new ListBuffer[Effect]

          (requiredParametersFull, requiredParameterSimple) match {
            case (Some(requiredParametersFull), Some(requiredParameterSimple)) =>
              effects ++= parametersToEffect(pdxIdentifier, requiredParametersFull_str)
              effects ++= simpleParameterToEffect(pdxIdentifier, requiredParametersSimple_str)
            case (Some(requiredParametersFull), None) =>
              effects ++= parametersToEffect(pdxIdentifier, requiredParametersFull_str)
            case (None, Some(requiredParameterSimple)) =>
              effects ++= simpleParameterToEffect(pdxIdentifier, requiredParametersSimple_str)
            case (None, None) =>
            // todo (bad)
          }

          loadedEffects ++= effects
        } else {
          System.out.println("No parameters for " + pdxIdentifier + " in effects database.")
        }
      }
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
    loadedEffects.toList
  }

  private def parseEnumSet(enumSetString: String): Option[Set[ScopeType]] = {
    Option(enumSetString).filter(_.nonEmpty).filter(_ != "none").map { str =>
      str.split(", ").flatMap { enumName =>
        Try(ScopeType.valueOf(enumName)).toOption
      }.toSet
    }
  }

  private def parametersToEffect(pdxIdentifier: String, requiredParametersFull_str: String): ListBuffer[Effect] = {
    val effects: ListBuffer[Effect] = new ListBuffer[Effect]
    val alternateParameters = requiredParametersFull_str.split("\\s+\\|\\s+")
    for (alternateParameter <- alternateParameters) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      for (i <- parametersStrlist.indices) {
        val parameterStr = parametersStrlist(i).trim
        var data = parameterStr.splitWithDelimiters("(<[a-z_-]+>|\\|)", -1)
        data = data.filter((s: String) => s.nonEmpty)
        if (data.length >= 2) {
          effects ++= parametersToBlockEffect(pdxIdentifier, parameterStr)
        } else if (data.length == 1) {
          /* handle as simple parameter, *unless* is list */
          if (data(0).trim.startsWith("<") && data(0).trim.endsWith(">")) {
            /* if <list[<type>]>, handle as list */
            val paramTypeStr = data(0).trim
            if (paramTypeStr.startsWith("<list[")) {
              effects ++= parametersToBlockEffect(pdxIdentifier, parameterStr)
            } else {
              effects ++= simpleParameterToEffect(pdxIdentifier, parameterStr)
            }
          }
        }
        else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
        if (data.length >= 3) {
          effects ++= parametersToBlockEffect(pdxIdentifier, parameterStr)
        }
      }
    }
    effects
  }

  private def simpleParameterToEffect(pdxIdentifier: String, requiredParametersSimple_str: String): Option[Effect] = {
    var paramValueType: Option[ParameterValueType] = None

    for (alternateParameter <- requiredParametersSimple_str.split("\\s+\\|\\s+")) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      for (i <- parametersStrlist.indices) {
        paramValueType = None
        val parameterStr = parametersStrlist(i).trim
        var data = parameterStr.splitWithDelimiters("(<[a-z_-]+>|\\|)", -1)
        data = data.filter((s: String) => s.nonEmpty)

        if (data.length == 1) {
          if (data(0).trim.startsWith("<") && data(0).trim.endsWith(">")) {
            val paramTypeStr = data(0).trim
            paramValueType = ParameterValueType.of(paramTypeStr)
          }
        }
        else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
      }
    }

    paramValueType.getOrElse(return None) match {
      case ParameterValueType.country => Some(
        new ReferencePDX[CountryTag](() => CountryTag.toList, c => Some(c.get), "country") with SimpleEffect {
        })
      case ParameterValueType.cw_bool => Some(
        new BooleanPDX(pdxIdentifier, false, BoolType.TRUE_FALSE) with SimpleEffect {
        })
      case ParameterValueType.cw_float => Some(
        new DoublePDX(pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.cw_int => Some(
        new IntPDX(pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.cw_string => Some(
        new StringPDX(pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.idea => Some(
        new ReferencePDX[Idea](() => Idea.listAllIdeas, _.id, pdxIdentifier) with SimpleEffect {  // in future: ReferencePDX[Idea]
        })
      case ParameterValueType.state => Some(
        new ReferencePDX[State](() => State.list, s => s.name.value, pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.province => Some(
        new ReferencePDX[Province](() => Province.list, p => Some(p.id.toString), pdxIdentifier) with SimpleEffect {
        })
      case _ =>
        None
    }
  }

  private def parametersToBlockEffect(pdxIdentifier: String, requiredParameters_str: String): Option[Effect] = {
    type Parameter = (name: String, `type`: ParameterValueType, isList: Boolean)
    val effectParameters = new ListBuffer[Parameter]

    for (alternateParameter <- requiredParameters_str.split("\\s+\\|\\s+")) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      for (i <- parametersStrlist.indices) {
        val parameterStr = parametersStrlist(i).trim
        val parameters = parameterStr.split(",", -1).map(s => s.trim).filter(_.nonEmpty)
          .map(s => s.splitWithDelimiters("(<[a-zA-Z0-9@_\\- ;:+\\[\\]]+>|\\|)", -1).map(s => s.trim).filter(s => s.nonEmpty))
        if (parameters.length >= 1) {
          for (parameter <- parameters) {
            var paramValueType: Option[ParameterValueType] = None
            var isListType = false
            val paramIdentifier = parameter(0)

            if (parameter.length == 2) {
              if (parameter(1).startsWith("<") && parameter(1).endsWith(">")) {
                val paramTypeStr = parameter(1)
                paramValueType = ParameterValueType.of(paramTypeStr)
              }
              else {
                // todo
              }
            }
            if (parameter.length > 2) {
              /* special cases like @multiple */
              if (parameter(2).startsWith("@")) {
                // todo handle flags and stuff
              }

              if (parameter(1).startsWith("<") && parameter(1).endsWith(">")) {
                val paramTypeStr = parameter(1)
                paramValueType = ParameterValueType.of(paramTypeStr)
              }
              else {
                // todo
              }
            }
            else if (parameter.length == 1) {
              if (parameter(0).startsWith("<") && parameter(0).endsWith(">")) {
                /* better not be simple effect */
                if (parameter(0).startsWith("<list[")) {
                  /* find type within list[] */
                  val paramTypeStr = parameter(0).split("\\[|\\]")(1)
                  paramValueType = ParameterValueType.of(paramTypeStr)
                  isListType = true
                }
                else throw new InvalidParameterException("Invalid parameter definition: " + pdxIdentifier)
              }
              else if (parameter(0).startsWith("@")) {
                // todo handle initial flags and stuff
                // such as @limit
              }
              else throw new InvalidParameterException("Invalid parameter definition: " + pdxIdentifier)
            }

            if (paramValueType.isDefined)
              effectParameters += ((paramIdentifier, paramValueType.get, isListType))
            else {
              // TODO just log. bad log but just log. is debug log maybe. or more.
//              throw new InvalidParameterException(
//                "Invalid parameter definition (parameter value type unknown): " + parameterStr)
            }
          }
        }
        else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
      }
    }

    if (effectParameters.isEmpty) {
      None
    }
    else {
//      val blockEffect = newStructuredEffectBlock(pdxIdentifier, new ListBuffer[? <: PDXScript[?]]) with BlockEffect {
//        override def getPDXIdentifier: String = pdxIdentifier
//      }
//      Some(blockEffect)
      val effectPDXParameters: ListBuffer[PDXScript[?]] = effectParameters.collect {
        case (name = id, `type` = ParameterValueType.ace_type) => new StringPDX(id)     // ex: type = fighter_genius
        case (name = id, `type` = ParameterValueType.ai_strategy) => new StringPDX(id)  // ex: type = alliance
        case (name = id, `type` = ParameterValueType.character) => new StringPDX(id)    // ex: character = OMA_sultan. in future: ReferencePDX[Character]
        case (name = id, `type` = ParameterValueType.country) => new ReferencePDX[CountryTag](() => CountryTag.toList, c => Some(c.get), "country")
        case (name = id, `type` = ParameterValueType.cw_bool) => new BooleanPDX(id, false, BoolType.TRUE_FALSE)
        case (name = id, `type` = ParameterValueType.cw_float) => new DoublePDX(id)
        case (name = id, `type` = ParameterValueType.cw_int) => new IntPDX(id)
        //case (id, ParameterValueType.cw_list) => new CollectionPDX[]()
        case (name = id, `type` = ParameterValueType.cw_string) => new StringPDX(id)
        case (name = id, `type` = ParameterValueType.cw_trait) => new StringPDX(id)     // ex: trait = really_good_boss
        case (name = id, `type` = ParameterValueType.decision) => new StringPDX(id)     // ex: activate_decision = my_decision. in future: ReferencePDX[Decision]
        case (name = id, `type` = ParameterValueType.doctrine_category) => new StringPDX(id) // ex: category = land_doctrine
        case (name = id, `type` = ParameterValueType.flag) => new StringPDX(id)         // ex: set_state_flag = my_flag. boolean flag.
        case (id, ParameterValueType.idea, false) => new ReferencePDX[Idea](() => Idea.listAllIdeas, idea => idea.id, id)//new ReferencePDX[Idea]        // ex: idea = my_idea
        case (id, ParameterValueType.idea, true) => new ListPDX[ReferencePDX[Idea]](() => new ReferencePDX[Idea](() => Idea.listAllIdeas, idea => idea.id, id), id)   // ex: idea = my_idea // todo fix (id)
        case (name = id, `type` = ParameterValueType.mission, isList = false) => new StringPDX(id)      // ex: activate_mission = my_mission (in future: ReferencePDX[Mission])
        //case (id, ParameterValueType.modifier) => new ReferencePDX[Modifier]
        case (name = id, `type` = ParameterValueType.scope) => new StringPDX(id)        // ex: scope = my_scope
        case (name = id, `type` = ParameterValueType.state) => new ReferencePDX[State](() => State.list, s => s.stateID.asSomeString, id)
        case (name = id, `type` = ParameterValueType.equipment) => new StringPDX(id)    // ex: type = fighter_equipment_0. in future: ReferencePDX[Equipment]
        case (name = id, `type` = ParameterValueType.strategic_region) => new IntPDX(id, ExpectedRange.ofPositiveInt) // ex: region = 5. in future: ReferencePDX[StratRegion]
        case (name = id, `type` = ParameterValueType.building) => new StringPDX(id) // todo can improve (type = industrial_complex is example of a building)
        case (name = id, `type` = ParameterValueType.operation_token) => new StringPDX(id)  // ex: token = token_test. in future: ReferencePDX[OperationToken]
        case (name = id, `type` = ParameterValueType.ideology) => new StringPDX(id)         // ex: ruling_party = democratic
        case (name = id, `type` = ParameterValueType.sub_ideology) => new StringPDX(id)     // ex: ideology = liberalism
        case (name = id, `type` = ParameterValueType.province) => new ReferencePDX[Province](() => Province.list, p => Some(p.id.toString), id)
        case (name = id, `type` = ParameterValueType.resource) => new StringPDX(id)         // ex: type = oil. in future: limited to known resources (reference pdx?)
        case (name = id, `type` = ParameterValueType.tech_category) => new StringPDX(id)    // ex: category = radar_tech. in future: ReferencePDX[TechCategory]
        case (name = id, `type` = ParameterValueType.advisor_slot) => new StringPDX(id)     // ex: slot = political_advisor. in future: limited to known slots
        case (name = id, `type` = ParameterValueType.event) => new StringPDX(id)            // ex: id = my_event.1 in future: ReferencePDX[Event]
        case (name = id, `type` = ParameterValueType.wargoal) => new StringPDX(id)          // ex: type = annex_everything in future: ReferencePDX[WarGoal]. Wargoals are found in /Hearts of Iron IV/common/wargoals/*.txt.
      }.to(ListBuffer)
      val structuredEffectBlock = new StructuredPDX(pdxIdentifier) with BlockEffect {
        override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
          effectPDXParameters
        }
      }
      Some(structuredEffectBlock)
    }
  }
}
