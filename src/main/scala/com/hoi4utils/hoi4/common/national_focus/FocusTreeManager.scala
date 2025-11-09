package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParserException}
import com.hoi4utils.script.{FocusTreeErrorGroup, PDXError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.ExecutionContext
import scala.jdk.javaapi.CollectionConverters
import scala.util.boundary

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
object FocusTreeManager extends LazyLogging with PDXReadable:
  override val cleanName: String = "FocusTrees"
  val focusTrees = new mutable.HashSet[FocusTree]()
  val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  var focusTreeErrors: ListBuffer[FocusTreeErrorGroup] = ListBuffer.empty

  /* other */
  private val _sharedFocusFiles = new mutable.HashSet[SharedFocusFile]()

  def observeFocusTrees: ObservableList[FocusTree] = FXCollections.observableArrayList(CollectionConverters.asJava(focusTrees))

  /** Reads all focus trees from the focus trees folder, creating FocusTree instances for each. */
  def read(): Boolean = {
    val modFocusFolder = HOIIVFiles.Mod.focus_folder
    if !modFocusFolder.exists || !modFocusFolder.isDirectory then
      logger.error(s"In ${this.getClass.getSimpleName} - ${modFocusFolder} is not a directory, or it does not exist.")
      false
    else if modFocusFolder.listFiles == null || modFocusFolder.listFiles.length == 0 then
      logger.warn(s"No focuses found in ${modFocusFolder}")
      false
    else
      // create focus trees from files
      modFocusFolder.listFiles().filter(_.getName.endsWith(".txt")).par.foreach: f =>
        if (hasFocusTreeHeader(f))
          new FocusTree(f)
        else
          add(new SharedFocusFile(f))
      System.err.println(s"Shared focus files: ${_sharedFocusFiles.size}")
      System.err.println(s"Shared focuses: ${_sharedFocusFiles.map(_.sharedFocuses.size).sum}")
      true
  }

  /** Clears all focus trees and any other relevant values. */
  override def clear(): Unit =
    focusTrees.clear()
    focusTreeFileMap.clear()

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  def add(focusTree: FocusTree): Set[FocusTree] =
    focusTrees += focusTree
    focusTree.focusFile match
      case Some(file) => focusTreeFileMap.put(file, focusTree)
      case None =>
    focusTrees.toSet

  /**
   * Adds a shared focus file to the list of shared focus files.
   *
   * @param sharedFocusFile the shared focus file to add
   * @return the updated list of focus trees
   */
  def add(sharedFocusFile: SharedFocusFile): Set[SharedFocusFile] =
    _sharedFocusFiles += sharedFocusFile
    _sharedFocusFiles.toSet

  /** Returns focus tree corresponding to the tag, if it exists*/
  def get(tag: CountryTag | File): Option[FocusTree] =
    tag match
      case t: CountryTag => focusTrees.find(_.countryTag == t)
      case f: File => focusTreeFileMap.get(f)

  def sharedFocusFiles: Set[SharedFocusFile] = _sharedFocusFiles.toSet

  def sharedFocusFilesAsPseudoTrees: Set[PseudoSharedFocusTree] =
    _sharedFocusFiles.map(sff => PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.toList, s"${sff.fileName}")).toSet

  def sharedFocuses: Set[SharedFocus] =
    _sharedFocusFiles.flatMap(_.sharedFocuses).toSet

  def hasFocusTreeHeader(file: File): Boolean =
    val parser = Parser(file)
    try {
      val rootNode = parser.parse
      rootNode.contains(focusTreeIdentifier)
    } catch {
      case e: ParserException =>
        logger.error(s"Error parsing file ${file.getName} when determining if it is a focus tree file: ${e.getMessage}")
        false
    }
