package com.hoi4utils

import java.io.File
import scala.compiletime.uninitialized

/**
 * Converted to Scala 3.7.1 while maintaining exact same usage pattern
 */
object HOIIVFiles {

  // Nested object for mod files
  object Mod {
    var folder: File = uninitialized
    var focus_folder: File = uninitialized
    var ideas_folder: File = uninitialized
    var states_folder: File = uninitialized
    var strat_region_dir: File = uninitialized
    var localization_folder: File = uninitialized
    var common_folder: File = uninitialized
    var units_folder: File = uninitialized
    var interface_folder: File = uninitialized
    var resources_file: File = uninitialized
    var state_category_dir: File = uninitialized
    var country_folder: File = uninitialized
    var country_tags_folder: File = uninitialized
    var province_map_file: File = uninitialized
    var definition_csv_file: File = uninitialized
  }

  // Nested object for base (HOIIV) files
  object HOI4 {
    var folder: File = uninitialized
    var localization_folder: File = uninitialized
    var units_folder: File = uninitialized
    var interface_folder: File = uninitialized
    var resources_file: File = uninitialized
    var state_category_dir: File = uninitialized
    var country_folder: File = uninitialized
    var country_tags_folder: File = uninitialized
    var province_map_file: File = uninitialized
    var definition_csv_file: File = uninitialized
  }

  // Example of a file that might be shared or used as a default mod folder
  val usersParadoxHOIIVModFolder: File =
    File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod")

  def setModPathChildDirs(modPath: String): Unit = {
    Mod.folder = File(modPath)
    Mod.common_folder = File(modPath, "common")
    Mod.focus_folder = File(modPath, "common" + File.separator + "national_focus")
    Mod.ideas_folder = File(modPath, "common" + File.separator + "ideas")
    Mod.units_folder = File(modPath, "common" + File.separator + "units")
    Mod.states_folder = File(modPath, "history" + File.separator + "states")
    Mod.localization_folder = File(modPath, "localisation" + File.separator + "english") // 's' vs 'z' note in the original comment
    Mod.strat_region_dir = File(modPath, "map" + File.separator + "strategicregions")
    Mod.interface_folder = File(modPath, "interface")
    Mod.resources_file = File(modPath, "common" + File.separator + "resources" + File.separator + "00_resources.txt")
    Mod.state_category_dir = File(modPath, "common" + File.separator + "state_category")
    Mod.country_folder = File(modPath, "history" + File.separator + "countries")
    Mod.country_tags_folder = File(modPath, "common" + File.separator + "country_tags")
    Mod.province_map_file = File(modPath, "map" + File.separator + "provinces.bmp")
    Mod.definition_csv_file = File(modPath, "map" + File.separator + "definition.csv")
  }

  def setHoi4PathChildDirs(hoi4Path: String): Unit = {
    HOI4.folder = File(hoi4Path)
    HOI4.localization_folder = File(hoi4Path, "localisation" + File.separator + "english")
    HOI4.units_folder = File(hoi4Path, "common" + File.separator + "units")
    HOI4.interface_folder = File(hoi4Path, "interface")
    HOI4.resources_file = File(hoi4Path, "common" + File.separator + "resources" + File.separator + "00_resources.txt")
    HOI4.state_category_dir = File(hoi4Path, "common" + File.separator + "state_category")
    HOI4.country_folder = File(hoi4Path, "history" + File.separator + "countries")
    HOI4.country_tags_folder = File(hoi4Path, "common" + File.separator + "country_tags")
    HOI4.province_map_file = File(hoi4Path, "map" + File.separator + "provinces.bmp")
    HOI4.definition_csv_file = File(hoi4Path, "map" + File.separator + "definition.csv")
  }

  // A sample validation method that uses both mod and base files
  def isUnitsFolderValid: Boolean = {
    isValidDirectory(Mod.units_folder) && isValidDirectory(HOI4.units_folder)
  }

  /** Checks if a directory is valid */
  private def isValidDirectory(folder: File): Boolean = {
    folder != null && folder.exists() && folder.isDirectory()
  }
}