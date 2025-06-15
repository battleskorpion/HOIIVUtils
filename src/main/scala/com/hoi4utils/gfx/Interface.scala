package com.hoi4utils.gfx

import com.hoi4utils.parser.{Parser, ParserException}
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable

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
class Interface(private val file: File = null) extends LazyLogging:
  private val spriteTypes: mutable.Set[SpriteType] = new mutable.HashSet
  readGFXFile(file)

  private def readGFXFile(file: File): Unit =
    spriteTypes.clear()
    
    /* load listed sprites */
    val spriteTypeNodes = new Parser(file, this.getClass).rootNode.filterCaseInsensitive("spriteTypes").subFilterCaseInsensitive("spriteType")
    if spriteTypeNodes == null then 
      logger.warn(s"No SpriteTypes defined in interface .gfx file, $file")
      return
    
    val validSpriteTypes = spriteTypeNodes.filter(_.containsAllCaseInsensitive("name", "texturefile"))
    
    for spriteType <- validSpriteTypes do
      try
        val name = spriteType.getValueCaseInsensitive("name").$stringOrElse("").replace("\"", "")
        val filename = spriteType.getValueCaseInsensitive("texturefile").$stringOrElse("").replace("\"", "")
        if name.isEmpty || filename.isEmpty then
          logger.warn(s"SpriteType in interface .gfx file, $file, has empty name or texturefile.")
        else
          val gfx = SpriteType(name, filename, basepath = file.getParentFile.getParentFile)
          spriteTypes.add(gfx)
          Interface.gfxMap.put(name, gfx)
      catch
        case e: ParserException =>
          logger.error(s"Error parsing SpriteType in interface .gfx file, $file")

  /**
   * Returns the name of the file represented by an instance of the Interface class
   *
   * @return name of this Interface file
   */
  def getName: String = file.getName

  /**
   * Returns the path for the file represented by an instance of the Interface class
   *
   * @return filepath for this Interface file
   */
  def getPath: String = file.getPath

/**
 * Represents a .gfx file in interface folder
 * Contains a set of SpriteTypes
 */
object Interface extends PDXReadable with LazyLogging:
  val gfxMap: mutable.Map[String, SpriteType] = new mutable.HashMap
  val interfaceFiles: mutable.Map[File, Interface] = new mutable.HashMap

  def getGFX(icon: String): Option[String] =
    gfxMap.get(icon).map(_.gfx)

  def read(): Boolean = {
    val modSuccess = readInterface(HOIIVFiles.Mod.interface_folder)
    val hoi4Success = readInterface(HOIIVFiles.HOI4.interface_folder)
    modSuccess && hoi4Success
  }

  private def readInterface(interfaceFolder: File): Boolean =
    if !interfaceFolder.exists || !interfaceFolder.isDirectory then
      false
    else
      val files = Option(interfaceFolder.listFiles).getOrElse(Array.empty[File])
      if files.isEmpty then
        false
      else
        files
          .filter(_.getName.endsWith(".gfx"))
          .foreach(file => interfaceFiles.put(file, new Interface(file)))
        true

  def clear(): Unit = {
    gfxMap.clear()
    interfaceFiles.clear()
  }