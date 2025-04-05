package com.hoi4utils.clausewitz.script

import language.experimental.namedTuples
import com.hoi4utils.clausewitz.data.focus.FocusTree
import com.hoi4utils.clausewitz.map.StrategicRegion
import com.hoi4utils.clausewitz_parser.{Node, Parser}
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File

class PDXScriptTests extends AnyFunSuiteLike {

  private val testPath = "src/test/resources/clausewitz_parser/"
  private val validFocusTreeTestFiles = List(
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
    new File(testPath + "texas_tree.txt"),
  )
  private val validFocusTestFiles = List(
    new File(testPath + "focus_with_search_filter_test1.txt"),
    new File(testPath + "focus_with_search_filter_test2.txt"),
    new File(testPath + "carriage_return.txt"),
  )
  private val validStratRegionTestFiles = List(
    new File(testPath + "StrategicRegion.txt"),
  )
  private val filesToTest = List(
    new File(testPath + "specialinfantry.txt"),
  )
    .appendedAll(validFocusTreeTestFiles)
    .appendedAll(validFocusTestFiles)
    .appendedAll(validStratRegionTestFiles)

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

  def withValidFocusTrees(testFunction: FocusTree => Unit): Unit = {
    validFocusTreeTestFiles.foreach(file => {
      val parser = new Parser(file)
      val node = parser.parse
      assert(node != null, s"Failed to parse $file")
      val focusTree = new FocusTree()
      focusTree.loadPDX(node)
      testFunction(focusTree)
    })
  }


  def withValidStratRegions(testFunction: StrategicRegion => Unit): Unit = {
    validStratRegionTestFiles.foreach(file => {
      val parser = new Parser(file)
      val node = parser.parse
      assert(node != null, s"Failed to parse $file")
      val stratRegion = new StrategicRegion()
      stratRegion.loadPDX(node)
      testFunction(stratRegion)
    })
  }


  test("Some PDXScript objects should be loaded through loadPDX() when present") {
    withValidFocusTrees { focusTree =>
      assert(focusTree.pdxProperties.nonEmpty)
    }
  }

  test("MultiPDX should load PDXScript objects") {
    withValidFocusTrees { focusTree =>
      assert(focusTree.focuses.nonEmpty)
    }
  }

//  test("MultiPDX should be defined when there is some PDXScript it can load") {
//    withFocusTrees { focusTree =>
//      assert(focusTree.focuses.isDefined)
//    }
//  }

  test("StringPDX should be defined when present") {
    withValidFocusTrees { focusTree =>
      assert(focusTree.id.isDefined)
    }
  }

  test("StringPDX should have an obtainable value when applicable") {
    withValidFocusTrees { focusTree =>
      assert(focusTree.id.value.nonEmpty)
    }
  }

  test("StringPDX test") {
    withValidFocusTrees { focusTree =>
      assert(focusTree.focuses
        .flatMap(_.pdxProperties)
        .forall(_ match {
          case s: StringPDX =>
            s.getOrElse("").isInstanceOf[String]
          case _ => true
        })
      )
    }
  }

//  test("ReferencePDX test") {
//    withValidFocusTrees { focusTree =>
//      assert(focusTree.country.isDefined, s"Country is not defined: ${focusTree.country}, focus tree: ${focusTree}")
//      assert(focusTree.country.value.nonEmpty)
//      assert(focusTree.country.referenceName.nonEmpty)
//    }
//  }

  test("Strategic region has findable between") {
    withValidStratRegions { stratRegion =>
      assert(stratRegion.pdxProperties.nonEmpty)
      assert(stratRegion.weather.period.nonEmpty)
      assert(stratRegion.weather.period.exists(_.between.exists(_ @== 4.11)))
      assert(stratRegion.weather.period.size == 13)
    }
  }

  test("remove region") {
    withValidStratRegions { stratRegion =>
      assert(stratRegion.pdxProperties.nonEmpty)
      stratRegion.weather.period.removeIf(_.between.exists(_ @== 4.11))
      assert(stratRegion.weather.period.size == 12)
      stratRegion.savePDX()
    }
  }
  
}
