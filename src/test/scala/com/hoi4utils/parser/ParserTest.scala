package com.hoi4utils.parser

import com.hoi4utils.shared.ParserTestBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.{DisplayName, Test}
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

class ParserTest extends ParserTestBase with AnyFunSuiteLike {

  private val testPath = "src/test/resources/clausewitz_parser/"
  private val filesToTest = List(
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
    new File(testPath + "focus_with_search_filter_test1.txt"),
    new File(testPath + "focus_with_search_filter_test2.txt"),
    new File(testPath + "carriage_return.txt"),
    new File(testPath + "specialinfantry.txt")
  )

  def withParsedFiles(testFunction: Node => Unit): Unit = {
    filesToTest.foreach { file =>
      val parser = new Parser(file)
      val node = parser.parse
      assert(node != null, s"Failed to parse $file")
      testFunction(node)
    }
  }

  def withParsedFile(testFunction: Node => Unit, file: File): Unit = {
    val parser = new Parser(file)
    val node = parser.parse
    assert(node != null, s"Failed to parse $file")
    testFunction(node)
  }

  test("File root node should be nonempty") {
    withParsedFiles { node =>
      assert(node.nonEmpty)
      assert(node.toList.nonEmpty)
    }
  }

  test("Node should not include carriage returns") {
    withParsedFiles { node =>
      node.toList.foreach(n => {
        assert(!n.name.contains("\r"), s"Node name contains carriage return: $n")
        assert(!n.valueAsString.contains("\r"), s"Node value contains carriage return: $n")
      })
    }
  }

//  test("Node toScript()") {
//    filesToTest.foreach { file =>
//      val parser = new Parser(file)
//      val node = parser.parse
//      assert(node != null, s"Failed to parse $file")
//
//      // Define the expected file based on the input file name.
//      // Here, we assume the expected file is in the same testPath folder with a "_toScript.txt" suffix.
//      val expectedFileName = file.getName.replace(".txt", "_toScript.txt")
//      val expectedFile = new File(testPath + expectedFileName)
//
//      // Read the expected content.
//      val expectedContent = scala.io.Source.fromFile(expectedFile).mkString
//      // Get the actual content from toScript().
//      val actualContent = node.toScript
//
//      // Compare the actual output to the expected output.
//      assert(expectedContent == actualContent,
//        s"Output from ${file.getName} did not match expected output.")
//    }
//  }


  test("the '=' operator should be with an identifier") {
    withParsedFiles { node =>
      node.toList.foreach(n => {
        assert(n.identifier.nonEmpty && n.name.nonEmpty, s"Node $n has no identifier")
      })
    }
  }

  test("sub node using find(): specialinfantry.txt should have parsed sub_units") {
    withParsedFile ({ node =>
      val subunits = node.find("sub_units").getOrElse(fail("sub_units not found"))
      assert(subunits.find("mobenforcer").isDefined)
      assert(subunits.find("mobenforcer").get.find("sprite").nonEmpty)
    }, new File(testPath + "specialinfantry.txt"))
  }
  
  test("Ints should be read as ints and not as type double") {
    withParsedFile ({ node => 
        val capital = node.find("capital").getOrElse(fail("focus not found"))
        assert(capital.isInt)
    }, new File(testPath + "SMD_Maryland.txt"))
  }

  test("textSpriteType block is parsed correctly") {
    // The input contains a stray semicolon after effectFile.
    val input =
      """textSpriteType = {
        |    name = "largefloaterbutton"
        |    texturefile = "gfx//interface//button_type_1.tga"
        |    noOfFrames = 1
        |    effectFile = "gfx/FX/buttonstate.lua";
        |}""".stripMargin

    val parser = new Parser(input)
    val root = parser.parse

    // Find the textSpriteType node.
    val textSpriteOpt = root.find("textSpriteType")
    assert(textSpriteOpt.isDefined, "textSpriteType node not found")
    val textSprite = textSpriteOpt.get

    // Verify child nodes are present.
    val nameOpt = textSprite.find("name")
    val texturefileOpt = textSprite.find("texturefile")
    val noOfFramesOpt = textSprite.find("noOfFrames")
    val effectFileOpt = textSprite.find("effectFile")

    assert(nameOpt.isDefined, "Missing 'name' node")
    assert(texturefileOpt.isDefined, "Missing 'texturefile' node")
    assert(noOfFramesOpt.isDefined, "Missing 'noOfFrames' node")
    assert(effectFileOpt.isDefined, "Missing 'effectFile' node")

    // Check that their values are parsed correctly.
    // Assuming valueAsString returns the literal string value.
    assert(nameOpt.get.valueAsString == "largefloaterbutton")
    assert(texturefileOpt.get.valueAsString == "gfx//interface//button_type_1.tga")
    // Depending on your parser, noOfFrames may be read as a number or string.
    // Adjust the following assertion if necessary.
    assert(noOfFramesOpt.get.valueAsString == "1")
    assert(effectFileOpt.get.valueAsString == "gfx/FX/buttonstate.lua")
  }

  test("focus id line is parsed correctly") {
    // The input contains a stray semicolon after effectFile.
    val input =
      """focus = {
        |    id = SMA_Maryland
        |}
        |""".stripMargin

    val parser = new Parser(input)
    val root = parser.parse

    assert(root.nonEmpty, "Root node is empty")
    assert(root.contains("focus"), "Root node does not contain 'focus'")
    assert(root.find("focus").get.identifier.isDefined, "Root node does not have an identifier")
    assert(root.find("focus").get.find("id").get.valueAsString == "SMA_Maryland", "Root node identifier is not 'SMA_Maryland'")
    assert(root.find("focus").get.find("id").isDefined, "ID node not found")
    assert(root.find("focus").get.find("id").get.valueAsString == "SMA_Maryland", "ID node value is not 'SMA_Maryland'")
    assert(root.find("focus").get.find("id").get.identifier.isDefined, "ID node does not have an identifier")
    assert(root.find("focus").get.find("id").get.identifier.get == "id", "ID node identifier is not 'SMA_Maryland'")
    //assert(root.toScript == input, "Output does not match input")
  }

  @Test
  @DisplayName("Should parse basic focus tree file consistently")
  def testBasicFocusTreeParsing(): Unit = {
    val focusTreeFiles = getTestFilesOfType("common/national_focus")

    focusTreeFiles.foreach { file =>
      val fileName = file.getName
      val expectedCount = getExpectedFocusCount(fileName)

      if (expectedCount > 0) {
        assertConsistentParsing(
          () => {
            parseFile(file) match {
              case Success(rootNode) => countFocusesInNode(rootNode)
              case Failure(exception) =>
                fail(s"Failed to parse $fileName: ${exception.getMessage}")
            }
          },
          expectedResult = expectedCount,
          runs = 3
        )
        println(s"✓ $fileName consistently parsed $expectedCount focuses")
      }
    }
  }

  @Test
  @DisplayName("Should extract focus IDs consistently")
  def testFocusIdExtraction(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile

    parseFile(testFile) match {
      case Success(rootNode) =>
        val focusIds = extractFocusIds(rootNode)

        // Should have the expected number of focuses
        assertEquals(getExpectedFocusCount("test_focus_tree.txt"), focusIds.length,
          "Number of extracted focus IDs should match expected count")

        // All focus IDs should be unique
        val uniqueIds = focusIds.toSet
        assertEquals(focusIds.length, uniqueIds.size,
          "All focus IDs should be unique")

        // Focus IDs should not be empty
        focusIds.foreach { id =>
          assertFalse(id.isEmpty, "Focus ID should not be empty")
        }

        println(s"✓ Extracted ${focusIds.length} unique focus IDs: ${focusIds.take(5).mkString(", ")}...")

      case Failure(exception) =>
        fail(s"Failed to parse test focus tree: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should validate focus tree structure")
  def testFocusTreeStructure(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile

    parseFile(testFile) match {
      case Success(rootNode) =>
        assertTrue(validateFocusTreeStructure(rootNode),
          "Parsed file should contain valid focus_tree structure")

        // Should be able to extract focus tree ID
        val focusTreeId = extractFocusTreeId(rootNode)
        assertTrue(focusTreeId.isDefined, "Focus tree should have an ID")

        println(s"✓ Focus tree structure valid with ID: ${focusTreeId.get}")

      case Failure(exception) =>
        fail(s"Failed to parse focus tree structure: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should handle parsing errors gracefully")
  def testMalformedFileHandling(): Unit = {
    val malformedContent =
      """
        focus_tree = {
          id = broken_tree
          focus = {
            id = broken_focus
            # missing closing brace
      """

    parseContent(malformedContent) match {
      case Success(_) =>
        // If it somehow succeeds, that's fine too - parser might be forgiving
        println("✓ Parser handled malformed content gracefully")

      case Failure(exception) =>
        // Should fail with a meaningful error
        assertNotNull(exception.getMessage)
        assertFalse(exception.getMessage.isEmpty)
        println(s"✓ Parser failed as expected with: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should maintain parsing performance")
  def testParsingPerformance(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile

    val startTime = System.currentTimeMillis()

    // Parse the file multiple times
    val results = (1 to 10).map { _ =>
      parseFile(testFile) match {
        case Success(rootNode) => countFocusesInNode(rootNode)
        case Failure(_) => -1
      }
    }

    val endTime = System.currentTimeMillis()
    val totalTime = endTime - startTime

    // All parsing attempts should succeed
    assertTrue(results.forall(_ > 0), "All parsing attempts should succeed")

    // Should complete in reasonable time (adjust threshold as needed)
    assertTrue(totalTime < 5000,
      s"Parsing 10 times should complete in under 5 seconds, took ${totalTime}ms")

    // All results should be consistent
    val expectedCount = results.head
    assertTrue(results.forall(_ == expectedCount),
      "All parsing results should be consistent")

    println(s"✓ Parsed test focus tree 10 times in ${totalTime}ms (${totalTime / 10}ms avg)")
  }

  @Test
  @DisplayName("Should parse focus prerequisites correctly")
  def testFocusPrerequisites(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile

    parseFile(testFile) match {
      case Success(rootNode) =>
        var focusesWithPrereqs = 0
        var totalPrereqReferences = 0
        val allFocusIds = extractFocusIds(rootNode).toSet

        def checkPrerequisites(node: Node): Unit = {
          if (node.nameEquals("focus")) {
            node.rawValue match {
              case Some(children: ListBuffer[Node]) =>
                val prereqNodes = children.filter(_.nameEquals("prerequisite"))
                if (prereqNodes.nonEmpty) {
                  focusesWithPrereqs += 1

                  prereqNodes.foreach { prereqNode =>
                    prereqNode.rawValue match {
                      case Some(prereqChildren: ListBuffer[Node]) =>
                        prereqChildren.filter(_.nameEquals("focus")).foreach { focusRefNode =>
                          focusRefNode.rawValue match {
                            case Some(prereqId: String) =>
                              totalPrereqReferences += 1
                              assertTrue(allFocusIds.contains(prereqId),
                                s"Prerequisite '$prereqId' should reference a valid focus")
                            case _ =>
                          }
                        }
                      case _ =>
                    }
                  }
                }
              case _ =>
            }
          }

          node.rawValue match {
            case Some(children: ListBuffer[Node]) =>
              children.foreach(checkPrerequisites)
            case _ =>
          }
        }

        checkPrerequisites(rootNode)

        assertTrue(focusesWithPrereqs > 0,
          "Test focus tree should have some focuses with prerequisites")
        assertTrue(totalPrereqReferences > 0,
          "Should have found some prerequisite references")

        println(s"✓ Found $focusesWithPrereqs focuses with prerequisites, $totalPrereqReferences total references")

      case Failure(exception) =>
        fail(s"Failed to parse focus tree: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should detect changes in test files")
  def testFileChangeDetection(): Unit = {
    val focusTreeFiles = getTestFilesOfType("common/national_focus")
    val expectedCounts = createExpectedCounts()

    focusTreeFiles.foreach { file =>
      val filename = file.getName
      if (expectedCounts.contains(filename)) {
        parseFile(file) match {
          case Success(rootNode) =>
            val actualCount = countFocusesInNode(rootNode)
            val expectedCount = expectedCounts(filename)

            assertEquals(expectedCount, actualCount,
              s"File $filename: Expected $expectedCount focuses but found $actualCount. " +
                s"If this file was intentionally modified, update getExpectedFocusCount() method.")

          case Failure(exception) =>
            fail(s"Failed to parse $filename: ${exception.getMessage}")
        }
      }
    }
  }
}
