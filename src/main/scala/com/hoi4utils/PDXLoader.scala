package com.hoi4utils

import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.effect.EffectDatabase
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.hoi4.modifier.ModifierDatabase
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import com.hoi4utils.ui.MenuController
import com.hoi4utils.HOIIVFiles.isValidDirectory
import com.map.{ResourcesFile, State}
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label

import java.awt.EventQueue
import java.beans.PropertyChangeListener
import java.io.File
import java.util.Properties

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
    LocalizationManager.getOrCreate(() => new EnglishLocalizationManager).reload()
    
    List (
      Interface,
      State,
      Country,
      CountryTag,
      FocusTree,
      IdeaFile,
      ResourcesFile
    ).foreach(readPDX)
  }
  
  private def readPDX(pdx: PDXReadable)(implicit properties: Properties, label: Label): Unit = {
    val property = s"valid.${pdx.name}"

    MenuController.updateLoadingStatus(label, s"Loading ${pdx.name} files...")
    try
      if (pdx.read()) properties.setProperty(property, "true")
      else
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.name}")
    catch
      case e: Exception =>
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.name}", e)
  }

  def clearPDX(): Unit = {
    Interface.clear()
    State.clear()
    Country.clear()
    CountryTag.clear()
    FocusTree.clear()
    IdeaFile.clear()
    ResourcesFile.clear()
  }


  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.removePropertyChangeListener(listener)
  }
}