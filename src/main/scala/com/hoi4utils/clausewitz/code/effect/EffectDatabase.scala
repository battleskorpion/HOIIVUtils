package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.data.country.CountryTag
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

// todo make singleton instance and have like load? or something
object EffectDatabase {
  private var _effects: List[Effect] = List()

  private def newStructuredEffectBlock(pdxIdentifier: String, childScriptsList: ListBuffer[? <: PDXScript[?]]) = new StructuredPDX(pdxIdentifier) {
    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = childScriptsList

    def nodeEquals(other: PDXScript[?]): Boolean = {
      // todo
      false
    }
  }

  // todo idk
  try {
    Class.forName("org.sqlite.JDBC")
  }
  catch {
    case e: ClassNotFoundException =>
      throw new RuntimeException(e)
  }

  def apply(): PDXSupplier[Effect] = {
    new PDXSupplier[Effect] {
      override def simplePDXSupplier(): Option[Node => Option[SimpleEffect]] = {
        Some((expr: Node) => {
          _effects.filter(_.isInstanceOf[SimpleEffect])
            .find(_.getPDXIdentifier == expr.identifier)
            .map(_.clone().asInstanceOf[SimpleEffect])
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[BlockEffect]] = {
        Some((expr: Node) => {
          _effects.filter(_.isInstanceOf[BlockEffect])
            .find(_.getPDXIdentifier == expr.identifier)
            .map(_.clone().asInstanceOf[BlockEffect])
        })
      }
    }
  }

  def effects: List[Effect] = _effects
}

class EffectDatabase(databaseName: String) {
  private var connection: Connection = _

  try {
    val url = getClass.getClassLoader.getResource(databaseName) // class loader loads resource consistently it seems
    if (url == null) throw new SQLException("Unable to find '" + databaseName + "'")
    val tempFile = File.createTempFile("effects", ".db")
    tempFile.deleteOnExit()
    Using(url.openStream) { inputStream =>
      Files.copy(inputStream, tempFile.toPath, StandardCopyOption.REPLACE_EXISTING)
    }.recover {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
    }
    connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath)
    EffectDatabase._effects = loadEffects
  } catch {
    case e@(_: IOException | _: SQLException) =>
      e.printStackTrace()
  }
  
  def this() = {
    this("databases/effects.db")  // should be 'databases/effects.db'.
  }

  private def createTable(): Unit = {
    val createTableSQL = "CREATE TABLE IF NOT EXISTS effects ("
      + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "identifier TEXT,"
      + "supported_scopes TEXT," // Add this column for supported scopes
      + "supported_targets TEXT" // Add this column for supported targets
      + ")"

    try {
      val createTable = connection.prepareStatement(createTableSQL)
      createTable.executeUpdate
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }

  def loadEffects: List[Effect] = {
    val loadedEffects = new ListBuffer[Effect]
    val retrieveSQL = "SELECT * FROM effects"
    try {
      val retrieveStatement = connection.prepareStatement(retrieveSQL)
      val resultSet = retrieveStatement.executeQuery
      while (resultSet.next) {
        val pdxIdentifier = resultSet.getString("identifier")
        // System.out.println("id " + identifier);
        val supportedScopes_str = resultSet.getString("supported_scopes")
        val supportedTargets_str = resultSet.getString("supported_targets")
        val requiredParametersFull_str = resultSet.getString("required_parameters_full")
        val requiredParametersSimple_str = resultSet.getString("required_parameters_simple")
        val optionalParameters_str = resultSet.getString("optional_parameters")

        val supportedScopes = parseEnumSet(supportedScopes_str)
        var supportedTargets = parseEnumSet(supportedTargets_str)
        // im just guessing at this code right now, I just want it to work basically.
        if (!(requiredParametersFull_str == null && requiredParametersSimple_str == null)) {
          if (supportedScopes.isEmpty) {
            throw new InvalidParameterException("Invalid scope definition: " + supportedScopes_str)
          }

          /* required parameters */
          val requiredParametersFull = Option(requiredParametersFull_str)
          val requiredParameterSimple = Option(requiredParametersSimple_str)

          val effects = new ListBuffer[Effect]

          (requiredParametersFull, requiredParameterSimple) match {
            case (Some(requiredParametersFull), Some(requiredParameterSimple)) =>
              effects ++= parametersToEffect(pdxIdentifier, requiredParametersFull_str)
            case (Some(requiredParametersFull), None) =>
              effects ++= parametersToEffect(pdxIdentifier, requiredParametersFull_str)
            case (None, Some(requiredParameterSimple)) =>
              effects ++= simpleParameterToEffect(pdxIdentifier, requiredParametersSimple_str)
            //            effects.addOne(new SimpleEffect(pdxIdentifier, () => new ReferencePDX[Effect](loadedEffects, _.identifier, pdxIdentifier)) {
            //            })
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

  private def parametersToEffect(pdxIdentifier: String, requiredParametersFull_str: String): ListBuffer[Effect] = {
    val effects: ListBuffer[Effect] = new ListBuffer[Effect]
    val alternateParameters = requiredParametersFull_str.split("\\s+\\|\\s+")
    for (alternateParameter <- alternateParameters) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      val childScripts = new ListBuffer[PDXScript[?]]
      for (i <- parametersStrlist.indices) {
        val parameterStr = parametersStrlist(i).trim
        var data = parameterStr.splitWithDelimiters("(<[a-z_-]+>|\\|)", -1)
        data = data.filter((s: String) => s.nonEmpty)
        if (data.length >= 2) {
//          val paramIdentifierStr = data(0).trim
//          if (data(1).trim.startsWith("<") && data(1).trim.endsWith(">")) {
//            val paramTypeStr = data(1).trim
//            val paramValueType: Option[ParameterValueType] = ParameterValueType.of(paramTypeStr)
//          }
          // replace with this: ? if it works!
          effects ++= parametersToBlockEffect(pdxIdentifier, parameterStr)
        } else if (data.length == 1) {
          effects ++= simpleParameterToEffect(pdxIdentifier, parameterStr) // really a simple parameter
        }
        else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
        if (data.length >= 3) {
          // todo !!!!!! do something with the complex effects (block effects) right here!!!
          var f = 0
          // idk
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
//                if (data.length >= 2) {
//                  val paramIdentifierStr = data(0).trim
//                  val paramTypeStr = data(1).trim
//                  val paramValueType = ParameterValueType.of(paramTypeStr)
//                }
        if (data.length == 1) {
          if (data(0).trim.startsWith("<") && data(0).trim.endsWith(">")) {
            //
            val paramTypeStr = data(0).trim
            paramValueType = ParameterValueType.of(paramTypeStr)
          }
          else {
            // todo
          }
        }
        else throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
        if (data.length >= 3) {
          // idk
          // todo handled by parametersToBlockEffect??? or
        }
      }
    }
    
    paramValueType.getOrElse(return None) match {
      case ParameterValueType.cw_int => Some(
        new IntPDX(pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.cw_float => Some(
        new DoublePDX(pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.cw_string => Some(
        new StringPDX(pdxIdentifier) with SimpleEffect {
        })
      // todo update default value/bool type
      case ParameterValueType.cw_bool => Some(
        new BooleanPDX(pdxIdentifier, false, BoolType.TRUE_FALSE) with SimpleEffect {
        })
      case ParameterValueType.state => Some(
        new ReferencePDX[State](() => State.list, s => Some(s.name), pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.province => Some(
        new ReferencePDX[Province](() => CollectionConverters.asScala(Province.list), p => Some(p.idStr()), pdxIdentifier) with SimpleEffect {
        })
      case ParameterValueType.country => Some(
        new ReferencePDX[CountryTag](() => CountryTag.toList, c => Some(c.get), "country") with SimpleEffect {
        })
      case _ =>
        None // todo ??? !!!
    }
  }

  private def parametersToBlockEffect(pdxIdentifier: String, requiredParameters_str: String): Option[Effect] = {
    var effectParameters: ListBuffer[(String, ParameterValueType)] = new ListBuffer[(String, ParameterValueType)]

    for (alternateParameter <- requiredParameters_str.split("\\s+\\|\\s+")) {
      val parametersStrlist = alternateParameter.split("\\s+,\\s+")
      for (i <- parametersStrlist.indices) {
        val parameterStr = parametersStrlist(i).trim
        val parameters = parameterStr.split(",", -1).map(s => s.trim).filter(_.nonEmpty)
          .map(s => s.splitWithDelimiters("(<[a-zA-Z0-9@_\\- ;:+]+>|\\|)", -1).map(s => s.trim).filter(s => s.nonEmpty))
        if (parameters.length >= 1) {
          for (parameter <- parameters) {
            var paramValueType: Option[ParameterValueType] = None
            var paramIdentifier = parameter(0).trim

            if (parameter.length == 2) {
              if (parameter(1).trim.startsWith("<") && parameter(1).trim.endsWith(">")) {
                val paramTypeStr = parameter(1).trim
                paramValueType = ParameterValueType.of(paramTypeStr)
              }
              else {
                // todo
              }
            }
            if (parameter.length >= 2) {
              // todo (i think this would mean weird stuff like optional?)
            }
            else if (parameter.length == 1) {
              if (parameters.length <= 1)
                throw new InvalidParameterException("Invalid parameter definition: " + parameterStr)
              else {
                // todo (means theres a simple parameter option)
              }
            }

            if (paramValueType.isDefined)
              effectParameters += (paramIdentifier -> paramValueType.get)
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
        case (id, ParameterValueType.cw_int) => new IntPDX(id)
        case (id, ParameterValueType.cw_float) => new DoublePDX(id)
        case (id, ParameterValueType.cw_string) => new StringPDX(id)
        case (id, ParameterValueType.cw_bool) => new BooleanPDX(id, false, BoolType.TRUE_FALSE)
        case (id, ParameterValueType.state) => new ReferencePDX[State](() => State.list, s => Some(s.name), id)
        case (id, ParameterValueType.province) => new ReferencePDX[Province](() => CollectionConverters.asScala(Province.list), p => Some(p.idStr()), id)
        case (id, ParameterValueType.country) => new ReferencePDX[CountryTag](() => CountryTag.toList, c => Some(c.get), "country")
        case (id, ParameterValueType.building) => new StringPDX(id) // todo can improve (type = industrial_complex is example of a building)
      }.to(ListBuffer)
      val structuredEffectBlock = new StructuredPDX(pdxIdentifier) with BlockEffect {
        override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
          effectPDXParameters
        }
      }
      Some(structuredEffectBlock)
    }
  }
  
  private def parseEnumSet(enumSetString: String): Option[Set[ScopeType]] = {
    Option(enumSetString).filter(_.nonEmpty).filter(_ != "none").map { str =>
      str.split(", ").flatMap { enumName =>
        Try(ScopeType.valueOf(enumName)).toOption
      }.toSet
    }
  }

  def close(): Unit = {
    try connection.close()
    catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }
}
