package com.hoi4utils.hoi4mod.common.national_focus

import com.hoi4utils.hoi4mod.common.country_tags.CountryTag
import com.hoi4utils.main.HOIIVFiles
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
object FocusTreesManager extends LazyLogging with PDXReadable:
  val focusTrees = new ListBuffer[FocusTree]()
  val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  var focusTreeErrors: ListBuffer[String] = ListBuffer.empty

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
        new FocusTree(f)
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

  /** Returns focus tree corresponding to the tag, if it exists*/
  def get(tag: CountryTag): Option[FocusTree] = boundary {
    focusTrees.foreach: tree =>
      tree.countryTag match
        case Some(t) if tag.equals(t.tag) => boundary.break(Some(tree))
        case _ => // Do nothing
    None
  }