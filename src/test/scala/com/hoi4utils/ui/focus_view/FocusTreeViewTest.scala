package com.hoi4utils.ui.focus_view

import com.hoi4utils.hoi4mod.common.national_focus.{Focus, FocusTreeFile}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FocusTreeViewTest extends AnyFlatSpec with Matchers {

  "FocusTreeView" should "initialize with empty focus tree" in {
    val view = new FocusTreeView(None)
    view.focusTree shouldBe None
    view.getSelectedFocuses shouldBe empty
  }

  it should "accept focus tree and update state" in {
    val view = new FocusTreeView(None)
    val tree = new FocusTreeFile()

    view.focusTree = tree
    view.focusTree should contain(tree)
  }

  it should "handle grid line toggle" in {
    val view = new FocusTreeView(None)

    // Should not throw exception
    noException should be thrownBy view.toggleGridLines()
  }

  it should "clear selections properly" in {
    val view = new FocusTreeView(None)

    view.clearSelection()
    view.getSelectedFocuses shouldBe empty
  }

  it should "calculate required size for empty tree" in {
    val view = new FocusTreeView(None)

    // Should not throw exception when redrawing empty tree
    noException should be thrownBy view.redraw()
  }
}

class FocusNodeTest extends AnyFlatSpec with Matchers {

  "FocusNode" should "initialize with focus" in {
    val tree = new FocusTreeFile()
    val focus = new Focus(tree)
    focus.setID("test_focus")

    val node = new FocusNode(focus, _ => (), (_, _) => ())
    node.focus shouldBe focus
    node.toString should include("test_focus")
  }

  it should "handle position updates" in {
    val tree = new FocusTreeFile()
    val focus = new Focus(tree)
    val node = new FocusNode(focus, _ => (), (_, _) => ())

    // Should not throw exception
    noException should be thrownBy node.updatePosition(100, 100, 90, 140)
  }

  it should "handle selection state changes" in {
    val tree = new FocusTreeFile()
    val focus = new Focus(tree)
    val node = new FocusNode(focus, _ => (), (_, _) => ())

    // Should not throw exception
    noException should be thrownBy {
      node.setSelected(true)
      node.setSelected(false)
    }
  }

  it should "handle hover state changes" in {
    val tree = new FocusTreeFile()
    val focus = new Focus(tree)
    val node = new FocusNode(focus, _ => (), (_, _) => ())

    // Should not throw exception
    noException should be thrownBy {
      node.setHovered(true)
      node.setHovered(false)
    }
  }
}