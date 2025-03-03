package com.hoi4utils.clausewitz.data.gfx

import com.hoi4utils.clausewitz.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.Parser
import com.hoi4utils.clausewitz_parser.ParserException
import org.apache.logging.log4j.{LogManager, Logger}


import scala.jdk.javaapi.CollectionConverters
import java.io.File
import java.util
import java.util.{HashMap, HashSet, List, Objects}
import scala.collection.mutable


/**
 * Represents a .gfx file in interface folder
 * Contains a set of SpriteTypes
 */
object Interface {
  private val gfxMap: mutable.Map[String, SpriteType] = new mutable.HashMap
  private var interfaceFiles: mutable.Map[File, Interface] = new mutable.HashMap

  def getGFX(icon: String): Option[String] = {
    getSpriteType(icon) match {
      case Some(spriteType) => Some(spriteType.gfx)
      case None => None
    }
  }

  def getSpriteType(icon: String): Option[SpriteType] = gfxMap.get(icon)

  def numGFX: Int = gfxMap.size

  /**
   * lists all SpriteType objects found in interface files
   *
   * @return
   */
  def listGFX: Iterable[SpriteType] = gfxMap.values

  /**
   * returns all .gfx files parsed
   *
   * @return list of all .gfx files of type Interface
   */
  def listGFXFiles: Iterable[Interface] = interfaceFiles.values

  /**
   * lists .gfx files which Interface class represents and which
   * have been read in.
   *
   * @return list of all .gfx files of type Interface
   */
  def list: Iterable[Interface] = listGFXFiles

  /**
   * Returns the number of .gfx interface files read
   *
   * @return number of interface files read
   */
  def numFiles: Int = interfaceFiles.size

  def reloadGFXFiles(): Unit = {
    val dir = HOIIVFiles.Mod.interface_folder
    if (!dir.exists || !dir.isDirectory) System.err.println("interface directory does not exist")
    val files = dir.listFiles
    if (files == null || files.isEmpty) System.err.println("interface directory is empty")

    gfxMap.clear()
    for (file <- files.filter(_.getName.endsWith(".gfx"))) {
      interfaceFiles.put(file, new Interface(file))
    }
  }
}

/**
 * interface file class
 *
 * @param file .gfx file to be read
 *
 * read all in mod\nadivided-dev\interface
 * <p> format example: </p> <pre>
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
 * } </pre>
*/
class Interface(private val file: File)
{
  private val LOGGER: Logger = LogManager.getLogger(getClass)
  private var spriteTypes: mutable.Set[SpriteType] = new mutable.HashSet

  /* init */
  readGFXFile(file)

  private def readGFXFile(file: File): Unit = {
    spriteTypes.clear()

    val parser = new Parser(file)
    try {
      parser.parse
    } catch {
      case e: ParserException =>
        LOGGER.error(s"Error parsing interface .gfx file, $file.\nException: $e")
        return
    }

    /* load listed sprites */
    val spriteTypeNodes = {
      parser.rootNode.filter("spriteTypes").subFilter("spriteType")
    }
    if (spriteTypeNodes == null) {
      LOGGER.warn(s"No SpriteTypes defined in interface .gfx file, $file")
      return
    }

    val validSpriteTypes = spriteTypeNodes.filter(_.containsAll("name", "texturefile"))
    for (spriteType <- validSpriteTypes) {
      try {
        val name = spriteType.getValue("name").string
        val filename = spriteType.getValue("texturefile").string
        val gfx = new SpriteType(name, filename)
        spriteTypes.add(gfx)
        Interface.gfxMap.put(name, gfx)
      } catch {
        case e: ParserException =>
          LOGGER.error(s"Error parsing SpriteType in interface .gfx file, $file")
      }
    }
  }

  /**
   * Returns the name of the file represented by an instance of the Interface class
   *
   * @return name of this Interface file
   */
  def getName = file.getName

  /**
   * Returns the path for the file represented by an instance of the Interface class
   *
   * @return filepath for this Interface file
   */
  def getPath = file.getPath
}
