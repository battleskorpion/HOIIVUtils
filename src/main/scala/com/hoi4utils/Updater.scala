package com.hoi4utils

import dotty.tools.sjs.ir.Trees.JSBinaryOp.||
import org.apache.logging.log4j.{LogManager, Logger}

import scala.sys.process.*
import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.swing.JOptionPane
import scala.io.Source

/**
 * Updater class to check for updates and update the application.
 * Gets the latest version from GitHub releases.
 * Runs the Updater.jar file to update the application.
 * Closes the application and runs the updater.
 */
class Updater {
  val LOGGER: Logger = LogManager.getLogger(classOf[Updater])
  var lV = "0.0.0"
  def updateCheck(v: String, hDir: File): Unit = {
    LOGGER.debug("Checking for updates...")
    val tempUprJar = new File(hDir.getAbsolutePath
      + File.separator + "Updater"
      + File.separator + "target"
      + File.separator + "Updater.jar.temp")
    LOGGER.debug("Deleting temp updater jar")
    if (tempUprJar.exists) updateUpdater(tempUprJar)
    lV =
      try {
        val apiUrl = s"https://api.github.com/repos/battleskorpion/HOIIVUtils/releases/latest"
        val source = Source.fromURL(apiUrl)
        val response = source.mkString
        val json = ujson.read(response)
        json("tag_name").str
      } catch {
        case e: Exception =>
          LOGGER.error(s"Failed to fetch latest version: ${e.getMessage}")
          "0.0.0"
      }
    if (lV == "0.0.0") return
    LOGGER.debug("Current Version: " + v)
    LOGGER.debug("Latest Version: " + lV)
    this.lV = lV
    try {
      if (Version(lV) > Version(v))
        LOGGER.debug("Update found")
        val response = JOptionPane.showConfirmDialog(
          null,
          s"Do you want to update to the latest version?\nCurrent Version: $v\nLatest Version: $lV\n \n This will delete your settings!",
          "Update Available",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE
        )
        if (response == JOptionPane.YES_OPTION) update(hDir) // closes the program
      else LOGGER.debug("No updates found")
    } catch {
      case e: IllegalArgumentException =>
        LOGGER.error("Failed to obtain version number from properties.")
    }
  }

  private def update(hDir: File): Unit = {
    LOGGER.info("Updating...")
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
        LOGGER.error("Failed to update: " + e.getMessage)
  }
  
  private def updateUpdater(tempUprJar: File): Unit = {
    LOGGER.debug("Updating updater...")
    val updaterJar = new File(tempUprJar.getAbsolutePath.replace(".temp", ""))
    Files.copy(tempUprJar.toPath, updaterJar.toPath, StandardCopyOption.REPLACE_EXISTING)
    if (updaterJar.exists()) {
      LOGGER.debug("Updater updated successfully")
    } else {
      LOGGER.debug("Failed to update updater")
    }
    if (tempUprJar.delete()) {
      LOGGER.debug("Temporary updater file deleted successfully")
    } else {
      LOGGER.debug("Failed to delete temporary updater file")
    }
  }
}

case class Version(major: Int, minor: Int, patch: Int) extends Ordered[Version] {
  override def compare(that: Version): Int =
    Ordering[(Int, Int, Int)]
      .compare((major, minor, patch), (that.major, that.minor, that.patch))

  override def toString: String = s"$major.$minor.$patch"
}

object Version {
  /** Parses strings like "1.2.3" (and will throw on malformed input) */
  def apply(s: String): Version = {
    if (s == null || s.isEmpty) throw new IllegalArgumentException("Version string cannot be null or empty")
    val Array(a, b, c) = s.split("\\.", 3)
    Version(a.toInt, b.toInt, c.toInt)
  }
}