package com.hoi4utils.hoi4.country

import com.hoi4utils.{FileUtils, HOIIVUtils}

import java.io.{File, IOException}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.jdk.CollectionConverters.*

/**
 * Manages loading and querying HOI4 country tags, both modded and base.
 */
object CountryTagsManager extends HOIIVUtils with Iterable[CountryTag] {
  // lazy so we only load once
  private lazy val countryTags: Seq[CountryTag] = loadCountryTags()

  private def listTagFiles(pathKey: String): Seq[File] = {
    Option(pathKey).map { basePath =>
      val folder = new File(s"$basePath\\common\\country_tags")
      if (folder.exists() && folder.isDirectory) {
        Option(folder.listFiles()).map(_.toList).getOrElse(Nil)
      } else Nil
    }.getOrElse(Nil)
  }

  @throws[IOException]
  private def loadCountryTags(): Seq[CountryTag] = {
    val tagsBuf = ListBuffer.empty[CountryTag]

    val modFiles  = listTagFiles("mod.path").filterNot(_.getName.contains("dynamic_countries"))
    val baseFiles = listTagFiles("hoi4.path").filterNot(_.getName.contains("dynamic_countries"))

    def readFiles(files: Seq[File], skipDuplicates: Boolean): Unit = {
      for (file <- files; line <- Source.fromFile(file).getLines()) {
        val data = line.replaceAll("\\s", "")
        if (FileUtils.usefulData(data)) {
          val key = data.takeWhile(_ != '=').trim
          val tag = new CountryTag(key)
          if (!tag.equals(CountryTag.NULL_TAG) && (!skipDuplicates || !tagsBuf.contains(tag))) {
            tagsBuf += tag
          }
        }
      }
    }

    readFiles(modFiles, skipDuplicates = false)
    readFiles(baseFiles, skipDuplicates = true)

    tagsBuf.toList
  }

  /** Returns all loaded country tags, loading on first access. */
  def getCountryTags: Seq[CountryTag] = countryTags

  /** True if the given tag string exists among loaded country tags. */
  def exists(tag: String): Boolean =
    countryTags.contains(new CountryTag(tag))

  /** Iterable implementation over CountryTag. */
  override def iterator: Iterator[CountryTag] = countryTags.iterator
}
