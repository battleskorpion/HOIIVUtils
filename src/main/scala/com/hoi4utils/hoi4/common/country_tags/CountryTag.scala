package com.hoi4utils.hoi4.common.country_tags

import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.Parser
import com.hoi4utils.script.{PDXReadable, Referable}
import com.typesafe.scalalogging.LazyLogging

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
class CountryTag private(val tag: String, var file: Option[File] = None) extends Comparable[CountryTag] with Referable {
  CountryTag.addTag(this)

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

object CountryTag extends Iterable[CountryTag] with LazyLogging with PDXReadable {
  val NULL_TAG: CountryTag = CountryTag("###")
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')
  private lazy val _tagList: ListBuffer[CountryTag] = {
    ListBuffer[CountryTag]()
  }

  def read(): Boolean = {
    val tags = loadCountryTags()
    if (tags.isEmpty)
      logger.error(s"No country tags loaded!?")
      return false
    _tagList.addAll(tags)
    true
  }
  
  override def clear(): Unit = _tagList.clear()

  @throws[IOException]
  private def loadCountryTags(): Seq[CountryTag] = {
    val tagsBuf = ListBuffer.empty[CountryTag]

    val modFiles = listTagFiles(HOIIVFiles.Mod.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))
    val baseFiles = listTagFiles(HOIIVFiles.HOI4.country_tags_folder).filterNot(_.getName.contains("dynamic_countries"))

    def readFiles(files: Seq[File], skipDuplicates: Boolean): Unit = {
      for (file <- files) {
        val parser = new Parser(file)
        val rootNode = parser.parse

        rootNode.foreach(node => {
          CountryTag(node.name.trim, file)
        })
        Using.resource(Source.fromFile(file)) { source =>
          source
            .getLines()
            .map(_.replaceAll("\\s", ""))
            .filter(data => data.nonEmpty && data.head != '#')
            .map(data => CountryTag(data.takeWhile(_ != '=').trim, file))
            .filter(_ != CountryTag.NULL_TAG)
            .filter(tag => !skipDuplicates || !tagsBuf.contains(tag))
            .foreach(tagsBuf += _)
        }
      }
    }

    readFiles(modFiles, skipDuplicates = false)
    readFiles(baseFiles, skipDuplicates = true)

    tagsBuf.toList
  }

  def apply(tag: String): CountryTag = _tagList.find(_.get == tag) match
    case None => new CountryTag(tag)
    case Some(countryTag) => countryTag

  def apply(tag: String, file: File): CountryTag = _tagList.find(t => t.get == tag) match
    case None => new CountryTag(tag, Some(file))
    case Some(countryTag) =>
      if (countryTag.file.isEmpty)
        countryTag.file = Some(file)
      else if (countryTag.file.get != file)
        val message = s"Tag '$tag' from file '$file' already found in loaded tags, file: ${countryTag.file}"
        CountryFile.countryErrors += message
      countryTag

  override def iterator: Iterator[CountryTag] = _tagList.iterator

  //  def tagList(): List[CountryTag] = _tagList
  def addTag(tag: CountryTag): Unit = _tagList.addOne(tag)

  private def listTagFiles(folder: File): Seq[File] = {
    if (folder.exists() && folder.isDirectory) {
      Option(folder.listFiles()).map(_.toList).getOrElse(Nil)
    } else Nil
  }

  /** Returns all loaded country tags, loading on first access. */
  def countryTags: Seq[CountryTag] = _tagList.toList

  def exists(tag: String): Boolean = _tagList.exists(_.equals(tag))
}