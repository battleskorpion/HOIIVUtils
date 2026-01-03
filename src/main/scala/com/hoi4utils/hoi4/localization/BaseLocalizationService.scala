package com.hoi4utils.hoi4.localization

import com.hoi4utils.Providers.Provider
import com.hoi4utils.exceptions.{LocalizationExistsException, NoLocalizationManagerException, UnexpectedLocalizationStatusException}
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.parser.{ExpectedCause, ParsingContext, ParsingError}
import com.hoi4utils.Providers.*
import com.typesafe.scalalogging.LazyLogging

import java.io.*
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag


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
      getOrCreate(() => {
        val locFileService = provided[LocalizationFileService]
        new EnglishLocalizationService(locFileService)
      }),
      getOrCreate(() => {
        val locFileService = provided[LocalizationFileService]
        new RussianLocalizationService(locFileService)
      }),
      getOrCreate(() => {
        val locFileService = provided[LocalizationFileService]
        new SpanishLocalizationService(locFileService)
      })

    )

}

// Group a base localization and its optional description together.
case class LocalizationGroup(base: Option[Localization], desc: Option[Localization])

trait LocalizationService(locFileService: LocalizationFileService) {
  /**
   * @return map of localizations and their keys.
   */
  protected def localizations: LocalizationCollection
  // todo let user change?
  private[localization] def capitalizationWhitelist: Set[String]

  /**
   * Reloads the localizations. The specific behavior of this method
   * depends on the implementation.
   */
  def reload(): Unit

  /**
   * Retrieves the localization for the given key.
   *
   * @param key the key for the localization to retrieve
   * @return an Option containing the Localization for the given key,
   *         or None if the localization does not exist
   */
  @throws[IllegalArgumentException]
  def getLocalization(key: String): Option[Localization]

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
  def setLocalization(key: String, localization: Localization): Option[Localization]

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
  def setLocalization(key: String, version: Option[Int] = None, text: String, file: File): Option[Localization]

  /**
   * Replaces the localization for the given key with the given text.
   *
   * @param key the key of the localization to replace
   * @throws IllegalArgumentException              if the key is null or localization with the given key does not exist.
   * @throws UnexpectedLocalizationStatusException if the localization is not replaceable.
   */
  @throws[IllegalArgumentException]
  @throws[UnexpectedLocalizationStatusException]
  def replaceLocalization(key: String, text: String): Option[Localization]

  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, file: File): Unit =
    addLocalization(localization, localizations, file)

  /**
   * Checks if the given localization ID is localized (has a localization entry).
   *
   * @param localizationId the ID of the localization to check
   * @return true if the localization is localized, false otherwise
   */
  def isLocalized(localizationId: String): Boolean = getLocalization(localizationId).nonEmpty

  def getLocalizations: Iterable[Localization] = localizations.getAll

  /**
   * Gets all localization files managed by this manager.
   *
   * @return all localization files.
   */
  def localizationFiles: Iterable[File] = localizations.getLocalizationFiles

  def languageId: String

  protected def replacePrevious(key: String, localization: Localization): Option[Localization] =
    if localizations.containsKey(key) then replace(key, localization)
    else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")

  protected def replace(key: String, localization: Localization) =
    localizations.get(key) match
      case Some(prevLocalization) =>
        if prevLocalization.isReplaceableBy(localization) then
          localizations.replace(key, localization)
        else throw new UnexpectedLocalizationStatusException(prevLocalization, localization)
      case None => throw new IllegalArgumentException("There is no existing mod localization to replace for the given key.")

  /**
   * Adds a new localization to the localization list if it does not already exist.
   *
   * @param localization the localization to add
   */
  @throws[LocalizationExistsException]
  protected def addLocalization(localization: Localization, localizationCollection: LocalizationCollection, file: File): Unit =
    if (localizationCollection.containsKey(localization.id)) throw new LocalizationExistsException(localization)
    localizationCollection.add(localization, file)

  /**
   * Loads localization. Loads mod localization after vanilla to give mod localizations priority
   */
  protected def loadLocalization(): Unit =
    localizations.clear()
    locFileService.loadLocalization()

  def saveLocalization(): Unit =
    locFileService.saveLocalization(localizations)

  /**
   *
   * @param file
   * @param localization
   */
  protected def writeLocalization(file: File, localization: Localization): Unit =
    locFileService.writeLocalization(file, localization)

}

/**
 * Abstract class that manages localizations for a system.
 * This class provides functionality to set, get, replace, and manage localizations by key.
 * It also allows reloading and saving localizations.
 */
abstract class BaseLocalizationService(locFileService: LocalizationFileService) extends LocalizationService(locFileService) with LazyLogging {

  /**
   * @inheritdoc
   */
  protected def localizations: LocalizationCollection
  private[localization] def capitalizationWhitelist: Set[String]

  /**
   * @inheritdoc
   */
  override def reload(): Unit = loadLocalization()

  /**
   * @inheritdoc
   */
  override def getLocalization(key: String): Option[Localization] = localizations.get(key)

  /**
   * @inheritdoc
   */
  override def setLocalization(key: String, localization: Localization): Option[Localization] =
    if localizations.containsKey(key) then replacePrevious(key, localization)
    else
      if (localization.isNew) {
//        localizations.add(localization, file)
//        Some(localization)
      }
      else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")
    None

  /**
   * @inheritdoc
   */
  override def setLocalization(key: String, version: Option[Int] = None, text: String, file: File): Option[Localization] =
    localizations.get(key) match
      case Some(prevLocalization) =>
        val localization = prevLocalization.copyForReplace(text, version, file)
        localizations.replace(key, localization)
      case None =>
        val localization = new Localization(key, version, text, Localization.Status.NEW)
        localizations.add(localization, file)
        Some(localization)

  /**
   * @inheritdoc
   */
  override def replaceLocalization(key: String, text: String): Option[Localization] =
    require(key != null, "Key cannot be null.")
    localizations.get(key) match
      case Some(prevLocalization) =>
        val localization = prevLocalization.copyForReplace(text)
        // there are maps that support mapping to null, which is why the null check is necessary.
        // (read the docs for the replace method)
        localizations.replace(key, localization)
      case None => throw new IllegalArgumentException("Localization with the given key does not exist.")

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
