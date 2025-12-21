package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.exceptions.LocalizationPreconditionException
import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.localization.{LocalizationManager, Property}
import com.typesafe.scalalogging.LazyLogging

import java.io.{File, IOException}
import java.time.LocalDateTime


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
  def fixLocalization(focusTree: FocusTree, generateDefaultDescs: Boolean): Int = {
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
      logger.debug("TEST 1")
    }

    // add desc localization if missing
    focuses filter (_.isUnlocalized(Property.DESCRIPTION)) foreach { focus =>
      logger.debug(s"Missing localization for focus: ${focus.id.str}")
      if (generateDefaultDescs) setGeneratedDescLocalization(focus, locManager, locFile)
      else setEmptyDescLocalization(focus, locManager, locFile)
      logger.debug("TEST 2")
    }

    logger.debug("Finished fixing focus localization.")
    focuses.count(f => f.hasNewLocalization)
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
    val focusName = extractFocusName(focus.id getOrElse "Unnamed_Focus")
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
    var _focusName = focusName.trim
    val underscoreIndex = _focusName.indexOf('_')
    if (underscoreIndex >= 0) {
      val maybeTag = _focusName.substring(0, underscoreIndex)
      if (CountryTag.exists(maybeTag)) _focusName = _focusName.substring(underscoreIndex + 1)
    }
    _focusName
  }

  private def generateDescription = "Added on " + LocalDateTime.now + " by HOIIVUtils."
}
