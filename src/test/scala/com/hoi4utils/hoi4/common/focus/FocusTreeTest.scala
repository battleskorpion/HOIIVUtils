// TODO sorry no junit 

//package com.hoi4utils.hoi4.common.focus
//
//import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager
//import com.hoi4utils.parser.Node
//import com.hoi4utils.shared.TestBase
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.{AfterEach, BeforeEach, DisplayName, Test}
//import org.scalatest.funsuite.AnyFunSuiteLike
//
//import java.io.File
//import scala.collection.mutable.ListBuffer
//import scala.util.{Failure, Success}
//
//class FocusTreeTest extends TestBase with AnyFunSuiteLike {
//
//	@BeforeEach
//	def setUpParserTest(): Unit = {
//		// Clear any global state from previous tests
//		FocusTreeManager.clear()
//
//		// Get paths to test resources
//		testModPath = getTestResourcePath("test_mods/demo_mod")
//	}
//
//	@AfterEach
//	def tearDownParserTest(): Unit = {
//		// Clear global state after test
//		FocusTreeManager.clear()
//	}
//	
//	@Test
//	@DisplayName("Should parse basic focus tree file consistently")
//	def testBasicFocusTreeParsing(): Unit = {
//		val focusTreeFiles = getTestFilesOfType("common/national_focus")
//
//		focusTreeFiles.foreach { file =>
//			val fileName = file.getName
//			val expectedCount = getExpectedFocusCount(fileName)
//
//			if (expectedCount > 0) {
//				assertConsistentParsing(
//					() => {
//						parseFile(file) match {
//							case Success(rootNode) => countFocusesInNode(rootNode)
//							case Failure(exception) =>
//								fail(s"Failed to parse $fileName: ${exception.getMessage}")
//						}
//					},
//					expectedResult = expectedCount,
//					runs = 3
//				)
//				println(s"✓ $fileName consistently parsed $expectedCount focuses")
//			}
//		}
//	}
//
//	@Test
//	@DisplayName("Should extract focus IDs consistently")
//	def testFocusIdExtraction(): Unit = {
//		val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile
//
//		parseFile(testFile) match {
//			case Success(rootNode) =>
//				val focusIds = extractFocusIds(rootNode)
//
//				// Should have the expected number of focuses
//				assertEquals(getExpectedFocusCount("test_focus_tree.txt"), focusIds.length,
//					"Number of extracted focus IDs should match expected count")
//
//				// All focus IDs should be unique
//				val uniqueIds = focusIds.toSet
//				assertEquals(focusIds.length, uniqueIds.size,
//					"All focus IDs should be unique")
//
//				// Focus IDs should not be empty
//				focusIds.foreach { id =>
//					assertFalse(id.isEmpty, "Focus ID should not be empty")
//				}
//
//				println(s"✓ Extracted ${focusIds.length} unique focus IDs: ${focusIds.take(5).mkString(", ")}...")
//
//			case Failure(exception) =>
//				fail(s"Failed to parse test focus tree: ${exception.getMessage}")
//		}
//	}
//
//	@Test
//	@DisplayName("Should validate focus tree structure")
//	def testFocusTreeStructure(): Unit = {
//		val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile
//
//		parseFile(testFile) match {
//			case Success(rootNode) =>
//				assertTrue(validateFocusTreeStructure(rootNode),
//					"Parsed file should contain valid focus_tree structure")
//
//				// Should be able to extract focus tree ID
//				val focusTreeId = extractFocusTreeId(rootNode)
//				assertTrue(focusTreeId.isDefined, "Focus tree should have an ID")
//
//				println(s"✓ Focus tree structure valid with ID: ${focusTreeId.get}")
//
//			case Failure(exception) =>
//				fail(s"Failed to parse focus tree structure: ${exception.getMessage}")
//		}
//	}
//
//	@Test
//	@DisplayName("Should handle parsing errors gracefully")
//	def testMalformedFileHandling(): Unit = {
//		val malformedContent =
//			"""
//				focus_tree = {
//					id = broken_tree
//					focus = {
//						id = broken_focus
//						# missing closing brace
//			"""
//
//		parseContent(malformedContent) match {
//			case Success(_) =>
//				// If it somehow succeeds, that's fine too - parser might be forgiving
//				println("✓ Parser handled malformed content gracefully")
//
//			case Failure(exception) =>
//				// Should fail with a meaningful error
//				assertNotNull(exception.getMessage)
//				assertFalse(exception.getMessage.isEmpty)
//				println(s"✓ Parser failed as expected with: ${exception.getMessage}")
//		}
//	}
//
//	@Test
//	@DisplayName("Should parse focus prerequisites correctly")
//	def testFocusPrerequisites(): Unit = {
//		val testFile = testModPath.resolve("common/national_focus/test_focus_tree.txt").toFile
//
//		parseFile(testFile) match {
//			case Success(rootNode) =>
//				var focusesWithPrereqs = 0
//				var totalPrereqReferences = 0
//				val allFocusIds = extractFocusIds(rootNode).toSet
//
//				def checkPrerequisites(node: Node): Unit = {
//					if (node.nameEquals("focus")) {
//						node.rawValue match {
//							case Some(children: ListBuffer[Node]) =>
//								val prereqNodes = children.filter(_.nameEquals("prerequisite"))
//								if (prereqNodes.nonEmpty) {
//									focusesWithPrereqs += 1
//
//									prereqNodes.foreach { prereqNode =>
//										prereqNode.rawValue match {
//											case Some(prereqChildren: ListBuffer[Node]) =>
//												prereqChildren.filter(_.nameEquals("focus")).foreach { focusRefNode =>
//													focusRefNode.rawValue match {
//														case Some(prereqId: String) =>
//															totalPrereqReferences += 1
//															assertTrue(allFocusIds.contains(prereqId),
//																s"Prerequisite '$prereqId' should reference a valid focus")
//														case _ =>
//													}
//												}
//											case _ =>
//										}
//									}
//								}
//							case _ =>
//						}
//					}
//
//					node.rawValue match {
//						case Some(children: ListBuffer[Node]) =>
//							children.foreach(checkPrerequisites)
//						case _ =>
//					}
//				}
//
//				checkPrerequisites(rootNode)
//
//				assertTrue(focusesWithPrereqs > 0,
//					"Test focus tree should have some focuses with prerequisites")
//				assertTrue(totalPrereqReferences > 0,
//					"Should have found some prerequisite references")
//
//				println(s"✓ Found $focusesWithPrereqs focuses with prerequisites, $totalPrereqReferences total references")
//
//			case Failure(exception) =>
//				fail(s"Failed to parse focus tree: ${exception.getMessage}")
//		}
//	}
//
//	@Test
//	@DisplayName("Should detect changes in test files")
//	def testFileChangeDetection(): Unit = {
//		val focusTreeFiles = getTestFilesOfType("common/national_focus")
//		val expectedCounts = createExpectedCounts()
//
//		focusTreeFiles.foreach { file =>
//			val filename = file.getName
//			if (expectedCounts.contains(filename)) {
//				parseFile(file) match {
//					case Success(rootNode) =>
//						val actualCount = countFocusesInNode(rootNode)
//						val expectedCount = expectedCounts(filename)
//
//						assertEquals(expectedCount, actualCount,
//							s"File $filename: Expected $expectedCount focuses but found $actualCount. " +
//								s"If this file was intentionally modified, update getExpectedFocusCount() method.")
//
//					case Failure(exception) =>
//						fail(s"Failed to parse $filename: ${exception.getMessage}")
//				}
//			}
//		}
//	}
//
//}
//
//
//// old file
////package com.hoi4utils.clausewitz.data.focus
////
////import com.hoi4utils.hoi4.focus.{Focus, FocusTree}
////
////import java.io.{File, PrintWriter}
////import org.scalatest.funsuite.AnyFunSuiteLike
////import org.scalatest.BeforeAndAfterEach
////
////import scala.collection.mutable
////
////
////// Assuming Property is defined in the localization package.
////import com.hoi4utils.clausewitz.localization.Property
////// Import CountryTag from its package.
////import com.hoi4utils.hoi4.country.CountryTag
////
////// Dummy implementation for CountryTag for testing purposes.
////class DummyCountryTag(tag: String) extends CountryTag(tag) {
////  override def get: String = tag
////  override def compareTo(o: CountryTag): Int = tag.compareTo(o.get)
////}
////
////class FocusTreeTest extends AnyFunSuiteLike with BeforeAndAfterEach {
////
////  // Clear FocusTree's static collections between tests.
////  override def beforeEach(): Unit = {
////    FocusTree.clear()
////  }
////
////  test("setID and toString return correct id") {
////    val focusTree = new FocusTree
////    focusTree.setID("FT1")
////    assert(focusTree.toString == "FT1")
////  }
////
////  test("addNewFocus increases focus count") {
////    val focusTree = new FocusTree
////    val initialCount = focusTree.listFocuses.size
////    val focus = new Focus(focusTree)
////    focus.id.set("focus1")
////    focusTree.addNewFocus(focus)
////    assert(focusTree.listFocuses.size == initialCount + 1)
////  }
////
////  test("listFocusIDs returns correct focus ids") {
////    val focusTree = new FocusTree
////    val focus1 = new Focus(focusTree)
////    focus1.id.set("f1")
////    val focus2 = new Focus(focusTree)
////    focus2.id.set("f2")
////    focusTree.addNewFocus(focus1)
////    focusTree.addNewFocus(focus2)
////    val ids = focusTree.listFocusIDs
////    assert(ids.contains("f1"))
////    assert(ids.contains("f2"))
////    assert(ids.size == 2)
////  }
////
////  test("minX returns minimum absolute x coordinate among focuses") {
////    val focusTree = new FocusTree
////    // Create dummy focuses by setting their x coordinate.
////    val focus1 = new Focus(focusTree)
////    focus1.x @= 10
////    val focus2 = new Focus(focusTree)
////    focus2.x @= 5
////    val focus3 = new Focus(focusTree)
////    focus3.x @= 20
////    focusTree.addNewFocus(focus1)
////    focusTree.addNewFocus(focus2)
////    focusTree.addNewFocus(focus3)
////    assert(focusTree.minX == 5)
////  }
////
////  test("nextTempFocusID returns unique id not already used") {
////    val focusTree = new FocusTree
////    // With no focuses added, the next temp ID should be "focus_1"
////    val tempId1 = focusTree.nextTempFocusID()
////    assert(tempId1 == "focus_1")
////    // Add a focus with that ID.
////    val focus = new Focus(focusTree)
////    focus.id.set(tempId1)
////    focusTree.addNewFocus(focus)
////    // The next temp ID should differ.
////    val tempId2 = focusTree.nextTempFocusID()
////    assert(tempId2 != tempId1)
////  }
////
////  test("setCountryTag sets and retrieves country tag correctly") {
////    val focusTree = new FocusTree
////    // Extract a stable reference for country.
////    val countryPDX = focusTree.country
////    // Ensure there is at least one TagModifierPDX in the country's modifier.
////    if (countryPDX.modifier.isEmpty) {
////      val tagModifier = new countryPDX.TagModifierPDX
////      countryPDX.modifier += tagModifier
////    }
////    val dummyTag = new DummyCountryTag("TST")
////    focusTree.setCountryTag(dummyTag)
////    val ct = focusTree.countryTag
////    assert(ct.isDefined)
////    assert(ct.get.get == "TST")
////  }
////
////  test("getLocalizableProperties returns property map with NAME equal to id") {
////    val focusTree = new FocusTree
////    focusTree.setID("FT_PROP")
////    val props = focusTree.getLocalizableProperties
////    assert(props.contains(Property.NAME))
////    assert(props(Property.NAME) == "FT_PROP")
////  }
////
////  test("getLocalizableGroup returns focuses collection") {
////    val focusTree = new FocusTree
////    val focus1 = new Focus(focusTree)
////    focus1.id.set("f1")
////    focusTree.addNewFocus(focus1)
////    val group = focusTree.getLocalizableGroup
////    assert(group.exists {
////      case f: Focus => f.id.value.contains("f1")
////      case _        => false
////    })
////  }
////
////  test("compareTo compares based on country tag and id") {
////    // Create two focus trees with different ids and country tags.
////    val ft1 = new FocusTree
////    ft1.setID("A")
////    val countryPDX1 = ft1.country
////    if (countryPDX1.modifier.isEmpty) {
////      countryPDX1.modifier += new countryPDX1.TagModifierPDX
////    }
////    val tag1 = new DummyCountryTag("TAG1")
////    ft1.setCountryTag(tag1)
////
////    val ft2 = new FocusTree
////    ft2.setID("B")
////    val countryPDX2 = ft2.country
////    if (countryPDX2.modifier.isEmpty) {
////      countryPDX2.modifier += new countryPDX2.TagModifierPDX
////    }
////    val tag2 = new DummyCountryTag("TAG2")
////    ft2.setCountryTag(tag2)
////
////    // Compare based on country tag first.
////    assert(ft1.compareTo(ft2) < 0)
////  }
////
////  test("iterator iterates over focuses") {
////    val focusTree = new FocusTree
////    val focus1 = new Focus(focusTree)
////    focus1.id.set("iter1")
////    val focus2 = new Focus(focusTree)
////    focus2.id.set("iter2")
////    focusTree.addNewFocus(focus1)
////    focusTree.addNewFocus(focus2)
////    val ids = focusTree.iterator.map(_.id.value.getOrElse("")).toList
////    assert(ids.contains("iter1"))
////    assert(ids.contains("iter2"))
////    assert(ids.size == 2)
////  }
////
////  test("setFile and getFile work correctly") {
////    // Create a temporary file.
////    val tempFile = File.createTempFile("testFocusTree", ".txt")
////    tempFile.deleteOnExit()
////    val focusTree = new FocusTree
////    focusTree.setFile(tempFile)
////    assert(focusTree.focusFile.contains(tempFile))
////    assert(focusTree.getFile.contains(tempFile))
////  }
////
////  test("static add and clear methods update static collections") {
////    val ft1 = new FocusTree
////    ft1.setID("FT_STATIC_1")
////    FocusTree.add(ft1)
////    assert(FocusTree.listFocusTrees.exists(_.toString == "FT_STATIC_1"))
////    FocusTree.clear()
////    assert(FocusTree.listFocusTrees.isEmpty)
////  }
////
////  test("FocusTree.get(CountryTag) returns correct focus tree") {
////    val ft = new FocusTree
////    ft.setID("FT_CT")
////    val countryPDX = ft.country
////    // Ensure a TagModifierPDX exists.
////    if (countryPDX.modifier.isEmpty) {
////      countryPDX.modifier += new countryPDX.TagModifierPDX
////    }
////    val dummyTag = new DummyCountryTag("COUNTRY1")
////    ft.setCountryTag(dummyTag)
////    FocusTree.add(ft)
////    val retrieved = FocusTree.get(dummyTag)
////    assert(retrieved != null)
////    assert(retrieved.toString == "FT_CT")
////  }
////}