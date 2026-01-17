//package com.hoi4utils.main
//
//import com.hoi4utils.hoi4.localization.service.{BaseLocalizationService, EnglishLocalizationService, LocalizationFileService, LocalizationService}
//import com.hoi4utils.hoi4.localization.{LocalizationFormatter, YMLFileService}
//import com.hoi4utils.main.HOIIVUtils.getConfig
//import javafx.application.Application
//import zio.{ZIO, *}
//import zio.macros.ServiceReloader
//
//import scala.annotation.experimental
//
//object ZHOIIVUtils extends ZIOAppDefault {
//
//  private type ROut = com.hoi4utils.main.Config & ServiceReloader
//    & LocalizationService
//
//  //  private var _runtime: Runtime[LocalizationService] = null
//  def getActiveRuntime: Runtime[ROut] = runtime
//
//  val configLayer: ZLayer[Any, Throwable, com.hoi4utils.main.Config] = ZLayer.fromZIO {
//    ZIO.attempt(new com.hoi4utils.main.ConfigManager().createConfig).tap { config =>
//      // Bridge: Sync the legacy static object for non-ZIO code
//      ZIO.attempt {
//        HOIIVUtils.setConfig(config)
//        val initializer: Unit = new Initializer().initialize(config)
//      }
//    }
//  }
//
//  // Define the environment for your application
//  val appLayer: ZLayer[Any, Throwable, ROut] = {
//    ZLayer.make[ROut](
//      configLayer,
//      ZLayer.succeed(Set.empty[String]),
//      YMLFileService.live,
//      LocalizationFormatter.live,
//      LocalizationFileService.live,
//      LocalizationService.reloadable,
//      ServiceReloader.live,
//      ZLayer.Debug.tree 
//    )
//  }
//
//  override val runtime: Runtime[ROut] =
//    Unsafe.unsafe { implicit unsafe =>
//      Runtime.unsafe.fromLayer(appLayer)
//    }
//
//  override def run =
//    app.provideSome[ZIOAppArgs](appLayer)
//
//  private def app: ZIO[ZIOAppArgs & ROut, Throwable, Unit] =
//    for {
//      //      rt   <- ZIO.runtime[LocalizationService].provide(appLayer)
//      //      _    <- ZIO.succeed { _runtime = rt }
//      args <- getArgs
//      _ <- ZIO.attemptBlocking(Application.launch(classOf[App], args.toArray *))
//    } yield ()
//
//}
