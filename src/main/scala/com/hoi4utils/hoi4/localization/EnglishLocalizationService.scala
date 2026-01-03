package com.hoi4utils.hoi4.localization

import com.typesafe.scalalogging.LazyLogging

class EnglishLocalizationService(locFileService: LocalizationFileService) extends BaseLocalizationService(locFileService) with LazyLogging {

  /**
   * Localization collection for all English localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: LocalizationCollection = localizationCollection

  // todo let user change?
  override def capitalizationWhitelist: Set[String] =
    Set("a", "above", "after", "among", // among us
      "an", "and", "around", "as", "at", "below", "beneath", "beside", "between", "but", "by", "for", "from", "if", "in", "into", "nor", "of", "off", "on", "onto", "or", "over", "since", "the", "through", "throughout", "to", "under", "underneath", "until", "up", "with")

  override def languageId: String = "english"

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}
