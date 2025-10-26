package com.hoi4utils.hoi4.map.province

import java.io.File
import scala.io.Source
import scala.util.Try

object DefinitionCSV {
  /**
   * Represents the definition for a province loaded from the CSV.
   *
   * @param id        the province id
   * @param red       red component (0-255)
   * @param green     green component (0-255)
   * @param blue      blue component (0-255)
   * @param terrain   terrain type, e.g. "sea", "land", "lake"
   * @param isCoastal a flag (true/false) from the CSV
   * @param subtype   a more specific type, e.g. "ocean", "forest", "plains"
   * @param score     an integer value from the CSV (could represent weight or another parameter)
   */
  case class ProvinceDefinition(
                                 id: Int,
                                 red: Int,
                                 green: Int,
                                 blue: Int,
                                 terrain: String,
                                 isCoastal: Boolean,
                                 subtype: String,
                                 score: Int
                               )

  /**
   * Loads province definitions from the given CSV file.
   *
   * The CSV is expected to have lines formatted like:
   *
   *     0;0;0;0;sea;false;unknown;10
   *     1;57;35;244;sea;false;ocean;3
   *     ...
   *
   * Fields are separated by semicolons.
   *
   * @param file the CSV file to load.
   * @return a Map of province id to ProvinceDefinition.
   */
  def load(file: File): Map[Int, ProvinceDefinition] = {
    val source = Source.fromFile(file)
    try {
      source.getLines().flatMap { line =>
        val trimmed = line.trim
        // Skip empty lines or comments (if any)
        if (trimmed.isEmpty || trimmed.startsWith("#")) None
        else {
          val parts = trimmed.split(";").map(_.trim)
          if (parts.length >= 8) {
            Try {
              val id = parts(0).toInt
              val red = parts(1).toInt
              val green = parts(2).toInt
              val blue = parts(3).toInt
              val terrain = parts(4)
              val isCoastal = parts(5).toBoolean
              val subtype = parts(6)
              val score = parts(7).toInt
              ProvinceDefinition(id, red, green, blue, terrain, isCoastal, subtype, score)
            }.toOption
          } else None
        }
      }.toList.map(pd => pd.id -> pd).toMap
    } finally {
      source.close()
    }
  }
}
