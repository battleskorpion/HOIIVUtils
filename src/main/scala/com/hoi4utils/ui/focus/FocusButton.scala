package com.hoi4utils.ui.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.ui.custom_javafx.image.ScalaFXImageUtils
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Button
import scalafx.scene.image.Image

import scala.util.boundary

class FocusButton extends Button with LazyLogging:
  private var focus = ???
  private val gfxFocusUnavailable: Image = loadFocusUnavailableImage("focus_unavailable_bg.dds")

  private def loadFocusUnavailableImage(focusUnavailablePath: String): Image = boundary:
    val inputStream =
      try
        getClass.getClassLoader.getResourceAsStream(focusUnavailablePath)
      catch
        case e: Exception =>
          logger.error(s"Failed to load focus unavailable image from $focusUnavailablePath", e)
          boundary.break(null)
    val buffer = new Array[Byte](inputStream.available)
    inputStream.read(buffer)
    inputStream.close()
    ScalaFXImageUtils.imageFromDDS(
      DDSReader.read(buffer, DDSReader.ARGB, 0) match
        case Some(value) => value
        case None => ???,
      DDSReader.getWidth(buffer),
      DDSReader.getHeight(buffer)
    )