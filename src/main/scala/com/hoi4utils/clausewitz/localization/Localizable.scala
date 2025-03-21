package com.hoi4utils.clausewitz.localization

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.ui.JavaFXUIManager
import org.jetbrains.annotations.NotNull

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.{HashMap, Map}

/**
 * Interface for Clausewitz-localizable objects. This interface is specifically related to
 * Clausewitz-engine localization, and not general localization/i18n
 */
trait Localizable {
  /**
   * Default method to get the number of localizable properties.
   *
   * @note this method should not usually be overwritten.
   * @return the number of localizable properties.
   */
  def numLocalizableProperties: Int = getLocalizableProperties.size

  /**
   * Default method to get the localizable property identifiers and keys.
   *
   * @return a map of localizable property identifiers and keys.
   */
  def getLocalizableProperties: mutable.Map[Property, String]
  
  def localizableProperty(property: Property): Option[String] = getLocalizableProperties.get(property)

  def getLocalizationKeys: Iterable[String] = getLocalizableProperties.values

  /**
   * Add a localizable property using the specified key.
   *
   * @param property        the localized property to add.
   * @param localizationKey the property localization key to add.
   */
  def addLocalizableProperty(property: Property, localizationKey: String): Unit = {
    getLocalizableProperties.put(property, localizationKey)
  }

  /**
   * Default method to clear the localizable properties.
   */
  def clearLocalizableProperties(): Unit = {
    getLocalizableProperties.clear()
  }

  def getLocalization: Iterable[Localization] = LocalizationManager.getAll(getLocalizationKeys)

  /**
   * Gets the localization for the given property.
   *
   * @param property the localizable property to get localization for.
   * @return the localization for the given property.
   */
  def localization(property: Property): Option[Localization] = {
    val key = getLocalizableProperties.get(property)
    key match {
      case Some(k) => LocalizationManager.get(k)
      case None => None
    }
  }

  /**
   * Gets the localization text for the given property.
   *
   * @param property the localizable property to get localization text for.
   * @return the localization text for the given property, or a placeholder if the localization
   *         is null.
   */
  def localizationText(property: Property): String = {
    localization(property) match {
      case Some(l) => l.text
      case None => "[null]"
    }
  }

  def localizationStatus(property: Property): Localization.Status = {
    localization(property) match {
      case Some(l) => l.status
      case None => Localization.Status.MISSING
    }
  }

  // todo may bring back at some point
  //	/**
  //	 * Sets the localization for the given property to the new value, or creates a new localization if none exists.
  //	 * @param property the localizable property to set.
  //	 * @param text the new localization text.
  //\	 */
  //	default void setLocalization(Property property, String text) {
  //		setLocalization(property, text, primaryLocalizationFile());
  //	}

  def primaryLocalizationFile: Option[File] = {
    val localizableGroup = getLocalizableGroup
    localizableGroup.flatMap(ll => ll.getLocalizationKeys).map(LocalizationManager.getLocalizationFile).filter(_ != null).headOption match {
      case Some(f) => Some(f)
      case None => None
    }
  }

  /**
   * Gets the localizable group of objects that this object is a part of.
   *
   * @return the localizable group of objects.
   */
  def getLocalizableGroup: Iterable[_ <: Localizable]

  /**
   * Asks the user to determine the localization file to use.
   *
   * @return the localization file to use.
   */
  def askUserForLocalizationFile: File = {
    val initialDirectory = HOIIVFiles.Mod.localization_folder
    var file = JavaFXUIManager.openChooser(initialDirectory, false)
    if (file == null) file = new File(HOIIVFiles.Mod.localization_folder, "HOIUtils_extra_localization.yml")
    file
  }

  /**
   * Sets the localization for the given property to the new value, or creates a new localization if none exists.
   *
   * @param property the localizable property to set.
   * @param text     the new localization text.
   * @param file     the file the localization belongs in.
   */
  def setLocalization(property: Property, text: String, file: File): Unit = {
    setLocalization(property, null, text, file)
  }

  /**
   * Sets the localization for the given property to the new value, or creates a new localization if none exists.
   *
   * @param property the localizable property to set.
   * @param version  the localization version number
   * @param text     the new localization text.
   * @param file     the file the localization belongs in.
   */
  def setLocalization(property: Property, version: Integer, text: String, file: File): Unit = {
    val key = getLocalizableProperties.get(property)
    key match {
      case Some(k) =>
        LocalizationManager.get.setLocalization(k, version, text, file)
      case None => //LocalizationManager.get.addLocalization(text, file)
    }
  }

  /**
   * Sets the localization for the given property to the new value.
   *
   * @param property the localizable property to set.
   * @param text     the new localization text.
   */
  def replaceLocalization(property: Property, text: String): Unit = {
    val key = getLocalizableProperties.get(property)
    key match {
      case Some(k) => LocalizationManager.get.replaceLocalization(k, text)
      case None =>
    }
  }
  
}
