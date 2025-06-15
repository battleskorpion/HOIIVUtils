package com.hoi4utils.gfx

import com.hoi4utils.gfx.Interface.interfaceErrors
import com.hoi4utils.parser.Parser
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * Interface file class for reading .gfx files
 *
 * @param file .gfx file to be read
 *
 * Reads all files in mod/interface folder
 * Format example:
 * {{{
 * spriteTypes = {
 *   SpriteType = {
 *     name = "GFX_focus_SVA_virginia_officers"
 *     texturefile = "gfx/interface/goals/focus_SVA_virginia_officers.dds"
 *   }
 *   SpriteType = {
 *     name = "GFX_focus_generic_chromium"
 *     texturefile = "gfx/interface/goals/focus_generic_chromium.dds"
 *   }
 *   SpriteType = {
 *     name = "GFX_focus_generic_manpower"
 *     texturefile = "gfx/interface/goals/focus_generic_manpower.dds"
 *   }
 * }
 * }}}
 */
final class Interface private (file: File) extends LazyLogging:

  private val spriteTypes: Set[SpriteType] = readGFXFile(file)

  private def readGFXFile(file: File): Set[SpriteType] =
    Try {
      val parser = Parser(file, this.getClass)
      val spriteTypeNodes = parser.rootNode
        .filterCaseInsensitive("spriteTypes")
        .subFilterCaseInsensitive("spriteType")

      Option(spriteTypeNodes) match
        case None =>
          interfaceErrors.addOne(s"No SpriteTypes defined in interface .gfx file: $file")
          Set.empty
        case Some(nodes) =>
          val validNodes = nodes.filter(_.containsAllCaseInsensitive("name", "texturefile"))

          validNodes.flatMap { spriteType =>
            val name = cleanString(spriteType.getValueCaseInsensitive("name").$stringOrElse(""))
            val filename = cleanString(spriteType.getValueCaseInsensitive("texturefile").$stringOrElse(""))

            if name.nonEmpty && filename.nonEmpty then
              val gfx = SpriteType(name, filename, basepath = file.getParentFile.getParentFile)
              Interface.registerSprite(name, gfx)
              Some(gfx)
            else
              interfaceErrors.addOne(s"SpriteType in $file has empty name or texturefile")
              None
          }.toSet
    } match
      case Success(sprites) => sprites
      case Failure(exception) =>
        interfaceErrors.addOne(s"Failed to read GFX file $file: ${exception.getMessage}")
        Set.empty

  private def cleanString(str: String): String = str.replace("\"", "").trim

  /** Returns the name of this Interface file */
  def getName: String = file.getName

  /** Returns the path of this Interface file */
  def getPath: String = file.getPath

  /** Returns the sprite types in this interface file */
  def getSpriteTypes: Set[SpriteType] = spriteTypes

object Interface extends PDXReadable with LazyLogging:

  // Thread-safe collections for concurrent access
  private val gfxMap: TrieMap[String, SpriteType] = TrieMap.empty
  private val interfaceFiles: TrieMap[File, Interface] = TrieMap.empty
  var interfaceErrors: ListBuffer[String] = ListBuffer.empty

  /** Factory method for creating Interface instances */
  def apply(file: File): Option[Interface] =
    require(file != null, "File cannot be null")
    if file.exists() && file.isFile then
      Try(new Interface(file)).toOption
    else
      interfaceErrors.addOne(s"File does not exist or is not a file: $file")
      None

  /** Get GFX path by icon name */
  def getGFX(icon: String): Option[String] =
    gfxMap.get(icon).map(_.gfx)

  /** Get all registered sprite names */
  def getAllSpriteNames: Set[String] = gfxMap.keySet.toSet

  /** Get sprite by name */
  def getSprite(name: String): Option[SpriteType] = gfxMap.get(name)

  /** Read interface files from both mod and HOI4 directories */
  def read(): Boolean =
    val modResult = readInterfaceDirectory(HOIIVFiles.Mod.interface_folder)
    val hoi4Result = readInterfaceDirectory(HOIIVFiles.HOI4.interface_folder)
    modResult && hoi4Result

  private def readInterfaceDirectory(interfaceFolder: File): Boolean =
    if !interfaceFolder.exists() || !interfaceFolder.isDirectory then
      interfaceErrors.addOne(s"Interface directory does not exist or is not a directory: $interfaceFolder")
      false
    else
      val gfxFiles = Option(interfaceFolder.listFiles())
        .getOrElse(Array.empty[File])
        .filter(_.getName.endsWith(".gfx"))
        .toVector

      if gfxFiles.isEmpty then
        logger.info(s"No .gfx files found in $interfaceFolder")
        true
      else
        val results = gfxFiles.map { file =>
          Interface(file) match
            case Some(interface) =>
              interfaceFiles.put(file, interface)
              true
            case None =>
              interfaceErrors.addOne(s"Failed to create Interface for file: $file")
              false
        }

        results.forall(identity)

  /** Register a sprite in the global map */
  private[gfx] def registerSprite(name: String, sprite: SpriteType): Unit =
    gfxMap.put(name, sprite)

  /** Get all loaded interface files */
  def getInterfaceFiles: Map[File, Interface] = interfaceFiles.toMap

  /** Clear all cached data */
  def clear(): Unit =
    gfxMap.clear()
    interfaceFiles.clear()
    interfaceErrors.clear()