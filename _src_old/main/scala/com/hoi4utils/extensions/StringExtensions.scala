package com.hoi4utils.extensions

import com.hoi4utils.ddsreader.DDSReader
import javafx.scene.image.Image

extension (path: String)
  def getDDSImage: Option[Image] =
    if path.isEmpty then None
    else
      try
        Some(DDSReader.readDDSImage(path).get)
      catch
        case _: IllegalArgumentException => None // If the string is not a valid image path or format, return None