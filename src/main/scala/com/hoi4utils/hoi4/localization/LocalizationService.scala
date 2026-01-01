package com.hoi4utils.hoi4.localization

import com.hoi4utils.Providers.Provider
import com.hoi4utils.exceptions.{LocalizationExistsException, NoLocalizationManagerException, UnexpectedLocalizationStatusException}
import com.hoi4utils.hoi4.localization.LocalizationService.localizationErrors
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.{ExpectedCause, ParsingContext, ParsingError}
import com.hoi4utils.Providers.*
import com.typesafe.scalalogging.LazyLogging

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Scanner
import scala.collection.mutable.ListBuffer
import scala.jdk.StreamConverters.StreamHasToScala
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Using}


object LocalizationService extends LazyLogging {
  private var primaryManager: Option[LocalizationService] = None
  private var managers: Map[Class[? <: LocalizationService], LocalizationService] = Map.empty
  val localizationErrors: ListBuffer[ParsingError] = ListBuffer.empty[ParsingError]

  given Provider[LocalizationService] = provide(LocalizationService.get)

  def getLocalizationFile(key: String): File = get.localizations.getLocalizationFile(key)

  @throws[NoLocalizationManagerException]
  def get: LocalizationService = primaryManager match
    case Some(mgr) => mgr
    case None => throw new NoLocalizationManagerException

  def getOrCreate[T <: LocalizationService](create: () => T)(using ct: ClassTag[T]): T =
    val clazz = ct.runtimeClass.asInstanceOf[Class[T]]
    managers.get(clazz).map(_.asInstanceOf[T]).getOrElse {
      val mgr = create()
      add(mgr)
      mgr
    }

  private def add[T <: LocalizationService](mgr: T)(using ct: ClassTag[T]): Unit =
    val clazz = ct.runtimeClass.asInstanceOf[Class[T]]
    managers = managers + (clazz -> mgr)

  def get(key: String): Option[Localization] = get.getLocalization(key)

  def getAll(localizationKeys: Iterable[String]): Iterable[Localization] = get.localizations.getAll(localizationKeys)

  @throws[IllegalArgumentException]
  def find(key: String): Option[Localization] = get.getLocalization(key)

  /**
   * Sets the primary manager for localization management.
   *
   * @param manager the LocalizationManager to set as the primary manager
   */
  private def selectPrimaryManager[T <: LocalizationService: ClassTag](): Unit =
    val clazz = summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    managers.get(clazz) match
      case Some(mgr) =>
        primaryManager = Some(mgr)
      case None =>
        throw new IllegalStateException(s"Localization manager of type ${clazz.getName} not found.")

  private def setManager(): Unit = HOIIVUtils.get("localization.primaryLanguage") match
    case "english" => selectPrimaryManager[EnglishLocalizationService]()
    case "russian" => selectPrimaryManager[RussianLocalizationService]()
    case "spanish" => selectPrimaryManager[SpanishLocalizationService]()
    case other =>
      logger.error("Unknown primary localization manager setting: " + other + ". Defaulting to English localization manager.")
      selectPrimaryManager[EnglishLocalizationService]()

  def reload(): Unit =
    requiredLocalizationManagers foreach (_.reload())
    setManager()
    logger.info("Localization managers reloaded. Primary manager: " + primaryManager.get.getClass.getSimpleName)

  def requiredLocalizationManagers: List[LocalizationService] =
    List(
      getOrCreate(() => new EnglishLocalizationService),
      getOrCreate(() => new RussianLocalizationService),
      getOrCreate(() => new SpanishLocalizationService)
    )

}

/**
 * Abstract class that manages localizations for a system.
 * This class provides functionality to set, get, replace, and manage localizations by key.
 * It also allows reloading and saving localizations.
 */
abstract class LocalizationService extends LazyLogging {

//  given Provider[LocalizationService] = provide(LocalizationService.get)
//  given Provider[LocalizationFormatter] = provide(LocalizationFormatter())

  /**
   * @return map of localizations and their keys.
   */
  protected def localizations: LocalizationCollection
  // todo let user change?
  private[localization] def capitalizationWhitelist: Set[String]

  // Group a base localization and its optional description together.
  case class LocalizationGroup(base: Option[Localization], desc: Option[Localization])

  /**
   * Reloads the localizations. The specific behavior of this method
   * depends on the implementation.
   */
  def reload(): Unit = loadLocalization()

  /**
   * Retrieves the localization for the given key.
   *
   * @param key the key for the localization to retrieve
   * @return an Option containing the Localization for the given key,
   *         or None if the localization does not exist
   */
  @throws[IllegalArgumentException]
  def getLocalization(key: String): Option[Localization] = localizations.get(key)

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
  def setLocalization(key: String, localization: Localization): Option[Localization] =
    if localizations.containsKey(key) then replacePrevious(key, localization)
    else
      if (localization.isNew) {
//        localizations.add(localization, file)
//        Some(localization)
      }
      else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")
    None

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
  def setLocalization(key: String, version: Option[Int] = None, text: String, file: File): Option[Localization] =
    localizations.get(key) match
      case Some(prevLocalization) =>
        val localization = prevLocalization.copyForReplace(text, version, file)
        localizations.replace(key, localization)
      case None =>
        val localization = new Localization(key, version, text, Localization.Status.NEW)
        localizations.add(localization, file)
        Some(localization)

  /**
   * Replaces the localization for the given key with the given text.
   *
   * @param key the key of the localization to replace
   * @throws IllegalArgumentException              if the key is null or localization with the given key does not exist.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable.
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def replaceLocalization(key: String, text: String): Option[Localization] =
    require(key != null, "Key cannot be null.")
    localizations.get(key) match
      case Some(prevLocalization) =>
        val localization = prevLocalization.copyForReplace(text)
        // there are maps that support mapping to null, which is why the null check is necessary.
        // (read the docs for the replace method)
        localizations.replace(key, localization)
      case None => throw new IllegalArgumentException("Localization with the given key does not exist.")

  private def replacePrevious(key: String, localization: Localization): Option[Localization] =
    if localizations.containsKey(key) then replace(key, localization)
    else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")

  private def replace(key: String, localization: Localization) =
    localizations.get(key) match
      case Some(prevLocalization) =>
        if prevLocalization.isReplaceableBy(localization) then
          localizations.replace(key, localization)
        else throw new UnexpectedLocalizationStatusException(prevLocalization, localization)
      case None => throw new IllegalArgumentException("There is no existing mod localization to replace for the given key.")

  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, file: File): Unit =
    addLocalization(localization, localizations, file)

  /**
   * Adds a new localization to the localization list if it does not already exist.
   *
   * @param localization the localization to add
   */
  @throws[LocalizationExistsException]
  protected def addLocalization(localization: Localization, localizationCollection: LocalizationCollection, file: File): Unit =
    require(localization != null, "Localization cannot be null.")
    if (localizationCollection.containsKey(localization.id)) throw new LocalizationExistsException(localization)
    localizationCollection.add(localization, file)

  /**
   * Checks if the given localization ID is localized (has a localization entry).
   *
   * @param localizationId the ID of the localization to check
   * @return true if the localization is localized, false otherwise
   */
  def isLocalized(localizationId: String): Boolean = getLocalization(localizationId).nonEmpty

  def localizationList: Iterable[Localization] = localizations.getAll

  /**
   * Gets all localization files managed by this manager.
   *
   * @return all localization files.
   */
  def localizationFiles: Iterable[File] = localizations.getLocalizationFiles

  def languageId: String

  def versionNumberRegex = ":(\\d*)" //  (version number is optional)

  /**
   * Loads localization. Loads mod localization after vanilla to give mod localizations priority
   */
  protected def loadLocalization(): Unit = {
    localizations.clear()

    // vanilla
    if (HOIIVFiles.HOI4.localization_folder == null)
      logger.error("'HOI4 localization folder' is null.")
    else if (!HOIIVFiles.HOI4.localization_folder.exists)
      logger.error("'HOI4 localization folder' does not exist.")
    else if (!HOIIVFiles.HOI4.localization_folder.isDirectory)
      logger.error("'HOI4 localization folder' is not a directory.")
    else
      loadLocalization(HOIIVFiles.HOI4.localization_folder, Localization.Status.VANILLA)

    // mod
    if (HOIIVFiles.Mod.localization_folder == null)
      logger.error("'Mod localization folder' is null.")
    else if (!HOIIVFiles.Mod.localization_folder.exists)
      logger.error("'Mod localization folder' does not exist.")
    else if (!HOIIVFiles.Mod.localization_folder.isDirectory)
      logger.error("'Mod localization folder' is not a directory.")
    else
      loadLocalization(HOIIVFiles.Mod.localization_folder, Localization.Status.EXISTS)
  }

  protected def loadLocalization(localizationFolder: File, status: Localization.Status): Unit =
    val files = localizationFolder.listFiles
    if (files == null) return

    for (file <- files)
      if (file.isDirectory) loadLocalization(file, status)
      else if (file.getName.endsWith(".yml")) loadLocalizationFile(file, status)

  // todo wow :(
  protected def loadLocalizationFile(file: File, status: Localization.Status): Boolean = {
    Using.resource(new Scanner(file)) { scanner =>
      // check language
      if !readLanguageHeader(scanner, file, languageId) then
        // either wrong language or missing header, error already recorded
        false
      else
        // read localization file
        var lineNumber = 0
        while scanner.hasNextLine do
          lineNumber += 1
          given ParsingContext(file, Some(lineNumber))
          val line = scanner.nextLine()
          parseLine(line, file, status)
        true
    }
  }

  /**
   * Reads the header (`l_<language`> line). Returns `true` if the correct language was found.
   */
  private def readLanguageHeader(scanner: Scanner, file: File, languageId: String): Boolean =
    var languageFound = false
    var continue = true

    var lineNumber = 0
    while scanner.hasNextLine && !languageFound && continue do
      lineNumber += 1
      given ParsingContext(file, Some(lineNumber))
      var line = scanner.nextLine()
      // ignore BOM
      if line.startsWith("\uFEFF") then line = line.substring(1)

      val trimmed = line.trim
      if trimmed.nonEmpty && !trimmed.startsWith("#") then
        if !trimmed.startsWith(s"l_$languageId") then
          if !trimmed.startsWith("l_") then
            // not language we want, ignore file
            continue = false
          else
            // improper format
            val pdxError = new ParsingError("Localization file is malformed", ExpectedCause(languageId), s"$trimmed")
            localizationErrors += pdxError
            continue = false
        else
          languageFound = true
          continue = false

    // if false: there was no text to trigger a fail earlier, meaning everything is commented out or there's nothing,
    // so don't worry and ignore file.
    languageFound

  private def parseLine(using ParsingContext)(line: String, file: File, status: Localization.Status): Unit=
    val trimmed = line.trim
    // ignore other lines
    if trimmed.nonEmpty && !trimmed.startsWith("#") then parseLocalizationLine(trimmed, file, status)

  /**
   * Parses one non-empty, non-comment line.
   */
  private def parseLocalizationLine(using ParsingContext)(line: String, file: File, status: Localization.Status): Unit =
    val data = line.splitWithDelimiters(versionNumberRegex, 2)

    if data.length != 3 then
      val pdxError = new ParsingError("Invalid localization file format", "incorrect number of line elements", line)
      localizationErrors += pdxError
      return

    // trim whitespace
    data mapInPlace (_.trim)

    val key = data(0)
    // ignore ":" before version number
    val version = {
      val v = data(1).substring(1).trim
      if v.isBlank then None else v.toIntOption
    }
    // ignore escaped quotes
    var value = data(2).replaceAll("//\"", "\u0000")

    val startQuote = value.indexOf("\"")
    val endQuote = value.lastIndexOf("\"")

    if validateLocalizationDef(startQuote, endQuote, value) then
      // remove leading/trailing quotes (and any comments)
      value = value.substring(startQuote + 1, endQuote)
      // fix file format issues (as it is a UTF-8 BOM file)
      value = value.trim.replaceAll("(Â§)", "§")

      /**
       * .yml example format:
       * CONTROLS_GREECE: "Controls all states in the §Y$strategic_region_greece$§! strategic region"
       * CONTROLS_ASIA_MINOR:1 "Controls all states in the §Y$strategic_region_asia_minor$§! strategic region"
       */
      val localization = new Localization(key, version, value, status)
      localizations.add(localization, file)

  def validateLocalizationDef(using ParsingContext)(startQuoteIndex: Int, endQuoteIndex: Int, value: String): Boolean =
    val extra = if startQuoteIndex >= 0 && endQuoteIndex + 1 <= value.length then value.substring(endQuoteIndex + 1).trim else ""
    // Validate
    val hasExtraInvalid = extra.nonEmpty && !extra.startsWith("#")
    val quotesInvalid = startQuoteIndex != 0 || endQuoteIndex == -1 || startQuoteIndex == endQuoteIndex

    if hasExtraInvalid || quotesInvalid then
      val causes =
        Seq(
          Option.when(hasExtraInvalid)("extraneous non-comment data after localization entry: expected comment, whitespace, or end of line"),
          Option.when(quotesInvalid)("localization value is not enclosed in quotes")
        ).flatten

      val parseError = new ParsingError("Invalid localization file format", causes.mkString("; "), extra)
      localizationErrors += parseError
      false
    else true

  def saveLocalization(): Unit = {
    val localizationFolder = HOIIVFiles.Mod.localization_folder
    val locFiles: Seq[File] =
      Files.walk(localizationFolder.toPath)
        .toScala(Seq)
        .filter(Files.isRegularFile(_))
        .filter(path => path.getFileName.endsWith(".yml"))
        .map(_.toFile)

    if (locFiles == null) return

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
      if (sortAlphabetically)
        mergedGroups.toList.sortBy(_._1)
      else
        // Determine the order from the existing file: use the first occurrence of each base key.
        val existingOrder: List[String] = existingLocalization.map(_.baseKey).distinct
        val inFileGroups = existingOrder.flatMap { key =>
          mergedGroups.get(key).map(group => key -> group)
        }
        // Append any new groups
        val newKeys = mergedGroups.keys.toList.filterNot(existingOrder.toSet)
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
        logger.error("Failed to write new localization to file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
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
          logger.debug("Replaced localization " + localization.key)
          continue = false
        }
      }
      if (!lineReplaced) throw new IOException // todo better exception
      Files.write(Paths.get(file.getAbsolutePath), lines)
    } catch {
      case exc: IOException =>
        logger.error("Failed to update localization in file. " + "\n\tLocalization: " + entry + "\n\tFile: " + file.getAbsolutePath)
    }
  }

  @throws[IOException]
  protected def getLocalizationWriter(file: File, append: Boolean) = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))

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

  /**
   * Parses a quoted string starting at startIndex (which should point to a double quote).
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
