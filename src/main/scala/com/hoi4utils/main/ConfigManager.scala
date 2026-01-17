package com.hoi4utils.main

import com.hoi4utils.internal.ConfigException
import com.typesafe.scalalogging.LazyLogging
import dotty.tools.sjs.ir.Trees.JSUnaryOp.!

import java.io.*
import java.nio.file.{Path, Paths}
import java.util.Properties
import scala.util.Try

class ConfigManager extends LazyLogging:
  /**
   * @return Configured HOIIVUtils configuration for use by the application
   */
  @throws[ConfigException]
  def createConfig: Config =
    val jarPath = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toAbsolutePath
    val hDir = jarPath.getParent.getParent
    val hPropertiesPath = Paths.get(s"$hDir${File.separator}HOIIVUtils.properties").toAbsolutePath
    val hPropertiesJarResource = this.getClass.getClassLoader.getResourceAsStream("HOIIVUtils.properties")
    val hVersionTempPath = Paths.get(s"$hDir${File.separator}version.properties").toAbsolutePath
    val hVersionJarResource = this.getClass.getClassLoader.getResourceAsStream("version.properties")
    val hProperties = new Properties()

    validateConfigResources(
      hDir,
      hPropertiesPath,
      hPropertiesJarResource,
      hVersionTempPath,
      hVersionJarResource,
      hProperties
    )
    new Config(hDir, hPropertiesPath, hPropertiesJarResource, hVersionTempPath, hVersionJarResource, hProperties)

  @throws[ConfigException]
  def validateConfigResources(hDir: Path, hPropertiesPath: Path, hPropertiesJarResource: InputStream, hVersionTempPath: Path, hVersionJarResource: InputStream, hProperties: Properties): Unit =
    val errorPrefix = "Config creation error"

    // we don't check hVersionJarResource
    val problems = List(
      Option.when(hDir == null)("hDir is null"),
      Option.when(hPropertiesPath == null)("hPropertiesPath is null"),
      Option.when(hPropertiesJarResource == null)("missing hPropertiesJarResource"),
      Option.when(hVersionTempPath == null)("hVersionTempPath is null"),
      Option.when(hProperties == null)("hProperties is null")
    ).flatten
  
    if (problems.nonEmpty) {
      problems.foreach(p => logger.error(s"$errorPrefix: $p"))
      val msg = errorPrefix + ":\n - " + problems.mkString("\n - ")
      throw new ConfigException(msg)
    }

  def saveProperties(config: Config): Unit =
    val defaultProperties = config.getPropertiesJarResource
    if config.getPropertiesPath.toFile.exists() then createHOIIVUtilsPropertiesFile(config, defaultProperties)
    val output = new FileOutputStream(config.getPropertiesPath.toFile)
    config.getProperties.store(output, "HOIIVUtils Configuration")
    output.close()

  def loadProperties(config: Config): Unit =
    val hPropertiesFile = config.getPropertiesPath.toFile
    val hVersionTempPath = config.getVersionTempPath.toFile
    val defaultProperties = config.getPropertiesJarResource
    val stream = new FileOutputStream(hVersionTempPath)
    val buffer = new Array[Byte](1024)
    Iterator
      .continually(defaultProperties.read(buffer))
      .takeWhile(_ != -1)
      .foreach(bytesRead => stream.write(buffer, 0, bytesRead))
    val input = new FileInputStream(hVersionTempPath)
    config.getProperties.load(input)
    val version: String = config.getProperty("version")
    input.close()
    stream.close()
    if !hPropertiesFile.exists then createHOIIVUtilsPropertiesFile(config, defaultProperties)
    val input2 = new FileInputStream(hPropertiesFile)
    config.getProperties.load(input2)
    input2.close()

    config.getProperties.setProperty("version", version)
    if hVersionTempPath.exists() && hVersionTempPath.delete() then return
    else logger.error("Failed to delete temporary version file: {}", hVersionTempPath)

  def createHOIIVUtilsPropertiesFile(config: Config, defaultProperties: InputStream): Unit =
    val stream = new FileOutputStream(config.getPropertiesPath.toFile)
    val buffer = new Array[Byte](1024)
    Iterator
      .continually(defaultProperties.read(buffer))
      .takeWhile(_ != -1)
      .foreach(bytesRead => stream.write(buffer, 0, bytesRead))
    stream.close()
