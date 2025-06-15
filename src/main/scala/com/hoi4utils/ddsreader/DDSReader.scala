/**
 * DDSReader.scala
 *
 * Copyright (c) 2015 Kenji Sasaki
 * Released under the MIT license.
 * https://github.com/npedotnet/DDSReader/blob/master/LICENSE
 *
 * Converted to idiomatic Scala
 */

package com.hoi4utils.ddsreader

import javafx.scene.image.{Image, PixelFormat, WritableImage}

import java.io.{File, IOException}
import java.nio.file.Files
import scala.util.{Failure, Success, Try}

object DDSReader {

  // Color channel ordering
  case class Order(redShift: Int, greenShift: Int, blueShift: Int, alphaShift: Int)

  val ARGB: Order = Order(16, 8, 0, 24)
  val ABGR: Order = Order(0, 8, 16, 24)

  // Image format constants
  private object ImageType {
    val DXT1 = 0x44585431
    val DXT2 = 0x44585432
    val DXT3 = 0x44585433
    val DXT4 = 0x44585434
    val DXT5 = 0x44585435
    val A1R5G5B5 = (1 << 16) | 2
    val X1R5G5B5 = (2 << 16) | 2
    val A4R4G4B4 = (3 << 16) | 2
    val X4R4G4B4 = (4 << 16) | 2
    val R5G6B5   = (5 << 16) | 2
    val R8G8B8   = (1 << 16) | 3
    val A8B8G8R8 = (1 << 16) | 4
    val X8B8G8R8 = (2 << 16) | 4
    val A8R8G8B8 = (3 << 16) | 4
    val X8R8G8B8 = (4 << 16) | 4
  }

  // RGBA Masks
  private val maskMap = Map(
    ImageType.A1R5G5B5 -> Array(0x7C00, 0x03E0, 0x001F, 0x8000),
    ImageType.X1R5G5B5 -> Array(0x7C00, 0x03E0, 0x001F, 0x0000),
    ImageType.A4R4G4B4 -> Array(0x0F00, 0x00F0, 0x000F, 0xF000),
    ImageType.X4R4G4B4 -> Array(0x0F00, 0x00F0, 0x000F, 0x0000),
    ImageType.R5G6B5   -> Array(0xF800, 0x07E0, 0x001F, 0x0000),
    ImageType.R8G8B8   -> Array(0xFF0000, 0x00FF00, 0x0000FF, 0x000000),
    ImageType.A8B8G8R8 -> Array(0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000),
    ImageType.X8B8G8R8 -> Array(0x000000FF, 0x0000FF00, 0x00FF0000, 0x00000000),
    ImageType.A8R8G8B8 -> Array(0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000),
    ImageType.X8R8G8B8 -> Array(0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000)
  )

  // Bit conversion tables
  private val BIT5 = (0 to 31).map(i => (i * 255.0 / 31.0).round.toInt).toArray
  private val BIT6 = (0 to 63).map(i => (i * 255.0 / 63.0).round.toInt).toArray

  // Header extraction methods using bit manipulation helpers
  private def extractInt32(buffer: Array[Byte], offset: Int): Int =
    (buffer(offset) & 0xFF) |
      ((buffer(offset + 1) & 0xFF) << 8) |
      ((buffer(offset + 2) & 0xFF) << 16) |
      ((buffer(offset + 3) & 0xFF) << 24)

  def getWidth(buffer: Array[Byte]): Int = extractInt32(buffer, 16)
  def getHeight(buffer: Array[Byte]): Int = extractInt32(buffer, 12)
  def getMipmap(buffer: Array[Byte]): Int = extractInt32(buffer, 28)
  def getPixelFormatFlags(buffer: Array[Byte]): Int = extractInt32(buffer, 80)
  def getFourCC(buffer: Array[Byte]): Int =
    ((buffer(84) & 0xFF) << 24) | ((buffer(85) & 0xFF) << 16) |
      ((buffer(86) & 0xFF) << 8) | (buffer(87) & 0xFF)
  def getBitCount(buffer: Array[Byte]): Int = extractInt32(buffer, 88)
  def getRedMask(buffer: Array[Byte]): Int = extractInt32(buffer, 92)
  def getGreenMask(buffer: Array[Byte]): Int = extractInt32(buffer, 96)
  def getBlueMask(buffer: Array[Byte]): Int = extractInt32(buffer, 100)
  def getAlphaMask(buffer: Array[Byte]): Int = extractInt32(buffer, 104)

  /**
   * Main reading method that decodes DDS pixel data
   */
  def read(buffer: Array[Byte], order: Order, mipmapLevel: Int): Option[Array[Int]] = {
    val width = getWidth(buffer)
    val height = getHeight(buffer)
    val mipmap = getMipmap(buffer)

    getImageType(buffer) match {
      case None => None
      case Some(imageType) =>
        val (finalWidth, finalHeight, offset) = calculateOffsetAndDimensions(
          buffer, imageType, width, height, mipmap, mipmapLevel)

        decodePixels(imageType, finalWidth, finalHeight, offset, buffer, order)
    }
  }

  private def getImageType(buffer: Array[Byte]): Option[Int] = {
    val flags = getPixelFormatFlags(buffer)

    if ((flags & 0x04) != 0) {
      // DXT format
      Some(getFourCC(buffer))
    } else if ((flags & 0x40) != 0) {
      // RGB format
      detectRGBFormat(buffer, flags)
    } else {
      None // YUV or LUMINANCE not supported
    }
  }

  private def detectRGBFormat(buffer: Array[Byte], flags: Int): Option[Int] = {
    val bitCount = getBitCount(buffer)
    val redMask = getRedMask(buffer)
    val greenMask = getGreenMask(buffer)
    val blueMask = getBlueMask(buffer)
    val alphaMask = if ((flags & 0x01) != 0) getAlphaMask(buffer) else 0

    val candidates = maskMap.filter { case (_, masks) =>
      masks(0) == redMask && masks(1) == greenMask &&
        masks(2) == blueMask && masks(3) == alphaMask
    }

    candidates.headOption.map(_._1).filter { imageType =>
      val expectedBitCount = imageType & 0xFF
      expectedBitCount == 0 || bitCount == expectedBitCount * 8
    }
  }

  private def calculateOffsetAndDimensions(
                                            buffer: Array[Byte],
                                            imageType: Int,
                                            initialWidth: Int,
                                            initialHeight: Int,
                                            mipmap: Int,
                                            mipmapLevel: Int
                                          ): (Int, Int, Int) = {
    var width = initialWidth
    var height = initialHeight
    var offset = 128 // header size

    if (mipmapLevel > 0 && mipmapLevel < mipmap) {
      for (_ <- 0 until mipmapLevel) {
        val blockSize = imageType match {
          case ImageType.DXT1 => 8 * ((width + 3) / 4) * ((height + 3) / 4)
          case ImageType.DXT2 | ImageType.DXT3 | ImageType.DXT4 | ImageType.DXT5 =>
            16 * ((width + 3) / 4) * ((height + 3) / 4)
          case _ => (imageType & 0xFF) * width * height
        }
        offset += blockSize
        width = math.max(1, width / 2)
        height = math.max(1, height / 2)
      }
    }

    (width, height, offset)
  }

  private def decodePixels(
                            imageType: Int,
                            width: Int,
                            height: Int,
                            offset: Int,
                            buffer: Array[Byte],
                            order: Order
                          ): Option[Array[Int]] = {
    imageType match {
      case ImageType.DXT1 => Some(decodeDXT1(width, height, offset, buffer, order))
      case ImageType.DXT2 => Some(decodeDXT3(width, height, offset, buffer, order))
      case ImageType.DXT3 => Some(decodeDXT3(width, height, offset, buffer, order))
      case ImageType.DXT4 => Some(decodeDXT5(width, height, offset, buffer, order))
      case ImageType.DXT5 => Some(decodeDXT5(width, height, offset, buffer, order))
      case ImageType.A1R5G5B5 => Some(readA1R5G5B5(width, height, offset, buffer, order))
      case ImageType.X1R5G5B5 => Some(readX1R5G5B5(width, height, offset, buffer, order))
      case ImageType.A4R4G4B4 => Some(readA4R4G4B4(width, height, offset, buffer, order))
      case ImageType.X4R4G4B4 => Some(readX4R4G4B4(width, height, offset, buffer, order))
      case ImageType.R5G6B5 => Some(readR5G6B5(width, height, offset, buffer, order))
      case ImageType.R8G8B8 => Some(readR8G8B8(width, height, offset, buffer, order))
      case ImageType.A8B8G8R8 => Some(readA8B8G8R8(width, height, offset, buffer, order))
      case ImageType.X8B8G8R8 => Some(readX8B8G8R8(width, height, offset, buffer, order))
      case ImageType.A8R8G8B8 => Some(readA8R8G8B8(width, height, offset, buffer, order))
      case ImageType.X8R8G8B8 => Some(readX8R8G8B8(width, height, offset, buffer, order))
      case _ => None
    }
  }

  // DXT Decoding methods
  private def decodeDXT1(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] = {
    val pixels = Array.ofDim[Int](width * height)
    var index = offset
    val w = (width + 3) / 4
    val h = (height + 3) / 4

    for (i <- 0 until h; j <- 0 until w) {
      val c0 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2
      val c1 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2

      for (k <- 0 until 4 if 4 * i + k < height) {
        val byte = buffer(index)
        index += 1
        val indices = Array(byte & 0x03, (byte & 0x0C) >> 2, (byte & 0x30) >> 4, (byte & 0xC0) >> 6)

        for ((t, l) <- indices.zipWithIndex if 4 * j + l < width) {
          pixels(4 * width * i + 4 * j + width * k + l) = getDXTColor(c0, c1, 0xFF, t, order)
        }
      }
    }
    pixels
  }

  private def decodeDXT3(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] = {
    var index = offset
    val w = (width + 3) / 4
    val h = (height + 3) / 4
    val pixels = Array.ofDim[Int](width * height)

    for (i <- 0 until h; j <- 0 until w) {
      // Create alpha table (4bit to 8bit)
      val alphaTable = Array.ofDim[Int](16)
      for (k <- 0 until 4) {
        val a0 = buffer(index) & 0xFF
        val a1 = buffer(index + 1) & 0xFF
        index += 2
        alphaTable(4 * k) = 17 * ((a0 & 0xF0) >> 4)
        alphaTable(4 * k + 1) = 17 * (a0 & 0x0F)
        alphaTable(4 * k + 2) = 17 * ((a1 & 0xF0) >> 4)
        alphaTable(4 * k + 3) = 17 * (a1 & 0x0F)
      }

      val c0 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2
      val c1 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2

      for (k <- 0 until 4 if 4 * i + k < height) {
        val byte = buffer(index)
        index += 1
        val indices = Array(byte & 0x03, (byte & 0x0C) >> 2, (byte & 0x30) >> 4, (byte & 0xC0) >> 6)

        for ((t, l) <- indices.zipWithIndex if 4 * j + l < width) {
          pixels(4 * width * i + 4 * j + width * k + l) =
            getDXTColor(c0, c1, alphaTable(4 * k + l), t, order)
        }
      }
    }
    pixels
  }

  private def decodeDXT5(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] = {
    var index = offset
    val w = (width + 3) / 4
    val h = (height + 3) / 4
    val pixels = Array.ofDim[Int](width * height)

    for (i <- 0 until h; j <- 0 until w) {
      val a0 = buffer(index) & 0xFF
      val a1 = buffer(index + 1) & 0xFF
      index += 2

      val b0 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8) | ((buffer(index + 2) & 0xFF) << 16)
      index += 3
      val b1 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8) | ((buffer(index + 2) & 0xFF) << 16)
      index += 3

      val alphaTable = (0 until 16).map { k =>
        val shift = (k % 8) * 3
        val value = if (k < 8) b0 else b1
        (value >> shift) & 0x07
      }.toArray

      val c0 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2
      val c1 = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2

      for (k <- 0 until 4 if 4 * i + k < height) {
        val byte = buffer(index)
        index += 1
        val indices = Array(byte & 0x03, (byte & 0x0C) >> 2, (byte & 0x30) >> 4, (byte & 0xC0) >> 6)

        for ((t, l) <- indices.zipWithIndex if 4 * j + l < width) {
          pixels(4 * width * i + 4 * j + width * k + l) =
            getDXTColor(c0, c1, getDXT5Alpha(a0, a1, alphaTable(4 * k + l)), t, order)
        }
      }
    }
    pixels
  }

  // RGB reading methods - using functional approach with pixel processing
  private def readA1R5G5B5(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels16Bit(width, height, offset, buffer) { rgba =>
      val r = BIT5((rgba & 0x7C00) >> 10)
      val g = BIT5((rgba & 0x03E0) >> 5)
      val b = BIT5(rgba & 0x001F)
      val a = 255 * ((rgba & 0x8000) >> 15)
      combineChannels(r, g, b, a, order)
    }

  private def readX1R5G5B5(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels16Bit(width, height, offset, buffer) { rgba =>
      val r = BIT5((rgba & 0x7C00) >> 10)
      val g = BIT5((rgba & 0x03E0) >> 5)
      val b = BIT5(rgba & 0x001F)
      combineChannels(r, g, b, 255, order)
    }

  private def readA4R4G4B4(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels16Bit(width, height, offset, buffer) { rgba =>
      val r = 17 * ((rgba & 0x0F00) >> 8)
      val g = 17 * ((rgba & 0x00F0) >> 4)
      val b = 17 * (rgba & 0x000F)
      val a = 17 * ((rgba & 0xF000) >> 12)
      combineChannels(r, g, b, a, order)
    }

  private def readX4R4G4B4(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels16Bit(width, height, offset, buffer) { rgba =>
      val r = 17 * ((rgba & 0x0F00) >> 8)
      val g = 17 * ((rgba & 0x00F0) >> 4)
      val b = 17 * (rgba & 0x000F)
      combineChannels(r, g, b, 255, order)
    }

  private def readR5G6B5(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels16Bit(width, height, offset, buffer) { rgba =>
      val r = BIT5((rgba & 0xF800) >> 11)
      val g = BIT6((rgba & 0x07E0) >> 5)
      val b = BIT5(rgba & 0x001F)
      combineChannels(r, g, b, 255, order)
    }

  private def readR8G8B8(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] = {
    val pixels = Array.ofDim[Int](width * height)
    var index = offset
    for (i <- pixels.indices) {
      val b = buffer(index) & 0xFF
      val g = buffer(index + 1) & 0xFF
      val r = buffer(index + 2) & 0xFF
      index += 3
      pixels(i) = combineChannels(r, g, b, 255, order)
    }
    pixels
  }

  private def readA8B8G8R8(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels32Bit(width, height, offset, buffer) { (index, buf) =>
      val r = buf(index) & 0xFF
      val g = buf(index + 1) & 0xFF
      val b = buf(index + 2) & 0xFF
      val a = buf(index + 3) & 0xFF
      combineChannels(r, g, b, a, order)
    }

  private def readX8B8G8R8(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels32Bit(width, height, offset, buffer) { (index, buf) =>
      val r = buf(index) & 0xFF
      val g = buf(index + 1) & 0xFF
      val b = buf(index + 2) & 0xFF
      combineChannels(r, g, b, 255, order)
    }

  private def readA8R8G8B8(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels32Bit(width, height, offset, buffer) { (index, buf) =>
      val b = buf(index) & 0xFF
      val g = buf(index + 1) & 0xFF
      val r = buf(index + 2) & 0xFF
      val a = buf(index + 3) & 0xFF
      combineChannels(r, g, b, a, order)
    }

  private def readX8R8G8B8(width: Int, height: Int, offset: Int, buffer: Array[Byte], order: Order): Array[Int] =
    processPixels32Bit(width, height, offset, buffer) { (index, buf) =>
      val b = buf(index) & 0xFF
      val g = buf(index + 1) & 0xFF
      val r = buf(index + 2) & 0xFF
      combineChannels(r, g, b, 255, order)
    }

  // Helper methods for pixel processing
  private def processPixels16Bit(
                                  width: Int,
                                  height: Int,
                                  offset: Int,
                                  buffer: Array[Byte]
                                )(processor: Int => Int): Array[Int] = {
    val pixels = Array.ofDim[Int](width * height)
    var index = offset
    for (i <- pixels.indices) {
      val rgba = (buffer(index) & 0xFF) | ((buffer(index + 1) & 0xFF) << 8)
      index += 2
      pixels(i) = processor(rgba)
    }
    pixels
  }

  private def processPixels32Bit(
                                  width: Int,
                                  height: Int,
                                  offset: Int,
                                  buffer: Array[Byte]
                                )(processor: (Int, Array[Byte]) => Int): Array[Int] = {
    val pixels = Array.ofDim[Int](width * height)
    var index = offset
    for (i <- pixels.indices) {
      pixels(i) = processor(index, buffer)
      index += 4
    }
    pixels
  }

  private def combineChannels(r: Int, g: Int, b: Int, a: Int, order: Order): Int =
    (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift)

  // DXT helper methods
  private def getDXTColor(c0: Int, c1: Int, a: Int, t: Int, order: Order): Int = t match {
    case 0 => getDXTColor1(c0, a, order)
    case 1 => getDXTColor1(c1, a, order)
    case 2 => if (c0 > c1) getDXTColor2_1(c0, c1, a, order) else getDXTColor1_1(c0, c1, a, order)
    case 3 => if (c0 > c1) getDXTColor2_1(c1, c0, a, order) else 0
    case _ => 0
  }

  private def getDXTColor2_1(c0: Int, c1: Int, a: Int, order: Order): Int = {
    val r = (2 * BIT5((c0 & 0xFC00) >> 11) + BIT5((c1 & 0xFC00) >> 11)) / 3
    val g = (2 * BIT6((c0 & 0x07E0) >> 5) + BIT6((c1 & 0x07E0) >> 5)) / 3
    val b = (2 * BIT5(c0 & 0x001F) + BIT5(c1 & 0x001F)) / 3
    combineChannels(r, g, b, a, order)
  }

  private def getDXTColor1_1(c0: Int, c1: Int, a: Int, order: Order): Int = {
    val r = (BIT5((c0 & 0xFC00) >> 11) + BIT5((c1 & 0xFC00) >> 11)) / 2
    val g = (BIT6((c0 & 0x07E0) >> 5) + BIT6((c1 & 0x07E0) >> 5)) / 2
    val b = (BIT5(c0 & 0x001F) + BIT5(c1 & 0x001F)) / 2
    combineChannels(r, g, b, a, order)
  }

  private def getDXTColor1(c: Int, a: Int, order: Order): Int = {
    val r = BIT5((c & 0xFC00) >> 11)
    val g = BIT6((c & 0x07E0) >> 5)
    val b = BIT5(c & 0x001F)
    combineChannels(r, g, b, a, order)
  }

  private def getDXT5Alpha(a0: Int, a1: Int, t: Int): Int = {
    if (a0 > a1) {
      t match {
        case 0 => a0
        case 1 => a1
        case 2 => (6 * a0 + a1) / 7
        case 3 => (5 * a0 + 2 * a1) / 7
        case 4 => (4 * a0 + 3 * a1) / 7
        case 5 => (3 * a0 + 4 * a1) / 7
        case 6 => (2 * a0 + 5 * a1) / 7
        case 7 => (a0 + 6 * a1) / 7
        case _ => 0
      }
    } else {
      t match {
        case 0 => a0
        case 1 => a1
        case 2 => (4 * a0 + a1) / 5
        case 3 => (3 * a0 + 2 * a1) / 5
        case 4 => (2 * a0 + 3 * a1) / 5
        case 5 => (a0 + 4 * a1) / 5
        case 6 => 0
        case 7 => 255
        case _ => 0
      }
    }
  }

  /**
   * Reads a DDS image from a file path and returns it as a JavaFX Image.
   */
  def readDDSImage(path: String): Try[Image] =
    readDDSImage(new File(path))

  /**
   * Reads a DDS image from a File and returns it as a JavaFX Image.
   */
  def readDDSImage(file: File): Try[Image] = {
    for {
      buffer <- Try(Files.readAllBytes(file.toPath))
      width = getWidth(buffer)
      height = getHeight(buffer)
      pixels <- read(buffer, ARGB, 0) match {
        case Some(pixels) => Success(pixels)
        case None => Failure(new IOException(s"Failed to decode DDS image: $file"))
      }
      image <- Try {
        val writableImage = new WritableImage(width, height)
        val writer = writableImage.getPixelWriter
        writer.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width)
        writableImage: Image
      }
    } yield image
  }
}