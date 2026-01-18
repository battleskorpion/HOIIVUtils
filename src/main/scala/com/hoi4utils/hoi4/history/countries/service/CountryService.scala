package com.hoi4utils.hoi4.history.countries.service

import com.hoi4utils.hoi4.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.map.state.StateService
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.ParsingContext
import com.hoi4utils.script.{PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import zio.{RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import scala.collection.mutable.ListBuffer

trait CountryService extends PDXReadable with LazyLogging {
  var countryErrors: ListBuffer[PDXFileError]

  def list: List[CountryFile]
  def add(country: CountryFile): Iterable[CountryFile]
}

object CountryService {
  val live: URLayer[CountryTagService & StateService, CountryService] = {
    ZLayer.fromFunction(CountryServiceImpl.apply)
  }
}

case class CountryServiceImpl(countryTagService: CountryTagService, stateService: StateService) extends CountryService {
  override val cleanName: String = "Countries"
  var countryErrors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]
  private val countries = new ListBuffer[CountryFile]()

  override def read(): Task[Boolean] =
    val folder = HOIIVFiles.Mod.country_folder

    if (!folder.exists || !folder.isDirectory) {
      ZIO.logError(s"In ${this.getClass.getSimpleName} - $folder is not a directory, or it does not exist.")
        .as(false)
    } else if (folder.listFiles == null || folder.listFiles.length == 0) {
      ZIO.logError(s"No focuses found in $folder")
        .as(false)
    } else {
      ZIO.foreachDiscard(folder.listFiles().filter(_.getName.endsWith(".txt")).toSeq) { f =>
        given ParsingContext(f)
        for {
          tagName <- ZIO.attempt(f.getName.split(" ")(0))
          tag <- CountryTag(tagName, f)
          _ <- ZIO.attempt {
            val countryFile = new CountryFile(f, tag)(countryErrors, stateService)
            add(countryFile)
            countryFile
          }
        } yield ()
      }.provideLayer(ZLayer.succeed(countryTagService)).as(true)
    }

  /**
   * Clears all countries and any other relevant values.
   */
  override def clear(): Task[Unit] =
    ZIO.succeed(countries.clear())

  override def list: List[CountryFile] = countries.toList

  /**
   * Adds a focus tree to the list of focus trees.
   *
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  override def add(country: CountryFile): Iterable[CountryFile] = {
    countries += country
    countries
  }
}

