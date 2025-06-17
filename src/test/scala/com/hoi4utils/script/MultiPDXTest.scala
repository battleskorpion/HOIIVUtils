package com.hoi4utils.script

import com.hoi4utils.HOIIVUtilsTest
import com.hoi4utils.parser.Node
import org.scalatest.BeforeAndAfterEach

import scala.collection.mutable.ListBuffer

class MultiPDXTest extends HOIIVUtilsTest with BeforeAndAfterEach {

  // Mock PDXScript implementation for testing
  class MockPDXScript(var testValue: String = "") extends PDXScript[String] {
    private var _node: Option[Node] = None
    private var _isUndefined: Boolean = true

    override def loadPDX(expression: Node): Unit = {
      _node = Some(expression)
      _isUndefined = false
      testValue = expression.name
    }

    override def set(expression: Node): Unit = {
      _node = Some(expression)
      testValue = expression.name
    }

    override def set(obj: String): String = {
      testValue = obj
      _isUndefined = false
      obj
    }

    override def value: Option[String] = if (_isUndefined) None else Some(testValue)

    override def getNode: Option[Node] = _node

    override def getNodes: List[Node] = _node.toList

    override def clearNode(): Unit = {
      _node = None
    }

    override def setNull(): Unit = {
      _isUndefined = true
      testValue = ""
    }

    override def isUndefined: Boolean = _isUndefined

    override def isDefined: Boolean = !_isUndefined

    override def isValidIdentifier(node: Node): Boolean = true

    override def isValidID(identifier: String): Boolean = true

    override def pdxIdentifier: String = "mock"

    override def getOrElse(elseValue: String): String = value.getOrElse(elseValue)

    override def loadOrElse(exp: Node, value: String): Unit = {
      try {
        loadPDX(exp)
      } catch {
        case _: Exception => set(value)
      }
    }

    override def updateNodeTree(): Unit = {}

    override def toScript: String = testValue

    override def equals(other: PDXScript[?]): Boolean = {
      other match {
        case mock: MockPDXScript => this.testValue == mock.testValue
        case _ => false
      }
    }

    /**
     * Set the node value to the given value.
     *
     * Obviously, if T != to the type of the value,
     * the new value may not be semantically correct. However, we need to allow this for
     * flexibility i.e. setting a PDX of type double with an int value, and this also matches
     * the underlying node class functionality.
     */
    override protected def setNode(value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = ???

    /**
     * Load the PDX script represented by the given expressions.
     *
     * @param expressions
     */
    override def loadPDX(expressions: Iterable[Node]): Iterable[Node] = ???
}

  var multiPDX: MultiPDX[MockPDXScript] = _
  val simpleSupplier: () => MockPDXScript = () => new MockPDXScript()
  val blockSupplier: () => MockPDXScript = () => new MockPDXScript()

  override def beforeEach(): Unit = {
    super.beforeEach()
    multiPDX = new MultiPDX[MockPDXScript](
      Some(simpleSupplier),
      Some(blockSupplier),
      List("test_id", "another_id")
    )
  }

  test("MultiPDX should initialize with empty list") {
    assert(multiPDX.isEmpty)
    assert(multiPDX.length == 0)
    assert(multiPDX.value.isEmpty)
  }

  test("MultiPDX should support various constructor overloads") {
    val multiPDX1 = new MultiPDX[MockPDXScript](
      Some(simpleSupplier),
      Some(blockSupplier),
      "id1", "id2", "id3"
    )
    assert(multiPDX1.isEmpty)

    val multiPDX2 = new MultiPDX[MockPDXScript](
      Some(simpleSupplier),
      Some(blockSupplier)
    )
    assert(multiPDX2.isEmpty)
  }

  test("MultiPDX should add PDXScript objects manually") {
    val mockScript = new MockPDXScript("test_value")
    multiPDX += mockScript

    assert(multiPDX.length == 1)
    assert(!multiPDX.isEmpty)
    assert(multiPDX.value.isDefined)
    assert(multiPDX(0) == mockScript)
  }

  test("MultiPDX should remove PDXScript objects") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2
    assert(multiPDX.length == 2)

    multiPDX -= mockScript1
    assert(multiPDX.length == 1)
    assert(multiPDX(0) == mockScript2)

    multiPDX.remove(mockScript2)
    assert(multiPDX.isEmpty)
  }

  test("MultiPDX should load PDX from Node with simple value") {
    val node = new Node("test_id", "=", "simple_value")

    multiPDX.loadPDX(node)

    assert(multiPDX.length == 1)
    assert(multiPDX(0).testValue == "test_id")
  }

  test("MultiPDX should load PDX from Node with block value") {
    val childNodes = ListBuffer(
      new Node("child1", "=", "value1"),
      new Node("child2", "=", "value2")
    )
    val blockNode = new Node("test_id", "=", childNodes)

    multiPDX.loadPDX(blockNode)

    assert(multiPDX.length == 1)
    assert(multiPDX(0).testValue == "test_id")
  }

  test("MultiPDX should load multiple PDX from Iterable of Nodes") {
    val nodes = List(
      new Node("test_id", "=", "value1"),
      new Node("another_id", "=", "value2"),
      new Node("invalid_id", "=", "value3") // This should be filtered out
    )

    val remaining = multiPDX.loadPDX(nodes)

    assert(multiPDX.length == 2)
    assert(remaining.size == 1)
    assert(remaining.head.name == "invalid_id")
  }

  test("MultiPDX should support iteration") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2

    val values = multiPDX.map(_.testValue).toList
    assert(values == List("value1", "value2"))
  }

  test("MultiPDX should support foreach operation") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2

    var count = 0
    multiPDX.foreach(_ => count += 1)
    assert(count == 2)
  }

  test("MultiPDX should support filtering operations") {
    val mockScript1 = new MockPDXScript("keep")
    val mockScript2 = new MockPDXScript("remove")
    val mockScript3 = new MockPDXScript("keep")

    multiPDX += mockScript1
    multiPDX += mockScript2
    multiPDX += mockScript3

    multiPDX.filterInPlace(_.testValue == "keep")

    assert(multiPDX.length == 2)
    assert(multiPDX.forall(_.testValue == "keep"))
  }

  test("MultiPDX should support removeIf operation") {
    val mockScript1 = new MockPDXScript("keep")
    val mockScript2 = new MockPDXScript("remove")
    val mockScript3 = new MockPDXScript("keep")

    multiPDX += mockScript1
    multiPDX += mockScript2
    multiPDX += mockScript3

    multiPDX.removeIf(_.testValue == "remove")

    assert(multiPDX.length == 2)
    assert(multiPDX.forall(_.testValue == "keep"))
  }

  test("MultiPDX should clear all elements") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2

    multiPDX.clear()

    assert(multiPDX.isEmpty)
    assert(multiPDX.length == 0)
  }

  test("MultiPDX should add new PDX using addNewPDX") {
    val newPDX = multiPDX.addNewPDX()

    assert(multiPDX.length == 1)
    assert(multiPDX(0) == newPDX)
    assert(newPDX.isInstanceOf[MockPDXScript])
  }

  test("MultiPDX should handle supplier application correctly") {
    // Test with only simple supplier
    val simpleOnlyMulti = new MultiPDX[MockPDXScript](Some(simpleSupplier), None, "test")
    val simpleScript = simpleOnlyMulti.applySomeSupplier()
    assert(simpleScript.isInstanceOf[MockPDXScript])

    // Test with only block supplier
    val blockOnlyMulti = new MultiPDX[MockPDXScript](None, Some(blockSupplier), "test")
    val blockScript = blockOnlyMulti.applySomeSupplier()
    assert(blockScript.isInstanceOf[MockPDXScript])

    // Test with both suppliers (should prefer simple)
    val bothScript = multiPDX.applySomeSupplier()
    assert(bothScript.isInstanceOf[MockPDXScript])
  }

  test("MultiPDX should throw exception when no suppliers available") {
    val noSupplierMulti = new MultiPDX[MockPDXScript](None, None, "test")

    assertThrows[RuntimeException] {
      noSupplierMulti.applySomeSupplier()
    }

    assertThrows[RuntimeException] {
      noSupplierMulti.addNewPDX()
    }
  }

  test("MultiPDX should handle node tree updates") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2

    // Should not throw exception
    multiPDX.updateNodeTree()

    // Verify the structure is maintained
    assert(multiPDX.length == 2)
    assert(multiPDX(0) == mockScript1)
    assert(multiPDX(1) == mockScript2)
  }

  test("MultiPDX should handle clearNode operation") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    // Set up some nodes
    mockScript1.loadPDX(new Node("test1", "=", "value1"))
    mockScript2.loadPDX(new Node("test2", "=", "value2"))

    multiPDX += mockScript1
    multiPDX += mockScript2

    multiPDX.clearNode()

    assert(mockScript1.getNode.isEmpty)
    assert(mockScript2.getNode.isEmpty)
  }

  test("MultiPDX should return correct nodes") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    val node1 = new Node("test1", "=", "value1")
    val node2 = new Node("test2", "=", "value2")

    mockScript1.loadPDX(node1)
    mockScript2.loadPDX(node2)

    multiPDX += mockScript1
    multiPDX += mockScript2

    val nodes = multiPDX.getNodes
    assert(nodes.length == 2)
    assert(nodes.contains(node1))
    assert(nodes.contains(node2))
  }

  test("MultiPDX should handle array-like access") {
    val mockScript1 = new MockPDXScript("value1")
    val mockScript2 = new MockPDXScript("value2")

    multiPDX += mockScript1
    multiPDX += mockScript2

    assert(multiPDX(0) == mockScript1)
    assert(multiPDX(1) == mockScript2)

    assertThrows[IndexOutOfBoundsException] {
      multiPDX(2)
    }
  }

  test("MultiPDX equals should always return false") {
    val other = new MultiPDX[MockPDXScript](Some(simpleSupplier), Some(blockSupplier), "test")
    assert(!multiPDX.equals(other))
    assert(!multiPDX.equals(multiPDX)) // Even comparing to itself
  }
}