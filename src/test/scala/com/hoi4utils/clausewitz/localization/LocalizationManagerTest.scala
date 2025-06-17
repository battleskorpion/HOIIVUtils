package com.hoi4utils.clausewitz.localization

import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File

class LocalizationManagerTest extends AnyFunSuiteLike {
  private val testPath = "src/test/resources/clausewitz/localization/"
  private val validFocusTreeLocFiles = List(
    new File(testPath + "focus_Alaska_l_english.yml"),
    new File(testPath + "focus_Alabama_l_english.yml"),
    new File(testPath + "focus_Massachusetts_SMA_l_english.yml"),
  )

  private val filesToTest = List(
  ).appendedAll(validFocusTreeLocFiles)

//  test("Localization file format should be valid") {
//    // Regex to parse lines of the form: <whitespace*><key>:<version*><whitespace*>"value"
//    // - (\S+) captures the key.
//    // - (\d*) captures an optional version.
//    // - "(.*?)" non-greedily captures the quoted value.
//    // - (.*) captures any trailing text (such as whitespace or comments).
//    val localizationRegex = """\s*(\S+):(\d*)\s*"(.*?)"(.*)""".r
//
//    filesToTest.foreach { file =>
//      // Read the entire file as a single string and then split it into lines.
//      val fileLines = Files.readAllLines(file.toPath).toArray.mkString("\n")
//      val lines = fileLines.split("\n").toList
//
//      // Assume the first non-empty line is the header.
//      val header = lines.headOption.getOrElse("")
//
//      // Validate each line after the header.
//      lines.drop(1).foreach { line =>
//        line.trim match {
//          case "" => // Skip empty lines.
//          case localizationRegex(key, version, value) =>
//            // Optionally create a Localization to further validate if needed:
//            // Localization(key, if (version.isEmpty) null else version.toInt, value, Localization.Status.EXISTS)
//            ()
//          case invalidLine =>
//            fail(s"Localization format was considered invalid. File: $file, Localization: '$invalidLine'")
//        }
//      }
//    }
//  }
}
