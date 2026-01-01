package com.hoi4utils.hoi4.localization

import com.hoi4utils.Providers.*
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class LocalizationFileService extends LazyLogging {
  /**
   * Updates a localization file by merging new localizations with existing ones.
   *
   * @param file               The localization file to update.
   * @param localizationList   The list of new or updated localizations.
   * @param sortAlphabetically If true, groups are sorted alphabetically; if false, new entries are appended.
   */
  def updateLocalizationFile(file: File, localizationList: Seq[Localization], sortAlphabetically: Boolean): Unit = {
    // Read the entire file as lines.
    val ymlFileService = provided[YMLFileService]

    // Assume the first non-empty line is a header (for example, "l_english:")
    val lines = ymlFileService.readLines(file)
    val header = lines.headOption.getOrElse("")

    val existingLocalization: Seq[Localization] =
      lines.flatMap(LocalizationParser.parseLine)
    logger.debug(s"File: ${file.getName}, Existing localizations: ${existingLocalization.size}")

    /* --- Group by base key --- */
    val existingGroups: Map[String, LocalizationGroup] =
      existingLocalization.groupBy(_.baseKey).map { case (base, locs) =>
        val baseEntry = locs.find(!_.id.endsWith("_desc"))
        val descEntry = locs.find(_.id.endsWith("_desc"))
        base -> LocalizationGroup(baseEntry, descEntry)
      }

    val newGroups: Map[String, LocalizationGroup] =
      localizationList.groupBy(_.baseKey).map { case (base, locs) =>
        val baseEntry = locs.find(!_.id.endsWith("_desc"))
        val descEntry = locs.find(_.id.endsWith("_desc"))
        base -> LocalizationGroup(baseEntry, descEntry)
      }

    /* --- Merge groups --- */
    // For each base key, let new entries override existing ones when available.
    val mergedGroups: Map[String, LocalizationGroup] = (existingGroups.keySet ++ newGroups.keySet).map { key =>
      val existing = existingGroups.getOrElse(key, LocalizationGroup(None, None))
      val added = newGroups.getOrElse(key, LocalizationGroup(None, None))

      // New update takes precedence; otherwise, use the existing file value.
      val mergedGroup = LocalizationGroup(
        base = added.base.orElse(existing.base),
        desc = added.desc.orElse(existing.desc)
      )
      key -> mergedGroup
    }.toMap

    /* --- Ordering --- */
    val groupList: Seq[(String, LocalizationGroup)] = {
      if (sortAlphabetically)
        mergedGroups.toSeq.sortBy(_._1)
      else
        // Determine the order from the existing file: use the first occurrence of each base key.
        val existingOrder: Seq[String] = existingLocalization.map(_.baseKey).distinct
        val inFileGroups = existingOrder.flatMap { key =>
          mergedGroups.get(key).map(group => key -> group)
        }
        // Append any new groups
        val newKeys = mergedGroups.keys.toSeq.filterNot(existingOrder.toSet)
        inFileGroups ++ newKeys.map(key => key -> mergedGroups(key))
    }

    // --- Build new file content ---
    val newContent = new StringBuilder
    newContent.append(header).append("\n")

    val formatter: LocalizationFormatter = provided[LocalizationFormatter]
    // For each group, print the base loc and then the description loc (if any).
    for ((_, group) <- groupList)
      val func = (loc: Localization) => newContent.append("\t").append(formatter.formatLocalization(loc)).append("\n")
      group.base.foreach(func)
      group.desc.foreach(func)
      newContent.append("\n") // empty line between groups

    // Write the updated content back to the file.
    Files.write(file.toPath, newContent.toString.getBytes(StandardCharsets.UTF_8))
    //    Files.write(Paths.get(file.getAbsolutePath), lines)
  }
}

object LocalizationFileService {
  given Provider[LocalizationFileService] = provide(LocalizationFileService())
}
