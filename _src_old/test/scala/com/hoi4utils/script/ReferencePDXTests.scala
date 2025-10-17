package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import com.hoi4utils.script.ReferencePDX
import org.scalatest.funsuite.AnyFunSuite

// A simple dummy PDX object with an identifier.
case class DummyPDX(id: String)

class ReferencePDXTests extends AnyFunSuite {

  // Our dummy collection and supplier/extractor.
  val dummyCollection: List[DummyPDX] = List(
    DummyPDX("dummy1"),
    DummyPDX("dummy2"),
    DummyPDX("dummy3")
  )
  val dummySupplier: () => Iterable[DummyPDX] = () => dummyCollection
  val dummyIdExtractor: DummyPDX => Option[String] = (d: DummyPDX) => Some(d.id)

  test("set() with valid string sets referenceName and resolves reference") {
    // Create a node with identifier "ref", operator "=", and value "dummy1"
    val node = new Node("ref", "=", "dummy1")
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    referencePDX.set(node)

    // Check that the reference name was set to the value ("dummy1")
    assert(referencePDX.getReferenceName == "dummy1")
    // Check that the reference resolves correctly from the supplier.
    val resolved = referencePDX.value
    assert(resolved.isDefined)
    assert(resolved.get.id == "dummy1")
  }

  test("set() with invalid type throws NodeValueTypeException") {
    // Create a node with identifier "ref" but with a Double value,
    // which is not allowed (only String or Int is accepted).
    val node = new Node("ref", "=", 123.45)
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))

    intercept[NodeValueTypeException] {
      referencePDX.set(node)
    }
  }

  test("isUndefined returns true when reference is not found") {
    // Create a node with identifier "ref" and a value that doesn't match any DummyPDX.
    val node = new Node("ref", "=", "nonexistent")
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))

    referencePDX.set(node)
    // Because there is no DummyPDX with id "nonexistent", value should be empty.
    assert(referencePDX.value.isEmpty)
    assert(referencePDX.isUndefined)
  }

  test("Overloaded @= operator with String sets reference correctly") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    // Use the overloaded operator that takes a String.
    referencePDX @= "dummy2"

    assert(referencePDX.getReferenceName == "dummy2")
    val resolved = referencePDX.value
    assert(resolved.isDefined)
    assert(resolved.get.id == "dummy2")
  }

  test("Overloaded @= operator with T sets reference correctly") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    val dummy = DummyPDX("dummy3")

    // Use the overloaded operator that takes a T.
    referencePDX @= dummy
    assert(referencePDX.getReferenceName == "dummy3")
    // Ensure the reference is set and resolves to the same dummy.
    assert(referencePDX.value.isDefined)
    assert(referencePDX.value.get == dummy)
  }

  test("@== operator works for comparing with a String") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    referencePDX @= "dummy1"

    assert(referencePDX @== "dummy1")
    assert(!(referencePDX @== "dummy2"))
  }

  test("@== operator works for comparing with T using the extractor") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    referencePDX @= "dummy1"
    val dummy = DummyPDX("dummy1")
    assert(referencePDX @== dummy)
  }

  test("equals method returns true when reference names and suppliers match") {
    val referencePDX1 = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    val referencePDX2 = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))

    referencePDX1 @= "dummy1"
    referencePDX2 @= "dummy1"

    assert(referencePDX1 == referencePDX2)
  }

  test("setNull clears reference and referenceName") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    referencePDX @= "dummy1"
    assert(referencePDX.getReferenceName == "dummy1")
    assert(referencePDX.value.isDefined)

    referencePDX.setNull()
    assert(referencePDX.getReferenceName == null)
    assert(referencePDX.value.isEmpty)
  }

  // todo fix test 
//  test("updateNodeTree creates a new node when none exists") {
//    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
//    referencePDX @= "dummy1"
//    // Clear any existing node.
//    referencePDX.node = None
//    referencePDX.updateNodeTree()
//    assert(referencePDX.node.isDefined)
//    // Verify that the underlying node's value is updated to the reference name.
//    val createdNode = referencePDX.node.get
//    assert(createdNode.identifier.contains("ref"))
//    createdNode.rawValue match {
//      case Some(s: String) => assert(s == "dummy1")
//      case _               => fail("Expected node value to be a string 'dummy1'")
//    }
//  }

  // todo fix test
//  test("updateNodeTree updates an existing node with new reference name") {
//    val node = new Node("ref", "=", "dummy1")
//    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
//    referencePDX.set(node)
//
//    // Change the reference name manually.
//    referencePDX.setReferenceName("dummy_updated")
//    // Update the node tree.
//    referencePDX.updateNodeTree()
//    // The node should now reflect the updated reference name.
//    val updatedNode = referencePDX.node.get
//    updatedNode.rawValue match {
//      case Some(s: String) => assert(s == "dummy_updated")
//      case _               => fail("Expected node raw value to be a string")
//    }
//  }

  test("getReferenceCollection returns the complete collection") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    val collection = referencePDX.getReferenceCollection
    assert(collection.toList == dummyCollection)
  }

  test("getReferenceCollectionNames returns all identifiers") {
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    val names = referencePDX.getReferenceCollectionNames.toList
    assert(names.sorted == dummyCollection.map(_.id).sorted)
  }

  test("set() with node having an integer value sets referenceName as string") {
    // Create a mutable dummy collection that includes a DummyPDX with id "123".
    val mutableDummyCollection = scala.collection.mutable.ListBuffer(
      DummyPDX("123"),
      DummyPDX("dummy1")
    )
    val supplier: () => Iterable[DummyPDX] = () => mutableDummyCollection
    val referencePDX = new ReferencePDX[DummyPDX](supplier, dummyIdExtractor, List("ref"))
    val node = new Node("ref", "=", 123)
    referencePDX.set(node)

    assert(referencePDX.getReferenceName == "123")
    val resolved = referencePDX.value
    assert(resolved.isDefined)
    assert(resolved.get.id == "123")
  }

  test("set() with node with incorrect identifier throws UnexpectedIdentifierException") {
    // Here we assume that ReferencePDX checks that the node's identifier matches one of the allowed identifiers.
    // Creating a node with a wrong identifier should trigger an exception.
    val node = new Node("wrong", "=", "dummy1")
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    intercept[UnexpectedIdentifierException] {
      referencePDX.set(node)
    }
  }

  test("value caches the resolved reference") {
    val node = new Node("ref", "=", "dummy1")
    val referencePDX = new ReferencePDX[DummyPDX](dummySupplier, dummyIdExtractor, List("ref"))
    referencePDX.set(node)

    val firstResolution = referencePDX.value
    val secondResolution = referencePDX.value
    assert(firstResolution.isDefined && secondResolution.isDefined)
    // Verify that the same instance is returned.
    assert(firstResolution.get eq secondResolution.get)
  }
}
