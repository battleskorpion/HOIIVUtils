package com.hoi4utils.ui.custom_javafx.image

import scalafx.scene.image.{Image, WritableImage}

/**
 * Utility class for converting DDS pixel data into a JavaFX Image.
 */
object ScalaFXImageUtils {
  /**
   * Converts an array of ARGB pixel data into a JavaFX {@link Image}.
   *
   * @param ddspixels the array of pixel data in ARGB format
   * @param ddswidth  the width of the image
   * @param ddsheight the height of the image
   * @return a {@link Image} constructed from the provided pixel data
   */
  def imageFromDDS(ddspixels: Array[Int], ddswidth: Int, ddsheight: Int): Image = {
    val writableImage = new WritableImage(ddswidth, ddsheight)
    val pixelWriter = writableImage.getPixelWriter
    for (
      y <- 0 until ddsheight; 
      x <- 0 until ddswidth
    ) {
      val pixel = ddspixels(y * ddswidth + x)
      pixelWriter.setArgb(x, y, pixel)
    }
    writableImage
  }
}