package com.hoi4utils.hoi4.focus

import com.hoi4utils.HOIIVUtilsTest
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.Config
import com.hoi4utils.script.StringPDX

import java.io.File


class FocusTreeTest extends HOIIVUtilsTest {
  def getFocusTreeFile(config: Config, string: String): File = {
    val modPath = config.getProperties.getProperty("mod.path")
    val f = File(modPath + string)
    f
  }

  val testFocusTreePath: String = "\\common\\national_focus\\california.txt"

  val config: Config = setup()

  val focusTreeFile: File = getFocusTreeFile(config, testFocusTreePath)

  val readSuccess: Boolean = FocusTree.read(focusTreeFile)

  val focusTree: FocusTree = FocusTree.get(focusTreeFile)
    .getOrElse(throw new RuntimeException(s"Failed to load focus tree from file: ${focusTreeFile.getPath}"))



  test("Focus File should be parsed with all nodes") {
    val fileFocusCount = HOIIVUtilsTest().countOccurrencesInFile(focusTreeFile, "focus = {")
    assert(focusTree.focuses.size == fileFocusCount, s"\nFile: ${focusTreeFile.getPath}\n    Focuses found in file: $fileFocusCount\n    Focuses loaded: ${focusTree.focuses.size}")
  }

  test("Some PDXScript objects should be loaded through loadPDX() when present") {
    assert(focusTree.pdxProperties.nonEmpty, "FocusTree should have some PDXScript properties loaded")
  }

  test("MultiPDX should load PDXScript objects") {
    assert(focusTree.focuses.nonEmpty)
  }

  test("MultiPDX should be defined when there is some PDXScript it can load") {
    assert(focusTree.focuses.isDefined)
  }

  test("StringPDX should be defined when present") {
    assert(focusTree.id.isDefined)
  }

  test("StringPDX should have an obtainable value when applicable") {
    assert(focusTree.id.value.nonEmpty)
  }

  test("StringPDX test") {
    assert(focusTree.focuses.flatMap(_.pdxProperties).forall {
      case s: StringPDX => s.getOrElse("").isInstanceOf[String]
      case _ => true
    }
    )
  }

  test("StringPDX value instance test") {
    focusTree.pdxProperties.foreach {
      case s: StringPDX =>
        if (s.isDefined) {
          assert(s.valueIsInstanceOf[String], s"Expected StringPDX to have a String value, but got ${s.value}")
        }
      case _ => // do nothing
    }
  }
}