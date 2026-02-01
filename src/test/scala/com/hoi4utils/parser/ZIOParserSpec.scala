package com.hoi4utils.parser

import org.scalamock.ziotest.ScalamockZIOSpec
import zio.test.Result.fail
import zio.test.junit.JUnitRunnableSpec
import zio.{Scope, Task, ZIO}
import zio.test.{Spec, TestEnvironment, TestFailure, TestResult, assertTrue}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

// again i had JunitRunnableSpec extended then with scalamock but issues and didnt work anyways
object ZIOParserSpec extends ScalamockZIOSpec {

  private val testPath = "src/test/resources/pdx/"
  private val multiPDXFilesToTest = List(
    new File(testPath + "shared_focuses_1_shortest.txt"),
    new File(testPath + "shared_focuses_1_shorter.txt"),
    new File(testPath + "shared_focuses_1_short.txt"),
    new File(testPath + "shared_focuses_1_longer.txt"),
    new File(testPath + "shared_focuses_1_longerer.txt"),
    new File(testPath + "shared_focuses_1_longererer.txt"),
    new File(testPath + "shared_focuses_1_longerererer.txt"),
    new File(testPath + "shared_focuses_1_longy.txt"),
    new File(testPath + "shared_focuses_1_long.txt"),
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

  def withParsedFile(file: File)(testFunction: Node => TestResult): ZIO[Any, Throwable, TestResult] =
    val parser = new ZIOParser(file)
    parser.parse.map(testFunction)

  def foreachParsed(files: List[File] = filesToTest)(f: Node => TestResult): ZIO[Any, Throwable, TestResult] =
    ZIO.foreach(files)(new ZIOParser(_).parse).map { nodes =>
      TestResult.allSuccesses(nodes.map(f))
    }

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("ZIOParser")(
    test("File root node should be nonempty") {
      foreachParsed() { node =>
        assertTrue(node.nonEmpty, node.toList.nonEmpty)
      }
    },
    test("Node should not include carriage returns") {
      foreachParsed() { node =>
        assertTrue(
          node.toList.forall(n => !n.name.contains("\r") && !n.valueAsString.contains("\r"))
        )
      }
    },
    test("the '=' operator should be with an identifier") {
      foreachParsed() { node =>
        TestResult.allSuccesses(
          node.toList.map (n => assertTrue(n.identifier.nonEmpty && n.name.nonEmpty) ?? s"Node $n has no identifier")
        )
      }
    },
    test("sub node using find(): specialinfantry.txt should have parsed sub_units") {
      withParsedFile(new File(testPath + "specialinfantry.txt")) { node =>
        val sprite = for {
          subunits <- node.find("sub_units")
          enforcer <- subunits.find("mobenforcer")
          sprite   <- enforcer.find("sprite")
        } yield sprite

        // Failure here tells you exactly what was missing in the path
        assertTrue(sprite.isDefined, sprite.get.nonEmpty)
      }
    },
    test("Ints should be read as ints and not as type double") {
      withParsedFile(new File(testPath + "SMD_Maryland.txt")) { node =>
        val capital = node.find("capital")
        assertTrue(capital.isDefined, capital.get.isInt)
      }
    },
    test("textSpriteType block is parsed correctly") {
      // The input contains a stray semicolon after effectFile.
      val input =
        """textSpriteType = {
          |    name = "largefloaterbutton"
          |    texturefile = "gfx//interface//button_type_1.tga"
          |    noOfFrames = 1
          |    effectFile = "gfx/FX/buttonstate.lua";
          |}""".stripMargin

      val parser = new ZIOParser(input)
      for {
        rootNode <- parser.parse
        textSprite = rootNode.headOption("textSpriteType")
      } yield {
        // Find the textSpriteType node.
        assertTrue(textSprite.isDefined, {
          textSprite.exists { node =>
            // Verify child nodes are present.
            val name: String = node.get("name").map(_.valueAsString) 
            val texturefile = node.get("texturefile").map(_.valueAsString)
            val noOfFrames = node.get("noOfFrames").map(_.valueAsString)
            val effectFile = node.get("effectFile").map(_.valueAsString)

            name.contains("largefloaterbutton") &&
              texturefile.contains("gfx//interface//button_type_1.tga") &&
              noOfFrames.contains("1") &&
              effectFile.contains("gfx/FX/buttonstate.lua")
          }
        })
      }
    },
    test("focus id line is parsed correctly") {
      // The input contains a stray semicolon after effectFile.
      val input =
        """focus = {
          |    id = SMA_Maryland
          |}
          |""".stripMargin

      val parser = new ZIOParser(input)
      for {
        rootNode <- parser.parse
        focusNode = rootNode.get("focus")
        idNode = focusNode.flatMap(_.find("id"))
      } yield {
        assertTrue(
          rootNode.nonEmpty,
          rootNode.contains("focus"),
          focusNode.exists(_.identifier.isDefined),
          idNode.exists(_.valueAsString == "SMA_Maryland"),
          idNode.exists(_.identifier.isDefined),
          idNode.exists(_.valueAsString == "SMA_Maryland"),
          idNode.exists(_.identifier.isDefined),
          idNode.exists(_.identifier.get == "id")
        )
      }
      //assert(root.toScript == input, "Output does not match input")
    },
    test("Multiple shared focuses are parsed correctly") {
      foreachParsed(multiPDXFilesToTest) { node =>
        val sharedFocuses = node.filter("shared_focus")
        assertTrue(
          sharedFocuses.nonEmpty,
          sharedFocuses.forall(f =>
            f.find("id").isDefined && f.find("x").isDefined
          )
        )
      }
    }
  )
}
