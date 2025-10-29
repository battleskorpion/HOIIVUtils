package net.updater

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterEach

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.compiletime.uninitialized
import scala.util.Using

class UpdaterTest extends AnyFunSuite with BeforeAndAfterEach:
  var testDir: File = uninitialized
  var tempDir: File = uninitialized

  override def beforeEach(): Unit =
    // Create a unique temp directory for each test
    testDir = Files.createTempDirectory("updater-test").toFile
    tempDir = new File(testDir, "temp")
    tempDir.mkdirs()

  override def afterEach(): Unit =
    // Clean up test directory
    deleteRecursively(testDir)

  private def deleteRecursively(file: File): Unit =
    if file.isDirectory then
      file.listFiles().foreach(deleteRecursively)
    file.delete()

  test("extractZip should handle backslash path separators") {
    // Create a test zip with backslash separators (Windows-style)
    val zipFile = new File(tempDir, "test.zip")
    Using.resource(new ZipOutputStream(new FileOutputStream(zipFile))): zos =>
      // Create entries with backslashes
      zos.putNextEntry(new ZipEntry("folder1\\"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("folder1\\file1.txt"))
      zos.write("test content".getBytes)
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("folder2\\subfolder\\"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("folder2\\subfolder\\file2.txt"))
      zos.write("test content 2".getBytes)
      zos.closeEntry()

    // Extract the zip
    val extractDir = new File(tempDir, "extract")
    extractDir.mkdirs()

    // Use reflection to access private extractZip method
    val extractZipMethod = Updater.getClass.getDeclaredMethod("extractZip", classOf[String], classOf[String])
    extractZipMethod.setAccessible(true)
    extractZipMethod.invoke(Updater, zipFile.getAbsolutePath, extractDir.getAbsolutePath)

    // Verify the structure was created correctly
    assert(new File(extractDir, "folder1").isDirectory, "folder1 should be a directory")
    assert(new File(extractDir, "folder1/file1.txt").isFile, "folder1/file1.txt should be a file")
    assert(new File(extractDir, "folder2/subfolder").isDirectory, "folder2/subfolder should be a directory")
    assert(new File(extractDir, "folder2/subfolder/file2.txt").isFile, "folder2/subfolder/file2.txt should be a file")
  }

  test("extractZip should handle forward slash path separators") {
    // Create a test zip with forward slash separators (Unix-style)
    val zipFile = new File(tempDir, "test-forward.zip")
    Using.resource(new ZipOutputStream(new FileOutputStream(zipFile))): zos =>
      zos.putNextEntry(new ZipEntry("folder1/"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("folder1/file1.txt"))
      zos.write("test content".getBytes)
      zos.closeEntry()

    val extractDir = new File(tempDir, "extract-forward")
    extractDir.mkdirs()

    val extractZipMethod = Updater.getClass.getDeclaredMethod("extractZip", classOf[String], classOf[String])
    extractZipMethod.setAccessible(true)
    extractZipMethod.invoke(Updater, zipFile.getAbsolutePath, extractDir.getAbsolutePath)

    assert(new File(extractDir, "folder1").isDirectory, "folder1 should be a directory")
    assert(new File(extractDir, "folder1/file1.txt").isFile, "folder1/file1.txt should be a file")
  }

  test("extractZip should create parent directories for files") {
    val zipFile = new File(tempDir, "test-nested.zip")
    Using.resource(new ZipOutputStream(new FileOutputStream(zipFile))): zos =>
      // Add a file without explicitly adding its parent directory entries
      zos.putNextEntry(new ZipEntry("deep/nested/path/file.txt"))
      zos.write("nested content".getBytes)
      zos.closeEntry()

    val extractDir = new File(tempDir, "extract-nested")
    extractDir.mkdirs()

    val extractZipMethod = Updater.getClass.getDeclaredMethod("extractZip", classOf[String], classOf[String])
    extractZipMethod.setAccessible(true)
    extractZipMethod.invoke(Updater, zipFile.getAbsolutePath, extractDir.getAbsolutePath)

    assert(new File(extractDir, "deep/nested/path").isDirectory, "Parent directories should be created")
    assert(new File(extractDir, "deep/nested/path/file.txt").isFile, "File should exist")
  }

  test("copyDirectoryRecursively should copy all files and subdirectories") {
    // Create source directory structure
    val sourceDir = new File(testDir, "source")
    sourceDir.mkdirs()
    new File(sourceDir, "file1.txt").createNewFile()

    val subDir = new File(sourceDir, "subdir")
    subDir.mkdirs()
    new File(subDir, "file2.txt").createNewFile()

    val targetDir = new File(testDir, "target")

    // Use reflection to access private method
    val copyMethod = Updater.getClass.getDeclaredMethod("copyDirectoryRecursively", classOf[Path], classOf[Path])
    copyMethod.setAccessible(true)
    copyMethod.invoke(Updater, sourceDir.toPath, targetDir.toPath)

    assert(targetDir.exists(), "Target directory should exist")
    assert(new File(targetDir, "file1.txt").exists(), "file1.txt should be copied")
    assert(new File(targetDir, "subdir").isDirectory, "subdir should be copied")
    assert(new File(targetDir, "subdir/file2.txt").exists(), "subdir/file2.txt should be copied")
  }

  test("safeDeleteRecursively should delete all files and subdirectories") {
    // Create directory structure to delete
    val dirToDelete = new File(testDir, "to-delete")
    dirToDelete.mkdirs()
    new File(dirToDelete, "file1.txt").createNewFile()

    val subDir = new File(dirToDelete, "subdir")
    subDir.mkdirs()
    new File(subDir, "file2.txt").createNewFile()

    assert(dirToDelete.exists(), "Directory should exist before deletion")

    // Use reflection to access private method
    val deleteMethod = Updater.getClass.getDeclaredMethod("safeDeleteRecursively", classOf[Path])
    deleteMethod.setAccessible(true)
    deleteMethod.invoke(Updater, dirToDelete.toPath)

    assert(!dirToDelete.exists(), "Directory should be deleted")
  }

  test("safeDeleteRecursively should not fail if path doesn't exist") {
    val nonExistentPath = new File(testDir, "non-existent").toPath

    val deleteMethod = Updater.getClass.getDeclaredMethod("safeDeleteRecursively", classOf[Path])
    deleteMethod.setAccessible(true)

    // Should not throw exception
    assertDoesNotThrow:
      deleteMethod.invoke(Updater, nonExistentPath)
  }

  test("UpdateException should preserve cause") {
    val cause = new RuntimeException("Original error")
    val updateEx = new UpdateException("Update failed", cause)

    assert(updateEx.getMessage == "Update failed")
    assert(updateEx.getCause == cause)
  }

  test("UpdateException should work without cause") {
    val updateEx = new UpdateException("Update failed")

    assert(updateEx.getMessage == "Update failed")
    assert(updateEx.getCause == null)
  }

  test("Restructuring logic - simulate zip without HOIIVUtils folder") {
    // Create a zip structure that mimics version 14.9.0 (no HOIIVUtils folder)
    val zipFile = new File(tempDir, "no-hoiiv-folder.zip")
    Using.resource(new ZipOutputStream(new FileOutputStream(zipFile))): zos =>
      // Files at root level (not in HOIIVUtils folder)
      zos.putNextEntry(new ZipEntry("maps/"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("demo_mod/"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("target/"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("HOIIVUtils.bat"))
      zos.write("@echo off".getBytes)
      zos.closeEntry()

    val extractDir = new File(tempDir, "extract-no-folder")
    extractDir.mkdirs()

    val extractZipMethod = Updater.getClass.getDeclaredMethod("extractZip", classOf[String], classOf[String])
    extractZipMethod.setAccessible(true)
    extractZipMethod.invoke(Updater, zipFile.getAbsolutePath, extractDir.getAbsolutePath)

    // Verify files were extracted at root level
    assert(new File(extractDir, "maps").isDirectory, "maps should exist at root")
    assert(new File(extractDir, "demo_mod").isDirectory, "demo_mod should exist at root")
    assert(new File(extractDir, "HOIIVUtils.bat").isFile, "HOIIVUtils.bat should exist at root")
    assert(!new File(extractDir, "HOIIVUtils").exists(), "HOIIVUtils folder should not exist initially")
  }

  test("Restructuring logic - simulate zip with HOIIVUtils folder") {
    // Create a zip structure that mimics version 15.0.2+ (has HOIIVUtils folder with backslashes)
    val zipFile = new File(tempDir, "with-hoiiv-folder.zip")
    Using.resource(new ZipOutputStream(new FileOutputStream(zipFile))): zos =>
      zos.putNextEntry(new ZipEntry("HOIIVUtils\\"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("HOIIVUtils\\maps\\"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("HOIIVUtils\\demo_mod\\"))
      zos.closeEntry()

      zos.putNextEntry(new ZipEntry("HOIIVUtils\\HOIIVUtils.bat"))
      zos.write("@echo off".getBytes)
      zos.closeEntry()

    val extractDir = new File(tempDir, "extract-with-folder")
    extractDir.mkdirs()

    val extractZipMethod = Updater.getClass.getDeclaredMethod("extractZip", classOf[String], classOf[String])
    extractZipMethod.setAccessible(true)
    extractZipMethod.invoke(Updater, zipFile.getAbsolutePath, extractDir.getAbsolutePath)

    // Verify HOIIVUtils folder structure was created correctly
    assert(new File(extractDir, "HOIIVUtils").isDirectory, "HOIIVUtils folder should exist")
    assert(new File(extractDir, "HOIIVUtils/maps").isDirectory, "HOIIVUtils/maps should exist")
    assert(new File(extractDir, "HOIIVUtils/demo_mod").isDirectory, "HOIIVUtils/demo_mod should exist")
    assert(new File(extractDir, "HOIIVUtils/HOIIVUtils.bat").isFile, "HOIIVUtils/HOIIVUtils.bat should exist")
  }

  // Helper method for assertDoesNotThrow
  private def assertDoesNotThrow(block: => Unit): Unit =
    try
      block
    catch
      case e: Exception =>
        fail(s"Expected no exception, but got: ${e.getClass.getSimpleName}: ${e.getMessage}")
