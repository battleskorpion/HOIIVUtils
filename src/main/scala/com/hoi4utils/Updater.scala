package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVUtils.HOIIVUTILS_VERSION
import org.apache.logging.log4j.{LogManager, Logger}

import scala.sys.process.*
import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.swing.JOptionPane
import scala.io.Source
import scala.math.Ordered.orderingToOrdered

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
    if (tempUprJar.exists) {
      updateUpdater(tempUprJar)
      LOGGER.info("Deleting temp updater jar")
    }
    lV = getLatestVersion("battleskorpion/HOIIVUtils")
    LOGGER.debug("Current Version: " + v)
    LOGGER.debug("Latest Version: " + lV)
    if (lV <= "0.0.0") {
      LOGGER.error("Failed to fetch latest version")
      return
    }
    this.lV = lV
    val (maV: Int, miV: Int, paV: Int) = (ma(v), mi(v), pa(v))
    val (lmaV: Int, lmiV: Int, lpaV: Int) = (ma(lV), mi(lV), pa(lV))
    if ((lmaV, lmiV, lpaV) > (maV, miV, paV)) {
      LOGGER.debug("Update found")
      val response = JOptionPane.showConfirmDialog(
        null,
        s"Do you want to update to the latest version?\nCurrent Version: $v\nLatest Version: $lV",
        "Update Available",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
      )

      if (response == JOptionPane.YES_OPTION) {
        update(hDir) // closes the program
      }
    } else {
      LOGGER.debug("No updates found")
    }
  }

  private def ma(v: String): Int = {
    val maV = v.split("\\.")(0).toInt
    maV
  }

  private def mi(v: String): Int = {
    val miV = v.split("\\.")(1).toInt
    miV
  }

  private def pa(v: String): Int = {
    val buV = v.split("\\.")(2).toInt
    buV
  }

  private def getLatestVersion(lV: String): String = {
    try {
      val apiUrl = s"https://api.github.com/repos/$lV/releases/latest"
      val source = Source.fromURL(apiUrl)
      val response = source.mkString
      val json = ujson.read(response)
      json("tag_name").str
    } catch {
      case e: Exception =>
        println(s"Failed to fetch latest version: ${e.getMessage}")
        "0.0.0"
    }
  }
  
  def update(hDir: File): Unit = {
    println("Updating...")
    try {
      val updaterJar = hDir.getAbsolutePath
        + File.separator + "Updater"
        + File.separator + "target"
        + File.separator + "Updater.jar"
      println("updaterJar: " + updaterJar)
      val command = Seq("java", "-jar", updaterJar, hDir.getAbsolutePath, this.lV)
      val process = Process(command).run()
      sys.exit(0)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
  
  def updateUpdater(tempUprJar: File): Unit = {
    println("Updating updater...")

    val updaterJar = new File(tempUprJar.getAbsolutePath.replace(".temp", ""))

    Files.copy(tempUprJar.toPath, updaterJar.toPath, StandardCopyOption.REPLACE_EXISTING)

    if (updaterJar.exists()) {
      println("Updater updated successfully")
    } else {
      println("Failed to update updater")
    }
    if (tempUprJar.delete()) {
      println("Temporary updater file deleted successfully")
    } else {
      println("Failed to delete temporary updater file")
    }
  }
}
