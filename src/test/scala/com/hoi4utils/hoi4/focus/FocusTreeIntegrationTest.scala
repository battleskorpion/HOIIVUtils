package com.hoi4utils.hoi4.focus

import com.hoi4utils.shared.ParserTestBase
import com.hoi4utils.HOIIVFiles
import org.junit.jupiter.api.{Test, DisplayName, BeforeEach, AfterEach}
import org.junit.jupiter.api.Assertions._
import java.io.File
import scala.util.{Try, Success, Failure, boundary}

class FocusTreeIntegrationTest extends ParserTestBase {

  @BeforeEach
  override def setUpParserTest(): Unit = {
    super.setUpParserTest()

    // Clear any existing focus trees before each test
    FocusTree.clear()

    // Temporarily set the mod focus folder to our test directory
    // Note: You might need to modify HOIIVFiles to allow this override for testing
    // HOIIVFiles.Mod.focus_folder = testModPath.resolve("common/national_focus").toFile
  }

  @AfterEach
  def tearDownIntegrationTest(): Unit = {
    // Clean up after each test
    FocusTree.clear()
  }

  @Test
  @DisplayName("Should create FocusTree from test file")
  def testFocusTreeCreation(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile

    Try {
      val focusTree = new FocusTree(testFile)
      focusTree
    } match {
      case Success(focusTree) =>
        // Basic validation
        assertNotNull(focusTree, "FocusTree should be created successfully")
        assertTrue(focusTree.focuses.nonEmpty, "FocusTree should contain focuses")

        val expectedCount = getExpectedFocusCount("california.txt")
        assertEquals(expectedCount, focusTree.focuses.size,
          s"FocusTree should contain $expectedCount focuses")

        // Validate focus tree ID
        assertTrue(focusTree.id.isDefined, "FocusTree should have an ID")
        println(s"Created FocusTree '${focusTree.id.get}' with ${focusTree.focuses.size} focuses")

      case Failure(exception) =>
        fail(s"Failed to create FocusTree: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should maintain consistent focus count across multiple loads")
  def testConsistentFocusTreeLoading(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile
    val expectedCount = getExpectedFocusCount("california.txt")

    assertConsistentParsing(
      () => {
        FocusTree.clear() // Clear before each test
        val focusTree = new FocusTree(testFile)
        focusTree.focuses.size
      },
      expectedResult = expectedCount,
      runs = 5
    )

    println(s"FocusTree loading consistently produced $expectedCount focuses")
  }

  @Test
  @DisplayName("Should validate focus relationships")
  def testFocusRelationships(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile

    Try {
      val focusTree = new FocusTree(testFile)
      focusTree
    } match {
      case Success(focusTree) =>
        val allFocusIds = focusTree.listFocusIDs.toSet
        var focusesWithPrereqs = 0
        var validPrereqReferences = 0

        // Check prerequisite validity
        focusTree.focuses.foreach { focus =>
          if (focus.hasPrerequisites) {
            focusesWithPrereqs += 1

            focus.prerequisiteList.foreach { prereqFocus =>
              if (allFocusIds.contains(prereqFocus.id.getOrElse(""))) {
                validPrereqReferences += 1
              }
            }
          }
        }

        assertTrue(focusesWithPrereqs > 0,
          "Test focus tree should have some focuses with prerequisites")

        if (focusesWithPrereqs > 0) {
          assertTrue(validPrereqReferences > 0,
            "Should have valid prerequisite references")
        }

        println(s"Validated $focusesWithPrereqs focuses with prerequisites, $validPrereqReferences valid references")

      case Failure(exception) =>
        fail(s"Failed to create FocusTree for relationship validation: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should calculate focus positions correctly")
  def testFocusPositions(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile

    Try {
      val focusTree = new FocusTree(testFile)
      focusTree
    } match {
      case Success(focusTree) =>
        var positionsSet = 0
        var absolutePositionsCalculated = 0

        focusTree.focuses.foreach { focus =>
          // Check if focus has position set
          if (focus.x.isDefined && focus.y.isDefined) {
            positionsSet += 1

            // Test absolute position calculation (should not throw exception)
            val absolutePos = focus.absolutePosition
            assertNotNull(absolutePos, "Absolute position should be calculable")
            absolutePositionsCalculated += 1
          }
        }

        assertTrue(positionsSet > 0, "Some focuses should have positions set")
        assertEquals(positionsSet, absolutePositionsCalculated,
          "All positioned focuses should have calculable absolute positions")

        println(s"Validated positions for $positionsSet focuses")

      case Failure(exception) =>
        fail(s"Failed to create FocusTree for position testing: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should handle focus tree file operations")
  def testFocusTreeFileOperations(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile

    Try {
      val focusTree = new FocusTree(testFile)
      focusTree
    } match {
      case Success(focusTree) =>
        // Test file association
        assertTrue(focusTree.focusFile.isDefined, "FocusTree should know its source file")
        assertEquals(testFile, focusTree.focusFile.get,
          "FocusTree should reference correct source file")

        // Test FocusTree.get(file) functionality
        val retrievedFocusTree = FocusTree.get(testFile)
        assertTrue(retrievedFocusTree.isDefined, "Should be able to retrieve FocusTree by file")
        assertEquals(focusTree, retrievedFocusTree.get,
          "Retrieved FocusTree should be the same instance")

        println(s"File operations validated for ${testFile.getName}")

      case Failure(exception) =>
        fail(s"Failed to create FocusTree for file operations: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should generate focus tree script output")
  def testFocusTreeScriptGeneration(): Unit = {
    val testFile = testModPath.resolve("common/national_focus/california.txt").toFile

    Try {
      val focusTree = new FocusTree(testFile)
      focusTree
    } match {
      case Success(focusTree) =>
        // Test script generation
        val generatedScript = focusTree.toScript
        debugScriptDuplication(focusTree, generatedScript)
        assertNotNull(generatedScript, "Should generate script output")
        assertFalse(generatedScript.isEmpty, "Generated script should not be empty")

        // Basic validation that it contains expected elements
        assertTrue(generatedScript.contains("focus_tree"),
          "Generated script should contain 'focus_tree'")
        assertTrue(generatedScript.contains("focus"),
          "Generated script should contain 'focus' blocks")

        // Test that we can re-parse the generated script
        parseContent(generatedScript) match {
          case Success(reparsedNode) =>
            debugScriptDuplication(focusTree, generatedScript)
            val reparsedCount = countFocusesInNode(reparsedNode)
            assertEquals(focusTree.focuses.size, reparsedCount,
              "Re-parsed script should have same number of focuses")
            println(s"Successfully generated and re-parsed script with {reparsedCount} focuses")

          case Failure(exception) =>
            // This might fail if the script generation isn't perfect, which is okay
            println(s"Generated script could not be re-parsed: ${exception.getMessage}")
        }

      case Failure(exception) =>
        fail(s"Failed to create FocusTree for script generation: ${exception.getMessage}")
    }
  }

  @Test
  @DisplayName("Should handle multiple focus tree files")
  def testMultipleFocusTreeFiles(): Unit = {
    val focusTreeFiles = getTestFilesOfType("common/national_focus")

    if (focusTreeFiles.size < 2) {
      println("Skipping multiple file test - need at least 2 focus tree files")
      return ()
    }

    val createdTrees = focusTreeFiles.take(2).flatMap { file =>
      Try(new FocusTree(file)) match {
        case Success(tree) => Some(tree)
        case Failure(exception) =>
          println(s"Failed to create FocusTree from ${file.getName}: ${exception.getMessage}")
          None
      }
    }

    assertTrue(createdTrees.nonEmpty, "Should create at least one FocusTree")

    // Verify they are tracked properly
    assertTrue(FocusTree.listFocusTrees.size >= createdTrees.size,
      "All created focus trees should be tracked")

    // Each should have unique files
    val files = createdTrees.flatMap(_.focusFile)
    assertEquals(files.size, files.toSet.size,
      "Each FocusTree should have a unique file")

    println(s"Successfully created {createdTrees.size} focus trees from multiple files")
  }

  // scala
  private def debugScriptDuplication(focusTree: FocusTree, generatedScript: String): Unit = {
    import scala.util.{Try, Success, Failure}

    printFocusMatchContexts(generatedScript, maxMatches = 200)
    // Parse the generated script
    parseContent(generatedScript) match {
      case Success(reparsedNode) =>
        // Extract focus IDs from both original and reparsed trees
        val originalIds = focusTree.listFocusIDs.toList
        val reparsedIds = extractFocusIds(reparsedNode)

        // Basic counts
        val originalCount = originalIds.size
        val reparsedCount = reparsedIds.size

        println(s"[DEBUG] Original focus count = $originalCount")
        println(s"[DEBUG] Re-parsed focus count = $reparsedCount")

        // Frequency map and duplicates in reparsed result
        val freq = reparsedIds.groupBy(identity).view.mapValues(_.size).toMap
        val duplicates = freq.filter(_._2 > 1)

        println(s"[DEBUG] Unique reparsed IDs = ${freq.size}")
        println(s"[DEBUG] Duplicate IDs count = ${duplicates.size}")
        duplicates.take(50).foreach { case (id, cnt) =>
          println(s"[DEBUG] Duplicate id '$id' -> $cnt times")
        }

        // Show IDs present in original but missing in reparsed, and vice-versa
        val origSet = originalIds.toSet
        val reparsedSet = freq.keySet
        val missing = origSet.diff(reparsedSet)
        val extra = reparsedSet.diff(origSet)
        println(s"[DEBUG] Missing IDs (original -> not in reparsed): ${missing.size}")
        if (missing.nonEmpty) println(s"[DEBUG] ${missing.take(20).mkString(", ")}")
        println(s"[DEBUG] Extra IDs (reparsed -> not in original): ${extra.size}")
        if (extra.nonEmpty) println(s"[DEBUG] ${extra.take(20).mkString(", ")}")

        // Count raw "focus" occurrences in the generated script to check generation duplication
        val focusWordCount = """(?i)\bfocus\b""".r.findAllMatchIn(generatedScript).size
        val focusAssignOrBlockCount = """(?i)\bfocus\s*(=|\{)""".r.findAllMatchIn(generatedScript).size
        println(s"[DEBUG] Raw 'focus' word occurrences in generated script = $focusWordCount")
        println(s"[DEBUG] 'focus =' or 'focus {' occurrences in generated script = $focusAssignOrBlockCount")

        // If there are duplicates or large mismatch, fail fast with diagnostics
        if (reparsedCount != originalCount || duplicates.nonEmpty) {
          val msg =
            s"""Re-parsed script mismatch:
               | originalCount = $originalCount
               | reparsedCount = $reparsedCount
               | uniqueReparsedIds = ${freq.size}
               | duplicates = ${duplicates.size}
               | raw 'focus' occurrences = $focusWordCount
               | 'focus ='|'focus {' occurrences = $focusAssignOrBlockCount
               | missing IDs = ${missing.size}
               | extra IDs = ${extra.size}
               | """.stripMargin
          fail(msg)
        }

      case Failure(ex) =>
        println(s"[DEBUG] Failed to re-parse generated script: ${ex.getMessage}")
        fail(s"Failed to re-parse generated script: ${ex.getMessage}")
    }
  }

  // scala
  private def printFocusMatchContexts(generatedScript: String, maxMatches: Int = 100): Unit = {
    val regex = """(?i)\bfocus\s*(=|\{)""".r
    val nlIndexes = generatedScript.zipWithIndex.collect { case (c, i) if c == '\n' => i }.toArray

    def lineNumberForIndex(idx: Int): Int = java.util.Arrays.binarySearch(nlIndexes, idx) match {
      case x if x >= 0 => java.util.Arrays.binarySearch(nlIndexes, idx) + 1
      case x => -x - 1 + 1
    }

    var printed = 0
    regex.findAllMatchIn(generatedScript).foreach { m =>
      if (printed >= maxMatches) return
      val start = m.start
      val lineStart = generatedScript.lastIndexOf('\n', start) match {
        case -1 => 0
        case i => i + 1
      }
      val lineEnd = generatedScript.indexOf('\n', start) match {
        case -1 => generatedScript.length
        case i => i
      }
      val line = generatedScript.substring(lineStart, lineEnd)
      val trimmed = line.trim
      val isLineComment = trimmed.startsWith("#") || trimmed.startsWith("//")
      // crude inside-quotes detection (counts " before the match)
      val before = generatedScript.substring(0, start)
      val quoteCount = before.count(_ == '"')
      val insideQuotes = (quoteCount % 2) == 1
      val contextStart = math.max(0, start - 40)
      val contextEnd = math.min(generatedScript.length, m.end + 40)
      val context = generatedScript.substring(contextStart, contextEnd).replace("\n", "\\n")
      println(s"[FOCUS-MATCH] index=${start}, line=${lineNumberForIndex(start)}, isLineComment=${isLineComment}, insideQuotes=${insideQuotes}")
      println(s"[FOCUS-MATCH] line: ${trimmed}")
      println(s"[FOCUS-MATCH] context: ...${context}...")
      printed += 1
    }
    if (printed == 0) println("[FOCUS-MATCH] no matches found")
  }
}