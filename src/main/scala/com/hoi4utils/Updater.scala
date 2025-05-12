package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVUtils.HOIIVUTILS_VERSION

import scala.io.Source
import scala.math.Ordered.orderingToOrdered

class Updater {
  def updateCheck(v: String): Boolean = {
    println("Checking for updates...")

    println("Current Version: " + v)

    // get the latest release version fom github api / or just cheat and get the redirect from the latest release page where it has the version number at the end of the url

    val lV = getLatestVersion("battleskorpion/HOIIVUtils")
    println("Latest Version: " + lV)
    if (lV == "0.0.0") {
      println("Failed to fetch latest version")
      return false
    }

    val (maV: Int, miV: Int, paV: Int) = (ma(v), mi(v), pa(v))
    val (lmaV: Int, lmiV: Int, lpaV: Int) = (ma(lV), mi(lV), pa(lV))
    println("Current Version: " + maV + "." + miV + "." + paV)
    println("Latest Version: " + lmaV + "." + lmiV + "." + lpaV)

    println("maV: " + maV)
    println("miV: " + miV)
    println("buV: " + paV)
    println("lmaV: " + lmaV)
    println("lmiV: " + lmiV)
    println("lbuV: " + lpaV)

    if ((lmaV, lmiV, lpaV) > (maV, miV, paV)) {
      println("Update found")
      true
    } else {
      println("No updates found")
      false
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

  private def getLatestVersion(repo: String): String = {
    try {
      val apiUrl = s"https://api.github.com/repos/$repo/releases/latest"
      val response = Source.fromURL(apiUrl).mkString
      val json = ujson.read(response)
      json("tag_name").str
    } catch {
      case e: Exception =>
        println(s"Failed to fetch latest version: ${e.getMessage}")
        "0.0.0" // Return a default version string that will be considered older than any valid version
    }
  }
  
  def update(): Unit = {
    println("Updating...")
    // start a new process with the updater.jar file
  }
  
  def updateUpdater(): Unit = {
    println("Updating updater...")
    // check if a temp new downloaded updater.jar exists (updater.jar.temp), 
    // if it does, delete the current updater.jar and rename the temp file to updater.jar
    // before this, the update method should have downloaded the new updater.jar file
    // and named it to updater.jar.temp
  }
}
