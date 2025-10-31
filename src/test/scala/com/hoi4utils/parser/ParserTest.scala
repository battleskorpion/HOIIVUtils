package com.hoi4utils.parser

import com.hoi4utils.shared.TestBase
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Try, Failure, Success}

class ParserTest extends AnyFunSuiteLike {

  private val testPath = "src/test/resources/clausewitz_parser/"
  private val multiPDXFilesToTest = List(
    new File(testPath + "shared_focuses_multiple.txt"),
    new File(testPath + "shared_focuses_simple.txt")
  )
  private val filesToTest: List[File] = List(
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
    new File(testPath + "focus_with_search_filter_test1.txt"),
    new File(testPath + "focus_with_search_filter_test2.txt"),
    new File(testPath + "carriage_return.txt"),
    new File(testPath + "specialinfantry.txt")
  ).appendedAll(multiPDXFilesToTest)


  def withParsedFile(testFunction: Node => Unit, file: File): Unit = {
    val parser = new Parser(file)
    Try(parser.parse) match
      case Success(node) =>
        assert(node != null, s"Parser returned null for file: ${file.getPath}")
        testFunction(node)
      case Failure(e) =>
        // Fail the test and include full context
        fail(s"Failed to parse file [${file.getPath}] - ${e.getClass.getSimpleName}: ${e.getMessage}\n" +
          s"Exception: ${e.getStackTrace.mkString("\n")}")
  }

  def withParsedFiles(testFunction: Node => Unit): Unit = {
    filesToTest.foreach { file =>
      val parser = new Parser(file)
      Try(parser.parse) match
        case Success(node) =>
          assert(node != null, s"Parser returned null for file: ${file.getPath}")
          testFunction(node)
        case Failure(e) =>
          fail(s"Failed to parse file [${file.getPath}] - ${e.getClass.getSimpleName}: ${e.getMessage}\n" +
            s"Exception: ${e.getStackTrace.mkString("\n")}")
    }
  }

  def withParsedMultiPDXFiles(testFunction: Node => Unit): Unit = {
    multiPDXFilesToTest.foreach { file =>
      val parser = new Parser(file)
      Try(parser.parse) match
        case Success(node) =>
          assert(node != null, s"Parser returned null for file: ${file.getPath}")
          testFunction(node)
        case Failure(e) =>
          fail(s"Failed to parse multi-PDX file [${file.getPath}] - ${e.getClass.getSimpleName}: ${e.getMessage}\n" +
            s"Exception: ${e.getStackTrace.mkString("\n")}")
    }
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

  test("Multiple shared focuses are parsed correctly") {
    withParsedMultiPDXFiles ({ node =>
      val sharedFocuses = node.filter("shared_focus")
      assert(sharedFocuses.nonEmpty, "No shared_focus nodes found")

      sharedFocuses.foreach(focus =>
        assert(focus.find("id").isDefined, s"Focus [$focus] id is not defined")
        assert(focus.find("x").isDefined, s"Focus [$focus] x is not defined")
      )

    })
  }

}
