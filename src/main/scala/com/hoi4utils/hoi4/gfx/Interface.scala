package com.hoi4utils.hoi4.gfx

import com.hoi4utils.hoi4.common.country_tags.CountryTagService
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, Parser, ParserException, ParsingContext, ZIOParser}
import com.hoi4utils.script.{PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import zio.{Cause, RIO, Task, ZIO}

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

  def readGFXFile(): Task[InterfaceParseResult] =
    // pre-calculating for time efficiency
    val baseFolder = file.getParentFile.getParentFile
    spriteTypes.clear()
    readGFXFiles(baseFolder)

  private def readGFXFiles(baseFolder: File): ZIO[Any, Throwable, InterfaceParseResult] =
    given ParsingContext(file)

    val parseEffect = for {
      // ZIOParser constructor does blocking file I/O, so wrap in attemptBlocking
      parser <- ZIO.attemptBlocking(new ZIOParser(file))
      rootNode <- parser.parse
      spriteTypeNodes = rootNode.filterCaseInsensitive("spriteTypes").subFilterCaseInsensitive("spriteType")
      validSpriteTypes = spriteTypeNodes.view.filter(_.containsAllCaseInsensitive("name", "texturefile")) // TODO can filter out invalid nodes

      (errors, results) <- ZIO.partition(validSpriteTypes) { spriteNode =>
        processSpriteNode(spriteNode, baseFolder)
      }
    } yield InterfaceParseResult(errors.toList, results.toList)

    // Handle parsing errors gracefully - return empty result instead of failing
    parseEffect.catchAll { error =>
      ZIO.succeed {
        System.err.println(s"[WARN] Failed to parse ${file.getName}: ${error.getMessage}")
        InterfaceParseResult(List(InterfaceError.InitializationError(s"Parse error: ${error.getMessage}", error)), List.empty)
      }
    }

  private def processSpriteNode(spriteTypeNode: Node, baseFolder: File)(using ctx: ParsingContext): ZIO[Any, InterfaceError, (String, SpriteType)] =
    ZIO.logAnnotate("node", spriteTypeNode.name) {
      val name = spriteTypeNode.getValueCaseInsensitive("name").$stringOrElse("").replace("\"", "")
      val filename = spriteTypeNode.getValueCaseInsensitive("texturefile").$stringOrElse("").replace("\"", "")

      if name.isEmpty || filename.isEmpty then
        val err = InterfaceError.MissingField(
          node = spriteTypeNode,
          fieldName = if name.isEmpty then "name" else "texturefile"
        )
        // Log it using ZIO's structured logger
        ZIO.logErrorCause(err.message, Cause.fail(err)) *> ZIO.fail(err)
      else
        val gfx = new SpriteType(name, filename, basepath = baseFolder)
        spriteTypes.add(gfx)
        ZIO.succeed(name -> gfx)
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

case class InterfaceParseResult(errors: List[InterfaceError], sprites: List[(String, SpriteType)])

sealed trait InterfaceError extends Throwable:
  def message: String
  override def getMessage: String = message

object InterfaceError:
  case class MissingField(nodeName: String, fieldName: String, file: File, line: Option[Int], column: Option[Int] = None) extends InterfaceError:
    val message: String = (line, column) match
      case (Some(l), Some(c)) => s"Node '$nodeName' is missing required field '$fieldName' in $file:$line:$column"
      case (Some(l), None) => s"Node '$nodeName' is missing required field '$fieldName' in $file:$line"

  object MissingField:
    def apply(node: Node, fieldName: String)(using ctx: ParsingContext): MissingField =
      new MissingField(nodeName = node.name, fieldName = fieldName, file = ctx.file, line = ctx.line, column = ctx.column)

  case class InitializationError(message: String, cause: Throwable) extends InterfaceError
