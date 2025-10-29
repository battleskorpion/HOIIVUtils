package net.updater

import java.io.{BufferedInputStream, File, FileOutputStream, IOException}
import java.net.URL
import java.util.zip.{ZipEntry, ZipInputStream}
import java.nio.file.{FileVisitResult, Files, Path, Paths, SimpleFileVisitor, StandardCopyOption}
import java.nio.file.attribute.BasicFileAttributes
import scala.sys.process.*
import javax.swing.JOptionPane

object Updater:
  def main(args: Array[String]): Unit =
    println("Updating... This might take a second...")

    try
      runUpdate(args)
    catch
      case e: UpdateException =>
        println(s"ERROR: ${e.getMessage}")
        e.getCause match
          case null => // No underlying cause
          case cause =>
            println(s"Caused by: ${cause.getClass.getSimpleName}: ${cause.getMessage}")
            cause.printStackTrace()
        JOptionPane.showMessageDialog(null, s"Update failed:\n${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
      case e: Exception =>
        println(s"UNEXPECTED ERROR: ${e.getMessage}")
        e.printStackTrace()
        JOptionPane.showMessageDialog(null, s"Unexpected error during update:\n${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)

  private def runUpdate(args: Array[String]): Unit =
    var succ = true
    val testHDir = "C:\\Users\\Chris\\Documents\\GitHub\\HOIIVUtils"
    val testLV = "15.0.2"

    var hDir = testHDir
    var lV = testLV

    if args.length > 0 && args(0) != null then hDir = args(0)
    if args.length > 1 && args(1) != null then lV = args(1)

    // Validate version format
    if !lV.matches("\\d+\\.\\d+\\.\\d+") then
      throw new UpdateException(s"Invalid version format: $lV (expected format: X.Y.Z)")

    if hDir == testHDir && !lV.equals(testLV) then
      throw new UpdateException("Failed to get Users HOIIVUtils location. Please provide the correct path as the first argument.")

    println(s"Received hDir: $hDir")
    println(s"Received lastestVersion: $lV")

    // Validate that hDir exists and is a directory
    val hDirFile = new File(hDir)
    if !hDirFile.exists() then
      throw new UpdateException(s"HOIIVUtils directory does not exist: $hDir")
    if !hDirFile.isDirectory then
      throw new UpdateException(s"HOIIVUtils path is not a directory: $hDir")

    // create a temporary directory to download the latest release to
    val tempDir = new File(s"$hDir${File.separator}temp")

    if tempDir.exists() && tempDir.isDirectory then
      println("Temp folder already exists, will reuse it")
    else if tempDir.exists() && !tempDir.isDirectory then
      throw new UpdateException(s"Cannot create temp directory: a file with that name already exists at ${tempDir.getAbsolutePath}")
    else
      println("Creating temp directory...")
      if !tempDir.mkdirs() then
        throw new UpdateException(s"Failed to create temp directory at ${tempDir.getAbsolutePath}. Check permissions.")

    // download the latest release and extract it
    val zipPath = new File(s"${tempDir.getAbsolutePath}${File.separator}HOIIVUtils.zip")
    val lrUrl = s"https://github.com/battleskorpion/HOIIVUtils/releases/download/$lV/HOIIVUtils.zip"

    println(s"Downloading from: $lrUrl")

    try
      downloadReleaseAsset(lrUrl, zipPath.getAbsolutePath)
    catch
      case e: Exception =>
        throw new UpdateException(s"Failed to download release $lV from GitHub. Check internet connection and verify version exists.", e)

    if !zipPath.exists() then
      throw new UpdateException(s"Download appeared to succeed but zip file not found at ${zipPath.getAbsolutePath}")

    val zipSize = zipPath.length()
    println(s"Downloaded ${zipSize / 1024 / 1024}MB successfully")

    // Validate zip file is not empty and is reasonable size
    if zipSize < 1024 then
      throw new UpdateException(s"Downloaded zip file is suspiciously small (${zipSize} bytes). The download may have failed.")
    if zipSize > 500 * 1024 * 1024 then
      throw new UpdateException(s"Downloaded zip file is suspiciously large (${zipSize / 1024 / 1024}MB). Verify version is correct.")

    println("Extracting zip file...")

    try
      extractZip(zipPath.getAbsolutePath, tempDir.getAbsolutePath)
    catch
      case e: Exception =>
        throw new UpdateException(s"Failed to extract zip file. The download may be corrupted.", e)

    val newHOIIVUtils = s"${tempDir.getAbsolutePath}${File.separator}HOIIVUtils${File.separator}"

    // Check if HOIIVUtils folder exists in the zip, if not create it and move files
    if !new File(newHOIIVUtils).exists() || !new File(newHOIIVUtils).isDirectory then
      println("Zip does not contain HOIIVUtils folder, restructuring...")
      val hoiivUtilsFolder = new File(newHOIIVUtils)
      if !hoiivUtilsFolder.mkdirs() then
        throw new UpdateException(s"Failed to create HOIIVUtils folder at ${hoiivUtilsFolder.getAbsolutePath}")

      try
        // Move all extracted files (except the zip file itself) into HOIIVUtils folder
        val filesToMove = tempDir.listFiles().filter(f => f.getName != "HOIIVUtils.zip" && f.getName != "HOIIVUtils")
        if filesToMove.isEmpty then
          throw new UpdateException("No files found in extracted zip to restructure")

        filesToMove.foreach: file =>
          val dest = new File(hoiivUtilsFolder, file.getName)
          try
            if file.isDirectory then
              copyDirectoryRecursively(file.toPath, dest.toPath)
              safeDeleteRecursively(file.toPath)
            else
              Files.move(file.toPath, dest.toPath)
          catch
            case e: Exception =>
              throw new UpdateException(s"Failed to move ${file.getName} during restructuring", e)

        println("Restructured zip contents into HOIIVUtils folder")
      catch
        case e: UpdateException => throw e
        case e: Exception =>
          throw new UpdateException("Error during zip restructuring", e)

    if !new File(newHOIIVUtils).exists() || !new File(newHOIIVUtils).isDirectory then
      throw new UpdateException(s"Extraction failed: HOIIVUtils folder not found at ${newHOIIVUtils}")

    println("Extracted zip file successfully")

    def replace(pathStr: String): Unit =
      val fCurrent = new File(s"$hDir${File.separator}$pathStr")
      val fNew = new File(s"$newHOIIVUtils${File.separator}$pathStr")

      // Validate source file/directory exists
      if !fNew.exists() then
        throw new UpdateException(s"Source file/directory not found in update: $pathStr at ${fNew.getAbsolutePath}")

      println(s"  Replacing: $pathStr")

      try
        if fNew.isDirectory then
          // For directories, delete old and copy new
          if fCurrent.exists() then
            safeDeleteRecursively(fCurrent.toPath)
            if fCurrent.exists() then
              throw new UpdateException(s"Failed to delete old directory: $pathStr at ${fCurrent.getAbsolutePath}")

          copyDirectoryRecursively(fNew.toPath, fCurrent.toPath)

          if !fCurrent.exists() then
            throw new UpdateException(s"Failed to copy new directory: $pathStr to ${fCurrent.getAbsolutePath}")
        else
          // For files, use REPLACE_EXISTING
          Files.copy(fNew.toPath, fCurrent.toPath, StandardCopyOption.REPLACE_EXISTING)

          if !fCurrent.exists() then
            throw new UpdateException(s"Failed to copy new file: $pathStr to ${fCurrent.getAbsolutePath}")
      catch
        case e: UpdateException => throw e
        case e: Exception =>
          throw new UpdateException(s"Error replacing $pathStr", e)

    println("Updating files...")

    val filesToUpdate = List(
      "maps",
      "demo_mod",
      s"target${File.separator}HOIIVUtils.jar",
      "HOIIVUtils.bat",
      "HOIIVUtils.sh"
    )

    filesToUpdate.foreach: path =>
      replace(path)

    println("Updated all files successfully")

    // Handle updater jar update if it exists
    val uprJarl = s"${File.separator}Updater${File.separator}target${File.separator}updater.jar.temp"
    val uprJar = new File(hDir + uprJarl)
    val uprJarNew = new File(newHOIIVUtils + uprJarl)

    if uprJarNew.exists() then
      println("Updating updater jar...")
      try
        Files.move(uprJarNew.toPath, uprJar.toPath, StandardCopyOption.REPLACE_EXISTING)
        if !uprJar.exists() then
          throw new UpdateException(s"Failed to move updater jar to ${uprJar.getAbsolutePath}")
      catch
        case e: UpdateException => throw e
        case e: Exception =>
          throw new UpdateException("Failed to update updater jar", e)

    // Clean up temp directory
    println("Cleaning up temp directory...")
    try
      safeDeleteRecursively(tempDir.toPath)
      if tempDir.exists() then
        println("Warning: temp directory still exists, but continuing anyway")
    catch
      case e: Exception =>
        println(s"Warning: Failed to delete temp directory: ${e.getMessage}")
        // Don't fail the update if cleanup fails

    // Delete properties file to force regeneration
    val propF = new File(s"$hDir${File.separator}HOIIVUtils.properties")
    if propF.exists() then
      try
        if !propF.delete() then
          println("Warning: Failed to delete properties file, but continuing anyway")
      catch
        case e: Exception =>
          println(s"Warning: Error deleting properties file: ${e.getMessage}")

    println("\n====================================")
    println("Update completed successfully!")
    println("====================================\n")

    // Start HOIIVUtils
    print("Starting HOIIVUtils. This might take a second...")
    try
      val hoiivJar = new File(s"$hDir${File.separator}target${File.separator}HOIIVUtils.jar")
      if !hoiivJar.exists() then
        throw new UpdateException(s"HOIIVUtils.jar not found at ${hoiivJar.getAbsolutePath}")

      val command = Seq("cmd", "/c", "start", "cmd", "/k", "java", "-jar", hoiivJar.getAbsolutePath)
      Process(command).run()
      println(" Started!")
      println("Please close this window")
      sys.exit(0)
    catch
      case e: UpdateException => throw e
      case e: Exception =>
        throw new UpdateException("Failed to start HOIIVUtils", e)

  private def downloadReleaseAsset(url: String, outputFilePath: String): Unit =
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("Accept", "application/octet-stream")
    val in = new BufferedInputStream(connection.getInputStream)
    val out = new FileOutputStream(outputFilePath)
    val buffer = new Array[Byte](1024)
    var bytesRead = 0
    while
      bytesRead = in.read(buffer)
      bytesRead != -1
    do
      out.write(buffer, 0, bytesRead)
    in.close()
    out.close()
    println(s"Downloaded latest release")

  private def extractZip(zipFilePath: String, destDir: String): Unit =
    val buffer = new Array[Byte](1024)
    val zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))
    try
      var entry: ZipEntry = zipInputStream.getNextEntry
      while entry != null do
        // Normalize path separators - replace backslashes with forward slashes
        val normalizedName = entry.getName.replace('\\', '/')
        val newFile = new File(destDir, normalizedName)

        // Check if entry is a directory (either by isDirectory or ends with /)
        val isDir = entry.isDirectory || normalizedName.endsWith("/")

        if isDir then
          newFile.mkdirs()
        else
          new File(newFile.getParent).mkdirs()
          val outputStream = new FileOutputStream(newFile)
          try
            var len = 0
            while
              len = zipInputStream.read(buffer)
              len > 0
            do
              outputStream.write(buffer, 0, len)
          finally
            outputStream.close()
        zipInputStream.closeEntry()
        entry = zipInputStream.getNextEntry
    finally
      zipInputStream.close()

  private def copyDirectoryRecursively(source: Path, target: Path): Unit =
    Files.walkFileTree(source, new SimpleFileVisitor[Path]:
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
        Files.createDirectories(target.resolve(source.relativize(dir)))
        FileVisitResult.CONTINUE

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
        Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
        FileVisitResult.CONTINUE
    )

  private def safeDeleteRecursively(path: Path): Unit =
    if !Files.exists(path) then
      return // Silently return if path doesn't exist

    Files.walkFileTree(path, new SimpleFileVisitor[Path]:
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
        try
          Files.delete(file)
          FileVisitResult.CONTINUE
        catch
          case e: Exception =>
            println(s"Warning: Failed to delete file ${file}: ${e.getMessage}")
            FileVisitResult.CONTINUE

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult =
        if exc != null then
          println(s"Warning: Error accessing directory ${dir}: ${exc.getMessage}")
        try
          Files.delete(dir)
          FileVisitResult.CONTINUE
        catch
          case e: Exception =>
            println(s"Warning: Failed to delete directory ${dir}: ${e.getMessage}")
            FileVisitResult.CONTINUE
    )

// Custom exception class for update-specific errors
class UpdateException(message: String, cause: Throwable = null) extends Exception(message, cause)