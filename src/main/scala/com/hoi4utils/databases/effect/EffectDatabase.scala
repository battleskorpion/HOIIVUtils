package com.hoi4utils.databases.effect

import com.hoi4utils.custom_scala.{BoolType, ExpectedRange, RichString}
import com.hoi4utils.exceptions.InvalidParameterException
import com.hoi4utils.hoi4mod.common.country_tags.CountryTag
import com.hoi4utils.hoi4mod.common.idea.Idea
import com.hoi4utils.hoi4mod.map.province.Province
import com.hoi4utils.hoi4mod.map.state.State
import com.hoi4utils.hoi4mod.scope.ScopeType
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
import com.typesafe.scalalogging.LazyLogging

import java.io.{File, IOException}
import java.nio.file.{Files, StandardCopyOption}
import java.sql.*
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.util.{Try, Using, boundary}

object EffectDatabase extends LazyLogging {
  try {
    Class.forName("org.sqlite.JDBC")
  } catch {
    case e: ClassNotFoundException =>
      throw new RuntimeException(e)
  }

  private val edb: String = "databases/effects.db"
  private var _connection: Connection = uninitialized
  private var _effects: List[Effect] = List()
  var effectErrors: ListBuffer[String] = ListBuffer()

  def init(): Unit = {
    val url = getClass.getClassLoader.getResource(edb)
    if (url == null) throw new SQLException(s"Unable to find '$edb'")

    val tempFile = File.createTempFile("effects", ".db")
    tempFile.deleteOnExit()

    Using(url.openStream) { inputStream =>
      Files.copy(inputStream, tempFile.toPath, StandardCopyOption.REPLACE_EXISTING)
    }.recover {
      case e: Exception => Exception(e)
    }

    _connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath)
    _effects = loadEffects()
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

  def close(): Unit = if (_connection != null) _connection.close()

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
          // todo add metadata for where the unrecognized effect was found
          effectErrors += s"""No parameters for "$pdxIdentifier" in effects database.
              |Metadata:
              |  supported_scopes: $supportedScopes_str
              |  supported_targets: $supportedTargets_str
              |  required_parameters_full: $requiredParametersFull_str
              |  required_parameters_simple: $requiredParametersSimple_str
              |""".stripMargin
//          logger.error(s"No parameters for $pdxIdentifier in effects database.")
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

  private def simpleParameterToEffect(pdxIdentifier: String, requiredParametersSimple_str: String): Option[Effect] = boundary {
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

    paramValueType.getOrElse(boundary.break(None)) match {
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
    val effectParameters = new ListBuffer[(String, ParameterValueType, Boolean)]

    for (alternateParameter <- requiredParameters_str.split("\\s+\\|\\s+")) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      for (i <- parametersStrlist.indices) {
        val parameterStr = parametersStrlist(i).trim
        val parameters = parameterStr.split(",", -1)
          .map(_.trim)
          .filter(_.nonEmpty)
          .map(s => s.splitWithDelimiters("(<[a-zA-Z0-9@_\\- ;:+\\[\\]]+>|\\|)", -1).map(_.trim).filter(_.nonEmpty))
        if (parameters.length >= 1) {
          for (parameter <- parameters) {
            var paramValueType: Option[ParameterValueType] = None
            var isListType = false
            val paramIdentifier = parameter(0)

            if (parameter.length == 2) {
              if (parameter(1).startsWith("<") && parameter(1).endsWith(">")) {
                val paramTypeStr = parameter(1)
                paramValueType = ParameterValueType.of(paramTypeStr)
              } else {
                // todo: handle additional cases
              }
            }
            if (parameter.length > 2) {
              /* special cases like @multiple */
              if (parameter(2).startsWith("@")) {
                // todo: handle flags and stuff
              }
              if (parameter(1).startsWith("<") && parameter(1).endsWith(">")) {
                val paramTypeStr = parameter(1)
                paramValueType = ParameterValueType.of(paramTypeStr)
              } else {
                // todo: handle additional cases
              }
            } else if (parameter.length == 1) {
              if (parameter(0).startsWith("<") && parameter(0).endsWith(">")) {
                /* better not be simple effect */
                if (parameter(0).startsWith("<list[")) {
                  /* find type within list[] */
                  val paramTypeStr = parameter(0).split("\\[|\\]")(1)
                  paramValueType = ParameterValueType.of(paramTypeStr)
                  isListType = true
                } else throw new InvalidParameterException("Invalid parameter definition: " + pdxIdentifier)
              } else if (parameter(0).startsWith("@")) {
                // todo: handle initial flags and stuff such as @limit
              } else throw new InvalidParameterException("Invalid parameter definition: " + pdxIdentifier)
            }

            if (paramValueType.isDefined)
              effectParameters += ((paramIdentifier, paramValueType.get, isListType))
            else {
              // TODO: log the issue if parameter value type is unknown
            }
          }
        } else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
      }
    }

    if (effectParameters.isEmpty) {
      None
    } else {
      val effectPDXParameters: ListBuffer[PDXScript[?]] = effectParameters.collect {
        case (name, ParameterValueType.ace_type, _) => new StringPDX(name) // ex: type = fighter_genius
        case (name, ParameterValueType.ai_strategy, _) => new StringPDX(name) // ex: type = alliance
        case (name, ParameterValueType.character, _) => new StringPDX(name) // ex: character = OMA_sultan
        case (name, ParameterValueType.country, _) => new ReferencePDX[CountryTag](() => CountryTag.toList, c => Some(c.get), "country")
        case (name, ParameterValueType.cw_bool, _) => new BooleanPDX(name, false, BoolType.TRUE_FALSE)
        case (name, ParameterValueType.cw_float, _) => new DoublePDX(name)
        case (name, ParameterValueType.cw_int, _) => new IntPDX(name)
        case (name, ParameterValueType.cw_string, _) => new StringPDX(name)
        case (name, ParameterValueType.cw_trait, _) => new StringPDX(name) // ex: trait = really_good_boss
        case (name, ParameterValueType.decision, _) => new StringPDX(name) // ex: activate_decision = my_decision
        case (name, ParameterValueType.doctrine_category, _) => new StringPDX(name) // ex: category = land_doctrine
        case (name, ParameterValueType.flag, _) => new StringPDX(name) // ex: set_state_flag = my_flag
        case (name, ParameterValueType.idea, false) => new ReferencePDX[Idea](() => Idea.listAllIdeas, idea => idea.id, name)
        case (name, ParameterValueType.idea, true) => new ListPDX[ReferencePDX[Idea]](() => new ReferencePDX[Idea](() => Idea.listAllIdeas, idea => idea.id, name), name)
        case (name, ParameterValueType.mission, false) => new StringPDX(name) // ex: activate_mission = my_mission
        // Treat ParameterValueType.effect as a simple string here.
        case (name, ParameterValueType.effect, _) => new StringPDX(name) // ex: effect = my_effect
        case (name, ParameterValueType.state, _) => new ReferencePDX[State](() => State.list, s => s.stateID.asSomeString, name)
        case (name, ParameterValueType.equipment, _) => new StringPDX(name) // ex: type = fighter_equipment_0
        case (name, ParameterValueType.strategic_region, _) => new IntPDX(name, ExpectedRange.ofPositiveInt)
        case (name, ParameterValueType.building, _) => new StringPDX(name)
        case (name, ParameterValueType.operation_token, _) => new StringPDX(name)
        case (name, ParameterValueType.ideology, _) => new StringPDX(name)
        case (name, ParameterValueType.sub_ideology, _) => new StringPDX(name)
        case (name, ParameterValueType.province, _) => new ReferencePDX[Province](() => Province.list, p => Some(p.id.toString), name)
        case (name, ParameterValueType.resource, _) => new StringPDX(name)
        case (name, ParameterValueType.tech_category, _) => new StringPDX(name)
        case (name, ParameterValueType.advisor_slot, _) => new StringPDX(name)
        case (name, ParameterValueType.event, _) => new StringPDX(name)
        case (name, ParameterValueType.wargoal, _) => new StringPDX(name)
      }.to(ListBuffer)

      // If any of the parameters is of type effect, then use StructuredWithEffectsPDX.
      val useWithEffects = effectParameters.exists { case (_, paramType, _) => paramType == ParameterValueType.effect }

      val structuredEffectBlock = if (useWithEffects) {
        new StructuredWithEffectBlockPDX(pdxIdentifier) with BlockEffect {
          override protected def childScripts: mutable.Iterable[PDXScript[?]] = effectPDXParameters
        }
      } else {
        new StructuredPDX(pdxIdentifier) with BlockEffect {
          override protected def childScripts: mutable.Iterable[PDXScript[?]] = effectPDXParameters
        }
      }
      Some(structuredEffectBlock)
    }
  }
}