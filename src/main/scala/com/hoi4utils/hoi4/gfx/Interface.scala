package com.hoi4utils.hoi4.gfx

import com.hoi4utils.hoi4.common.country_tags.CountryTagService
import com.hoi4utils.hoi4.scope.ScopeType.root
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParserException, ParsingContext}
import com.hoi4utils.script.{PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import zio.{RIO, Task, ZIO}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*
import scala.util.boundary

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
class Interface(private val file: File) {
  private var spriteTypes: mutable.Set[SpriteType] = new mutable.HashSet

  /* init */
  readGFXFile(file)

  private def readGFXFile(file: File): RIO[InterfaceService, Unit] = {
    spriteTypes.clear()

    for {
      service <- ZIO.service[InterfaceService]
      interfaceErrors = service.interfaceErrors
      _ <- ZIO.attempt {
        val parser = new Parser(file)
        given ParsingContext(file)

        val rootNode = try {
          parser.parse
        } catch {
          case e: ParserException =>
            val pdxError = new PDXFileError(
              exception = e,
              additionalInfo = Map("context" -> "Error parsing interface .gfx file")
            )
            interfaceErrors += pdxError
            null 
        }
        
        if (root != null) {
          /* load listed sprites */
          val spriteTypeNodes =
            rootNode.filterCaseInsensitive("spriteTypes").subFilterCaseInsensitive("spriteType")
            
          if (spriteTypeNodes == null)
            val pdxError = new PDXFileError(
              additionalInfo = Map(
                "context" -> "No SpriteTypes defined in interface .gfx file",
                "reason" -> "spriteTypeNodes is null"
              )
            )
            interfaceErrors += pdxError
          else
            val validSpriteTypes = spriteTypeNodes.filter(_.containsAllCaseInsensitive("name", "texturefile"))
            for (spriteType <- validSpriteTypes) {
              try {
                val name = spriteType.getValueCaseInsensitive("name").$stringOrElse("").replace("\"", "")
                val filename = spriteType.getValueCaseInsensitive("texturefile").$stringOrElse("").replace("\"", "")
                if (name.isEmpty || filename.isEmpty) {
                  val pdxError = new PDXFileError(
                    additionalInfo = Map(
                      "context" -> "SpriteType in interface .gfx file has empty name or texturefile",
                      "name" -> name,
                      "filename" -> filename
                    )
                  )
                  interfaceErrors += pdxError
                }
                else {
                  val gfx = new SpriteType(name, filename, basepath = file.getParentFile.getParentFile)
                  spriteTypes.add(gfx)
                  service.addSpriteType(name, gfx)
                }
              } catch {
                case e: ParserException =>
                  val pdxError = new PDXFileError(
                    exception = e,
                    additionalInfo = Map("context" -> "Error parsing SpriteType in interface .gfx file")
                  )
                  interfaceErrors += pdxError
              }
            }
        }
      }
    } yield ()
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
