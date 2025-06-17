package com.hoi4utils

import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.effect.EffectDatabase
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.hoi4.modifier.ModifierDatabase
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import com.hoi4utils.parser.Parser.parserFileErrors
import com.hoi4utils.localization.LocalizationManager.{loadedLocFiles, localizationErrors, getOrCreate}
import com.hoi4utils.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4.country.Country.countryErrors
import com.hoi4utils.hoi4.focus.Focus.focusErrors
import com.hoi4utils.hoi4.focus.FocusTree.focusTreeFileErrors
import com.hoi4utils.hoi4.idea.Idea.ideaErrors
import com.hoi4utils.hoi4.idea.IdeaFile.ideaFileErrors
import com.map.ResourcesFile.resourcesFileErrors
import com.map.State.stateErrors
import com.hoi4utils.StateFilesWatcher.statesThatChanged
import com.hoi4utils.ui.MenuController
import com.map.{ResourcesFile, State}
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label

import java.beans.PropertyChangeListener
import java.util.Properties
import scala.collection.mutable.ListBuffer

/**
 * Loads in the mod and hoi4 together
 *
 * TODO: @Skorp Update the ChangeNotifier and FileWatcher or delete this todo if working as intended
 */
class PDXLoader extends LazyLogging {
  private val changeNotifier = new PublicFieldChangeNotifier(this.getClass)
  
  def load(hProperties: Properties, loadingLabel: Label): Unit = {
    implicit val properties: Properties = hProperties
    implicit val label: Label = loadingLabel

    MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
    ModifierDatabase.init()
    MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
    EffectDatabase.init()

    MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
    val hoi4Path = hProperties.getProperty("hoi4.path")
    val modPath = hProperties.getProperty("mod.path")
    HOIIVFiles.setNewFiles(hoi4Path, modPath)
    new StateFilesWatcher()


    changeNotifier.checkAndNotifyChanges()

    MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
    if getOrCreate(() => new EnglishLocalizationManager).reload() then
      logger.info("Localization loaded successfully.")
      hProperties.setProperty("valid.localization", "true")
    else
      logger.error("Failed to load localization.")
      hProperties.setProperty("valid.localization", "false")
    
    
    List (
      Interface,
      ResourcesFile,
      State,
      Country,
      CountryTag,
      FocusTree,
      IdeaFile,
    ).foreach(readPDX)
  }

  private def readPDX(pdx: PDXReadable)(implicit properties: Properties, label: Label): Unit = {
    MenuController.updateLoadingStatus(label, s"Loading ${pdx.name} files...")
    if pdx.read() then properties.setProperty(s"valid.${pdx.name}", "true")
    else properties.setProperty(s"valid.${pdx.name}", "false")
  }

  def clearPDX(): Unit = {
    IdeaFile.clear()
    FocusTree.clear()
    CountryTag.clear()
    Country.clear()
    State.clear()
    ResourcesFile.clear()
    Interface.clear()
  }

  def clearLB(): Unit = {
    ListBuffer (
      localizationErrors,
      parserFileErrors,
      interfaceErrors,
      countryErrors,
      focusErrors,
      focusTreeFileErrors,
      ideaErrors,
      ideaFileErrors,
      resourcesFileErrors,
      stateErrors,
      loadedLocFiles
    ).foreach(_.clear())
  }

  def closeDB(): Unit = {
    ModifierDatabase.close()
    EffectDatabase.close()
  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.removePropertyChangeListener(listener)
  }
}