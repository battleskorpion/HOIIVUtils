// todo impl later

//package com.hoi4utils.clausewitz.data.focus
//
//import com.hoi4utils.hoi4.focus.{Focus, FocusTree}
//
//import java.io.{File, PrintWriter}
//import org.scalatest.funsuite.AnyFunSuiteLike
//import org.scalatest.BeforeAndAfterEach
//
//import scala.collection.mutable
//
//
//// Assuming Property is defined in the localization package.
//import com.hoi4utils.clausewitz.localization.Property
//// Import CountryTag from its package.
//import com.hoi4utils.hoi4.country.CountryTag
//
//// Dummy implementation for CountryTag for testing purposes.
//class DummyCountryTag(tag: String) extends CountryTag(tag) {
//  override def get: String = tag
//  override def compareTo(o: CountryTag): Int = tag.compareTo(o.get)
//}
//
//class FocusTreeTest extends AnyFunSuiteLike with BeforeAndAfterEach {
//
//  // Clear FocusTree's static collections between tests.
//  override def beforeEach(): Unit = {
//    FocusTree.clear()
//  }
//
//  test("setID and toString return correct id") {
//    val focusTree = new FocusTree
//    focusTree.setID("FT1")
//    assert(focusTree.toString == "FT1")
//  }
//
//  test("addNewFocus increases focus count") {
//    val focusTree = new FocusTree
//    val initialCount = focusTree.listFocuses.size
//    val focus = new Focus(focusTree)
//    focus.id.set("focus1")
//    focusTree.addNewFocus(focus)
//    assert(focusTree.listFocuses.size == initialCount + 1)
//  }
//
//  test("listFocusIDs returns correct focus ids") {
//    val focusTree = new FocusTree
//    val focus1 = new Focus(focusTree)
//    focus1.id.set("f1")
//    val focus2 = new Focus(focusTree)
//    focus2.id.set("f2")
//    focusTree.addNewFocus(focus1)
//    focusTree.addNewFocus(focus2)
//    val ids = focusTree.listFocusIDs
//    assert(ids.contains("f1"))
//    assert(ids.contains("f2"))
//    assert(ids.size == 2)
//  }
//
//  test("minX returns minimum absolute x coordinate among focuses") {
//    val focusTree = new FocusTree
//    // Create dummy focuses by setting their x coordinate.
//    val focus1 = new Focus(focusTree)
//    focus1.x @= 10
//    val focus2 = new Focus(focusTree)
//    focus2.x @= 5
//    val focus3 = new Focus(focusTree)
//    focus3.x @= 20
//    focusTree.addNewFocus(focus1)
//    focusTree.addNewFocus(focus2)
//    focusTree.addNewFocus(focus3)
//    assert(focusTree.minX == 5)
//  }
//
//  test("nextTempFocusID returns unique id not already used") {
//    val focusTree = new FocusTree
//    // With no focuses added, the next temp ID should be "focus_1"
//    val tempId1 = focusTree.nextTempFocusID()
//    assert(tempId1 == "focus_1")
//    // Add a focus with that ID.
//    val focus = new Focus(focusTree)
//    focus.id.set(tempId1)
//    focusTree.addNewFocus(focus)
//    // The next temp ID should differ.
//    val tempId2 = focusTree.nextTempFocusID()
//    assert(tempId2 != tempId1)
//  }
//
//  test("setCountryTag sets and retrieves country tag correctly") {
//    val focusTree = new FocusTree
//    // Extract a stable reference for country.
//    val countryPDX = focusTree.country
//    // Ensure there is at least one TagModifierPDX in the country's modifier.
//    if (countryPDX.modifier.isEmpty) {
//      val tagModifier = new countryPDX.TagModifierPDX
//      countryPDX.modifier += tagModifier
//    }
//    val dummyTag = new DummyCountryTag("TST")
//    focusTree.setCountryTag(dummyTag)
//    val ct = focusTree.countryTag
//    assert(ct.isDefined)
//    assert(ct.get.get == "TST")
//  }
//
//  test("getLocalizableProperties returns property map with NAME equal to id") {
//    val focusTree = new FocusTree
//    focusTree.setID("FT_PROP")
//    val props = focusTree.getLocalizableProperties
//    assert(props.contains(Property.NAME))
//    assert(props(Property.NAME) == "FT_PROP")
//  }
//
//  test("getLocalizableGroup returns focuses collection") {
//    val focusTree = new FocusTree
//    val focus1 = new Focus(focusTree)
//    focus1.id.set("f1")
//    focusTree.addNewFocus(focus1)
//    val group = focusTree.getLocalizableGroup
//    assert(group.exists {
//      case f: Focus => f.id.value.contains("f1")
//      case _        => false
//    })
//  }
//
//  test("compareTo compares based on country tag and id") {
//    // Create two focus trees with different ids and country tags.
//    val ft1 = new FocusTree
//    ft1.setID("A")
//    val countryPDX1 = ft1.country
//    if (countryPDX1.modifier.isEmpty) {
//      countryPDX1.modifier += new countryPDX1.TagModifierPDX
//    }
//    val tag1 = new DummyCountryTag("TAG1")
//    ft1.setCountryTag(tag1)
//
//    val ft2 = new FocusTree
//    ft2.setID("B")
//    val countryPDX2 = ft2.country
//    if (countryPDX2.modifier.isEmpty) {
//      countryPDX2.modifier += new countryPDX2.TagModifierPDX
//    }
//    val tag2 = new DummyCountryTag("TAG2")
//    ft2.setCountryTag(tag2)
//
//    // Compare based on country tag first.
//    assert(ft1.compareTo(ft2) < 0)
//  }
//
//  test("iterator iterates over focuses") {
//    val focusTree = new FocusTree
//    val focus1 = new Focus(focusTree)
//    focus1.id.set("iter1")
//    val focus2 = new Focus(focusTree)
//    focus2.id.set("iter2")
//    focusTree.addNewFocus(focus1)
//    focusTree.addNewFocus(focus2)
//    val ids = focusTree.iterator.map(_.id.value.getOrElse("")).toList
//    assert(ids.contains("iter1"))
//    assert(ids.contains("iter2"))
//    assert(ids.size == 2)
//  }
//
//  test("setFile and getFile work correctly") {
//    // Create a temporary file.
//    val tempFile = File.createTempFile("testFocusTree", ".txt")
//    tempFile.deleteOnExit()
//    val focusTree = new FocusTree
//    focusTree.setFile(tempFile)
//    assert(focusTree.focusFile.contains(tempFile))
//    assert(focusTree.getFile.contains(tempFile))
//  }
//
//  test("static add and clear methods update static collections") {
//    val ft1 = new FocusTree
//    ft1.setID("FT_STATIC_1")
//    FocusTree.add(ft1)
//    assert(FocusTree.listFocusTrees.exists(_.toString == "FT_STATIC_1"))
//    FocusTree.clear()
//    assert(FocusTree.listFocusTrees.isEmpty)
//  }
//
//  test("FocusTree.get(CountryTag) returns correct focus tree") {
//    val ft = new FocusTree
//    ft.setID("FT_CT")
//    val countryPDX = ft.country
//    // Ensure a TagModifierPDX exists.
//    if (countryPDX.modifier.isEmpty) {
//      countryPDX.modifier += new countryPDX.TagModifierPDX
//    }
//    val dummyTag = new DummyCountryTag("COUNTRY1")
//    ft.setCountryTag(dummyTag)
//    FocusTree.add(ft)
//    val retrieved = FocusTree.get(dummyTag)
//    assert(retrieved != null)
//    assert(retrieved.toString == "FT_CT")
//  }
//}
