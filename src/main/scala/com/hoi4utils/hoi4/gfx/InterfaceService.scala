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
import scala.collection.concurrent.TrieMap

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
  private val gfxMap: TrieMap[String, SpriteType] = new TrieMap
  private var interfaceFiles: TrieMap[File, Interface] = new TrieMap
  var interfaceErrors: ListBuffer[PDXFileError] = ListBuffer.empty[PDXFileError]

  override def getGFX(icon: String): UIO[Option[String]] = {
    for {
      spriteType <- getSpriteType(icon)
      result = spriteType.map(_.gfx)
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

  private def readMod(): Task[Boolean] = {
    val folder = HOIIVFiles.Mod.interface_folder
    if (!folder.exists || !folder.isDirectory) {
      ZIO.logError("Warning: mod interface directory does not exist").as(false)
    } else if (folder.listFiles == null || folder.listFiles.isEmpty) {
      ZIO.logError("Warning: mod interface directory is empty").as(false)
    } else {
      val gfxFiles = folder.listFiles.filter(_.getName.endsWith(".gfx")).toList

      ZIO.foreachPar(gfxFiles) { f =>
        for {
          interface <- ZIO.succeed(new Interface(f))
          itemsToAdd <- interface.readGFXFile(interfaceErrors)
          _ <- ZIO.foreachDiscard(itemsToAdd) { case (name, gfx) =>
            addSpriteType(name, gfx)
          }
        } yield (f, interface)
      }
      .withParallelism(8)
      .map { interfaceFilesMap =>
        interfaceFilesMap.foreach { (f, interface) =>
          interfaceFiles.put(f, interface)
        }
        true
      }
    } <* ZIO.logInfo(s"Read Interface files from $folder")
  }

  private def readHoi4(): Task[Boolean] = {
    val folder = HOIIVFiles.HOI4.interface_folder
    if (!folder.exists || !folder.isDirectory) {
      ZIO.logError("Warning: HOI4 interface directory does not exist").as(false)
    } else if (folder.listFiles == null || folder.listFiles.isEmpty) {
      ZIO.logError("Warning: HOI4 interface directory is empty").as(false)
    } else {
      val gfxFiles = folder.listFiles.filter(_.getName.endsWith(".gfx"))

      ZIO.foreachPar(gfxFiles) { f =>
        for {
          interface <- ZIO.succeed(new Interface(f))
          itemsToAdd <- interface.readGFXFile(interfaceErrors)
          _ <- ZIO.foreachDiscard(itemsToAdd) { case (name, gfx) =>
            addSpriteType(name, gfx)
          }
//          _ <- ZIO.logError(s"read $f")
        } yield (f, interface)
      }.map { interfaceFilesMap =>
        interfaceFilesMap.foreach { (f, interface) =>
          interfaceFiles.put(f, interface)
        }
        true
      }
    } <* ZIO.logInfo(s"Read Interface files from $folder")
  }

  def read(): Task[Boolean] = readMod() && readHoi4()

  override def clear(): Task[Unit] =
    ZIO.succeed(gfxMap.clear()) &> ZIO.succeed(interfaceFiles.clear())

}
