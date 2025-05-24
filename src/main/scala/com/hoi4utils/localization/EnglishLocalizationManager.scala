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
  
  override def localizations: LocalizationCollection = localizationCollection

  // todo let user change?
  override def capitalizationWhitelist: Set[String] = {
    Set("a", "above", "after", "among", // among us
      "an", "and", "around", "as", "at", "below", "beneath", "beside", "between", "but", "by", "for", "from", "if", "in", "into", "nor", "of", "off", "on", "onto", "or", "over", "since", "the", "through", "throughout", "to", "under", "underneath", "until", "up", "with")
  }

  override def toString: String = "EnglishLocalizationManager{" + "localizations=" + localizationCollection + "}"
}