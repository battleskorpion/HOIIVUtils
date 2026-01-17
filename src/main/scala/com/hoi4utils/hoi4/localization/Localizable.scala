package com.hoi4utils.hoi4.localization

import com.hoi4utils.exceptions.LocalizationPropertyException
import com.hoi4utils.hoi4.localization.Property.{DESCRIPTION, NAME}
import com.hoi4utils.hoi4.localization.service.LocalizationService
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.ui.javafx.application.JavaFXUIManager
import zio.{Unsafe, ZIO}

import java.io.File
import scala.collection.mutable

/**
 * Interface for Clausewitz-localizable objects. This interface is specifically related to
 * Clausewitz-engine localization, and not general localization/i18n
 */
trait Localizable {

  private def runZIO[A](effect: ZIO[LocalizationService, Throwable, A]): A =
    Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.getActiveRuntime.unsafe.run(effect).getOrThrow()
    }

  /**
   * Default method to get the number of localizable properties.
   *
   * @note this method should not usually be overwritten.
   * @return the number of localizable properties.
   */
  def numLocalizableProperties: Int = localizableProperties.size

  /**
   * Default method to get the localizable property identifiers and keys.
   *
   * @return a map of localizable property identifiers and keys.
   */
  def localizableProperties: Map[Property, String]

  def localizableProperty(property: Property): Option[String] = localizableProperties.get(property)

  def getLocalizationKeys: Iterable[String] = localizableProperties.values

//  /**
//   * Add a localizable property using the specified key.
//   *
//   * @param property        the localized property to add.
//   * @param localizationKey the property localization key to add.
//   */
//  def addLocalizableProperty(property: Property, localizationKey: String): Unit = {
//    localizableProperties.put(property, localizationKey)
//  }

//  /**
//   * Default method to clear the localizable properties.
//   */
//  def clearLocalizableProperties(): Unit =
//    localizableProperties.clear()

  def getLocalization: Iterable[Localization] = {
    runZIO(ZIO.serviceWithZIO[LocalizationService](_.getAll(getLocalizationKeys)))
  }

  /**
   * Gets the localization for the given property.
   *
   * @param property the localizable property to get localization for.
   * @return the localization for the given property.
   */
  def localization(property: Property): Option[Localization] = localizableProperties.get(property) match
    case Some(k) => runZIO(ZIO.serviceWithZIO[LocalizationService](_.getLocalization(k)))
    case None => None

  /**
   * Gets the localization text for the given property.
   *
   * @param property the localizable property to get localization text for.
   * @return the localization text for the given property, or a placeholder if the localization
   *         is null.
   */
  def localizationText(property: Property): Option[String] = localization(property) match
    case Some(l) => Some(l.text)
    case None => None

  def locName: Option[String] = this.localizationText(NAME)

  def localizationStatus(property: Property): Localization.Status = localization(property) match
    case Some(l) => l.status
    case None => Localization.Status.MISSING

  def localizationStatuses: Map[Property, Localization.Status] =
    localizableProperties.iterator.map(m => (m._1, localizationStatus(m._1))).toMap

  // todo may bring back at some point
  //	/**
  //	 * Sets the localization for the given property to the new value, or creates a new localization if none exists.
  //	 * @param property the localizable property to set.
  //	 * @param text the new localization text.
  //\	 */
  //	default void setLocalization(Property property, String text) {
  //		setLocalization(property, text, primaryLocalizationFile());
  //	}

  def primaryLocalizationFile: ZIO[LocalizationService, Nothing, Option[File]] =
    val keys = getLocalizableGroup.flatMap(_.getLocalizationKeys).toList
    ZIO.serviceWithZIO[LocalizationService] { service =>
      ZIO.foreach(keys)(service.getLocalizationFile)
        .map(_.find(_ != null))
    }
    
//    val localizableGroup = getLocalizableGroup
//    localizableGroup.flatMap(ll => ll.getLocalizationKeys).map(ZIO.serviceWithZIO[LocalizationService](_.getLocalizationFile)).find(_ != null) match
//      case Some(f) => Some(f)
//      case None => None

  /**
   * Gets the localizable group of objects that this object is a part of.
   * @return the localizable group of objects.
   */
  def getLocalizableGroup: Iterable[? <: Localizable]

  /**
   * Asks the user to determine the localization file to use.
   * @return the localization file to use.
   */
  def askUserForLocalizationFile: File =
    val initialDirectory = HOIIVFiles.Mod.localization_folder
    var file = JavaFXUIManager.openChooser(initialDirectory, false)
    if (file == null) file = new File(HOIIVFiles.Mod.localization_folder, "HOIIVUtils_extra_localization.yml")
    file

  /**
   * Sets the localization for the given property to the new value, or creates a new localization if none exists.
   *
   * @param property the localizable property to set.
   * @param text     the new localization text.
   * @param file     the file the localization belongs in.
   */
  def setLocalization(property: Property, text: String, file: File): Unit =
    setLocalization(property, None, text, file)

  /**
   * Sets the localization for the given property to the new value
   *
   * @param property the localizable property to set.
   * @param version  the localization version number
   * @param text     the new localization text.
   * @param file     the file the localization belongs in.
   */
  def setLocalization(property: Property, version: Option[Int], text: String, file: File): Unit =
    localizableProperties.get(property) match
      case Some(k) => runZIO(ZIO.serviceWithZIO[LocalizationService](_.setLocalization(k, version, text, file)))
      case None => throw new LocalizationPropertyException(property, this)

  /**
   * Sets the localization for the given property to the new value.
   *
   * @param property the localizable property to set.
   * @param text     the new localization text.
   */
  def replaceLocalization(property: Property, text: String): Unit =
    localizableProperties.get(property) match
      case Some(k) => runZIO(ZIO.serviceWithZIO[LocalizationService](_.replaceLocalization(k, text)))
      case None =>

  /**
   * Sets the name localization to the new value.
   *
   * @param text     the new localization text.
   */
  def replaceName(text: String): Unit = replaceLocalization(Property.NAME, text)

  def hasNewLocalization: Boolean = this.localizationStatuses.values.exists(s => s == Localization.Status.NEW)
}
