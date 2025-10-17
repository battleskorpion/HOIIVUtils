package com.hoi4utils.shared

import com.hoi4utils.hoi4mod.common.national_focus
import com.hoi4utils.hoi4mod.common.national_focus.FocusTreeFile
import com.hoi4utils.parser.{Node, Parser}
import org.junit.jupiter.api.{AfterEach, BeforeEach}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.io.Source
import scala.util.{Try, Using}

/**
 * Base class for parser tests providing utilities to work with test mod files
 */
abstract class ParserTestBase {

  protected var testModPath: Path = uninitialized

  @BeforeEach
  def setUpParserTest(): Unit = {
    // Clear any global state from previous tests
    national_focus.FocusTreeFile.clear()

    // Get paths to test resources
    testModPath = getTestResourcePath("test_mods/demo_mod")
  }

  @AfterEach
  def tearDownParserTest(): Unit = {
    // Clear global state after test
    national_focus.FocusTreeFile.clear()
  }

  /**
   * Get path to test resource file
   */
  protected def getTestResourcePath(resourcePath: String): Path = {
    val resource = getClass.getClassLoader.getResource(resourcePath)
    if (resource == null) {
      throw new RuntimeException(s"Test resource not found: $resourcePath")
    }
    Paths.get(resource.toURI)
  }

  /**
   * Read test file content as string
   */
  protected def readTestFile(relativePath: String): String = {
    val fullPath = testModPath.resolve(relativePath)
    Using(Source.fromFile(fullPath.toFile, "UTF-8")) { source =>
      source.mkString
    }.get
  }

  /**
   * Parse a file using your Parser class
   */
  protected def parseFile(file: File): Try[Node] = {
    Try {
      val parser = new Parser(file)
      parser.parse
    }
  }

  /**
   * Parse file content using your Parser class
   */
  protected def parseContent(content: String): Try[Node] = {
    Try {
      val parser = new Parser(content)
      parser.parse
    }
  }

  /**
   * Get all files of a specific type from test mod
   */
  protected def getTestFilesOfType(subPath: String, extension: String = ".txt"): List[File] = {
    val directory = testModPath.resolve(subPath)
    if (!Files.exists(directory)) {
      return List.empty
    }

    Files.walk(directory)
      .filter(Files.isRegularFile(_))
      .filter(_.toString.endsWith(extension))
      .map(_.toFile)
      .toArray
      .toList
      .asInstanceOf[List[File]]
  }

  /**
   * Count focuses in a parsed focus tree node
   */
  protected def countFocusesInNode(rootNode: Node): Int = {
    var focusCount = 0

    def countFocusesRecursive(node: Node): Unit = {
      // Only count nodes named "focus" that have a block structure (not just references)
      if (node.nameEquals("focus") && node.isParent) {
        focusCount += 1
      }

      node.rawValue match {
        case Some(children: ListBuffer[Node]) =>
          children.foreach(countFocusesRecursive)
        case _ => // Not a parent node
      }
    }

    countFocusesRecursive(rootNode)
    focusCount
  }

  /**
   * Extract all focus IDs from a parsed node
   */
  protected def extractFocusIds(rootNode: Node): List[String] = {
    val focusIds = ListBuffer[String]()

    def extractIdsRecursive(node: Node): Unit = {
      if (node.nameEquals("focus")) {
        // Look for id field within this focus
        node.rawValue match {
          case Some(children: ListBuffer[Node]) =>
            children.find(_.nameEquals("id")) match {
              case Some(idNode) =>
                idNode.rawValue match {
                  case Some(id: String) => focusIds += id
                  case _ =>
                }
              case None =>
            }
          case _ =>
        }
      }

      node.rawValue match {
        case Some(children: ListBuffer[Node]) =>
          children.foreach(extractIdsRecursive)
        case _ =>
      }
    }

    extractIdsRecursive(rootNode)
    focusIds.toList
  }

  /**
   * Assert that parser results are consistent across multiple runs
   */
  protected def assertConsistentParsing[T](
                                            parseOperation: () => T,
                                            expectedResult: T,
                                            runs: Int = 5
                                          ): Unit = {
    val results = (1 to runs).map(_ => parseOperation())

    // All runs should produce the same result
    results.foreach { result =>
      assert(result == expectedResult,
        s"Expected $expectedResult, but got $result")
    }

    // All runs should be identical
    val firstResult = results.head
    results.tail.foreach { result =>
      assert(result == firstResult, "Parser produced different results across runs")
    }
  }

  /**
   * Create expected counts map for validation
   * Update these numbers based on your actual test files
   */
  protected def createExpectedCounts(): Map[String, Int] = {
    Map(
      "test_focus_tree.txt" -> 15, // Correct count: 15 actual focuses in file
      "california.txt" -> 226      // Actual parsed count: 226 focus blocks detected by parser
    )
  }

  /**
   * Manually count expected focuses in a focus tree file
   * This should be manually verified and updated when test files change
   */
  protected def getExpectedFocusCount(filename: String): Int = {
    filename match {
      case "test_focus_tree.txt" => 15  // Correct count: 15 actual focuses in file
      case "california.txt" => 226      // Actual parsed count: 226 focus blocks detected by parser
      case _ => 0
    }
  }

  /**
   * Validate focus tree structure
   */
  protected def validateFocusTreeStructure(rootNode: Node): Boolean = {
    // Check if root contains focus_tree nodes
    rootNode.rawValue match {
      case Some(children: ListBuffer[Node]) =>
        children.exists(_.nameEquals("focus_tree"))
      case _ => false
    }
  }

  /**
   * Extract focus tree ID from parsed node
   */
  protected def extractFocusTreeId(rootNode: Node): Option[String] = {
    def findFocusTreeId(node: Node): Option[String] = {
      if (node.nameEquals("focus_tree")) {
        node.rawValue match {
          case Some(children: ListBuffer[Node]) =>
            children.find(_.nameEquals("id")) match {
              case Some(idNode) =>
                idNode.rawValue match {
                  case Some(id: String) => Some(id)
                  case _ => None
                }
              case None => None
            }
          case _ => None
        }
      } else {
        node.rawValue match {
          case Some(children: ListBuffer[Node]) =>
            children.flatMap(findFocusTreeId).headOption
          case _ => None
        }
      }
    }

    findFocusTreeId(rootNode)
  }
}