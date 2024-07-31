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
class ReferencePDX[T <: PDXScript[?]](final protected var referenceCollectionSupplier: () => Iterable[T],
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
      val referenceID = idExtractor.apply(reference)
      referenceID match {
        case null =>
        case referenceName =>
          this.reference = Some(reference)
          return Some(reference)
        case _ =>
      }
    }
    None
  }

  override def equals(other: AbstractPDX[?]): Boolean = {
    other match {
      case referencePDX: ReferencePDX[?] =>
        referenceName == referencePDX.referenceName && this.referenceCollectionSupplier == referencePDX.referenceCollectionSupplier && this.idExtractor == referencePDX.idExtractor
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

  def getReferenceCollectionNames: Iterable[String] = referenceCollectionSupplier().map(idExtractor)

  override def isUndefined: Boolean = {
    resolveReference()
    reference.isEmpty
  }
}
