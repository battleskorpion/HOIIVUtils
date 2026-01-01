package com.hoi4utils.hoi4.localization

import com.typesafe.scalalogging.LazyLogging

class RussianLocalizationService extends LocalizationService with LazyLogging {

  /**
   * Localization collection for all Russian localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: LocalizationCollection = localizationCollection

  // todo let user change?
  /**
   * @inheritdoc
   * for Russian, capitalization is only for the first letter, and for nouns
   */
  override def capitalizationWhitelist: Set[String] = Set.empty

  override def languageId: String = "russian"

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}
