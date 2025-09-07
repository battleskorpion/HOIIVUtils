package com.hoi4utils.hoi4.country

import com.hoi4utils.parser.Parser
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

object CountryTag extends Iterable[CountryTag] with LazyLogging with PDXReadable {

  val NULL_TAG: CountryTag = CountryTag("###")
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')
  private lazy val _tagList: ListBuffer[CountryTag] = {
    //    println("Initializing _tagList")
    ListBuffer[CountryTag]()
  }

  def read(): Boolean = {
    if (!HOIIVFiles.Mod.country_tags_folder.exists || !HOIIVFiles.Mod.country_tags_folder.isDirectory) {
      logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.country_tags_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.country_tags_folder.listFiles == null || HOIIVFiles.Mod.country_tags_folder.listFiles.length == 0) {
      logger.warn(s"No country tags found in ${HOIIVFiles.Mod.country_tags_folder}")
      false
    } else {
      logger.info("Reading country tags from " + HOIIVFiles.Mod.country_tags_folder)

      // create focus trees from files
      HOIIVFiles.Mod.country_tags_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
        val parser = new Parser(f)
        val rootNode = parser.parse

        rootNode.foreach(node => {
          CountryTag(node.name.trim, f)
        })
      }
      true
    }
  }

  def apply(tag: String): CountryTag = _tagList.find(_.get eq tag) match
    case Some(countryTag) => countryTag
    case None => new CountryTag(tag)

  def apply(tag: String, file: File): CountryTag = _tagList.find(t => t.get eq tag) match
    case Some(countryTag) => countryTag
    case None =>
      logger.warn(s"In CountryTag.fromFile - Tag '$tag' from file '$file' not found in loaded tags.")
      new CountryTag(tag, Some(file))

  override def iterator: Iterator[CountryTag] = _tagList.iterator

  //  def tagList(): List[CountryTag] = _tagList
  def addTag(tag: CountryTag): Unit = _tagList.addOne(tag)
  
}

/**
 *
 * @param tag the country tag (generally a 3 letter code)
 * @param file if this tag is defined in the mod's (or vanilla's) country tags files, file it is defined in,
 *             or should be defined in.
 */
class CountryTag private(val tag: String, file: Option[File] = None) extends Comparable[CountryTag] {
  CountryTag.addTag(this)

  def get: String = tag
  
  override def toString: String = {
    tag
  }
  
  override def equals(obj: Any): Boolean = obj match
    case other: CountryTag => tag eq other.tag
    case str: String => tag eq str
    case _ => false

  override def compareTo(o: CountryTag): Int = {
    tag.compareTo(o.tag)
  }

  def isFileDefined: Boolean = file.isDefined
}
