package com.hoi4utils.clausewitz_parser

import dotty.tools.dotc.core.StdNames.str
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

  test("Node toScript()") {
    filesToTest.foreach { file =>
      val parser = new Parser(file)
      val node = parser.parse
      assert(node != null, s"Failed to parse $file")

      // Define the expected file based on the input file name.
      // Here, we assume the expected file is in the same testPath folder with a "_toScript.txt" suffix.
      val expectedFileName = file.getName.replace(".txt", "_toScript.txt")
      val expectedFile = new File(testPath + expectedFileName)

      // Read the expected content.
      val expectedContent = scala.io.Source.fromFile(expectedFile).mkString
      // Get the actual content from toScript().
      val actualContent = node.toScript

      // Compare the actual output to the expected output.
      assert(expectedContent == actualContent,
        s"Output from ${file.getName} did not match expected output.")
    }
  }


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
        assert(capital.nodeValue.isInt)
    }, new File(testPath + "SMD_Maryland.txt"))
  }

}
