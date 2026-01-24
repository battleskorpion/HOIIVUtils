package com.hoi4utils.hoi4.common.country_tags

import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParsingContext}
import com.hoi4utils.script.{PDXFileError, PDXReadable, Referable}
import com.typesafe.scalalogging.LazyLogging
import zio.{Task, URIO, ZIO}

import java.io.{File, IOException}
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

/**
 *
 * @param tag the country tag (generally a 3 letter code)
 * @param file if this tag is defined in the mod's (or vanilla's) country tags files, file it is defined in,
 *             or should be defined in.
 */
class CountryTag private[country_tags](val tag: String, var file: Option[File] = None) extends Comparable[CountryTag] with Referable {
  def get: String = tag

  override def toString: String = {
    tag
  }

  override def equals(obj: Any): Boolean = obj match
    case other: CountryTag => tag == other.tag
    case str: String => tag == str
    case _ => false

  override def compareTo(o: CountryTag): Int = {
    tag.compareTo(o.tag)
  }

  def isFileDefined: Boolean = file.isDefined

  override def referableID: Option[String] = Some(tag)
}

object CountryTag {
  val NULL_TAG: CountryTag = new CountryTag("###")

  /**
   * Creates or retrieves a CountryTag for the given tag string.
   *
   * @param tag the country tag string
   * @return the CountryTag instance
   */
  def apply(tag: String): URIO[CountryTagService, CountryTag] = {
    for {
      service <- ZIO.service[CountryTagService]
      existingTag <- service.findExisting(tag)
      result = existingTag match {
        case Some(countryTag) => countryTag
        case None             =>
          val newTag = new CountryTag(tag)
          service.addTag(newTag)
          newTag
      }
    } yield result
  }

  /**
   * Creates or retrieves a CountryTag for the given tag string, associating it with the provided file.
   *
   * @param tag  the country tag string
   * @param file the file where the tag is defined
   * @return
   * @note `using` has to specified as second parameter list to avoid ambiguous overload issues
   */
  def apply(tag: String, file: File)(using ParsingContext): URIO[CountryTagService, CountryTag] =
    for {
      service <- ZIO.service[CountryTagService]
      existingTag <- service.findExisting(tag)
      result = existingTag match {
        case Some(countryTag) =>
          if (countryTag.file.isEmpty)
            countryTag.file = Some(file)
          else if (countryTag.file.get != file)
            val pdxError = new PDXFileError(
              additionalInfo = Map(
                "context" -> "Duplicate country tag found",
                "tag" -> tag,
              )
            )
            service.errors += pdxError
          countryTag
        case None =>
          val newTag = new CountryTag(tag, Some(file))
          service.addTag(newTag)
          newTag
      }
    } yield result
}
