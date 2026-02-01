package com.hoi4utils.databases.modifier

import com.hoi4utils.parser.{Node, PDXValueNode, SeqNode}
import com.hoi4utils.script.PDXSupplier

import java.io.{File, IOException}
import java.nio.file.{Files, StandardCopyOption}
import java.sql.*
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.util.Using

object ModifierDatabase {
  try {
    Class.forName("org.sqlite.JDBC")
  } catch {
    case e: ClassNotFoundException =>
      throw new RuntimeException(e)
  }

  private val mdb: String = "databases/modifiers.db"
  private var _connection: Connection = uninitialized
  private var _modifiers: List[Modifier] = List()

  // Initialize the database
  def init(): Unit = {
    try {
      val url = getClass.getClassLoader.getResource(mdb)
      if (url == null) throw new SQLException(s"Unable to find '$mdb'")

      val tempFile = File.createTempFile("modifiers", ".db")
      tempFile.deleteOnExit()

      Using(url.openStream) { inputStream =>
        Files.copy(inputStream, tempFile.toPath, StandardCopyOption.REPLACE_EXISTING)
      }.recover {
        case e: Exception =>
          println(s"An error occurred: ${e.getMessage}")
      }

      _connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath)

      createTable()

      _modifiers = loadModifiers
    } catch {
      case e@(_: IOException | _: SQLException) =>
        e.printStackTrace()
    }
  }

  def apply(): PDXSupplier[Modifier] = {
    new PDXSupplier[Modifier] {
      override def simplePDXSupplier(): Option[PDXValueNode[?] => Option[Modifier]] = {
        Some((expr: PDXValueNode[?]) => {
          _modifiers.filter(_.isInstanceOf[Modifier]) // todo? 
            .find(_.pdxIdentifier == expr.name)
            .map(_.clone().asInstanceOf[Modifier])
        })
      }

      override def blockPDXSupplier(): Option[SeqNode => Option[Modifier]] = {
        Some((expr: SeqNode) => {
          _modifiers.filter(_.isInstanceOf[Modifier])
            .find(_.pdxIdentifier == expr.name)
            .map(_.clone().asInstanceOf[Modifier])
        })
      }
    }
  }

  def modifiers: List[Modifier] = _modifiers

  private def createTable(): Unit = {
    val createTableSQL = "CREATE TABLE IF NOT EXISTS modifiers (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "identifier TEXT," +
        "color_type TEXT," +
        "value_type TEXT," +
        "precision INTEGER," +
        "postfix TEXT," +
        "category TEXT" +
        ")"
    try {
      val createTable = _connection.prepareStatement(createTableSQL)
      createTable.executeUpdate
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }

  def insertModifier(
                        identifier: String,
                        colorType: String,
                        valueType: String,
                        precision: Int,
                        postfix: String,
                        category: String
                    ): Unit = {
    val insertSQL = "INSERT INTO modifiers " +
        "(identifier, color_type, value_type, precision, postfix, category) VALUES (?, ?, ?, ?, ?, ?)"
    try {
      val insertStatement = _connection.prepareStatement(insertSQL)
      insertStatement.setString(1, identifier)
      insertStatement.setString(2, colorType)
      insertStatement.setString(3, valueType)
      insertStatement.setInt(4, precision)
      insertStatement.setString(5, postfix)
      insertStatement.setString(6, category)
      insertStatement.executeUpdate
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }

//  def insertModifier(modifier: Modifier): Unit = {
//    insertModifier(
//      modifier.identifier,
//      modifier.colorType.name,
//      modifier.valueType.name,
//      modifier._precision,
//      modifier.postfix.name,
//      modifier.category.toString
//    )
//  }

  private def loadModifiers: List[Modifier] = {
    val loadedModifiers = new ListBuffer[Modifier]
    val retrieveSQL = "SELECT * FROM modifiers"
    try {
      val retrieveStatement = _connection.prepareStatement(retrieveSQL)
      val resultSet = retrieveStatement.executeQuery
      while (resultSet.next) {
        val identifier = resultSet.getString("identifier")
        val colorType = resultSet.getString("color_type")
        val valueType = resultSet.getString("value_type")
        val precision = resultSet.getInt("precision")
        val postfix = resultSet.getString("postfix")
        val category = resultSet.getString("category")

        // TODO
//        val modifier = new Modifier(
//          Modifier.ColorType.valueOf(colorType),
//          Modifier.ValueType.valueOf(valueType),
//          precision,
//          Modifier.ValuePostfix.valueOf(postfix),
//          Set(ModifierCategory.valueOf(category))
//        )
//        loadedModifiers.addOne(modifier)
      }
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
    loadedModifiers.toList
  }

  def close(): Unit = {
    try {
      if (_connection != null) _connection.close()
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }
}