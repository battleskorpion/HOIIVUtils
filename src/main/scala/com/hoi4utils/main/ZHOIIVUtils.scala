package com.hoi4utils.main

import com.hoi4utils.hoi4.localization.{BaseLocalizationService, EnglishLocalizationService, LocalizationFileService, LocalizationFormatter, LocalizationService, YMLFileService}
import javafx.application.Application
import zio.*
import zio.macros.ServiceReloader

object ZHOIIVUtils extends ZIOAppDefault {

  private type ROut = LocalizationService & ServiceReloader

  //  private var _runtime: Runtime[LocalizationService] = null
  def getActiveRuntime: Runtime[ROut] = runtime

  // Define the environment for your application
  val appLayer: ZLayer[Any, Nothing, ROut] = {
    ZLayer.make[ROut](
      ZLayer.succeed(Set.empty[String]),
      YMLFileService.live,
      LocalizationFormatter.live,
      LocalizationFileService.live,
      EnglishLocalizationService.live,
      ServiceReloader.live
    )
  }

  override val runtime: Runtime[ROut] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.unsafe.fromLayer(appLayer)
    }

  override def run =
    for {
//      rt   <- ZIO.runtime[LocalizationService].provide(appLayer)
//      _    <- ZIO.succeed { _runtime = rt }
      args <- getArgs
      _ <- ZIO.attemptBlocking(Application.launch(classOf[App], args.toArray *))
    } yield ()
}
