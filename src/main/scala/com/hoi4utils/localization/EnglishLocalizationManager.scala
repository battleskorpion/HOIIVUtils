package com.hoi4utils.localization

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.exceptions.LocalizationExistsException
import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.typesafe.scalalogging.LazyLogging

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Scanner
import scala.Option
import scala.jdk.javaapi.CollectionConverters
import scala.util.control.Exception.noCatch.opt

object EnglishLocalizationManager {
  protected val l_english = "l_english:"
  protected val versionNumberRegex = ":(\\d*)" //  (version number is optional)

  protected val language_def: String = l_english
}

class EnglishLocalizationManager extends LocalizationManager with LazyLogging {
  /* */ 
  setManager(this)

  /**
   * Localization collection for all English localization. 
   */
  final protected val localizationCollection = new LocalizationCollection

  //    public static void setLanguage(String language) {
  //        language_def = "l_" + language;
  //    }
  override def reload(): Unit = {
    localizationCollection.clear()
    // load all localization files within the localization folder and
    // any subfolders
    loadLocalization()
  }

  protected def loadLocalization(): Unit = {
    /* Load mod localization after vanilla to give mod localizations priority */
    // vanilla
    if (HOIIVFiles.HOI4.localization_folder == null)
      logger.warn("'HOI4 localization folder' is null.")
    else if (!HOIIVFiles.HOI4.localization_folder.exists)
      logger.warn("'HOI4 localization folder' does not exist.")
    else if (!HOIIVFiles.HOI4.localization_folder.isDirectory)
      logger.warn("'HOI4 localization folder' is not a directory.")
    else
      loadLocalization(HOIIVFiles.HOI4.localization_folder, Localization.Status.VANILLA)

    // mod
    if (HOIIVFiles.Mod.localization_folder == null)
      logger.warn("'Mod localization folder' is null.")
    else if (!HOIIVFiles.Mod.localization_folder.exists)
      logger.warn("'Mod localization folder' does not exist.")
    else if (!HOIIVFiles.Mod.localization_folder.isDirectory)
      logger.warn("'Mod localization folder' is not a directory.")
    else
      loadLocalization(HOIIVFiles.Mod.localization_folder, Localization.Status.EXISTS)
  }


  protected def loadLocalization(localizationFolder: File, status: Localization.Status): Unit = {
    val files = localizationFolder.listFiles
    if (files == null) return
    for (file <- files) {
      if (file.isDirectory) loadLocalization(file, status)
      else if (file.getName.endsWith(".yml")) loadLocalizationFile(file, status)
      else logger.info("Localization files can only be of type .yml. File: {}", file.getAbsolutePath)
      logger.debug("Loaded localization file: {}", file.getAbsolutePath)
    }
  }

  // todo wow :(
  protected def loadLocalizationFile(file: File, Status: Localization.Status): Unit = {
    val scanner = new Scanner(file)
    try {
      // check language
      var languageFound = false
      while (scanner.hasNextLine && !languageFound) {
        var line = scanner.nextLine
        /* ignore BOM */
        if (line.startsWith("\uFEFF")) line = line.substring(1)
        if (line.trim.nonEmpty && line.trim.charAt(0) != '#') {
          if (!line.trim.startsWith(EnglishLocalizationManager.language_def)) {
            System.out.println("Localization file is not in English: " + file.getAbsolutePath)
            return
          }
          else languageFound = true
        }
      }
      if (!languageFound) {
        System.out.println("Localization file does not have a language definition: " + file.getAbsolutePath)
        return
      }
      while (scanner.hasNextLine) {
        val line = scanner.nextLine
        if (line.trim.nonEmpty && line.trim.charAt(0) != '#') {
          val data = line.splitWithDelimiters(EnglishLocalizationManager.versionNumberRegex, 2)
          if (data.length != 3) {
            System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: incorrect number of line elements")
          } else {
            // trim whitespace
            for (i <- data.indices) data(i) = data(i).trim
            // ignore ":" before version number
            data(1) = data(1).substring(1)

            // ignore escaped quotes
            data(2) = data(2).replaceAll("//\"", "\u0000")
            val startQuote = data(2).indexOf("\"")
            val endQuote = data(2).lastIndexOf("\"")
            val extra = data(2).substring(endQuote + 1).trim
            var invalid = false
            if (extra.nonEmpty && !extra.startsWith("#")) {
              System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: extraneous non-comment data after localization entry: " + extra)
              invalid = true
            }
            if (startQuote != 0 || endQuote == -1 || startQuote == endQuote) {
              System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: localization value is not correctly enclosed in quotes")
              invalid = true
            }
            if (!invalid) {
              // remove leading/trailing quotes (and any comments)
              data(2) = data(2).substring(startQuote + 1, endQuote)
              /*
              .yml example format:
              CONTROLS_GREECE: "Controls all states in the §Y$strategic_region_greece$§! strategic region"
              CONTROLS_ASIA_MINOR:1 "Controls all states in the §Y$strategic_region_asia_minor$§! strategic region"
              */
              var key: String = null
              var version: Option[Int] = None 
              var value: String = null
              if (data(1).isBlank) {
                key = data(0).trim
                version = None 
                value = data(2).trim
              }
              else {
                key = data(0).trim
                version = data(1).trim.toIntOption
                value = data(2).trim
              }
              // fix file format issues (as it is a UTF-8 BOM file)
              value = value.replaceAll("(Â§)", "§")
              val localization = new Localization(key, version, value, Status)
              localizationCollection.add(localization, file)
            }
          }
        }
      }
    } catch {
      case exc: IOException =>
        exc.printStackTrace()
    } finally if (scanner != null) scanner.close()
  }

  override def saveLocalization(): Unit = {
    val localizationFolder = HOIIVFiles.Mod.localization_folder
    val files = localizationFolder.listFiles
    if (files == null) return
    // Separate new and changed localizations
    val changedLocalizations = localizationCollection.filterByStatus(Localization.Status.UPDATED)
    val newLocalizations = localizationCollection.filterByStatus(Localization.Status.NEW)
    // Save updated and new localizations
//    val sortAlphabetically = false
//    changedLocalizations.forEach(entry => updateLocalizationFile(entry._1, entry._2.toList, sortAlphabetically))
//    newLocalizations.forEach(entry => updateLocalizationFile(entry._1, entry._2.toList, sortAlphabetically))
// Merge the maps: for every file, combine the two lists (or use one if the file exists only in one map)
    val mergedLocalizations: Map[File, List[Localization]] =
      (changedLocalizations ++ newLocalizations)
        .groupBy(_._1)
        .view
        .mapValues(_.flatMap(_._2).toList)
        .toMap

    val sortAlphabetically = false
    mergedLocalizations.foreach { case (file, locList) =>
      updateLocalizationFile(file, locList, sortAlphabetically)
    }
  }

//  def writeAllLocalization(list: List[Localization], file: File): Unit = {
//    // Sort the list by the base key (i.e. key without the '_desc' suffix).
//    // For entries with the same base key, ensure the non-'_desc' version comes first.
//    val sortedList = list.sortWith { (loc1, loc2) =>
//      val base1 = if (loc1.ID.endsWith("_desc")) loc1.ID.dropRight(5) else loc1.ID
//      val base2 = if (loc2.ID.endsWith("_desc")) loc2.ID.dropRight(5) else loc2.ID
//      if (base1 == base2) !loc1.ID.endsWith("_desc")
//      else base1 < base2
//    }
//
//    sortedList.foreach { localization =>
//      localization.status match {
//        case Localization.Status.UPDATED => writeLocalization(file, localization)
//        case Localization.Status.NEW => writeLocalization(file, localization)
//        case _ =>
//          throw new IllegalStateException("Unexpected value: " + localization.status)
//      }
//    }
//  }

  /**
   * Updates a localization file by merging new localizations with existing ones.
   *
   * @param file               The localization file to update.
   * @param localizationList   The list of new or updated localizations.
   * @param sortAlphabetically If true, groups are sorted alphabetically; if false, new entries are appended.
   */
  def updateLocalizationFile(file: File, localizationList: List[Localization], sortAlphabetically: Boolean): Unit = {
    // Read the entire file as lines.
    val fileLines = Files.readAllLines(file.toPath).toArray.mkString("\n")

    // Assume the first non-empty line is a header (for example, "l_english:")
    val lines = fileLines.split("\n").toList
    val header = lines.headOption.getOrElse("")

    val existingLocalization: List[Localization] =
      lines.flatMap(LocalizationParser.parseLine)
    logger.debug(s"File: ${file.getName}, Existing localizations: ${existingLocalization.size}")

    /* --- Group by base key --- */
    val existingGroups: Map[String, LocalizationGroup] =
      existingLocalization.groupBy(_.baseKey).map { case (base, locs) =>
        val baseEntry = locs.find(!_.ID.endsWith("_desc"))
        val descEntry = locs.find(_.ID.endsWith("_desc"))
        base -> LocalizationGroup(baseEntry, descEntry)
      }

    val newGroups: Map[String, LocalizationGroup] =
      localizationList.groupBy(_.baseKey).map { case (base, locs) =>
        val baseEntry = locs.find(!_.ID.endsWith("_desc"))
        val descEntry = locs.find(_.ID.endsWith("_desc"))
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
    val groupList: List[(String, LocalizationGroup)] = {
      if (sortAlphabetically) {
        mergedGroups.toList.sortBy(_._1)
      } else {
        // Determine the order from the existing file: use the first occurrence of each base key.
        val existingOrder: List[String] = existingLocalization.map(_.baseKey).distinct
        val inFileGroups = existingOrder.flatMap { key =>
          mergedGroups.get(key).map(group => key -> group)
        }
        // Append any new groups
        val newKeys = mergedGroups.keys.toList.filterNot(existingOrder.toSet)
        inFileGroups ++ newKeys.map(key => key -> mergedGroups(key))
      }
    }

    // --- Build new file content ---
    val newContent = new StringBuilder
    newContent.append(header).append("\n")

    // For each group, print the base loc and then the description loc (if any).
    for ((_, group) <- groupList) {
      group.base.foreach(loc => newContent.append("\t").append(formatLocalization(loc)).append("\n"))
      group.desc.foreach(loc => newContent.append("\t").append(formatLocalization(loc)).append("\n"))
      newContent.append("\n")   // empty line between groups
    }

    // Write the updated content back to the file.
    Files.write(file.toPath, newContent.toString.getBytes(StandardCharsets.UTF_8))
//    Files.write(Paths.get(file.getAbsolutePath), lines)
  }


  /**
   * Use to replace existing localization with entry
   *
   * @param file
   * @param key
   * @param version
   * @param value
   * @param append
   */
  protected def writeLocalization(file: File, localization: Localization): Unit = {
    /* localization */
    val key = localization.ID
    val version = {
      if (localization.version == null) ""
      else String.valueOf(localization.version)
    }
    val value = localization.text

    var entry = "\t" + key + ":" + version + " \"" + value + "\""
    entry = entry.replaceAll("§", "Â§")

    val writer: PrintWriter = getLocalizationWriter(file, true)

    /* append is a quick add to end no more logic needed */
    val append = localization.status match {
      case Localization.Status.NEW => true
      case Localization.Status.UPDATED => false
      case _ => throw new IllegalStateException("Unexpected value: " + localization.status)
    }
    if (append) try {
      writer.println(entry)
    } catch {
      case exc: IOException =>
        System.err.println("Failed to write new localization to file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
    } finally {
      if (writer != null) writer.close()
    } else try {
      var lineReplaced = false
      val lines = Files.readAllLines(Paths.get(file.getAbsolutePath))
      for (i <- 0 until lines.size) {
        var continue = true
//        if (lines.get(i).trim.startsWith(key) && continue) {

//        }
        if (lines.get(i).filter(c => !c.isWhitespace).startsWith(key + ":") && continue) {
          lines.set(i, entry)
          lineReplaced = true
          System.out.println("Replaced localization " + key)
          continue = false
        }
      }
      if (!lineReplaced) throw new IOException // todo better exception
      Files.write(Paths.get(file.getAbsolutePath), lines)
    } catch {
      case exc: IOException =>
        System.err.println("Failed to update localization in file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
    }
  }

  /** Formats a Localization into a file line. */
  def formatLocalization(loc: Localization): String = {
    val versionStr = loc.version match {
      case null => ""
      case v => v.toString
    }
    // Build the entry string
    val entry = s"${loc.ID}:$versionStr \"${loc.text}\""
    entry.replaceAll("§", "Â§")   // necessary with UTF-8 BOM
  }

  @throws[IOException]
  protected def getLocalizationWriter(file: File, append: Boolean) = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))

  /**
   * {@inheritDoc }
   */
  @throws[LocalizationExistsException]
  override def addLocalization(localization: Localization, file: File): Unit = {
    addLocalization(localization, localizationCollection, file)
  }

  /**
   * Finds the localization corresponding to the given key/localization ID.
   *
   * @return the localization corresponding to the given ID, or null if not found.
   */
  @throws[IllegalArgumentException]
  override def getLocalization(key: String): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("localization ID cannot be null")
    localizationCollection.get(key)
  }
  
  override def localizations: LocalizationCollection = localizationCollection

  /**
   * Capitalizes every word in a string with a pre-set whitelist
   *
   * @param title
   * @return Returns the edited string unless the string has no words
   */
  override def titleCapitalize(title: String): String = {
    if (title == null) return null
    if (title.trim.isEmpty) return title

    val words = title.split(" ").toBuffer
    val whitelist = capitalizationWhitelist
    
    if (words.head.length == 1) words(0) = Character.toUpperCase(words.head.charAt(0)) + ""
    else if (words.head.length > 1) words(0) = Character.toUpperCase(words.head.charAt(0)) + words.head.substring(1)
    else {
      // todo this should never happen now right?
      System.out.println("first word length < 1")
    }
    
    System.out.println("num words: " + words.size)
    for (i <- 1 until words.size) {
      if (!LocalizationManager.isAcronym(words(i)) && !whitelist.contains(words(i))) {
        if (words(i).length == 1) {
          words(i) = Character.toUpperCase(words(i).charAt(0)) + ""
        }
        else if (words(i).length > 1) {
          // System.out.println("working cap");
          words(i) = Character.toUpperCase(words(i).charAt(0)) + words(i).substring(1)
        }
      }
    }
    
    System.out.println("capitalized: " + String.join(" ", CollectionConverters.asJava(words)))
    String.join(" ", CollectionConverters.asJava(words))
  }

  // todo let user change?
  override def capitalizationWhitelist: Set[String] = {
    Set("a", "above", "after", "among", // among us
      "an", "and", "around", "as", "at", "below", "beneath", "beside", "between", "but", "by", "for", "from", "if", "in", "into", "nor", "of", "off", "on", "onto", "or", "over", "since", "the", "through", "throughout", "to", "under", "underneath", "until", "up", "with")
  }

  override def toString: String = "EnglishLocalizationManager{" + "localizations=" + localizationCollection + "}"
}

object LocalizationParser:

  def parseLine(line: String): Option[Localization] =
    val colonIndex = line.indexOf(':')
    if colonIndex < 0 then
      None
    else
      val key = line.substring(0, colonIndex).trim
      val afterColon = line.substring(colonIndex + 1).trim

      // Split out the number (if any)
      val (numPart, restAfterNumber) = afterColon.span(_.isDigit)
      val ver = if numPart.isEmpty then null else numPart.toIntOption
      val afterNumber = restAfterNumber.trim

      // Check that we start with a quote
      if !afterNumber.startsWith("\"") then
        None
      else
        try
          val (quotedText, indexAfterQuote) = parseQuoted(afterNumber, 0)
          val trailing = afterNumber.substring(indexAfterQuote).trim
          Some(Localization(key, ver, quotedText, Localization.Status.EXISTS))
        catch
          case _: Exception => None

  /** Parses a quoted string starting at startIndex (which should point to a double quote).
   * It handles inner escaped quotes represented by a double double-quote.
   * Returns a tuple with the parsed string and the index immediately after the closing quote.
   */
  private def parseQuoted(s: String, startIndex: Int): (String, Int) =
    // We expect s(startIndex) to be the starting quote.
    val sb = new StringBuilder
    var i = startIndex + 1 // Skip the initial quote
    while i < s.length do
      if s(i) == '"' then
        // If the next char is also a quote, it is an escaped quote.
        if i + 1 < s.length && s(i + 1) == '"' then
          sb.append('"')
          i += 2
        else
          // End of quoted text.
          return (sb.toString, i + 1)
      else
        sb.append(s(i))
        i += 1
    throw new Exception("No closing quote found in input")

