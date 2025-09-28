package com.hoi4utils.main

import java.io.*
import java.nio.file.Path
import java.util.Properties

/**
 * Configuration class for HOIIVUtils application.
 * Holds all initialized resources and settings.
 */
class Config(
  private val hDir: Path,
  private val hPropertiesPath: Path,
  private val hPropertiesJarResource: InputStream,
  private val hVersionTempPath: Path,
  private val hVersionJarResource: InputStream,
  private val hProperties: Properties
            ):
  def getDir: Path = hDir
  def getPropertiesPath: Path = hPropertiesPath
  def getPropertiesJarResource: InputStream = hPropertiesJarResource
  def getVersionTempPath: Path = hVersionTempPath
  def getVersionJarResource: InputStream = hVersionJarResource
  def getProperties: Properties = hProperties