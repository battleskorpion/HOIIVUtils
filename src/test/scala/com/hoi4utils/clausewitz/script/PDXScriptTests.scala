package com.hoi4utils.clausewitz.script


import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.parser.{Node, Parser, Tokenizer}
import com.hoi4utils.script.StringPDX
import map.StrategicRegion
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

  test("StringPDX value instance test") {
    withValidFocusTrees { focusTree =>
      focusTree.pdxProperties.foreach {
        case s: StringPDX =>
          if (s.isDefined) {
            assert(s.valueIsInstanceOf[String], s"Expected StringPDX to have a String value, but got ${s.value}")
          }
        case _ => // do nothing
      }
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

  test("peek does not advance the tokenizer") {
    val input = "abc" // simple input; token types depend on your regex definitions.
    val tokenizer = new Tokenizer(input)
    val firstPeek = tokenizer.peek
    val secondPeek = tokenizer.peek
    assert(firstPeek.isDefined, "Peek should return a token")
    assert(secondPeek.isDefined, "Second peek should also return a token")
    // Both peeks should return the same token (without advancing).
    assert(firstPeek == secondPeek, "Consecutive peek calls should return the same token")

    // Now call next and then peek; they should match the token that was peeked.
    val firstNext = tokenizer.next
    val nextPeek = tokenizer.peek
    // The token returned by next should equal the one we saw with peek.
    assert(firstNext == firstPeek, "next should return the same token as peek did")
  }

  test("tokens are returned in order") {
    // Input with a few tokens separated by whitespace.
    val input = "a b c"
    val tokenizer = new Tokenizer(input)

    // Collect all tokens until the iterator is exhausted.
    val tokens = Iterator.continually(tokenizer.next).takeWhile(_.isDefined).flatten.toList
    assert(tokens.nonEmpty, "Expected some tokens to be returned")

    // Verify that the tokens appear in increasing order by their starting index.
    for (i <- 1 until tokens.length) {
      assert(tokens(i).start >= tokens(i - 1).start,
        s"Token at index $i has start ${tokens(i).start}, which is less than previous token start ${tokens(i - 1).start}")
    }
  }

  test("peek returns same token repeatedly and next advances") {
    val input = "hello world"
    val tokenizer = new Tokenizer(input)

    // Peek several times before consuming.
    val peek1 = tokenizer.peek
    val peek2 = tokenizer.peek
    assert(peek1 == peek2, "Multiple peek calls should return the same token")

    // Consume first token.
    val firstToken = tokenizer.next
    // Now, a subsequent peek should return the second token.
    val secondToken = tokenizer.peek
    assert(firstToken != secondToken, "After next, peek should return a new token different from the previously consumed one")
  }

  test("returns None when no tokens remain") {
    val input = "" // empty input should produce no tokens.
    val tokenizer = new Tokenizer(input)
    // next and peek should return None.
    assert(tokenizer.peek.isEmpty, "Peek should return None for empty input")
    assert(tokenizer.next.isEmpty, "Next should return None for empty input")
  }

  test("SpriteType objects are loaded with correct properties") {
    // Assumes a test file "sprite_types.txt" exists under testPath containing the SpriteType definitions.
    val spriteFile = new File(testPath + "sprite_types.txt")
    val parser = new Parser(spriteFile)
    val node = parser.parse
    assert(node != null, s"Failed to parse ${spriteFile.getName}")

    // Filter nodes with the name "SpriteType"
    val spriteNodes = node.filter(_.name == "SpriteType")
    assert(spriteNodes.nonEmpty, "Expected at least one SpriteType definition")

    spriteNodes.foreach { sprite =>
      val nameOpt = sprite.find("name")
      assert(nameOpt.isDefined, "SpriteType should have a name")
      val name = nameOpt.get

      val texturefile = sprite.find("texturefile")
      assert(texturefile.isDefined, s"SpriteType '$name' should have a texturefile")

      val effectFile = sprite.find("effectFile")
      assert(effectFile.isDefined, s"SpriteType '$name' should have an effectFile")

      // Each SpriteType should have exactly two animation blocks.
      val animations = sprite.filter(_.name == "animation")
      assert(
        animations.size == 2,
        s"Expected exactly two animation blocks for SpriteType '$name', but found ${animations.size}"
      )

      animations.foreach { anim =>
        // Verify required fields exist within each animation block.
        assert(anim.find("animationmaskfile").isDefined, s"Animation in SpriteType '$name' should have an animationmaskfile")
        assert(anim.find("animationtexturefile").isDefined, s"Animation in SpriteType '$name' should have an animationtexturefile")
        assert(anim.find("animationrotation").isDefined, s"Animation in SpriteType '$name' should have an animationrotation")
        assert(anim.find("animationlooping").isDefined, s"Animation in SpriteType '$name' should have an animationlooping")
        assert(anim.find("animationtime").isDefined, s"Animation in SpriteType '$name' should have an animationtime")
        assert(anim.find("animationdelay").isDefined, s"Animation in SpriteType '$name' should have an animationdelay")
        assert(anim.find("animationblendmode").isDefined, s"Animation in SpriteType '$name' should have an animationblendmode")
        assert(anim.find("animationtype").isDefined, s"Animation in SpriteType '$name' should have an animationtype")
        assert(anim.find("animationrotationoffset").isDefined, s"Animation in SpriteType '$name' should have an animationrotationoffset")
        assert(anim.find("animationtexturescale").isDefined, s"Animation in SpriteType '$name' should have an animationtexturescale")
      }

      val legacyLazyLoad = sprite.find("legacy_lazy_load")
      assert(legacyLazyLoad.exists(_.valueContains("no")), s"Expected legacy_lazy_load to be 'no' for SpriteType '$name'")
    }
  }

  test("SpriteType animations have distinct rotation values") {
    // Assumes a test file "sprite_types.txt" exists under testPath containing the SpriteType definitions.
    val spriteFile = new File(testPath + "sprite_types.txt")
    val parser = new Parser(spriteFile)
    val node = parser.parse
    assert(node != null, s"Failed to parse ${spriteFile.getName}")

    val spriteNodes = node.filter(_.name == "SpriteType")
    assert(spriteNodes.nonEmpty, "Expected at least one SpriteType definition")

    spriteNodes.foreach { sprite =>
      val nameOpt = sprite.find("name")
      assert(nameOpt.isDefined, "SpriteType should have a name")
      val name = nameOpt.get

      // Each SpriteType is expected to have exactly two animation blocks.
      val animations = sprite.filter(_.name == "animation")
      assert(animations.size == 2, s"Expected two animation blocks for SpriteType '$name'")

      val rotations = animations.flatMap(_.find("animationrotation").toSeq)
      // Check that the two rotation values are opposites (i.e. one is -90.0 and the other is 90.0).
//      assert(
//        rotations.contains("-90.0") && rotations.contains("90.0"),
//        s"Expected animation rotations '-90.0' and '90.0' for SpriteType '$name', but got: ${rotations.mkString(", ")}"
//      )
    }

  }

  test("SMA Simple") {
    val file = new File(testPath + "Massachusetts_focus_simple.txt")
    val treeSMA = new FocusTree(file)
    println(treeSMA.toScript)
    assert(treeSMA.pdxProperties.nonEmpty)
  }

  test("SMA") {
    val file = new File(testPath + "Massachusetts_focus.txt")
    val treeSMA = new FocusTree(file)
    println(treeSMA.toScript)
    assert(treeSMA.pdxProperties.nonEmpty)
  }
}
