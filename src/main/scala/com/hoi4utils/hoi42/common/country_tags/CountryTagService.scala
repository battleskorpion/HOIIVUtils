package com.hoi4utils.hoi42.common.country_tags

import com.hoi4utils.hoi42.history.countries.CountryFile
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParsingContext}
import com.hoi4utils.script2.{PDXFileError, PDXReadable, Referable}
import com.typesafe.scalalogging.LazyLogging
import zio.{RIO, Task, UIO, URIO, URLayer, ZEnvironment, ZIO, ZLayer}

import java.io.{File, IOException}
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable.ParTrieMap
import scala.io.Source
import scala.util.Using

trait CountryTagService extends PDXReadable with LazyLogging {
  var errors: ListBuffer[PDXFileError]

  def tags: Set[CountryTag]
  def addTag(tag: CountryTag): Unit
  def addTag(tag: CountryTag, file: File): Unit
  def countryTags: Seq[CountryTag]
  def exists(tag: String): UIO[Boolean]
  def findExisting(tag: String): UIO[Option[CountryTag]]
  def findExisting(tag: String, file: File): UIO[Option[CountryTag]]
  def file(tag: CountryTag): UIO[Option[File]]
}

object CountryTagService {
  val live: URLayer[Any, CountryTagService] =
    ZLayer.derive[CountryTagServiceImpl]
}

case class CountryTagServiceImpl() extends CountryTagService {
  override val cleanName: String = "CountryTags"
  var errors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')
  private lazy val _tagList: ListBuffer[CountryTag] = {
    ListBuffer[CountryTag]()
  }
  
  private fileMap: ParTrieMap[CountryTag, File]() 

  override def read(): Task[Boolean] = {
    for {
      tags <- loadCountryTags()
      result <- ZIO.attempt {
        if (tags.isEmpty)
          logger.error(s"No country tags loaded!?")
          false
        else
          _tagList.addAll(tags)
          true
      }
    } yield result
  }

  override def clear(): Task[Unit] =
    ZIO.succeed(_tagList.clear())

  @throws[IOException]
  private def loadCountryTags(): Task[Seq[CountryTag]] = {
    val tagsBuf = ListBuffer.empty[CountryTag]

    val modFiles = listTagFiles(HOIIVFiles.Mod.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))
    val baseFiles = listTagFiles(HOIIVFiles.HOI4.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))

    def readFiles(files: Seq[File], skipDuplicates: Boolean): Task[Unit] = {
      ZIO.foreachDiscard(files) { file =>
        ZIO.attempt {
          scala.util.Using.resource(scala.io.Source.fromFile(file)) { source =>
            source.getLines()
              .map(_.replaceAll("\\s", ""))
              .filter(data => data.nonEmpty && data.head != '#')
              .map(_.takeWhile(_ != '=').trim)
              .toList
          }
        }.flatMap { tagNames =>
          given ParsingContext = ParsingContext(file)

          ZIO.foreachDiscard(tagNames) { tagName =>
            for {
              tag <- CountryTag(tagName, file)
              _ <- ZIO.unless(tag == CountryTag.NULL_TAG) {
                ZIO.unless(skipDuplicates && tagsBuf.contains(tag)) {
                  ZIO.succeed(tagsBuf += tag)
                }
              }
            } yield ()
          }
          .provideLayer(ZLayer.succeed(this))   // .provideEnvironment(ZEnvironment(this)).as(true)
        }
      }
    }

    for {
      _ <- readFiles(modFiles, skipDuplicates = false)
      _ <- readFiles(baseFiles, skipDuplicates = true)
    } yield tagsBuf.toList
  }

  override def tags: Set[CountryTag] = _tagList.toSet

  //  def tagList(): List[CountryTag] = _tagList
  override def addTag(tag: CountryTag): Unit = _tagList.addOne(tag)

  override def addTag(tag: CountryTag, file: File): Unit = 
    _tagList.addOne(tag)
    fileMap.add(tag, file)

  private def listTagFiles(folder: File): Seq[File] =
    if (folder.exists() && folder.isDirectory) {
      Option(folder.listFiles()).map(_.toList).getOrElse(Nil)
    } else Nil

  /** Returns all loaded country tags, loading on first access. */
  override def countryTags: Seq[CountryTag] = _tagList.toList

  override def exists(tag: String): UIO[Boolean] =
    ZIO.succeed(_tagList.exists(_.equals(tag)))

  override def findExisting(tag: String): UIO[Option[CountryTag]] =
    ZIO.succeed(_tagList.find(_.get == tag))

  override def findExisting(tag: String, file: File): UIO[Option[CountryTag]] = 
    ZIO.succeed {
      val tag = _tagList.find(_.get == tag)
      fileMap.add(tag, file)
      tag 
    }
}
