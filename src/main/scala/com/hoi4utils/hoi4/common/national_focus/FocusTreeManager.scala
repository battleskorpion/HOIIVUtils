package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParserException}
import com.hoi4utils.script.PDXReadable
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters
import scala.util.boundary

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
object FocusTreeManager extends LazyLogging with PDXReadable:
  val focusTrees = new ListBuffer[FocusTree]()
  val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  var focusTreeErrors: ListBuffer[String] = ListBuffer.empty

  /* other */
  private val _sharedFocusFiles = new ListBuffer[SharedFocusFile]()

  def observeFocusTrees: ObservableList[FocusTree] = FXCollections.observableArrayList(CollectionConverters.asJava(focusTrees))

  /** Reads all focus trees from the focus trees folder, creating FocusTree instances for each. */
  def read(): Boolean =
    if !HOIIVFiles.Mod.focus_folder.exists || !HOIIVFiles.Mod.focus_folder.isDirectory then
      logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.focus_folder} is not a directory, or it does not exist.")
      false
    else if HOIIVFiles.Mod.focus_folder.listFiles == null || HOIIVFiles.Mod.focus_folder.listFiles.length == 0 then
      logger.warn(s"No focuses found in ${HOIIVFiles.Mod.focus_folder}")
      false
    else
      // create focus trees from files
      HOIIVFiles.Mod.focus_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach: f =>
        if (hasFocusTreeHeader(f))
          new FocusTree(f)
        else
          add(new SharedFocusFile(f))
      System.err.println(s"Shared focus files: ${_sharedFocusFiles.size}")
      System.err.println(s"Shared focuses: ${_sharedFocusFiles.map(_.sharedFocuses.size).sum}")
      true

  /** Clears all focus trees and any other relevant values. */
  override def clear(): Unit =
    focusTrees.clear()
    focusTreeFileMap.clear()

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  def add(focusTree: FocusTree): Iterable[FocusTree] =
    focusTrees += focusTree
    focusTree.focusFile match
      case Some(file) => focusTreeFileMap.put(file, focusTree)
      case None =>
    focusTrees

  /**
   * Adds a shared focus file to the list of shared focus files.
   *
   * @param sharedFocusFile the shared focus file to add
   * @return the updated list of focus trees
   */
  def add(sharedFocusFile: SharedFocusFile): Iterable[SharedFocusFile] =
    _sharedFocusFiles += sharedFocusFile
    _sharedFocusFiles

  /** Returns focus tree corresponding to the tag, if it exists*/
  def get(tag: CountryTag | File): Option[FocusTree] =
    tag match
      case t: CountryTag => focusTrees.find(_.countryTag == t)
      case f: File => focusTreeFileMap.get(f)

  def sharedFocusFiles: Iterable[SharedFocusFile] = _sharedFocusFiles.toList

  def sharedFocusFilesAsPseudoTrees: Iterable[PseudoSharedFocusTree] =
    _sharedFocusFiles.map(sff => PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.toList, s"${sff.fileName}"))

  def sharedFocuses: Iterable[SharedFocus] =
    _sharedFocusFiles.flatMap(_.sharedFocuses)

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
