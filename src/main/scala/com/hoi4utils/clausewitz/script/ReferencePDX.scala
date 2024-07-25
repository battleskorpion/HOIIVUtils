package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.Nullable

import java.util
import java.util.{Collection, List}
import java.util.function.Function
import java.util.function.Supplier
// todo fix this class

// the superclass will still be of type T (the type of the referenced pdxscript object)// the superclass will still be of type T (the type of the referenced pdxscript object)
// but the super obj can be null until the reference is resolved.// but the super obj can be null until the reference is resolved.
// this class will contain a string identifier that identifies the referenced pdxscript object// this class will contain a string identifier that identifies the referenced pdxscript object
// (usually by its 'id' PDXScript field)// (usually by its 'id' PDXScript field)

class ReferencePDX[T <: AbstractPDX[?]] extends AbstractPDX[T](pdxIdentifiers) {
  // the collection of potential pdxscript objects that this reference can point to
  final protected var referenceCollectionSupplier: Supplier[util.Collection[T]] = _
  final protected var idExtractor: Function[T, String] = _
  protected var referenceName: String = _

  def this(referenceCollectionSupplier: Supplier[util.Collection[T]], idExtractor: Function[T, String], PDXIdentifiers: String) = {
    this()
    this.referenceCollectionSupplier = referenceCollectionSupplier
    this.idExtractor = idExtractor
  }

  def this(referenceCollectionSupplier: Supplier[util.Collection[T]], idExtractor: Function[T, String], PDXIdentifiers: String*) = {
    this()
    this.referenceCollectionSupplier = referenceCollectionSupplier
    this.idExtractor = idExtractor
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    if (value.isString) referenceName = value
    else throw new NodeValueTypeException(expression, "string")
  }

  override def get(): T = {
    if (node != null) return node
    resolveReference
  }

  override def nodeEquals(other: PDXScript[_]): Boolean = {
    if (other.isInstanceOf[ReferencePDX[_]]) return referenceName == other.referenceName && this.referenceCollectionSupplier == other.referenceCollectionSupplier && this.idExtractor == other.idExtractor
    false
  }

  private def resolveReference: T = {
    val referenceCollection = referenceCollectionSupplier.get
    import scala.collection.JavaConversions._
    for (reference <- referenceCollection) {
      val referenceID = idExtractor.apply(reference)
      if (referenceID == null) continue //todo: continue is not supported
      if (referenceID == referenceName) {
        obj = reference
        return reference
      }
    }
    null
  }

  override def nodeEquals(other: AbstractPDX[_]): Boolean = {
    if (obj == null) {
      resolveReference
      if (obj == null) return false
    }
    obj.objEquals(other)
  }

  override def toScript: String = {
    val scripts = get
    if (scripts == null) return null
    (getPDXIdentifier + " = " + referenceName) + "\n"
  }

  def getReferenceName: String = referenceName

  def setReferenceName(newValue: String): Unit = {
    referenceName = newValue
    obj = null
  }

  def getReferenceCollection: util.Collection[T] = referenceCollectionSupplier.get

  def getReferenceCollectionNames: util.List[String] = referenceCollectionSupplier.get.stream.map(idExtractor).toList

  override def isUndefined: Boolean = {
    resolveReference
    obj == null
  }
}
