package com.hoi4utils.hoi4.localization


import zio.ZLayer

import java.io.File
import java.nio.file.Files

/**
 * Service for reading and writing to .yml (YAML) files.
 */
class YMLFileService {
  
  def read(file: File): String = Files.readAllLines(file.toPath).toArray.mkString("\n")
  
  def readLines(file: File): Seq[String] = read(file).split("\n").toSeq
}

object YMLFileService {
  val live: ZLayer[Any, Nothing, YMLFileService] = ZLayer.succeed(new YMLFileService)
}
