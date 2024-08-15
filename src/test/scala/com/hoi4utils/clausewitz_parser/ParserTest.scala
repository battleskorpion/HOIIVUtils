package com.hoi4utils.clausewitz_parser

// this actually seems to work. error highlighting may be scala plugin issue
// update: finally not error highlighting incorrectly??? cool ! cool :) cool.
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File

class ParserTest extends AnyFunSuiteLike {

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

  test("the '=' operator should be with an identifier") {
    withParsedFiles { node =>
      node.toList.foreach(n => {
        assert(n.identifier != null, s"Node $n has no identifier")
      })
    }
  }

  test("specialinfantry.txt should have parsed sub_units") {
    withParsedFile ({ node =>
      val subunits = node.find("sub_units").getOrElse(fail("sub_units not found"))
      assert(subunits.find("mobenforcer").isDefined)
      assert(subunits.find("mobenforcer").get.find("sprite").nonEmpty)
    }, new File(testPath + "specialinfantry.txt"))
  }

}
