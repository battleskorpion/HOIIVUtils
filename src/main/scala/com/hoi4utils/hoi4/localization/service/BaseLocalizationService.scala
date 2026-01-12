package com.hoi4utils.hoi4.localization.service

import com.hoi4utils.exceptions.{LocalizationExistsException, NoLocalizationManagerException, UnexpectedLocalizationStatusException}
import com.hoi4utils.hoi4.localization.*
import com.hoi4utils.improvedzio.macros.ImprovedMacros.ImprovedReloadableSyntax
import com.hoi4utils.main.{Config, HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.{ExpectedCause, ParsingContext, ParsingError}
import com.typesafe.scalalogging.LazyLogging
import zio.*

import java.io.*
import scala.annotation.experimental

// Group a base localization and its optional description together.
case class LocalizationGroup(base: Option[Localization], desc: Option[Localization])

trait LocalizationService {
  /**
   * @return map of localizations and their keys.
   */
  protected def localizations: UIO[LocalizationCollection]
  // todo let user change?
  private[localization] def capitalizationWhitelist: UIO[Set[String]]

  /**
   * Reloads localization.
   */
  def reload(): Task[Unit]

  /**
   * Retrieves the localization for the given key.
   *
   * @param key the key for the localization to retrieve
   * @return an Option containing the Localization for the given key,
   *         or None if the localization does not exist
   */
  @throws[IllegalArgumentException]
  def getLocalization(key: String): UIO[Option[Localization]]

  def getLocalizationFile(key: String): UIO[File]

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
  def setLocalization(key: String, localization: Localization): Task[Option[Localization]]

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
  def setLocalization(key: String, version: Option[Int] = None, text: String, file: File): Task[Option[Localization]]

  /**
   * Replaces the localization for the given key with the given text.
   *
   * @param key the key of the localization to replace
   * @throws IllegalArgumentException              if the key is null or localization with the given key does not exist.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable.
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def replaceLocalization(key: String, text: String): Task[Option[Localization]]

  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, file: File): Task[Unit] =
    for {
      locs <- localizations
      _ <- addLocalization(localization, locs, file)
    } yield ()

  /**
   * Checks if the given localization ID is localized (has a localization entry).
   *
   * @param localizationId the ID of the localization to check
   * @return true if the localization is localized, false otherwise
   */
  def isLocalized(localizationId: String): UIO[Boolean] = getLocalization(localizationId).map(_.nonEmpty)

  def getLocalizations: UIO[Iterable[Localization]] = localizations.map(_.getAll)

  def getAll(locs: Iterable[String]): UIO[Iterable[Localization]]

  /**
   * Gets all localization files managed by this manager.
   *
   * @return all localization files.
   */
  def localizationFiles: UIO[Iterable[File]] = localizations.map(_.getLocalizationFiles)

  def languageId: UIO[String]

  protected def replacePrevious(key: String, localization: Localization): Task[Option[Localization]] = {
    for {
      locs <- localizations
      exists = locs.containsKey(key)
      result <- if exists then replace(key, localization)
                else ZIO.fail(new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key."))
    } yield result
  }

  protected def replace(key: String, localization: Localization): Task[Option[Localization]] = {
    for {
      locs <- localizations
      result <- ZIO.attempt {
        locs.get(key) match
          case Some(prevLocalization) =>
            if prevLocalization.isReplaceableBy(localization) then
              locs.replace(key, localization)
            else throw new UnexpectedLocalizationStatusException(prevLocalization, localization)
          case None =>
            throw new IllegalArgumentException("There is no existing mod localization to replace for the given key.")
      }
    } yield result

  }

  /**
   * Adds a new localization to the localization list if it does not already exist.
   *
   * @param localization the localization to add
   */
  @throws[LocalizationExistsException]
  protected def addLocalization(localization: Localization, localizationCollection: LocalizationCollection, file: File): Task[Unit] =
    ZIO.attempt {
      if (localizationCollection.containsKey(localization.id)) throw new LocalizationExistsException(localization)
      localizationCollection.add(localization, file)
    }

  def saveLocalization(): Task[Unit]

  /**
   *
   * @param file
   * @param localization
   */
  protected def writeLocalization(file: File, localization: Localization): Task[Unit]

}

object LocalizationService extends LazyLogging {
  // TODO move object?
  def live: URLayer[com.hoi4utils.main.Config & LocalizationFileService, LocalizationService] = {
    ZLayer.service[com.hoi4utils.main.Config].flatMap { env =>
      val config = env.get[com.hoi4utils.main.Config]
      val lang = config.getProperty("localization.primaryLanguage")
      ZIO.logInfo(s"Initializing LocalizationService for language: $lang")

      lang match
        case "english" => EnglishLocalizationService.live
        case "braz_por" => PortugueseLocalizationService.live
        case "french" => FrenchLocalizationService.live
        case "german" => GermanLocalizationService.live
        case "japanese" => JapaneseLocalizationService.live
        case "korean" => KoreanLocalizationService.live
        case "polish" => PolishLocalizationService.live
        case "russian" => RussianLocalizationService.live
        case "simp_chinese" => SimplifiedChineseLocalizationService.live
        case "spanish" => SpanishLocalizationService.live
        case other =>
          logger.error("Unknown primary localization manager setting: " + other + ". Defaulting to English localization manager.")
          EnglishLocalizationService.live
    }
  }

  // TODO
  def reloadable =
    live.reloadable
}

/**
 * Abstract class that manages localizations for a system.
 * This class provides functionality to set, get, replace, and manage localizations by key.
 * It also allows reloading and saving localizations.
 */
abstract case class BaseLocalizationService(locFileService: LocalizationFileService) extends LocalizationService with LazyLogging {

  /**
   * @inheritdoc
   */
  protected def localizations: UIO[LocalizationCollection]
  private[localization] def capitalizationWhitelist: UIO[Set[String]]

  /**
   * @inheritdoc
   */
  override def reload(): Task[Unit] =
    for {
      locs <- localizations
      langId <- languageId
      _ <- ZIO.succeed(locs.clear()) *> locFileService.load(locs, langId)
    } yield ()

  /**
   * @inheritdoc
   */
  override def getLocalization(key: String): UIO[Option[Localization]] =
    for {
      locs <- localizations
      result <- ZIO.succeed(locs.get(key))
    } yield result

  def getLocalizationFile(key: String): UIO[File] =
    localizations map(_.getLocalizationFile(key))


  override def getAll(localizationKeys: Iterable[String]): UIO[Iterable[Localization]] =
    localizations map(_.getAll(localizationKeys))

  /**
   * @inheritdoc
   */
  override def setLocalization(key: String, localization: Localization): Task[Option[Localization]] =
    for {
      locs <- localizations
      result <- ZIO.attempt {
        if locs.containsKey(key) then replacePrevious(key, localization)
        else if (localization.isNew) {
          //        localizations.add(localization, file)
          //        Some(localization)
        }
        else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")
        None
      }
    } yield result

  /**
   * @inheritdoc
   */
  override def setLocalization(key: String, version: Option[Int] = None, text: String, file: File): Task[Option[Localization]] =
    for {
      locs <- localizations
      result <- ZIO.attempt {
        locs.get(key) match
          case Some(prevLocalization) =>
            val localization = prevLocalization.copyForReplace(text, version, file)
            locs.replace(key, localization)
          case None =>
            val localization = new Localization(key, version, text, Localization.Status.NEW)
            locs.add(localization, file)
            Some(localization)
      }
    } yield result

  /**
   * @inheritdoc
   */
  override def replaceLocalization(key: String, text: String): Task[Option[Localization]] = {
    for {
      locs <- localizations
      result <- ZIO.attempt {
        require(key != null, "Key cannot be null.")
        locs.get(key) match
          case Some(prevLocalization) =>
            val localization = prevLocalization.copyForReplace(text)
            // there are maps that support mapping to null, which is why the null check is necessary.
            // (read the docs for the replace method)
            locs.replace(key, localization)
          case None => throw new IllegalArgumentException("Localization with the given key does not exist.")
      }
    } yield result
  }

  override def saveLocalization(): Task[Unit] =
    localizations flatMap locFileService.save

  /**
   * @inheritdoc
   */
  override protected def writeLocalization(file: File, localization: Localization): Task[Unit] =
    ZIO.attempt {
      locFileService.write(file, localization)
    }

}

object BaseLocalizationService {
//  // Define the Layer: This says "I need a LocalizationFileService to create a LocalizationService"
//  val live: URLayer[LocalizationFileService, LocalizationService] =
//    ZLayer.fromFunction(BaseLocalizationService(_))
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
