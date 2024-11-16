package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.Nullable

import java.util.function.Function
import java.util.function.Supplier

import scala.collection.mutable.ListBuffer

/**
 * A PDXScript that may reference another PDXScript.
 *
 * @param referenceCollectionSupplier
 * @param idExtractor
 * @param pdxIdentifiers
 * @tparam T
 */
// removed T <: PDXScript[?]
class ReferencePDX[T](final protected var referenceCollectionSupplier: () => Iterable[T],
                                        final protected var idExtractor: T => Option[String], pdxIdentifiers: List[String])
  extends AbstractPDX[T](pdxIdentifiers) {

  // the string identifier of the referenced PDXScript
  protected[script] var referenceName: String = _
  protected[script] var reference: Option[T] = None

  def this(referenceCollectionSupplier: () => Iterable[T], idExtractor: T => Option[String], pdxIdentifiers: String*) = {
    this(referenceCollectionSupplier, idExtractor, pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    value match {
      case s: String =>
        referenceName = s
      case _ =>
        throw new NodeValueTypeException(expression, "string")
    }
  }

  override def get(): Option[T] = {
    if (reference.nonEmpty) return reference
    resolveReference()
  }

  private def resolveReference(): Option[T] = {
    val referenceCollection = referenceCollectionSupplier()
    for (reference <- referenceCollection) {
      val referenceID: Option[String] = idExtractor.apply(reference)
      if (referenceID.nonEmpty && referenceID.get.equals(referenceName)) {
        this.reference = Some(reference)
        return this.reference
      }
    }
    None
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case referencePDX: ReferencePDX[?] =>
        referenceName == referencePDX.referenceName && this.referenceCollectionSupplier == referencePDX.referenceCollectionSupplier
        && this.idExtractor == referencePDX.idExtractor
      case _ => false
    }
  }

  override def toScript: String = {
    val scripts = get()
    if (scripts == null) return null
    (getPDXIdentifier + " = " + referenceName) + "\n"
  }

  def getReferenceName: String = referenceName

  def setReferenceName(newValue: String): Unit = {
    referenceName = newValue
    node = null
  }

  def getReferenceCollection: Iterable[T] = referenceCollectionSupplier()

  def getReferenceCollectionNames: Iterable[String] = referenceCollectionSupplier().flatMap(idExtractor)

  override def isUndefined: Boolean = {
    resolveReference()
    reference.isEmpty
  }

  override def set(obj: T): Unit = {
    reference = Some(obj)
    referenceName = idExtractor.apply(obj).orNull // sure
  }
}
