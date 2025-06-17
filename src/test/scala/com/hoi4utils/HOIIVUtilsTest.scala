package com.hoi4utils

import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File
import scala.io.Source

class HOIIVUtilsTest extends AnyFunSuiteLike {

  private[hoi4utils] val resourceUrl = getClass.getClassLoader.getResource("clausewitz_parser/")
  private[hoi4utils] val testPath = resourceUrl.getPath
  private[hoi4utils] val validFocusTreeTestFiles = List(
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
    new File(testPath + "texas_tree.txt"),
  )
  private[hoi4utils] val validFocusTestFiles = List(
    new File(testPath + "focus_with_search_filter_test1.txt"),
    new File(testPath + "focus_with_search_filter_test2.txt"),
    new File(testPath + "carriage_return.txt"),
  )
  private[hoi4utils] val validStratRegionTestFiles = List(
    new File(testPath + "StrategicRegion.txt"),
  )
  private[hoi4utils] val filesToTest = List(
    new File(testPath + "specialinfantry.txt"),
  ).appendedAll(validFocusTreeTestFiles).appendedAll(validFocusTestFiles).appendedAll(validStratRegionTestFiles)

  /**
   * Sets up the configuration for the HOI4Utils test environment.
   *
   * @return The initialized configuration.
   */
  private[hoi4utils] def setup(): Config = {
    val config: Config = ConfigManager().createConfig
    Initializer().initialize(config)
    config
  }

  /**
   * Counts the occurrences of a specific string in a file.
   *
   * @param file The file to search in.
   * @param search The string to count occurrences of.
   * @return The number of occurrences of the search string in the file.
   */
  private[hoi4utils] def countOccurrencesInFile(file: File, search: String): Int =
    require(file.exists && file.isFile, s"File does not exist: ${file.getPath}")
    val source = Source.fromFile(file)
    try
      source.getLines().map(line => line.sliding(search.length).count(_ == search)).sum
    finally
      source.close()

  test("test files should exist") {
    filesToTest.foreach { file =>
      assert(file.exists(), s"Test file does not exist: ${file.getPath}")
    }
  }
}
