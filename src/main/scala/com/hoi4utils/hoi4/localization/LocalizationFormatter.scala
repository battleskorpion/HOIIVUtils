package com.hoi4utils.hoi4.localization

import com.hoi4utils.Providers.*
import com.typesafe.scalalogging.LazyLogging
import com.hoi4utils.hoi4.localization.BaseLocalizationService

import scala.jdk.javaapi.CollectionConverters

class LocalizationFormatter(localizationSvc: BaseLocalizationService) extends LazyLogging {

  /** Formats a Localization into a file line. */
  def formatLocalization(loc: Localization): String = {
    // Build the entry string
    val entry = s"${loc.id}:${loc.versionStr} \"${loc.text}\""
    entry.replaceAll("§", "Â§") // necessary with UTF-8 BOM
  }

  /**
   * Capitalizes every word in a string with a pre-set whitelist
   *
   * @param title the string to capitalize
   * @return Returns the edited string unless the string has no words
   */
  def titleCapitalize(title: String): String = {
    if (title.isBlank) return title

    val words = title.split(" ").filter(w => !w.isBlank).toBuffer
    val whitelist = localizationSvc.capitalizationWhitelist

    // capitalize first word
    words(0) = capitalizeWord(words.head)

    logger.debug("num words: " + words.size)
    for
      i <- 1 until words.size
      if !isAcronym(words(i))
      if !whitelist.contains(words(i))
    do
      words(i) = capitalizeWord(words(i))

    logger.debug("capitalized: " + String.join(" ", CollectionConverters.asJava(words)))
    String.join(" ", CollectionConverters.asJava(words))
  }

  private def capitalizeWord(word: String): String = {
    if (word.isBlank) word
    else if (word.length == 1) word.head.toUpper.toString
    else word.head.toUpper.toString + word.substring(1)
  }

  def isAcronym(word: String): Boolean = numCapitalLetters(word) == word.trim.length

  def numCapitalLetters(word: String): Int =
    if word.isBlank then 0
    else word count (_.isUpper)

}

object LocalizationFormatter:
  given (using Provider[BaseLocalizationService]): Provider[LocalizationFormatter] = 
    provide(LocalizationFormatter(provided[BaseLocalizationService]))
