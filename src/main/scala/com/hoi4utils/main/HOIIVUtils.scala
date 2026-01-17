
package com.hoi4utils.main

import com.hoi4utils.hoi4.localization.service.{BaseLocalizationService, EnglishLocalizationService, LocalizationFileService, LocalizationService}
import com.hoi4utils.hoi4.localization.{LocalizationFormatter, YMLFileService}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application
import zio.{ZIO, *}
import zio.macros.ServiceReloader

import javax.swing.JOptionPane
import scala.annotation.experimental

object HOIIVUtils extends ZIOAppDefault {

  private type ROut = com.hoi4utils.main.Config & ServiceReloader
    & LocalizationService

  //  private var _runtime: Runtime[LocalizationService] = null
  def getActiveRuntime: Runtime[ROut] = runtime

  val configLayer: ZLayer[Any, Throwable, com.hoi4utils.main.Config] = ZLayer.fromZIO {
    ZIO.attempt(new com.hoi4utils.main.ConfigManager().createConfig).tap { config =>
      // Bridge: Sync the legacy static object for non-ZIO code
      ZIO.attempt {
        HOIIVUtilsConfig.setConfig(config)
        val initializer: Unit = new Initializer().initialize(config)
      }
    }
  }

  // Define the environment for your application
  val appLayer: ZLayer[Any, Throwable, ROut] = {
    ZLayer.make[ROut](
      configLayer,
      ZLayer.succeed(Set.empty[String]),
      YMLFileService.live,
      LocalizationFormatter.live,
      LocalizationFileService.live,
      LocalizationService.reloadable,
      ServiceReloader.live,
      ZLayer.Debug.tree
    )
  }

  override val runtime: Runtime[ROut] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.unsafe.fromLayer(appLayer)
    }

  override def run =
    app.provideSome[ZIOAppArgs](appLayer)

  private def app: ZIO[ZIOAppArgs & ROut, Throwable, Unit] =
    for {
      //      rt   <- ZIO.runtime[LocalizationService].provide(appLayer)
      //      _    <- ZIO.succeed { _runtime = rt }
      args <- getArgs
      _ <- ZIO.attemptBlocking(Application.launch(classOf[App], args.toArray *))
      _ <- ZIO.logInfo("Application window closed, shutting down...")
    } yield ()

}

/** lol */
object HOIIVUtilsConfig extends LazyLogging:
  private var config: Option[com.hoi4utils.main.Config] = None

  def getConfig: com.hoi4utils.main.Config =
    config match
      case Some(c) => c
      case None => throw new RuntimeException("Configuration not initialized")

  def setConfig(newConfig: com.hoi4utils.main.Config): Unit = config = Some(newConfig)

  /**
   * Get a user saved property from property class saved properties, NOT from HOIIVUtils.properties.
   * We get saved HOIIVUtils.properties data only when the Menu is opened
   * @param key Property name
   * @return Property value or null if not found
   */
  def get(key: String): String = getConfig.getProperties.getProperty(key)

  /**
   * Set a user saved property that will be saved to HOIIVUtils.properties on save() call
   * @param key   Property key
   * @param value Property value
   */
  def set(key: String, value: String): Unit = getConfig.getProperties.setProperty(key, value)

  /**
   * Save the current configuration to HOIIVUtils.properties
   */
  def save(): Unit =
    try ConfigManager().saveProperties(getConfig)
    catch case e: Exception =>
      logger.error(s"v${getConfig.getProperties.getProperty("version")} Failed to save configuration: ${e.getMessage}")
      JOptionPane.showMessageDialog(null, s"version: ${getConfig.getProperties.getProperty("version")} Failed to save configuration: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
      throw new Exception(e)


