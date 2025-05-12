package net.updater

import java.io.{BufferedInputStream, File, FileOutputStream, IOException}
import java.util.zip.{ZipEntry, ZipInputStream}
import java.nio.file.{FileVisitResult, Files, Path, Paths, SimpleFileVisitor, StandardCopyOption}
import scala.collection.JavaConverters.asScalaIteratorConverter
import java.nio.file.attribute.BasicFileAttributes
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

  def update(hDir: String, lV: String): Boolean = {
    val succ = true
    println("Updating...")

    val tempDir = new File(hDir + File.separator + "temp")
    if (tempDir.exists() && tempDir.isDirectory) {
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

    val lrUrl = s"https://github.com/battleskorpion/HOIIVUtils/releases/download/$lV/HOIIVUtils.zip"
    downloadReleaseAsset(lrUrl, zipPath.getAbsolutePath)

    if (!zipPath.exists()) {
      println("zip file failed to download")
      // JOptionPane popup
      val succ = false
    } else {
      println("zip file downloaded")
    }

    extractZip(zipPath.getAbsolutePath, tempDir.getAbsolutePath)

    val newHOIIVUtils = tempDir.getAbsolutePath + File.separator + "HOIIVUtils" + File.separator

    if (!new File(newHOIIVUtils).exists() && !new File(newHOIIVUtils).isDirectory) {
      println("temp dir failed to extract")
      // JOptionPane popup
      val succ = false
    } else {
      println("temp dir extracted")
    }


    val demomodDirOld = new File(hDir + File.separator + "demo_mod")
    val demomodDirNew = new File(newHOIIVUtils + "demo_mod")

    safeDeleteRecursively(demomodDirOld.toPath)
    copyDirectoryRecursively(demomodDirNew.toPath, demomodDirOld.toPath)
    if (demomodDirOld.exists()) {
      println("demo_mod new copied")
    } else {
      println("demo_mod new failed to copy")
      // JOptionPane popup
      val succ = false
    }

    val mapsDirOld = new File(hDir + File.separator + "maps")
    val mapsDirNew = new File(newHOIIVUtils + "maps")

    safeDeleteRecursively(mapsDirOld.toPath)
    copyDirectoryRecursively(mapsDirNew.toPath, mapsDirOld.toPath)
    if (mapsDirOld.exists()) {
      println("maps new copied")
    } else {
      println("maps new failed to copy")
      // JOptionPane popup
      val succ = false
    }

    val hJarFileOld = new File(hDir + File.separator + "target" + File.separator + "HOIIVUtils.jar")
    val hJarFileNew = new File(newHOIIVUtils + "target" + File.separator + "HOIIVUtils.jar")
    Files.copy(hJarFileNew.toPath, hJarFileOld.toPath, StandardCopyOption.REPLACE_EXISTING) //scary
    if (hJarFileOld.exists()) {
      println("HUtils jar file copied")
    } else {
      println("HUtils jar file failed to copy")
      // JOptionPane popup
      val succ = false
    }

    val hBatFileOld = new File(hDir + File.separator + "HOIIVUtils.bat")
    val hBatFileNew = new File(newHOIIVUtils + "HOIIVUtils.bat")
    Files.copy(hBatFileNew.toPath, hBatFileOld.toPath, StandardCopyOption.REPLACE_EXISTING)
    if (hBatFileOld.exists()) {
      println("bat file copied")
    } else {
      println("mod bat file failed to copy")
      // JOptionPane popup
      val succ = false
    }

    val hSHFileOld = new File(hDir + File.separator + "HOIIVUtils.sh")
    val hSHFileNew = new File(newHOIIVUtils + "HOIIVUtils.sh")
    Files.copy(hSHFileNew.toPath, hSHFileOld.toPath, StandardCopyOption.REPLACE_EXISTING)
    if (hSHFileOld.exists()) {
      println("sh file copied")
    } else {
      println("sh file failed to copy")
    }
    val tempJar = new File(hDir + File.separator + "Updater" + File.separator + "target" +
      File.separator + "updater.jar.temp")
    val updaterJarNew = new File(newHOIIVUtils + "updater.jar")
//    Files.copy(updaterJarNew.toPath, tempJar.toPath, StandardCopyOption.REPLACE_EXISTING)
//    if (tempJar.exists()) {
//      println("updater jar file copied")
//    } else {
//      println("updater jar file failed to copy")
//       JOptionPane popup
//      val succ = false
//    }

    safeDeleteRecursively(tempDir.toPath)

    if (tempDir.exists()) {
      println("temp dir failed to delete")
      // JOptionPane popup
      val succ = false
    } else {
      println("temp dir deleted")
    }

    if (succ) {
      println("Update successful")
      // JOptionPane popup
    } else {
      println("Update failed")
      // JOptionPane popup
    }
    succ
  }

  private def restart(hDir: String): Unit = {
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

  private def downloadReleaseAsset(url: String, outputFilePath: String): Unit = {
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
    if (Files.exists(path)) {
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
      println(s"Deleted directory recursively: $path")
    } else {
      println(s"Directory does not exist: $path")
    }
  }
}