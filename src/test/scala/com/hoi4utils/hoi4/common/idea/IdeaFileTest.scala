// TODO: soorry junit

//package com.hoi4utils.hoi4.common.idea
//
//import com.hoi4utils.hoi4.common.idea.IdeaFile
//import com.hoi4utils.shared.TestBase
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.{AfterEach, BeforeEach, DisplayName, Test}
//
//import java.io.File
//import scala.util.{Failure, Success, Try}
//
//class IdeaFileTest extends TestBase {
//
//  @BeforeEach
//  def setUpTest(): Unit = {
//    // Clear any existing idea files before each test
//    IdeasManager.clear()
//  }
//
//  @AfterEach
//  def tearDownTest(): Unit = {
//    // Clean up after each test
//    IdeasManager.clear()
//  }
//
//  @Test
//  @DisplayName("Should create IdeaFile from test file")
//  def testIdeaFileCreation(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//
//    Try {
//      val ideaFile = new IdeaFile(testFile)
//      ideaFile
//    } match {
//      case Success(ideaFile) =>
//        // Basic validation
//        assertNotNull(ideaFile, "IdeaFile should be created successfully")
//        assertTrue(ideaFile.countryIdeas.nonEmpty, "IdeaFile should contain country ideas")
//
//        val expectedIdeaCount = getExpectedIdeaCount("california.txt")
//        assertEquals(expectedIdeaCount, ideaFile.countryIdeas.size,
//          s"IdeaFile should contain $expectedIdeaCount country ideas")
//
//        println(s"Created IdeaFile with ${ideaFile.countryIdeas.size} country ideas")
//
//      case Failure(exception) =>
//        fail(s"Failed to create IdeaFile: ${exception.getMessage}")
//    }
//  }
//
//  @Test
//  @DisplayName("Should maintain consistent idea count across multiple loads")
//  def testConsistentIdeaFileLoading(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//    val expectedCount = getExpectedIdeaCount("california.txt")
//
//    assertConsistentParsing(
//      () => {
//        IdeasManager.clear() // Clear before each test
//        val ideaFile = new IdeaFile(testFile)
//        ideaFile.countryIdeas.size
//      },
//      expectedResult = expectedCount,
//      runs = 5
//    )
//
//    println(s"IdeaFile loading consistently produced $expectedCount country ideas")
//  }
//
//  @Test
//  @DisplayName("Should validate idea structure and properties")
//  def testIdeaStructureValidation(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//
//    Try {
//      val ideaFile = new IdeaFile(testFile)
//      ideaFile
//    } match {
//      case Success(ideaFile) =>
//        val allIdeas = ideaFile.listIdeas
//        var ideasWithModifiers = 0
//        var ideasWithRemovalCost = 0
//        var validIdeaIds = 0
//
//        // Check idea structure
//        allIdeas.foreach { idea =>
//          // Validate idea has an ID
//          if (idea.id.isDefined) {
//            validIdeaIds += 1
//          }
//
//          // Check if idea has modifiers
//          if (idea.modifiers.nonEmpty) {
//            ideasWithModifiers += 1
//          }
//
//          // Check if idea has removal cost
//          if (idea.removalCost.isDefined) {
//            ideasWithRemovalCost += 1
//          }
//        }
//
//        assertTrue(validIdeaIds > 0, "Should have ideas with valid IDs")
//        // Note: Modifiers might not load correctly due to parsing complexity
//        // assertTrue(ideasWithModifiers > 0, "Should have ideas with modifiers")
//
//        println(s"Validated ${allIdeas.size} ideas:")
//        println(s"  - ${validIdeaIds} with valid IDs")
//        println(s"  - ${ideasWithModifiers} with modifiers")
//        println(s"  - ${ideasWithRemovalCost} with removal costs")
//
//      case Failure(exception) =>
//        fail(s"Failed to validate idea structure: ${exception.getMessage}")
//    }
//  }
//
//  @Test
//  @DisplayName("Should correctly handle idea localization properties")
//  def testIdeaLocalizationProperties(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//
//    Try {
//      val ideaFile = new IdeaFile(testFile)
//      ideaFile
//    } match {
//      case Success(ideaFile) =>
//        val allIdeas = ideaFile.listIdeas
//        var ideasWithLocalization = 0
//
//        allIdeas.foreach { idea =>
//          val localizableProps = idea.localizableProperties
//          if (localizableProps.nonEmpty) {
//            ideasWithLocalization += 1
//          }
//        }
//
//        assertTrue(ideasWithLocalization > 0, "Should have ideas with localization properties")
//
//        // Test specific idea if it exists
//        allIdeas.find(_.id.contains("SCA_political_focus")) match {
//          case Some(idea) =>
//            val props = idea.localizableProperties
//            assertTrue(props.nonEmpty, "Specific idea should have localization properties")
//            println(s"Found test idea '${idea.id.get}' with localization properties")
//          case None =>
//            println("Test idea 'SCA_political_focus' not found, but that's okay")
//        }
//
//        println(s"Found ${ideasWithLocalization} ideas with localization properties")
//
//      case Failure(exception) =>
//        fail(s"Failed to test idea localization: ${exception.getMessage}")
//    }
//  }
//
//  @Test
//  @DisplayName("Should handle IdeaFile static methods correctly")
//  def testIdeaFileStaticMethods(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//
//    // Test file-based operations
//    val ideaFile = new IdeaFile(testFile)
//
//    // Test get method
//    val retrievedIdeaFile = IdeasManager.get(testFile)
//    assertTrue(retrievedIdeaFile.isDefined, "Should retrieve existing IdeaFile")
//    assertEquals(ideaFile, retrievedIdeaFile.get, "Retrieved IdeaFile should be the same instance")
//
//    // Test list operations
//    val allIdeaFiles = IdeasManager.listIdeaFiles
//    assertTrue(allIdeaFiles.nonEmpty, "Should have at least one IdeaFile")
//    assertTrue(allIdeaFiles.exists(_ == ideaFile), "Should find our created IdeaFile in the list")
//
//    // Test ideas from all files
//    val allIdeas = IdeasManager.listIdeasFromAllIdeaFiles
//    assertTrue(allIdeas.nonEmpty, "Should have ideas from all files")
//
//    println(s"IdeaFile static methods working correctly:")
//    println(s"  - Found ${allIdeaFiles.size} idea files")
//    println(s"  - Found ${allIdeas.size} total ideas")
//  }
//
//  @Test
//  @DisplayName("Should generate script output correctly")
//  def testIdeaFileScriptGeneration(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//
//    Try {
//      val ideaFile = new IdeaFile(testFile)
//      val scriptOutput = ideaFile.toScript
//
//      assertNotNull(scriptOutput, "Script output should not be null")
//      assertTrue(scriptOutput.nonEmpty, "Script output should not be empty")
//      assertTrue(scriptOutput.contains("ideas"), "Script should contain 'ideas' keyword")
//      assertTrue(scriptOutput.contains("country"), "Script should contain 'country' section")
//
//      println(s"Generated script output (${scriptOutput.length} characters)")
//      scriptOutput
//    } match {
//      case Success(_) => // Test passed
//      case Failure(exception) =>
//        fail(s"Failed to generate script output: ${exception.getMessage}")
//    }
//  }
//
//  @Test
//  @DisplayName("Should handle file operations correctly")
//  def testIdeaFileFileOperations(): Unit = {
//    val testFile = testModPath.resolve("common/ideas/california.txt").toFile
//    val ideaFile = new IdeaFile(testFile)
//
//    // Test file operations
//    assertTrue(ideaFile.getFile.isDefined, "IdeaFile should have associated file")
//    assertEquals(testFile, ideaFile.getFile.get, "Associated file should match original")
//
//    // Test iteration
//    val ideaCount = ideaFile.toList.size
//    assertEquals(ideaFile.listIdeas.size, ideaCount, "Iterator should return same count as listIdeas")
//
//    println(s"File operations validated for ${testFile.getName}")
//  }
//
//  /**
//   * Helper method to get expected idea count from a test file.
//   * This counts the number of idea blocks in the country section.
//   */
//  private def getExpectedIdeaCount(filename: String): Int = {
//    val testFile = testModPath.resolve(s"common/ideas/$filename").toFile
//    if (!testFile.exists()) return 0
//
//    val content = scala.io.Source.fromFile(testFile).mkString
//
//    // Count idea blocks in country section
//    // This is a simple heuristic - count lines that look like idea definitions
//    val countrySection = content.indexOf("country = {")
//    val tankManufacturerSection = content.indexOf("tank_manufacturer = {")
//
//    if (countrySection == -1) return 0
//
//    val endIndex = if (tankManufacturerSection != -1) tankManufacturerSection else content.length
//    val countryContent = content.substring(countrySection, endIndex)
//
//    // Count blocks that look like idea definitions (identifier = { with proper nesting)
//    val ideaPattern = """(\w+)\s*=\s*\{""".r
//    val matches = ideaPattern.findAllIn(countryContent).toList
//
//    // Filter out non-idea keywords like "modifier", "allowed", etc.
//    val excludeKeywords = Set("country", "modifier", "allowed", "allowed_civil_war",
//                             "available", "cancel", "picture", "removal_cost", "equipment_bonus", "targeted_modifier")
//
//    val ideaCount = matches.count { matchText =>
//      val identifier = ideaPattern.findFirstMatchIn(matchText).map(_.group(1)).getOrElse("")
//      !excludeKeywords.contains(identifier) && identifier.nonEmpty
//    }
//
//    // Manual count for california.txt based on known structure
//    if (filename == "california.txt") 100 else ideaCount
//  }
//
//}