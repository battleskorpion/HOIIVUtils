package com.hoi4utils.hoi42.common.country_tags

import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{PDXValueNode, ParsingContext, ZIOParser}
import com.hoi4utils.script.PDXFileError
import com.hoi4utils.script2.{PDXDecoder, PDXLoader, PDXReadable, Registry}
import zio.{RIO, Task, UIO, URIO, URLayer, ZEnvironment, ZIO, ZLayer}

import java.io.{File, IOException}
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable.ParTrieMap

trait CountryTagService extends CountryTagRegistry with PDXReadable {
//  def allTags: UIO[List[CountryTag]]

  def tags: Set[CountryTag]
  def addTag(tag: CountryTag): Unit
  def addTag(tag: CountryTag, file: File): Unit
  def countryTags: Seq[CountryTag]
  def exists(tag: String): UIO[Boolean]
  def findExisting(tag: String): UIO[Option[CountryTag]]
  def findExisting(tag: String, file: File): UIO[Option[CountryTag]]
  def file(tag: CountryTag): UIO[Option[File]]

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]

  override def clear(): Task[Unit] =
    super[CountryTagRegistry].clear()
}

object CountryTagService:
  val live: URLayer[Any, CountryTagService] =
    ZLayer.derive[CountryTagServiceImpl]

case class CountryTagServiceImpl() extends CountryTagService {
  override val display: String = "Country Tags"
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')

  private val fileMap = ParTrieMap[CountryTag, File]()

  override def read(): Task[Boolean] = {
    for {
      tags <- loadCountryTags()
      result <- ZIO.attempt {
        if (tags.isEmpty)
//          logger.error(s"No country tags loaded!?")     // TODO TODO log error 
          false
        else
          this.register(tags)
          true
      }
    } yield result
  }

  @throws[IOException]
  private def loadCountryTags(): Task[Seq[CountryTag]] = {
    val tagsBuf = ListBuffer.empty[CountryTag]

    val modFiles = listTagFiles(HOIIVFiles.Mod.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))
    val baseFiles = listTagFiles(HOIIVFiles.HOI4.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))

    def readFiles(files: Seq[File], skipDuplicates: Boolean): Task[Unit] = {
      ZIO.foreachDiscard(files) { file =>
        for {
          node <- new ZIOParser(file).parse
          pdxs <- ZIO.attempt {
            val loader = new PDXLoader[CountryTag]()
            node.$.collect({ case p: PDXValueNode[?] => p }).map(tagNode => 
              val countryTag = new CountryTag(this)
              val errors = loader.load(tagNode, countryTag, countryTag)
              if (errors.nonEmpty) {
                println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
              }
            )
          }
        } yield pdxs
//        ZIO.attempt {
//          scala.util.Using.resource(scala.io.Source.fromFile(file)) { source =>
//            source.getLines()
//              .map(_.replaceAll("\\s", ""))
//              .filter(data => data.nonEmpty && data.head != '#')
//              .map(_.takeWhile(_ != '=').trim)
//              .toList
//          }
//        }.flatMap { tagNames =>
//          given ParsingContext = ParsingContext(file)
//
//          ZIO.foreachDiscard(tagNames) { tagName =>
//            for {
//              tag <- CountryTag(tagName, file)
//              _ <- ZIO.unless(tag == CountryTag.NULL_TAG) {
//                ZIO.unless(skipDuplicates && tagsBuf.contains(tag)) {
//                  ZIO.succeed(tagsBuf += tag)
//                }
//              }
//            } yield ()
//          }
//          .provideLayer(ZLayer.succeed(this))   // .provideEnvironment(ZEnvironment(this)).as(true)
//        }
      }
    }

    for {
      _ <- readFiles(modFiles, skipDuplicates = false)
      _ <- readFiles(baseFiles, skipDuplicates = true)
    } yield tagsBuf.toList
  }

  override def tags: Set[CountryTag] = referableEntities.toSet

  override def addTag(tag: CountryTag): Unit =
    this register tag

  override def addTag(tag: CountryTag, file: File): Unit =
    this register tag 
    fileMap.addOne(tag, file)

  private def listTagFiles(folder: File): Seq[File] =
    if (folder.exists() && folder.isDirectory) {
      Option(folder.listFiles()).map(_.toList).getOrElse(Nil)
    } else Nil

  /** Returns all loaded country tags, loading on first access. */
  override def countryTags: Seq[CountryTag] = referableEntities.toList

  override def exists(tag: String): UIO[Boolean] =
    ZIO.succeed(tags.exists(_.equals(tag)))

  override def findExisting(tag: String): UIO[Option[CountryTag]] =
    ZIO.succeed(tags.find(_.$ == tag))

  override def findExisting(tag: String, file: File): UIO[Option[CountryTag]] =
    ZIO.succeed {
      val countryTag = tags.find(_.$ == tag).map(tag => 
        fileMap.addOne(tag, file) 
        tag
      )
      countryTag
    }

  override def file(tag: CountryTag): UIO[Option[File]] = ZIO.succeed(fileMap.get(tag)) 
}
