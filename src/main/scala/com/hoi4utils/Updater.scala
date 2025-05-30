package com.hoi4utils

import com.typesafe.scalalogging.LazyLogging

import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption}
import javax.swing.JOptionPane
import scala.io.Source
import scala.sys.process.*

/**
 * Updater class to check for updates and update the application.
 * Gets the latest version from GitHub releases.
 * Runs the Updater.jar file to update the application.
 * Closes the application and runs the updater.
 */
class Updater extends LazyLogging {
  var lV: Version = Version.DEFAULT
  def updateCheck(v: Version, hDirPath: Path): Unit = {
    val hDir = hDirPath.toFile
    logger.debug("Checking for updates...")
    val tempUprJar = new File(hDir.getAbsolutePath
      + File.separator + "Updater"
      + File.separator + "target"
      + File.separator + "Updater.jar.temp")
    if (tempUprJar.exists) updateUpdater(tempUprJar)
    lV =
      try {
        val apiUrl = s"https://api.github.com/repos/battleskorpion/HOIIVUtils/releases/latest"
        val source = Source.fromURL(apiUrl)
        val response = source.mkString
        val json = ujson.read(response)
        Version(json("tag_name").str)
      } catch {
        case e: Exception =>
          logger.error(s"Failed to fetch latest version: ${e.getMessage}")
          Version.DEFAULT
      }
    if (lV == Version.DEFAULT) return
    logger.debug("Current Version: " + v)
    logger.debug("Latest Version: " + lV)
    this.lV = lV
    if (lV > v)
      logger.debug("Update found")
      val response = JOptionPane.showConfirmDialog(
        null,
        s"Do you want to update to the latest version?\nCurrent Version: $v\nLatest Version: $lV\n \n This will delete your settings!",
        "Update Available",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
      )
      if (response == JOptionPane.YES_OPTION) update(hDir) // closes the program
    else logger.debug("No updates found")
  }

  private def update(hDir: File): Unit = {
    logger.info("Updating...")
    try
      val updaterJar = hDir.getAbsolutePath
        + File.separator + "Updater"
        + File.separator + "target"
        + File.separator + "Updater.jar"
      val javaCmd = s"""java -jar "$updaterJar" "${hDir.getAbsolutePath}" "$lV""""
      val command = Seq("cmd", "/c", "start", "cmd", "/k", javaCmd)
      Process(command).run()
      sys.exit(0)
    catch
      case e: Exception =>
        logger.error("Failed to update: " + e.getMessage)
  }
  
  private def updateUpdater(tempUprJar: File): Unit = {
    logger.debug("Updating updater...")
    val updaterJar = new File(tempUprJar.getAbsolutePath.replace(".temp", ""))
    Files.copy(tempUprJar.toPath, updaterJar.toPath, StandardCopyOption.REPLACE_EXISTING)
    if (updaterJar.exists()) {
      logger.debug("Updater updated successfully")
    } else {
      logger.debug("Failed to update updater")
    }
    if (tempUprJar.delete()) {
      logger.debug("Temporary updater file deleted successfully")
    } else {
      logger.debug("Failed to delete temporary updater file")
    }
  }
}