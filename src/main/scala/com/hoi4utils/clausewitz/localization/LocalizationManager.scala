package com.hoi4utils.clausewitz.localization

import org.jetbrains.annotations.NotNull

import java.io.File
import java.util
import java.util.{HashSet, List, Objects}


object LocalizationManager {
  //static Map<String, Localization> localizations = new HashMap<>();
  private var primaryManager: Option[LocalizationManager] = None

  def getLocalizationFile(key: String): File = get.localizations.getLocalizationFile(key)

  @throws[NoLocalizationManagerException]
  def get: LocalizationManager = {
    primaryManager match {
      case Some(mgr) => mgr
      case None => throw new NoLocalizationManagerException
    }
  }

  def get(key: String): Option[Localization] = get.getLocalization(key)

  def getAll(localizationKeys: Iterable[String]): Iterable[Localization] = get.localizations.getAll(localizationKeys)

  @throws[IllegalArgumentException]
  def find(key: String): Localization = get.getLocalization(key)

  private[localization] def numCapitalLetters(word: String): Int = {
    if (word == null) return 0
    var num_cap_letters = 0
    num_cap_letters = 0
    for (j <- 0 until word.length) {
      if (Character.isUpperCase(word.charAt(j))) num_cap_letters += 1
    }
    num_cap_letters
  }

  private[localization] def isAcronym(word: String) = numCapitalLetters(word) == word.length
}

abstract class LocalizationManager {
  final def setManager(manager: LocalizationManager): Unit = {
    LocalizationManager.primaryManager = Some(manager)
  }

  def reload(): Unit

  /**
   * @param key
   * @return the localization for the given key.
   * @throws IllegalArgumentException
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
  def setLocalization(key: String, localization: Localization): Localization = {
    val localizations = localizations
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    if (localization == null) throw new IllegalArgumentException("Localization cannot be null.")
    if (localizations.containsLocalizationKey(key)) {
      val prevLocalization = localizations.get(key)
      if (prevLocalization.isReplaceableBy(localization)) return localizations.replace(key, localization)
      else throw new UnexpectedLocalizationStatusException(prevLocalization, localization)
    }
    else if (localization.isNew) {
    }
    else throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.")
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
  def setLocalization(key: String, text: String, file: File): Localization = setLocalization(key, null, text, file)

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
  def setLocalization(key: String, version: Integer, text: String, file: File): Localization = {
    val localizations = localizations
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    if (localizations.containsLocalizationKey(key)) {
      val prevLocalization = localizations.get(key)
      val localization = prevLocalization.replaceWith(text, version, file)
      localizations.replace(key, localization)
    }
    else {
      val localization = new Localization(key, version, text, Localization.Status.NEW)
      localizations.add(localization, file)
      null
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
  def replaceLocalization(key: String, text: String): Localization = {
    val localizations = localizations
    if (key == null) throw new IllegalArgumentException("Key cannot be null.")
    if (localizations.containsLocalizationKey(key)) {
      val prevLocalization = localizations.get(key)
      val localization = prevLocalization.replaceWith(text)
      // there are maps that support mapping to null, which is why the null check is necessary.
      // (read the docs for the replace method)
      Objects.requireNonNull(localizations.replace(key, localization))
    }
    else throw new IllegalArgumentException("Localization with the given key does not exist.")
  }

  def saveLocalization(): Unit

  /**
   * Adds a new localization to the localization list if it does not already exist.
   *
   * @param localization the localization to add
   */
  @throws[LocalizationExistsException]
  def addLocalization(localization: Localization, file: File): Unit

  def isLocalized(localizationId: String): Boolean

  /**
   * @return map of localizations and their keys.
   */
  protected def localizations: LocalizationCollection

  def localizationList: util.List[Localization] = localizations.getAll

  def titleCapitalize(trim: String): String

  // todo let user change?
  private[localization] def capitalizationWhitelist: util.HashSet[String]
}
