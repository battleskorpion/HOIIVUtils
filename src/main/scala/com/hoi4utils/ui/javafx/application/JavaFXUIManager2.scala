package com.hoi4utils.ui.javafx.application

import javafx.scene.Node
import javafx.stage.{DirectoryChooser, Stage}
import com.typesafe.scalalogging.LazyLogging
import com.hoi4utils.extensions.validateFolder

import java.io.File
import scala.util.Try

trait JavaFXUIManager2 extends LazyLogging:
  def open(): Unit

  /**
   * Opens a directory chooser dialog with robust fallback handling.
   *
   * @param ownerNode The JavaFX node that triggers the dialog (used to get the stage)
   * @param preferredInitialDir Optional preferred starting directory
   * @return Option containing the selected directory, or None if cancelled
   */
  protected def selectDirectory(
                                 ownerNode: Node,
                                 preferredInitialDir: Option[File] = None
                               ): Option[File] =
    val stage = Try(ownerNode.getScene.getWindow.asInstanceOf[Stage]).getOrElse(new Stage())

    val chooser = new DirectoryChooser()
    chooser.setTitle("Select Directory")

    // Try to set initial directory with fallback chain
    val initialDir = resolveInitialDirectory(preferredInitialDir)
    initialDir.foreach(chooser.setInitialDirectory)

    Option(chooser.showDialog(stage))

  /**
   * Resolves the initial directory for a file chooser with a robust fallback chain.
   *
   * @param preferredDir The preferred directory to start in
   * @return Option containing a valid directory, using fallbacks if preferred is invalid
   */
  private def resolveInitialDirectory(preferredDir: Option[File]): Option[File] =
    val candidates = Seq(
      preferredDir,
      Some(new File(System.getProperty("user.home"))),
      Some(new File(System.getProperty("user.dir"))),
      Some(new File("."))
    ).flatten

    candidates.find(isValidDirectory) match
      case Some(validDir) => Some(validDir)
      case None =>
        logger.warn("Could not resolve any valid initial directory for file chooser")
        None

  /**
   * Checks if a file is a valid, accessible directory.
   * Handles issues like OneDrive virtual folders.
   *
   * @param file The file to check
   * @return true if the file is a valid, accessible directory
   */
  private def isValidDirectory(file: File): Boolean =
    file.validateFolder("directory").isRight

  /**
   * Validates and normalizes a directory path.
   *
   * @param path The path string to validate
   * @return Option containing the validated File, or None if invalid
   */
  protected def validateDirectoryPath(path: String): Option[File] =
    if path == null || path.trim.isEmpty then None
    else
      val file = new File(path.trim)
      file.validateFolder("path") match
        case Right(validFile) => Some(validFile)
        case Left(error) =>
          logger.debug(s"Path validation failed: $error")
          None

  /**
   * Constructs a commonly used path with proper path separator handling.
   *
   * @param segments Path segments to join
   * @return The constructed path as a File
   */
  protected def buildPath(segments: String*): File =
    new File(segments.mkString(File.separator))

  /**
   * Gets a standard directory with fallback handling.
   * Common directories: user.home, user.dir, java.io.tmpdir
   *
   * @param propertyName The system property name
   * @return Option containing the directory if it exists
   */
  protected def getSystemDirectory(propertyName: String): Option[File] =
    Option(System.getProperty(propertyName))
      .flatMap(path => validateDirectoryPath(path))