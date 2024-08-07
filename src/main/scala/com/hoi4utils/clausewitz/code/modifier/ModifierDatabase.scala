package com.hoi4utils.clausewitz.code.modifier

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Try, Using}

import scala.collection.mutable.ListBuffer
import scala.util.{Try, Using}

class ModifierDatabase(databaseName: String) {
  private var connection: Connection = _

//  try {
//    val dbBytes = readDatabaseAsByteArray(databaseName)
//    connection = DriverManager.getConnection("jdbc:sqlite::memory:")
//    loadDatabase(dbBytes)
//    createTable()
//    loadModifiers
//  } catch {
//    case e@(_: SQLException | _: IOException) =>
//      e.printStackTrace()
//  }
  try {
    val url = getClass.getResource(databaseName)
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
    loadModifiers
  } catch {
    case e@(_: IOException | _: SQLException) =>
      e.printStackTrace()
  }


  def this() = {
    this("/main/resources_binary/databases/modifiers.db")
    //		for(Modifier modifier : Modifier.modifiers.values()) {
    //			System.out.println("modifier: " + modifier.identifier());
    //		}
  }

//  @throws[IOException]
//  private def readDatabaseAsByteArray(resourcePath: String) = {
//    System.out.println(System.getProperty("java.class.path"))
//    try {
//      val inputStream = getClass.getResourceAsStream(resourcePath)
//      val outputStream = new ByteArrayOutputStream
//      try {
//        if (inputStream == null) throw new IOException("Resource not found: " + resourcePath)
//        val buffer = new Array[Byte](1024)
//        var bytesRead = 0
//        while ((bytesRead = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, bytesRead)
//        outputStream.toByteArray
//      } finally {
//        if (inputStream != null) inputStream.close()
//        if (outputStream != null) outputStream.close()
//      }
//    }
//  }

//  @throws[SQLException]
//  private def loadDatabase(dbBytes: Array[Byte]): Unit = {
//    val sql = "RESTORE FROM MEMORY"
//    try {
//      val statement = connection.prepareStatement(sql)
//      try {
//        statement.setBytes(1, dbBytes)
//        statement.executeUpdate
//      } finally if (statement != null) statement.close()
//    }
//  }

  private def createTable(): Unit = {
    val createTableSQL = "CREATE TABLE IF NOT EXISTS modifiers ("
      + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "identifier TEXT," + "color_type TEXT,"
      + "value_type TEXT," + "precision INTEGER,"
      + "postfix TEXT," + "category TEXT"
      + ")"
    try {
      val createTable = connection.prepareStatement(createTableSQL)
      createTable.executeUpdate
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }

  def insertModifier(identifier: String, colorType: String, valueType: String, precision: Int, postfix: String, category: String): Unit = {
    val insertSQL = "INSERT INTO modifiers " + "(identifier, color_type, value_type, precision, postfix, category) VALUES (?, ?, ?, ?, ?, ?)"
    try {
      val insertStatement = connection.prepareStatement(insertSQL)
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

  def insertModifier(modifier: Modifier): Unit = {
    insertModifier(modifier.identifier, modifier.colorType.name, modifier.valueType.name, modifier.precision, modifier.postfix.name, modifier.category.stream.toList.get(0).toString)
  }

  def loadModifiers: List[Modifier] = {
    val loadedModifiers = new ListBuffer[Modifier]
    val retrieveSQL = "SELECT * FROM modifiers"
    try {
      val retrieveStatement = connection.prepareStatement(retrieveSQL)
      val resultSet = retrieveStatement.executeQuery
      while (resultSet.next) {
        val identifier = resultSet.getString("identifier")
        val colorType = resultSet.getString("color_type")
        val valueType = resultSet.getString("value_type")
        val precision = resultSet.getInt("precision")
        val postfix = resultSet.getString("postfix")
        val category = resultSet.getString("category")
        // Create a Modifier instance and add it to the loaded list
        val modifier = new Modifier(identifier, Modifier.ColorType.valueOf(colorType), Modifier.ValueType.valueOf(valueType), precision, Modifier.ValuePostfix.valueOf(postfix), ModifierCategory.valueOf(category))
        loadedModifiers.addOne(modifier)
      }
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    }
    loadedModifiers.toList
  }

  def close(): Unit = {
    try connection.close()
    catch {
      case e: SQLException =>
        e.printStackTrace()
    }
  }
}
