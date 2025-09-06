package com.hoi4utils.localization

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.exceptions.{LocalizationExistsException, NoLocalizationManagerException, UnexpectedLocalizationStatusException}
import com.typesafe.scalalogging.LazyLogging
import com.hoi4utils.RichString

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Scanner
import scala.jdk.javaapi.CollectionConverters


object LocalizationManager {
  private var primaryManager: Option[LocalizationManager] = None

  def getLocalizationFile(key: String): File = get.localizations.getLocalizationFile(key)

  @throws[NoLocalizationManagerException]
  def get: LocalizationManager = primaryManager match
      case Some(mgr) => mgr
      case None => throw new NoLocalizationManagerException
  
  def getOrCreate(createManager: () => LocalizationManager): LocalizationManager = primaryManager match
    case Some(mgr) => mgr
    case None => createManager()

  def get(key: String): Option[Localization] = get.getLocalization(key)

  def getAll(localizationKeys: Iterable[String]): Iterable[Localization] = get.localizations.getAll(localizationKeys)

  @throws[IllegalArgumentException]
  def find(key: String): Option[Localization] = get.getLocalization(key)

  private[localization] def numCapitalLetters(word: String): Int =
    if (word == null || word.isBlank) return 0
    word count (_.isUpper)

  def isAcronym(word: String): Boolean = numCapitalLetters(word) == word.trim.length
}

/**
 * Abstract class that manages localizations for a system.
 * This class provides functionality to set, get, replace, and manage localizations by key.
 * It also allows reloading and saving localizations.
 */
abstract class LocalizationManager extends LazyLogging {

  // Group a base localization and its optional description together.
  case class LocalizationGroup(base: Option[Localization], desc: Option[Localization])
  
  /**
   * Sets the primary manager for localization management.
   *
   * @param manager the LocalizationManager to set as the primary manager
   */
  final def setManager(manager: LocalizationManager): Unit = LocalizationManager.primaryManager = Some(manager)

  /**
   * Reloads the localizations. The specific behavior of this method
   * depends on the implementation.
   */
  def reload(): Unit = {
    loadLocalization()
  }

  /**
   * Retrieves the localization for the given key.
   *
   * @param key the key for the localization to retrieve
   * @return an Option containing the Localization for the given key, 
   *         or None if the localization does not exist
   * @throws IllegalArgumentException if the key is null
   */
  @throws[IllegalArgumentException]
  def getLocalization(key: String): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("localization ID cannot be null")
    localizations.get(key)
  }

  /**
   * Sets the localization for the given key.
   *
   * @param key          the key of the localization
   * @param localization the localization to set
   * @return the previous localization for the given key, or null if there was no previous localization.
   * @throws IllegalArgumentException              if the key or localization is null.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def setLocalization(key: String, localization: Localization): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    if (localization == null) throw new IllegalArgumentException("Localization cannot be null.")
    localizations.get(key) match {
      case Some(prevLocalization) =>
        if (prevLocalization.isReplaceableBy(localization)) return localizations.replace(key, localization)
        else throw new UnexpectedLocalizationStatusException(prevLocalization, localization)
      case None =>
        if (localization.isNew) {
          // todo
        }
        else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")
    }
    null
  }

  /**
   * Sets the localization for the given key, with the given text.
   *
   * @param key  the key of the localization
   * @param text the text of the localization
   * @return the previous localization for the given key, or null if there was no previous localization.
   * @throws IllegalArgumentException              if the key or localization is null.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def setLocalization(key: String, text: String, file: File): Option[Localization] = {
    setLocalization(key, None, text, file)
  }

  /**
   * Sets the localization for the given key, with the given text.
   *
   * @param key  the key of the localization
   * @param text the text of the localization
   * @return the previous localization for the given key, or null if there was no previous localization.
   * @throws IllegalArgumentException              if the key or localization is null.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def setLocalization(key: String, version: Option[Int], text: String, file: File): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    localizations.get(key) match {
      case Some(prevLocalization) =>
        val localization = prevLocalization.replaceWith(text, version, file)
        localizations.replace(key, localization)
      case None =>
        val localization = new Localization(key, version, text, Localization.Status.NEW)
        localizations.add(localization, file)
        Some(localization)
    }
  }

  /**
   * Replaces the localization for the given key with the given text.
   *
   * @param key the key of the localization to replace
   * @throws IllegalArgumentException              if the key is null or localization with the given key does not exist.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable.
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def replaceLocalization(key: String, text: String): Option[Localization] = {
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    localizations.get(key) match {
      case Some(prevLocalization) =>
        val localization = prevLocalization.replaceWith(text)
        // there are maps that support mapping to null, which is why the null check is necessary.
        // (read the docs for the replace method)
        localizations.replace(key, localization)
      case None => throw new IllegalArgumentException("Localization with the given key does not exist.")
    }
  }

  def saveLocalization(): Unit = {
    val localizationFolder = HOIIVFiles.Mod.localization_folder
    val files = localizationFolder.listFiles
    if (files == null) return
    // Separate new and changed localizations
    val changedLocalizations = localizations.filterByStatus(Localization.Status.UPDATED)
    val newLocalizations = localizations.filterByStatus(Localization.Status.NEW)
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

  /**
   * Adds a new localization to the localization list if it does not already exist.
   *
   * @param localization the localization to add
   */
  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, localizationCollection: LocalizationCollection, file: File): Unit = {
    if (localization == null) return
    if (localizationCollection.containsLocKey(localization.id)) throw new LocalizationExistsException(localization)
    localizationCollection.add(localization, file)
  }

  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, file: File): Unit = {
    addLocalization(localization, localizations, file)
  }

  /**
   * Checks if the given localization ID is localized (has a localization entry).
   *
   * @param localizationId the ID of the localization to check
   * @return true if the localization is localized, false otherwise
   */
  def isLocalized(localizationId: String): Boolean = getLocalization(localizationId).nonEmpty

  /**
   * @return map of localizations and their keys.
   */
  protected def localizations: LocalizationCollection

  def localizationList: Iterable[Localization] = localizations.getAll

  /**
   * Capitalizes every word in a string with a pre-set whitelist
   *
   * @param title the string to capitalize
   * @return Returns the edited string unless the string has no words
   */
  def titleCapitalize(title: String): String = {
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
  private[localization] def capitalizationWhitelist: Set[String]

  /** Formats a Localization into a file line. */
  def formatLocalization(loc: Localization): String = {
    // Build the entry string
    val entry = s"${loc.id}:${loc.versionStr} \"${loc.text}\""
    entry.replaceAll("§", "Â§") // necessary with UTF-8 BOM
  }

  /**
   * Loads localization. Loads mod localization after vanilla to give mod localizations priority
   */
  protected def loadLocalization(): Unit = {
    localizations.clear()

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
          if (!line.trim.startsWith(language_def)) {
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
          val data = line.splitWithDelimiters(versionNumberRegex, 2)
          if (data.length != 3) {
            System.err.println("Invalid localization file format: " + file.getAbsolutePath + "\n\tline: " + line + "\n\tReason: incorrect number of line elements")
          } else {
            // trim whitespace
            data mapInPlace (s => s.trim)
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
              if (data(1).isBlank)
                key = data(0).trim
                version = None
                value = data(2).trim
              else
                key = data(0).trim
                version = data(1).trim.toIntOption
                value = data(2).trim
              // fix file format issues (as it is a UTF-8 BOM file)
              value = value.replaceAll("(Â§)", "§")
              val localization = new Localization(key, version, value, Status)
              localizations.add(localization, file)
            }
          }
        }
      }
    } catch {
      case exc: IOException =>
        exc.printStackTrace()
    } finally if (scanner != null) scanner.close()
  }
  
  def language_def: String 
  
  def versionNumberRegex = ":(\\d*)" //  (version number is optional)

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
      newContent.append("\n") // empty line between groups
    }

    // Write the updated content back to the file.
    Files.write(file.toPath, newContent.toString.getBytes(StandardCharsets.UTF_8))
    //    Files.write(Paths.get(file.getAbsolutePath), lines)
  }

  /**
   *
   * @param file
   * @param localization
   */
  protected def writeLocalization(file: File, localization: Localization): Unit = {
    /* localization */
    val entry = localization.toString.replaceAll("§", "Â§")
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
        if (lines.get(i).filter(c => !c.isWhitespace).startsWith(localization.key + ":") && continue) {
          lines.set(i, entry)
          lineReplaced = true
          System.out.println("Replaced localization " + localization.key)
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

  @throws[IOException]
  protected def getLocalizationWriter(file: File, append: Boolean) = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))

  /**
   * Gets all localization files managed by this manager.
   * @return all localization files.
   */
  def localizationFiles: Iterable[File] = localizations.getLocalizationFiles

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
    