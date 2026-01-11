package com.hoi4utils.hoi4.localization

import com.hoi4utils.main.Config
import com.typesafe.scalalogging.LazyLogging
import zio.{UIO, URLayer, ZIO, ZLayer}

class SpanishLocalizationService(locFileService: LocalizationFileService) extends BaseLocalizationService(locFileService) with LazyLogging {

  /**
   * Localization collection for all Spanish localization.
   */
  final protected val localizationCollection = new LocalizationCollection

  override def localizations: UIO[LocalizationCollection] =
    ZIO.succeed(localizationCollection)

  // todo let user change?
  /**
   * @inheritdoc
   * TODO: figure out what to do for spanish
   */
  override def capitalizationWhitelist: UIO[Set[String]] = ZIO.succeed {Set.empty}

  override def languageId: UIO[String] = ZIO.succeed { "spanish" }

  override def toString: String = s"${getClass.getName}{" + "localizations=" + localizationCollection + "}"
}

object SpanishLocalizationService {
  val live: URLayer[LocalizationFileService, LocalizationService] =
    ZLayer.fromFunction(SpanishLocalizationService(_))
}
