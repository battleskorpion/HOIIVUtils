package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi42.gfx.InterfaceService
import javafx.scene.image.Image
import zio.{ULayer, URIO, ZIO, ZLayer}

import scala.collection.{SeqMap, mutable}

trait FocusService {
  def getDDSImage(focus: Focus): URIO[InterfaceService, Option[Image]]
}

object FocusService {
  val live: ULayer[FocusService] =
    ZLayer.derive[FocusServiceImpl]
}

case class FocusServiceImpl() extends FocusService {
  
  val focusImages = new mutable.HashMap[Focus, ImageAndGFX]()
  
  def getDDSImage(focus: Focus): URIO[InterfaceService, Option[Image]] =
    // bad code. it's fine for now.
    // Updated code, now its very bad code. its really not fine but its staying for now.
    // Updated again, its still very bad but shorter so better?
    for {
      interfaceService <- ZIO.service[InterfaceService]
      iconName: Option[String] = focus.icon.map(_.spriteID)
      gfxPath <- iconName match
        case Some(name) => interfaceService.getGFX(name)
        case None => ZIO.none
      _ddsImage = focusImages.get(focus).map(_.image)
      _ddsImageGFX = focusImages.get(focus).map(_.gfx)
      result = gfxPath.flatMap { gfx =>
        if _ddsImage.isDefined && _ddsImageGFX.get.equals(gfx) then
          _ddsImage
        else
          val newImage = DDSReader.readDDSImage(gfx).get
          focusImages.put(focus, ImageAndGFX(newImage, gfx))
          Some(newImage)
      }
    } yield result
} 

case class ImageAndGFX(image: Image, gfx: String) 

