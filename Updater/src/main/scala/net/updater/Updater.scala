package net.updater

import java.io.{BufferedInputStream, File, FileOutputStream}
import java.net.URL
import javax.swing.JOptionPane
import scala.sys.process.*

object Updater {
  def main(args: Array[String]): Unit = {
    Thread.sleep(10000)
    //    if (args.isEmpty) {
    //      println("No arguments provided!")
    //      sys.exit(1)
    //    }

    // Retrieve the passed argument (hDir)
    //    val hDir = args(0)
    val hDir = "C:\\Users\\User\\Documents\\GitHub\\HOIIVUtils"
//    val lV = args(1)
    val lV = "14.1.1"
    println(s"Received hDir: $hDir")
    println(s"Received lastestVersion: $lV")

    update(hDir, lV)
    restart(hDir)
  }

  def update(hDir: String, lV: String): Unit = {
    val succ = true
    println("Updating...")
    // delete HOIIVUtils.jar
    val oJar = new File(hDir + File.separator + "target" + File.separator + "HOIIVUtils.jar")
    if (oJar.exists() && oJar.isFile) {
      if (oJar.delete()) {
        println("Old Jar deleted")
      } else {
        println("Old Jar failed to delete")
      }
    } else {
      println("Old Jar doesn't exsist or isn't a file")
      // JOptionPane popup
      val succ = false
    }

    val tempDir = new File(hDir + File.separator + "temp")
    if (tempDir.exists() || tempDir.isDirectory) {
      println("temp already made")
    } else {
      if (tempDir.mkdirs()) {
        println("made temp dir")
      } else {
        println("Failed to make temp dir")
        // JOptionPane popup
        val succ = false
      }
    }

    println("output path: " + tempDir.getAbsolutePath)

    val zipPath = new File(tempDir.getAbsolutePath + File.separator + "HOIIVUtils.zip")
    if (zipPath.exists()) {
      if (zipPath.exists()) {
        if (zipPath.delete()) {
          println("zip deleted")
        } else {
          println("zip failed deleting")
        }
      } else {
        println("zip doesn't exsist")
      }
    }
    val lrUrl = s"https://github.com/battleskorpion/HOIIVUtils/releases/download/$lV/HOIIVUtils.zip"
    downloadReleaseAsset(lrUrl, zipPath.getAbsolutePath)

    if (zipPath.exists()) {
      if (zipPath.delete()) {
        println("zip deleted")
      } else {
        println("zip failed deleting")
      }
    } else {
      println("zip doesn't exsist")
    }

    if (tempDir.exists()) {
      if (tempDir.delete()) {
        println("temp dir deleted")
      } else {
        println("temp dir failed deleting")
      }
    } else {
      println("temp dir doesn't exsist")
    }

  }

  def restart(hDir: String): Unit = {
    try {
      val updaterJar = hDir + File.separator + "target" + File.separator + "HOIIVUtils.jar"
      println("updaterJar: " + updaterJar)
      val command = Seq("java", "-jar", updaterJar)
//      val process = Process(command).run()
      sys.exit(0)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def downloadReleaseAsset(url: String, outputFilePath: String): Unit = {
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("Accept", "application/octet-stream")

    val in = new BufferedInputStream(connection.getInputStream)
    val out = new FileOutputStream(outputFilePath)

    val buffer = new Array[Byte](1024)
    var bytesRead = 0
    while ( {
      bytesRead = in.read(buffer); bytesRead != -1
    }) {
      out.write(buffer, 0, bytesRead)
    }

    in.close()
    out.close()
    println(s"Downloaded release asset to $outputFilePath")
  }
}