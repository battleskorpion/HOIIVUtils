package com.hoi4utils.hoi4.localization

import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{ExpectedCause, ParsingContext, ParsingError}
import com.typesafe.scalalogging.LazyLogging
import zio.{Task, ZIO, ZLayer}
import com.hoi4utils.extensions.validateFolder

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Scanner
import scala.jdk.StreamConverters.StreamHasToScala
import scala.util.Using

trait LocalizationFileService {
  def load(localizations: LocalizationCollection, languageId: String): Task[Unit]
  def save(localizations: LocalizationCollection): Task[Unit]
  def write(file: File, localization: Localization): Task[Unit]
}

object LocalizationFileService {
  val live: ZLayer[YMLFileService & LocalizationFormatter, Nothing, LocalizationFileService] =
    ZLayer.fromFunction(LocalizationFileServiceImpl.apply)
}

case class LocalizationFileServiceImpl(ymlFileService: YMLFileService, locFormatter: LocalizationFormatter) extends LocalizationFileService with LazyLogging {
  /**
   * Updates a localization file by merging new localizations with existing ones.
   *
   * @param file               The localization file to update.
   * @param localizationList   The list of new or updated localizations.
   * @param sortAlphabetically If true, groups are sorted alphabetically; if false, new entries are appended.
   */
  def updateLocalizationFile(file: File, localizationList: Seq[Localization], sortAlphabetically: Boolean): Unit = {
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

    // For each group, print the base loc and then the description loc (if any).
    for ((_, group) <- groupList)
      val func = (loc: Localization) => newContent.append("\t").append(locFormatter.formatLocalization(loc)).append("\n")
      group.base.foreach(func)
      group.desc.foreach(func)
      newContent.append("\n") // empty line between groups

    // Write the updated content back to the file.
    Files.write(file.toPath, newContent.toString.getBytes(StandardCharsets.UTF_8))
    //    Files.write(Paths.get(file.getAbsolutePath), lines)
  }

  /**
   * Loads localization. Loads mod localization after vanilla to give mod localizations priority
   */
  def loadLocalization(): Unit =
    // vanilla
    HOIIVFiles.HOI4.localization_folder
      .validateFolder("HOI4 localization folder")
      .fold(
        logger.error,
        loadLocalization(_, Localization.Status.VANILLA)
      )

    // mod
    HOIIVFiles.Mod.localization_folder
      .validateFolder("Mod localization folder")
      .fold(
        logger.error,
        loadLocalization(_, Localization.Status.EXISTS)
      )

  protected def loadLocalization(localizations: LocalizationCollection, localizationFolder: File, status: Localization.Status, languageId: String): Unit =
    val files = localizationFolder.listFiles

    for (file <- files)
      if (file.isDirectory) loadLocalization(localizations, file, status, languageId)
      else if (file.getName.endsWith(".yml")) loadLocalizationFile(localizations, file, status, languageId)

  // todo wow :(
  protected def loadLocalizationFile(localizations: LocalizationCollection, file: File, status: Localization.Status, languageId: String): Boolean = {
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
          parseLine(localizations, line, file, status)
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
//            localizationErrors += pdxError      // TODO
            continue = false
        else
          languageFound = true
          continue = false

    // if false: there was no text to trigger a fail earlier, meaning everything is commented out or there's nothing,
    // so don't worry and ignore file.
    languageFound

  private def parseLine(using ParsingContext)(localizations: LocalizationCollection, line: String, file: File, status: Localization.Status): Unit =
    val trimmed = line.trim
    // ignore other lines
    if trimmed.nonEmpty && !trimmed.startsWith("#") then parseLocalizationLine(localizations, trimmed, file, status)

  /**
   * Parses one non-empty, non-comment line.
   */
  private def parseLocalizationLine(using ParsingContext)(localizations: LocalizationCollection, line: String, file: File, status: Localization.Status): Unit =
    val data = line.splitWithDelimiters(versionNumberRegex, 2)

    if data.length != 3 then
      val pdxError = new ParsingError("Invalid localization file format", "incorrect number of line elements", line)
//      localizationErrors += pdxError  // TODO
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
//      localizationErrors += parseError    // TODO TODO
      false
    else true

  protected def versionNumberRegex = ":(\\d*)" //  (version number is optional)

  override def save(localizations: LocalizationCollection): Task[Unit] =
    ZIO.attempt {
      val localizationFolder = HOIIVFiles.Mod.localization_folder
      if localizationFolder.exists() then
        val locFiles: Seq[File] =
          Files.walk(localizationFolder.toPath)
            .toScala(Seq)
            .filter(Files.isRegularFile(_))
            .filter(path => path.getFileName.endsWith(".yml"))
            .map(_.toFile)

        if locFiles != null then
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
        else ()
    }

  /**
   *
   * @param file
   * @param localization
   */
  def write(file: File, localization: Localization): Task[Unit] =
    ZIO.attempt {
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
