package com.hoi4utils.hoi4.gfx

import com.hoi4utils.hoi4.common.national_focus.{FocusTreeManager, FocusTreeManagerImpl}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.script.{PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import zio.{Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*

trait InterfaceService extends PDXReadable with LazyLogging {
  var interfaceErrors: ListBuffer[PDXFileError]

  def getGFX(icon: String): URIO[InterfaceService, Option[String]]
  def getSpriteType(icon: String): UIO[Option[SpriteType]]
  def addSpriteType(icon: String, sprite: SpriteType): UIO[Unit]
  def numGFX: Int
  def listGFX: Iterable[SpriteType]
  def listGFXFiles: Iterable[Interface]
  def list: Iterable[Interface]
  def getGFXMap: mutable.Map[String, SpriteType]
  def numFiles: Int
}

object InterfaceService {
  val live: URLayer[Any, InterfaceService] =
    ZLayer.derive[InterfaceServiceImpl]
}

/**
 * Represents a .gfx file in interface folder
 * Contains a set of SpriteTypes
 */
case class InterfaceServiceImpl() extends InterfaceService {
  override val cleanName: String = "InterfaceGFX"
  private val gfxMap: mutable.Map[String, SpriteType] = new mutable.HashMap
  private var interfaceFiles: mutable.Map[File, Interface] = new mutable.HashMap
  var interfaceErrors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]

  override def getGFX(icon: String): URIO[InterfaceService, Option[String]] = {
    for {
      spriteType <- getSpriteType(icon)
      result = spriteType match {
        case Some(spriteType) => Some(spriteType.gfx)
        case None => None
      }
    } yield result
  }

  override def getSpriteType(icon: String): UIO[Option[SpriteType]] =
    ZIO.succeed(gfxMap.get(icon))

  override def addSpriteType(icon: String, sprite: SpriteType): UIO[Unit] = ZIO.succeed(gfxMap.put(icon, sprite))

  override def numGFX: Int = gfxMap.size

  /** Lists all SpriteType objects found in interface files */
  override def listGFX: Iterable[SpriteType] = gfxMap.values

  /** Returns all .gfx files parsed */
  override def listGFXFiles: Iterable[Interface] = interfaceFiles.values

  /** Lists .gfx files which Interface class represents and which have been read in. */
  override def list: Iterable[Interface] = listGFXFiles

  // todo do we want this?
  override def getGFXMap: mutable.Map[String, SpriteType] = gfxMap

  /** Returns the number of .gfx interface files read */
  override def numFiles: Int = interfaceFiles.size

  private def readMod(): Boolean = {
    if (!HOIIVFiles.Mod.interface_folder.exists || !HOIIVFiles.Mod.interface_folder.isDirectory) {
      logger.error("Warning: mod interface directory does not exist")
      false
    } else if (HOIIVFiles.Mod.interface_folder.listFiles == null || HOIIVFiles.Mod.interface_folder.listFiles.isEmpty) {
      logger.error("Warning: mod interface directory is empty")
      false
    } else {
      val gfxFiles = HOIIVFiles.Mod.interface_folder.listFiles.filter(_.getName.endsWith(".gfx"))
      val interfaceFilesMap = gfxFiles.par.map(f => f -> new Interface(f)).seq

      interfaceFilesMap.foreach { (f, interface) =>
        interfaceFiles.put(f, interface)
      }
      true
    }
  }

  private def readHoi4(): Boolean = {
    if (!HOIIVFiles.HOI4.interface_folder.exists || !HOIIVFiles.HOI4.interface_folder.isDirectory) {
      logger.error("HOI4 interface directory does not exist")
      false
    } else if (HOIIVFiles.HOI4.interface_folder.listFiles == null || HOIIVFiles.HOI4.interface_folder.listFiles.isEmpty) {
      logger.error("HOI4 interface directory is empty")
      false
    } else {
      val gfxFiles = HOIIVFiles.HOI4.interface_folder.listFiles.filter(_.getName.endsWith(".gfx"))
      val interfaceFilesMap = gfxFiles.par.map(f => f -> new Interface(f)).seq

      interfaceFilesMap.foreach { (f, interface) =>
        interfaceFiles.put(f, interface)
      }
      true
    }
  }

  def read(): Task[Boolean] = ZIO.succeed(readMod() && readHoi4())

  override def clear(): Task[Unit] =
    ZIO.succeed(gfxMap.clear()) &> ZIO.succeed(interfaceFiles.clear())

}
