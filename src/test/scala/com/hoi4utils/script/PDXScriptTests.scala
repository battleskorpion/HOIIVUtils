package com.hoi4utils.script

import com.hoi4utils.hoi4.common.country_tags.CountryTagService
import com.hoi4utils.hoi4.common.national_focus.{FocusTree, FocusTreeManager}
import com.hoi4utils.hoi4.map.strategicregions.StrategicRegion
import com.hoi4utils.parser.{Node, Parser, Tokenizer, ZIOParser}
import com.hoi4utils.script.datatype.StringPDX
import org.scalamock.ziotest.ScalamockZIOSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}

import java.io.File

object PDXScriptTests extends ScalamockZIOSpec {

  private val testPath = "src/test/resources/pdx/"
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

  def foreachParsed(files: List[File] = filesToTest)(f: Node => TestResult): ZIO[Any, Throwable, TestResult] =
    ZIO.foreach(files)(new ZIOParser(_).parse).map { nodes =>
      TestResult.allSuccesses(nodes.map(f))
    }

  def withParsedFile(file: File)(testFunction: Node => TestResult): ZIO[Any, Throwable, TestResult] =
    val parser = new ZIOParser(file)
    parser.parse.map(testFunction)

  def foreachFocusTree(files: List[File] = validFocusTreeTestFiles)(f: FocusTree => TestResult): ZIO[FocusTreeManager & CountryTagService, Throwable, TestResult] =
    ZIO.foreach(files) { file =>
      for {
        treeManager <- ZIO.service[FocusTreeManager]
        tagsService <- ZIO.service[CountryTagService]
        node <- new ZIOParser(file).parse
        pdx <- ZIO.attempt {
          val focusTree = new FocusTree()(treeManager, tagsService)
          focusTree.loadPDX(node, Some(file))
          focusTree
        }
      } yield pdx
    }.map { pdxs =>
      TestResult.allSuccesses(pdxs.map(f))
    }

  def foreachStratRegion(files: List[File] = validStratRegionTestFiles)(f: StrategicRegion => TestResult): ZIO[Any, Throwable, TestResult] =
    ZIO.foreach(files) { file =>
      new ZIOParser(file).parse.flatMap { node =>
        ZIO.attempt {
          val stratRegion = new StrategicRegion()
          stratRegion.loadPDX(node, Some(file))
          stratRegion
        }
      }
    }.map { pdxs =>
      TestResult.allSuccesses(pdxs.map(f))
    }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("PDXScript")(
      test("Some PDXScript objects should be loaded through loadPDX() when present") {
        foreachFocusTree() { focusTree =>
          assertTrue(focusTree.pdxProperties.nonEmpty)
        }
      },
      test("MultiPDX should load PDXScript objects") {
        foreachFocusTree() { focusTree =>
          assertTrue(focusTree.focuses.nonEmpty)
        }
      },
      test("StringPDX should be defined when present") {
        foreachFocusTree() { focusTree =>
          assertTrue(focusTree.id.isDefined)
        }
      },
      test("StringPDX should have an obtainable value when applicable") {
        foreachFocusTree() { focusTree =>
          assertTrue(focusTree.id.value.nonEmpty)
        }
      },
      test("StringPDX test") {
        foreachFocusTree() { focusTree =>
          assertTrue(
            focusTree.focuses
              .flatMap(_.pdxProperties)
              .forall {
                case s: StringPDX =>
                  s.getOrElse("").isInstanceOf[String]
                case _ => true
              }
          )
        }
      },
      test("StringPDX value instance test") {
        foreachFocusTree() { focusTree =>
          val results = focusTree.pdxProperties.collect {
            case s: StringPDX if s.isDefined => assertTrue(s.valueIsInstanceOf[String])
          }
          TestResult.allSuccesses(results)
        }
      },
      test("Strategic region has findable between") {
        foreachStratRegion() { stratRegion =>
          assertTrue(
            stratRegion.pdxProperties.nonEmpty,
            stratRegion.weather.period.nonEmpty,
            stratRegion.weather.period.exists(_.between.exists(_ @== 4.11)),
            stratRegion.weather.period.size == 13
          )
        }
      },
      test("remove region") {
        foreachStratRegion() { stratRegion =>
          assertTrue(
            stratRegion.pdxProperties.nonEmpty,
            stratRegion.weather.period.filterNot(_.between.exists(_ @== 4.11)).size == 12
          )
        }
      },
      test("peek does not advance the tokenizer") {
        for {
          tokenizer <- ZIO.succeed(new Tokenizer("abc"))
          firstPeek  = tokenizer.peek
          secondPeek = tokenizer.peek
          _          = tokenizer.next // side effect
          nextPeek   = tokenizer.peek
        } yield {
          assertTrue(
            firstPeek.isDefined,
            secondPeek.isDefined,
            firstPeek == secondPeek,
            tokenizer.peek != firstPeek
          )
        }
      },
      test("tokens are returned in order") {
        // Input with a few tokens separated by whitespace.
        val input = "a b c"
        val tokenizer = new Tokenizer(input)

        // Collect all tokens until the iterator is exhausted.
        val tokens = Iterator.continually(tokenizer.next).takeWhile(_.isDefined).flatten.toList
        val assertNonEmpty = assertTrue(tokens.nonEmpty) ?? "Expected some tokens to be returned"

        // Verify that the tokens appear in increasing order by their starting index.
        val orderChecks = tokens.sliding(2).map {
          case List(prev, current) =>
            assertTrue(current.start >= prev.start) ?? s"Order violation: ${prev.value} -> ${current.value}"
          case _ => assertTrue(true)
        }.toList

        TestResult.allSuccesses(assertNonEmpty :: orderChecks)
      },
      test("peek returns same token repeatedly and next advances") {
        val input = "hello world"
        val tokenizer = new Tokenizer(input)

        // Peek several times before consuming.
        val peek1 = tokenizer.peek
        val peek2 = tokenizer.peek
        val sameCheck = assertTrue(peek1 == peek2) ?? "Multiple peek calls should return the same token"

        // Consume first token.
        val firstToken = tokenizer.next
        // Now, a subsequent peek should return the second token.
        val secondToken = tokenizer.peek
        val tokenCheck = assertTrue(firstToken != secondToken) ?? "After next, peek should return a new token different from the previously consumed one"
        TestResult.allSuccesses(sameCheck :: tokenCheck :: Nil)
      },
      test("returns None when no tokens remain") {
        val input = "" // empty input should produce no tokens.
        val tokenizer = new Tokenizer(input)
        // next and peek should return None.
        assertTrue(tokenizer.peek.isEmpty) ?? "Peek should return None for empty input" &&
        assertTrue(tokenizer.next.isEmpty) ?? "Next should return None for empty input"
      },
      test("SpriteType objects are loaded with correct properties") {
        // Assumes a test file "sprite_types.txt" exists under testPath containing the SpriteType definitions.
        val spriteFile = new File(testPath + "sprite_types.txt")
        val parser = new Parser(spriteFile)
        val node = parser.parse
        val parseCheck = assertTrue(node != null) ?? s"Failed to parse ${spriteFile.getName}"

        // Filter nodes with the name "SpriteType"
        val spriteNodes = node.filter(_.name == "SpriteType")
        val nonEmptyCheck = assertTrue(spriteNodes.nonEmpty) ?? "Expected at least one SpriteType definition"

        val spriteResults = spriteNodes.map { sprite =>
          val nameOpt = sprite.find("name")
          assertTrue(nameOpt.isDefined) ?? "SpriteType should have a name"
          val name = nameOpt.get

          val assertBasicProps = assertTrue(
            nameOpt.isDefined,
            sprite.find("texturefile").isDefined,
            sprite.find("effectFile").isDefined,
            sprite.find("legacy_lazy_load").exists(_.valueContains("no"))
          ) ?? s"Basic property missing in SpriteType '$name'"

          // Each SpriteType should have exactly two animation blocks.
          val animations = sprite.filter(_.name == "animation")
          val animSizeCheck = assertTrue(animations.size == 2) ?? s"Expected exactly two animation blocks for SpriteType '$name', but found ${animations.size}"

          val animChecks = animations.map { anim =>
            // Verify required fields exist within each animation block.
            assertTrue(
              anim.find("animationmaskfile").isDefined,
              anim.find("animationtexturefile").isDefined,
              anim.find("animationrotation").isDefined,
              anim.find("animationlooping").isDefined,
              anim.find("animationtime").isDefined,
              anim.find("animationdelay").isDefined,
              anim.find("animationblendmode").isDefined,
              anim.find("animationtype").isDefined,
              anim.find("animationrotationoffset").isDefined,
              anim.find("animationtexturescale").isDefined,
            ) ?? s"Animation block field missing in '$name'"
          }

          assertBasicProps && animSizeCheck && TestResult.allSuccesses(animChecks)
        }

        TestResult.allSuccesses(parseCheck :: nonEmptyCheck :: spriteResults.toList)
      }
      // TODO? 
//      test("SMA Simple") {
//        val file = new File(testPath + "Massachusetts_focus_simple.txt")
//        val treeSMA = new FocusTree(file)
//        assertTrue(treeSMA.pdxProperties.nonEmpty)
//      },
//      test("SMA") {
//        val file = new File(testPath + "Massachusetts_focus.txt")
//        val treeSMA = new FocusTree(file)
//        assertTrue(treeSMA.pdxProperties.nonEmpty)
//      }
    ).provide(
      FocusTreeManager.live,
      CountryTagService.live
    )

//  test("MultiPDX should be defined when there is some PDXScript it can load") {
//    withFocusTrees { focusTree =>
//      assertTrue(focusTree.focuses.isDefined)
//    }
//  }

  //  test("ReferencePDX test") {
//    foreachFocusTree { focusTree =>
//      assertTrue(focusTree.country.isDefined, s"Country is not defined: ${focusTree.country}, focus tree: ${focusTree}")
//      assertTrue(focusTree.country.value.nonEmpty)
//      assertTrue(focusTree.country.referenceName.nonEmpty)
//    }
//  }
}
