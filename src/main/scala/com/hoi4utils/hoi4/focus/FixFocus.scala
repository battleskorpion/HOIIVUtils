package com.hoi4utils.hoi4.focus

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.hoi4.country.CountryTagsManager
import com.hoi4utils.localization.{LocalizationManager, Property}
import com.typesafe.scalalogging.LazyLogging
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import scala.jdk.javaapi.CollectionConverters
import javax.swing.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime


/**
 * FixFocus is a utility class for fixing focus localization in a focus tree.
 * It ensures that all focuses have proper localization for their names and descriptions.
 */
object FixFocus extends LazyLogging {

  // TODO improve
  @throws[IOException]
  def fixLocalization(focusTree: FocusTree): Unit = {
    logger.debug("Starting fixLocalization for FocusTree: {}", focusTree)
    if (!validateFocusTree(focusTree)) return

    val locManager = LocalizationManager.get
    logger.debug("LocalizationManager loaded")

    val locFile = focusTree.primaryLocalizationFile.get
    logger.debug("Primary localization file: {}", locFile.getAbsolutePath)

    val focuses = CollectionConverters.asJavaCollection(focusTree.focuses)
    logger.debug("Total focuses in tree: {}", focuses.size)

    focuses.parallelStream.filter((focus: Focus) => {
      //val missingLocalization = focus.localization(Property.NAME) == null
      //if (missingLocalization) logger.debug("Missing localization for focus: {}", focus.id.str)
      //missingLocalization
      focus.localization(Property.NAME) match {
        case Some(_) => false
        case None =>
          logger.debug("Missing localization for focus: {}", focus.id.str)
          true
      }
    }).forEach((focus: Focus) => processFocusLocalization(focus, locManager, locFile))

    logger.debug("Finished fixing focus localization.")
  }

  private def validateFocusTree(focusTree: FocusTree): Boolean = {
    logger.debug("Validating FocusTree: {}", focusTree)
    if (focusTree == null) {
      logger.error("Focus tree is null.")
      JOptionPane.showMessageDialog(null, "Focus tree cannot be null.", "Error", JOptionPane.ERROR_MESSAGE)
      return false
    }

    if (focusTree.focuses == null || focusTree.focuses.isEmpty) {
      logger.error("Focus tree has NO focuses! Stopping initialization.")
      JOptionPane.showMessageDialog(null, "Error: Focus tree has no focuses.", "Error", JOptionPane.ERROR_MESSAGE)
      throw new IllegalStateException("Focus tree has no focuses.")
    }

    if (focusTree.primaryLocalizationFile.isEmpty) {
      logger.info("Focus tree has no localization file.") // todo say which focus tree

      JOptionPane.showMessageDialog(null, "Warning: Focus tree has no localization file.", "Warning", JOptionPane.WARNING_MESSAGE)
      return false
    }

    logger.debug("Focus tree is valid: {}", focusTree)
    true
  }

  private def processFocusLocalization(focus: Focus, locManager: LocalizationManager, locFile: File): Unit = {
    val focusName = extractFocusName(focus.id.getOrElse(null))
    // Format the focus name
    val formattedName = locManager.titleCapitalize(focusName.replaceAll("_+", " ").trim)

    // Set missing localizations
    focus.setLocalization(Property.NAME, formattedName, locFile)
    focus.setLocalization(Property.DESCRIPTION, generateDescription, locFile)
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
