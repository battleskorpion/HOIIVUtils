package com.hoi4utils

import com.typesafe.scalalogging.LazyLogging

import java.io.*
import java.nio.file.Paths
import java.util.Properties

class ConfigManager extends LazyLogging {
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)

  /**
   * @return Configured HOIIVUtils configuration for use by the application
   */
  def createConfig: Config = {
    val jarPath = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toAbsolutePath
    val hDir = jarPath.getParent.getParent
    val hPropertiesPath = Paths.get {s"$hDir${File.separator}HOIIVUtils.properties"}.toAbsolutePath
    val hPropertiesJarResource = this.getClass.getClassLoader.getResourceAsStream("HOIIVUtils.properties")
    val hVersionTempPath = Paths.get {s"$hDir${File.separator}version.properties"}.toAbsolutePath
    val hVersionJarResource = this.getClass.getClassLoader.getResourceAsStream("version.properties")
    val hProperties = new Properties()
    new Config(hDir, hPropertiesPath, hPropertiesJarResource, hVersionTempPath, hVersionJarResource, hProperties)
  }

  def saveProperties(config: Config): Unit = {
    val defaultProperties = config.getPropertiesJarResource
    if (config.getPropertiesPath.toFile.exists()) createHOIIVUtilsPropertiesFile(config, defaultProperties)
    val output = new FileOutputStream(config.getPropertiesPath.toFile)
    config.getProperties.store(output, "HOIIVUtils Configuration")
    output.close()
  }

  def loadProperties(config: Config): Unit = {
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
    val version: String = config.getProperties.getProperty("version")
    input.close()
    stream.close()
    if (!hPropertiesFile.exists) createHOIIVUtilsPropertiesFile(config, defaultProperties)
    val input2 = new FileInputStream(hPropertiesFile)
    config.getProperties.load(input2)
    input2.close()

    config.getProperties.setProperty("version", version)
    if (hVersionTempPath.exists() && hVersionTempPath.delete()) {
      logger.debug("Deleted temporary version file: {}", hVersionTempPath)
    } else {
      logger.warn("Failed to delete temporary version file: {}", hVersionTempPath)
    }
  }

  def createHOIIVUtilsPropertiesFile(config: Config, defaultProperties: InputStream): Unit = {
    val stream = new FileOutputStream(config.getPropertiesPath.toFile)
    val buffer = new Array[Byte](1024)
    Iterator
      .continually(defaultProperties.read(buffer))
      .takeWhile(_ != -1)
      .foreach(bytesRead => stream.write(buffer, 0, bytesRead))
    stream.close()
  }
}
