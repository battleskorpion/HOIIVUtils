package com.hoi4utils.hoi4.localization

import com.typesafe.scalalogging.LazyLogging

class SpanishLocalizationService extends LocalizationService with LazyLogging {

  /**
   * Localization collection for all Spanish localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: LocalizationCollection = localizationCollection

  // todo let user change?
  /**
   * @inheritdoc
   * TODO: figure out what to do for spanish
   */
  override def capitalizationWhitelist: Set[String] = Set.empty

  override def languageId: String = "spanish"

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}
