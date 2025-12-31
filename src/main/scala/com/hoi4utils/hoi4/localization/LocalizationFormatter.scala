package com.hoi4utils.hoi4.localization

import com.typesafe.scalalogging.LazyLogging
import com.hoi4utils.hoi4.localization.LocalizationManager

import scala.jdk.javaapi.CollectionConverters

class LocalizationFormatter extends LazyLogging {
  
  /**
   * Capitalizes every word in a string with a pre-set whitelist
   *
   * @param title the string to capitalize
   * @return Returns the edited string unless the string has no words
   */
  def titleCapitalize(title: String): String = {
    if (title.isBlank) return title

    val words = title.split(" ").filter(w => !w.isBlank).toBuffer
    val whitelist = LocalizationManager.get.capitalizationWhitelist

    // capitalize first word
    words(0) = capitalizeWord(words.head)

    logger.debug("num words: " + words.size)
    for
      i <- 1 until words.size
      if !LocalizationManager.isAcronym(words(i))
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
  
}
