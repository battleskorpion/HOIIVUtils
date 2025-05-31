package net.updater

import java.io.{BufferedInputStream, File, FileOutputStream, IOException}
import java.net.URL
import java.util.zip.{ZipEntry, ZipInputStream}
import java.nio.file.{FileVisitResult, Files, Path, Paths, SimpleFileVisitor, StandardCopyOption}
import java.nio.file.attribute.BasicFileAttributes
import scala.sys.process.*
 import javax.swing.JOptionPane

object Updater {
  var succ = true
  def main(args: Array[String]): Unit = {
//    // test:
    println("Updating... This might take a second...")
    var hDir = "C:\\Users\\User\\Documents\\GitHub\\HOIIVUtils"
    var lV = "14.9.0"
    if (args.length > 0 && args(0) != null) hDir = args(0)
    if (args.length > 1 && args(1) != null) lV = args(1)
    if (hDir == "C:\\Users\\User\\Documents\\GitHub\\HOIIVUtils") {
      println("Fatal error: Failed to get Users HOIIVUtils location")
      println("Stopping Update, Please let us know in the discord if you see this")
      System.exit(1)
    }
    println(s"Received hDir: $hDir")
    println(s"Received lastestVersion: $lV")
    // create a temporary directory to download the latest release to
    val tempDir = new File(s"$hDir${File.separator}temp")
    if (tempDir.exists() && tempDir.isDirectory)
      println("temp folder already made")
      else
      if (!tempDir.mkdirs())
        println("Failed to make temp dir")
        succ = false
    // download the latest release and extract it
    val zipPath = new File(s"${tempDir.getAbsolutePath}${File.separator}HOIIVUtils.zip")
    val lrUrl = s"https://github.com/battleskorpion/HOIIVUtils/releases/download/$lV/HOIIVUtils.zip"
    println(s"Downloading latest release...")
    downloadReleaseAsset(lrUrl, zipPath.getAbsolutePath)
    if (!zipPath.exists())
      println("zip file failed to download")
      succ = false

    println("Extracting zip file...")
    extractZip(zipPath.getAbsolutePath, tempDir.getAbsolutePath)
    val newHOIIVUtils = s"${tempDir.getAbsolutePath}${File.separator}HOIIVUtils${File.separator}"
    if (!new File(newHOIIVUtils).exists() && !new File(newHOIIVUtils).isDirectory)
      println("zip file failed to extract")
      succ = false
    println("Extracted zip file")
    def replace(pathStr: String): Boolean = {
      val fCurrent = new File(s"$hDir${File.separator}$pathStr")
      val fNew = new File(s"$newHOIIVUtils${File.separator}$pathStr")
      if (fCurrent.isDirectory)
        safeDeleteRecursively(fCurrent.toPath) // in case of any child items that are not replaced
        if (fCurrent.exists()) { println(s"$pathStr directory failed to delete"); return false }
        copyDirectoryRecursively(fNew.toPath, fCurrent.toPath)
        if (!fCurrent.exists()) { println(s"$pathStr directory failed to copy"); false } else true
      else
        val f = Files.copy(fNew.toPath, fCurrent.toPath, StandardCopyOption.REPLACE_EXISTING) //scary
        if (!fCurrent.exists() && f != fCurrent.toPath) { println(s"$pathStr file failed to copy"); false } else true
    }

    println("Updating files...")
    List("maps", "demo_mod", s"target${File.separator}HOIIVUtils.jar", "HOIIVUtils.bat", "HOIIVUtils.sh")
      .foreach(path => if (!replace(path)) { succ = false; throw new Exception(s"Failed to replace $path"); System.exit(1) })
    println("Updated files")
    val uprJarl = s"${File.separator}Updater${File.separator}target${File.separator}updater.jar.temp"
    val uprJar = new File(hDir + uprJarl)
    val uprJarNew = new File(newHOIIVUtils + uprJarl)
    if (uprJarNew.exists())
      Files.move(uprJarNew.toPath, uprJar.toPath)
      if (!uprJar.exists())
        println("updater jar file failed to copy")
        succ = false

    safeDeleteRecursively(tempDir.toPath)
    if (tempDir.exists())
      println("temp dir failed to delete")
      succ = false

    val propF = new File(s"$hDir${File.separator}HOIIVUtils.properties")
    if (!propF.delete()) println("Failed to delete properties file")

    if (succ)
      println("Update successful: ")
    else
      println("Update failed: ")
      JOptionPane.showMessageDialog(null, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE)

    print("Starting HOIIVUtils. This might take a second...")
    try
      val updaterJar = s"$hDir${File.separator}target${File.separator}HOIIVUtils.jar"
      val command = Seq("cmd", "/c", "start", "cmd", "/k", "java", "-jar", updaterJar)
      Process(command).run()
      println("Please close this window")
      sys.exit(0)
    catch
      case e: Exception =>
        e.printStackTrace()
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
    println(s"Downloaded latest release")
  }

  def extractZip(zipFilePath: String, destDir: String): Unit = {
    val buffer = new Array[Byte](1024)
    val zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))
    try {
      var entry: ZipEntry = zipInputStream.getNextEntry
      while (entry != null) {
        val newFile = new File(destDir, entry.getName)
        if (entry.isDirectory) {
          newFile.mkdirs()
        } else {
          new File(newFile.getParent).mkdirs()
          val outputStream = new FileOutputStream(newFile)
          try {
            var len = 0
            while ( {
              len = zipInputStream.read(buffer); len > 0
            }) {
              outputStream.write(buffer, 0, len)
            }
          } finally {
            outputStream.close()
          }
        }
        zipInputStream.closeEntry()
        entry = zipInputStream.getNextEntry
      }
    } finally {
      zipInputStream.close()
    }
  }

  def copyDirectoryRecursively(source: Path, target: Path): Unit = {
    Files.walkFileTree(source, new SimpleFileVisitor[Path]() {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.createDirectories(target.resolve(source.relativize(dir)))
        FileVisitResult.CONTINUE
      }
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
        FileVisitResult.CONTINUE
      }
    })
  }

  def safeDeleteRecursively(path: Path): Unit = {
    if (!Files.exists(path)) println(s"Directory does not exist: $path")
    Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }
}