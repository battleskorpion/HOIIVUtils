package com.hoi4utils

import org.apache.logging.log4j.LogManager

import java.io.*
import java.nio.file.{Path, Paths}
import java.util.Properties

class ConfigManager {
  private val LOGGER = LogManager.getLogger(this.getClass)
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)

  /**
   * @return Configured HOIIVUtils configuration for use by the application
   */
  def createConfig: Config = {
    val jarPath = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toAbsolutePath
    val hDir = jarPath.getParent.getParent
    val hPropertiesPath = Paths.get {s"$hDir${File.separator}HOIIVUtils.properties"}.toAbsolutePath
    val hPropertiesJarResource = this.getClass.getClassLoader.getResourceAsStream("HOIIVUtils.properties")
    val hProperties = new Properties()
    new Config(hDir, hPropertiesPath, hPropertiesJarResource, hProperties)
  }

  def saveProperties(config: Config): Unit = {
    if (config.getPropertiesPath.toFile.exists()) createHOIIVUtilsPropertiesFile(config)
    val output = new FileOutputStream(config.getPropertiesPath.toFile)
    config.getProperties.store(output, "HOIIVUtils Configuration")
    output.close()
  }

  def loadProperties(config: Config): Unit = {
    val hPropertiesFile = config.getPropertiesPath.toFile
    if (!hPropertiesFile.exists) createHOIIVUtilsPropertiesFile(config)
    val input = new FileInputStream(hPropertiesFile)
    config.getProperties.load(input)
    input.close()
  }

  def createHOIIVUtilsPropertiesFile(config: Config): Unit = {
    val defaultProperties = config.getPropertiesJarResource
    val stream = new FileOutputStream(config.getPropertiesPath.toFile)
    val buffer = new Array[Byte](1024)
    Iterator
      .continually(defaultProperties.read(buffer))
      .takeWhile(_ != -1)
      .foreach(bytesRead => stream.write(buffer, 0, bytesRead))
    stream.close()
  }
}
