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
class Updater extends LazyLogging:
  private var lV: Version = Version.DEFAULT

  /**
   * Checks for updates by fetching the latest version from GitHub releases.
   * Compares the current version with the latest version.
   * If an update is available, prompts the user to update.
   * @param v The current version of the application.
   * @param hDirPath The home directory path of the application.
   */
  def updateCheck(v: Version, hDirPath: Path): Unit = {
    val hDir = hDirPath.toFile
    logger.info("Checking for updates...")
    val tempUprJar = new File(hDir.getAbsolutePath
      + File.separator + "Updater"
      + File.separator + "target"
      + File.separator + "Updater.jar.temp")
    if tempUprJar.exists then updateUpdater(tempUprJar)
    lV =
      try
        val source = Source.fromURL("https://api.github.com/repos/battleskorpion/HOIIVUtils/releases/latest")
        val response = source.mkString
        val json = ujson.read(response)
        source.close()

        Version(json("tag_name").str)
      catch
        case e: Exception =>
          logger.error(s"Failed to fetch latest version, no internet?, using default version: 0.0.0")
          Version.DEFAULT
    if lV == Version.DEFAULT then return
    logger.info("Current Version: " + v)
    logger.info("Latest Version: " + lV)
    this.lV = lV
    lV match
      case lV if lV > v =>
        this.lV = lV
        logger.info("Update found")
        val response = JOptionPane.showConfirmDialog(
          null,
          s"Do you want to update to the latest version?\nCurrent Version: $v\nLatest Version: $lV\n \n This will delete your settings!",
          "Update Available",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE
        )
        if response == JOptionPane.YES_OPTION then update(hDir) // closes the program
      case lV if lV == v =>
        logger.info("You are already on the latest version")
      case lV if lV < v =>
        logger.info("You are on a newer version than the latest version on GitHub, no updates found")
      case com.hoi4utils.Version(_, _, _) =>
        logger.error("Failed to parse version")
  }

  /**
   * Runs the Updater.jar file to update the application.
   * Closes the application and runs the updater.
   * the updater if successful will run this application again.
   * @param hDir The home directory of the application.
   */
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

  /**
   * Updates the Updater.jar file if a temporary updater file exists.
   * Replaces the existing Updater.jar with the temporary file.
   * Deletes the temporary file after updating.
   *
   * (This is used to update the updater itself in case we compile a new one and release it.)
   * @param tempUprJar The temporary updater jar file.
   */
  private def updateUpdater(tempUprJar: File): Unit = {
    logger.debug("Updating updater...")
    val updaterJar = new File(tempUprJar.getAbsolutePath.replace(".temp", ""))
    Files.copy(tempUprJar.toPath, updaterJar.toPath, StandardCopyOption.REPLACE_EXISTING)
    if !updaterJar.exists() then logger.error("Failed to update updater")
    if !tempUprJar.delete() then logger.debug("Failed to delete temporary updater file")
  }