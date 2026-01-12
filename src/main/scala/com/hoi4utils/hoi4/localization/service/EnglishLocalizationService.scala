package com.hoi4utils.hoi4.localization.service

import com.hoi4utils.hoi4.localization.LocalizationCollection
import com.hoi4utils.hoi4.localization.service.{BaseLocalizationService, LocalizationFileService, LocalizationService}
import com.hoi4utils.main.Config
import com.typesafe.scalalogging.LazyLogging
import zio.{UIO, URLayer, ZIO, ZLayer}

class EnglishLocalizationService(locFileService: LocalizationFileService) extends BaseLocalizationService(locFileService) with LazyLogging {

  /**
   * Localization collection for all English localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: UIO[LocalizationCollection] = 
    ZIO.succeed(localizationCollection)

  // todo let user change?
  override def capitalizationWhitelist: UIO[Set[String]] =
    ZIO.succeed {
      Set("a", "above", "after", "among", // among us
        "an", "and", "around", "as", "at", "below", "beneath", "beside", "between", "but", "by", "for", "from", "if", "in", "into", "nor", "of", "off", "on", "onto", "or", "over", "since", "the", "through", "throughout", "to", "under", "underneath", "until", "up", "with")
    }

  override def languageId: UIO[String] = ZIO.succeed { "english" } 

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}

object EnglishLocalizationService {
    val live: URLayer[LocalizationFileService, LocalizationService] =
      ZLayer.fromFunction(EnglishLocalizationService(_))
}
