package com.hoi4utils.hoi4.localization

import com.typesafe.scalalogging.LazyLogging
import zio.{UIO, ZIO}

class RussianLocalizationService(locFileService: LocalizationFileService) extends BaseLocalizationService(locFileService) with LazyLogging {

  /**
   * Localization collection for all Russian localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: UIO[LocalizationCollection] =
    ZIO.succeed(localizationCollection)

  // todo let user change?
  /**
   * @inheritdoc
   * for Russian, capitalization is only for the first letter, and for nouns
   */
  override def capitalizationWhitelist: UIO[Set[String]] =
    ZIO.succeed {Set.empty}

  override def languageId: UIO[String] = ZIO.succeed { "russian" }

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}
