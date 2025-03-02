package com.hoi4utils.clausewitz.data.gfx

import com.hoi4utils.clausewitz.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.Parser
import com.hoi4utils.clausewitz_parser.ParserException

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

  def getGFX(icon: String): String = {
    getSpriteType(icon) match {
      case Some(spriteType) => spriteType.gfx
      case None => null
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

  def loadGFXFiles(): Unit = {
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
  readGFXFile(file)
  private var spriteTypes: mutable.Set[SpriteType] = new mutable.HashSet

  private def readGFXFile(file: File): Unit = {
    spriteTypes.clear()
    // read listed sprites
    
//    val interfaceParser = new Parser(file)
//    //Expression[] exps = interfaceParser.findAll("SpriteType={", false);
//    var rootNode: Node = null
//    try rootNode = interfaceParser.parse
//    catch {
//      case e: ParserException =>
//        throw new RuntimeException(e)
//    }
//    val nodes = CollectionConverters.asJava(rootNode.filter("SpriteType={").toList)
//    if (nodes == null) {
//      System.err.println("No SpriteTypes in interface .gfx file, " + file)
//      //System.out.println(interfaceParser.expression());
//      return
//    }
//    import scala.collection.JavaConversions._
//    for (exp <- nodes) {
//      if (!exp.contains("name=")) continue //todo: continue is not supported
//      val nameExp = exp.find("name=").getOrElse(null)
//      if (!exp.contains("texturefile=")) continue //todo: continue is not supported
//      val fileExp = exp.find("texturefile=").getOrElse(null)
//      var name = nameExp.$stringOrElse("")
//      name = name.replaceAll("\"", "") // get rid of quotes from clausewitz code for file pathname
//
//      if (name.isEmpty) continue //todo: continue is not supported
//      var filename = fileExp.$stringOrElse("")
//      filename = filename.replaceAll("\"", "")
//      if (filename.isEmpty) continue //todo: continue is not supported
//      val gfx = new SpriteType(name, filename)
//      spriteTypes.add(gfx)
//      Interface.gfxMap.put(name, gfx)
//    }
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
