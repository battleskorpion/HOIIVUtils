package com.hoi4utils.hoi4.focus

import com.hoi4utils.exceptions.LocalizationPreconditionException
import com.hoi4utils.hoi4.country.CountryTagsManager
import com.hoi4utils.localization.{LocalizationManager, Property}
import com.typesafe.scalalogging.LazyLogging
import lombok.NonNull

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
  @throws[LocalizationPreconditionException]
  def fixLocalization(focusTree: FocusTree, generateDefaultDescs: Boolean): Unit = {
    logger.debug(s"Starting fixLocalization for FocusTree: $focusTree")
    requireLocalizableFocusTree(focusTree)

    val locManager = LocalizationManager.get
    val locFile = focusTree.primaryLocalizationFile.get
    val focuses = focusTree.focuses

    logger.debug("Localization Manager loaded.")
    logger.debug(s"Primary localization file: ${locFile.getAbsolutePath}")
    logger.debug(s"Total focuses in tree: ${focuses.size}")

    // add name localization if missing
    focuses filter (_.isUnlocalized(Property.NAME)) foreach { focus =>
      logger.debug(s"Missing localization for focus: ${focus.id.str}")
      setGeneratedNameLocalization(focus, locManager, locFile)
    }

    // add desc localization if missing
    focuses filter (_.isUnlocalized(Property.DESCRIPTION)) foreach { focus =>
      logger.debug(s"Missing localization for focus: ${focus.id.str}")
      if (generateDefaultDescs) setGeneratedDescLocalization(focus, locManager, locFile)
      else setEmptyDescLocalization(focus, locManager, locFile)
    }

    logger.debug("Finished fixing focus localization.")
  }

  @throws[IllegalArgumentException]
  @throws[LocalizationPreconditionException]
  private def requireLocalizableFocusTree(focusTree: FocusTree): Unit = {
    if (focusTree == null) throw new IllegalArgumentException("Focus tree is null.")
    if (!focusTree.hasFocuses) throw LocalizationPreconditionException(s"Focus tree $focusTree has localizable focuses.")
    if (focusTree.primaryLocalizationFile.isEmpty) throw LocalizationPreconditionException(s"Focus tree $focusTree has a known primary localization file.")
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
