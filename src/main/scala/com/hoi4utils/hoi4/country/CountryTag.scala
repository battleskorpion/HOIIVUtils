package com.hoi4utils.hoi4.country

import com.hoi4utils.parser.Parser
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.collection.mutable.ListBuffer

class CountryTag(val tag: String) extends Comparable[CountryTag] {
  CountryTag.addTag(this)
  
  def get: String = tag
  
  override def toString: String = {
    tag
  }
  
  override def equals(obj: Any): Boolean = {
    obj match {
      case other: CountryTag =>
        tag.equals(other.tag)
      case string: String =>
        tag.equals(string)
      case _ => false
    }
  }

  override def compareTo(o: CountryTag): Int = {
    tag.compareTo(o.tag)
  }
}

object CountryTag extends Iterable[CountryTag] with LazyLogging with PDXReadable {

  val NULL_TAG = new CountryTag("###")
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')
  private lazy val _tagList: ListBuffer[CountryTag] = {
    //    println("Initializing _tagList")
    ListBuffer[CountryTag]()
  }

  def read(testFile: File = null): Boolean = {
    if (!HOIIVFiles.Mod.country_tags_folder.exists || !HOIIVFiles.Mod.country_tags_folder.isDirectory) {
      logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.country_tags_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.country_tags_folder.listFiles == null || HOIIVFiles.Mod.country_tags_folder.listFiles.length == 0) {
      logger.warn(s"No country tags found in ${HOIIVFiles.Mod.country_tags_folder}")
      false
    } else {

      // create focus trees from files
      HOIIVFiles.Mod.country_tags_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
        new Parser(f, this.getClass).rootNode.foreach(node => {
          new CountryTag(node.name.trim)
        })
      }
      true
    }
  }

  def clear(): Unit = {
    _tagList.clear()
  }

  def get(tag: String): CountryTag = {
    for (countryTag <- _tagList) {
      if (countryTag.get.equals(tag)) return countryTag
    }
    NULL_TAG
  }

  override def iterator: Iterator[CountryTag] = {
    _tagList.iterator
  }

  //  def tagList(): List[CountryTag] = _tagList
  def addTag(tag: CountryTag): Unit = {
    _tagList.addOne(tag)
  }
}
