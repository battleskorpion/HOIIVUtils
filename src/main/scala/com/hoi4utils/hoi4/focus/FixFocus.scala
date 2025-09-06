package com.hoi4utils.hoi4.focus

import com.hoi4utils.hoi4.country.CountryTagsManager
import com.hoi4utils.localization.{LocalizationManager, Property}
import com.typesafe.scalalogging.LazyLogging

import java.io.{File, IOException}
import java.time.LocalDateTime
import javax.swing.*


/**
 * FixFocus is a utility class for fixing focus localization in a focus tree.
 * It ensures that all focuses have proper localization for their names and descriptions.
 */
object FixFocus extends LazyLogging {

  /**
   * Fixes localization if necessary (who could've guessed).
   *
   * @param focusTree The focus tree to fix localization for.
   * @param generateDefaultDescs Whether to generate default descriptions for focuses that lack them,
   */
  @throws[IOException]
  def fixLocalization(focusTree: FocusTree, generateDefaultDescs: Boolean): Unit = {
    logger.debug("Starting fixLocalization for FocusTree: {}", focusTree)
    if (!validateFocusTree(focusTree)) return

    val locManager = LocalizationManager.get
    val locFile = focusTree.primaryLocalizationFile.get
    val focuses = focusTree.focuses

    logger.debug("Localization Manager loaded.")
    logger.debug("Primary localization file: {}", locFile.getAbsolutePath)
    logger.debug("Total focuses in tree: {}", focuses.size)

    // add name localization if missing
    focuses filter (_.isLocalized(Property.NAME)) foreach { focus =>
      logger.debug("Missing localization for focus: {}", focus.id.str)
      setGeneratedNameLocalization(focus, locManager, locFile)
    }

    // add desc localization if missing
    focuses filter (_.isLocalized(Property.DESCRIPTION)) foreach { focus =>
      logger.debug("Missing localization for focus: {}", focus.id.str)
      if (generateDefaultDescs) setGeneratedDescLocalization(focus, locManager, locFile)
      else setEmptyDescLocalization(focus, locManager, locFile)
    }

    logger.debug("Finished fixing focus localization.")
  }

  private def validateFocusTree(focusTree: FocusTree): Boolean = {
    if (focusTree == null) {
      logger.error("Focus tree is null.")
      JOptionPane.showMessageDialog(null, "Focus tree cannot be null.", "Error", JOptionPane.ERROR_MESSAGE)
      return false
    }

    if (!focusTree.hasFocuses) {
      logger.error("Focus tree has NO focuses! Stopping initialization.")
      JOptionPane.showMessageDialog(null, "Focus tree has no focuses.", "Error", JOptionPane.ERROR_MESSAGE)
      return false
    }

    if (focusTree.primaryLocalizationFile.isEmpty) {
      logger.info(s"Focus Tree $focusTree has no localization file.")
      JOptionPane.showMessageDialog(null, s"Warning: Could not find primary localization file for Focus Tree $focusTree.", "Warning", JOptionPane.WARNING_MESSAGE)
      return false  // todo?
    }

    logger.debug("Focus tree is valid: {}", focusTree)
    true
  }

  /**
   *
   *
   * @param focus
   * @param locManager
   * @param locFile
   */
  private def setGeneratedNameLocalization(focus: Focus, locManager: LocalizationManager, locFile: File): Unit = {
    val focusName = extractFocusName(focus.id.getOrElse(null))
    // Format the focus name
    val formattedName = locManager.titleCapitalize(focusName.replaceAll("_+", " ").trim)

    // Set missing localizations
    focus.setLocalization(Property.NAME, formattedName, locFile)
  }

  private def setGeneratedDescLocalization(focus: Focus, locManager: LocalizationManager, locFile: File): Unit = {
    focus.setLocalization(Property.DESCRIPTION, generateDescription, locFile)
  }

  private def setEmptyDescLocalization(focus: Focus, locManager: LocalizationManager, locFile: File): Unit = {
    focus.setLocalization(Property.DESCRIPTION, "", locFile)
  }

  private def extractFocusName(focusName: String): String = {
    var _focusName = focusName
    if (_focusName == null || _focusName.length < 4) return "Unnamed Focus" // Fallback in case of invalid focus name
    val tag = _focusName.substring(0, 3)
    if (CountryTagsManager.exists(tag)) {
      val hasUnderscore = _focusName.charAt(3) == '_'
      _focusName = _focusName.substring(if (hasUnderscore) 4 else 3)
    }
    _focusName
  }

  private def generateDescription = "Added on " + LocalDateTime.now + " by HOIIVUtils."
}
