package com.hoi4utils.hoi42.map.strategic_regions.service

import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.hoi42.map.strategic_regions.{StrategicRegion, StrategicRegionRegistry}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.ZIOParser
import com.hoi4utils.script2.PDXPropertyValueExtensions.*
import com.hoi4utils.script2.{PDXLoader, PDXReadable}
import zio.{Task, URIO, URLayer, ZIO, ZLayer}

import java.io.File

trait StrategicRegionService extends StrategicRegionRegistry with PDXReadable {
  def get(file: File): URIO[CountryTagService, Option[StrategicRegion]]
  def add(stratRegion: StrategicRegion): Iterable[StrategicRegion]

  def list: Set[StrategicRegion] // todo rename lols
  def get(id: Int): Option[StrategicRegion]
  def get(stratRegionName: String): Option[StrategicRegion]
  def readStratRegion(file: File): Boolean
  def removeStratRegion(file: File): Boolean

  override def clear(): Task[Unit] =
    super[StrategicRegionRegistry].clear()
}

object StrategicRegionService {
  val live: URLayer[CountryTagService, StrategicRegionService] =
    ZLayer.fromFunction(StrategicRegionServiceImpl.apply)
}

case class StrategicRegionServiceImpl(countryTagService: CountryTagService) extends StrategicRegionService {
  override val display: String = "Strategic Regions"

  /**
   * Creates Strategic Regions from reading files
   */
  def read(): Task[Boolean] =
    def readStratRegions(files: Seq[File], skipDuplicates: Boolean): Task[Seq[StrategicRegion]] = {
      ZIO.foreach(files) { file =>
        for {
          node <- new ZIOParser(file).parse
          pdx <- ZIO.attempt {
            val loader = new PDXLoader[StrategicRegion]()
            val stratRegion = new StrategicRegion(this, Some(file))
            val errors = loader.load(node, stratRegion, stratRegion)
            if (errors.nonEmpty) {
              println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
            }
            stratRegion
          }
        } yield pdx
      }
    }

    if !HOIIVFiles.Mod.strat_region_dir.exists || !HOIIVFiles.Mod.strat_region_dir.isDirectory then
      ZIO.logError(s"In StrategicRegion.java - ${HOIIVFiles.Mod.strat_region_dir} is not a directory, or it does not exist.")
      ZIO.succeed(false)

    else if HOIIVFiles.Mod.strat_region_dir.listFiles == null || HOIIVFiles.Mod.strat_region_dir.listFiles.isEmpty then
      ZIO.logError(s"No strategic regions found in ${HOIIVFiles.Mod.strat_region_dir}")
      ZIO.succeed(false)
    else
      val files = HOIIVFiles.Mod.strat_region_dir.listFiles().filter(_.getName.endsWith(".txt"))
      for {
        stratRegions <- readStratRegions(files, true)
        _ = stratRegions.foreach(add)
      } yield true

  override def stratRegions: Set[StrategicRegion] = referableEntities.toSet

  override def add(stratRegion: StrategicRegion): Iterable[StrategicRegion] =
    this register stratRegion
    stratRegions

  override def list: Set[StrategicRegion] = stratRegions.toSet

  override def get(id: Int): Option[StrategicRegion] =
    stratRegions.find(_.id @== id)

  override def get(stratRegionName: String): Option[StrategicRegion] =
    stratRegions.find(_.name @== stratRegionName)

  /**
   * If the state represented by the file exists in states list, removes the state
   * from the states list
   *
   * @param file state file
   */
  override def removeStratRegion(file: File): Boolean =
    val temp = new StrategicRegion(this, Some(file))
    stratRegions.find(_.id @== temp.id).exists(stratRegion =>
      this deregister stratRegion
      ZIO.logDebug("Removed strategic region " + stratRegion)
      true
    )
}
