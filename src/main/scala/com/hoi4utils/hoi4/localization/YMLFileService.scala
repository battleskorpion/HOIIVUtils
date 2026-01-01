package com.hoi4utils.hoi4.localization

import com.hoi4utils.Providers.{Provider, provide}

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
  given Provider[YMLFileService] = provide(YMLFileService())
}
