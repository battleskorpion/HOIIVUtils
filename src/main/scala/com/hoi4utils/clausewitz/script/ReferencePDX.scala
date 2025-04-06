package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.{Node}
import org.jetbrains.annotations.Nullable

import java.util.function.{Function, Supplier}
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

/**
 * A PDXObject that may reference another PDXObject.
 * TODO: change PDXScript to PDXObject
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * @param referenceCollectionSupplier
 * @param idExtractor
 * @param pdxIdentifiers
 * @tparam T
 */
// removed T <: PDXScript[?]
// but todo disallow string (and maybe other val types), and StringPDX, other primitive pdx
class ReferencePDX[T](final protected var referenceCollectionSupplier: () => Iterable[T],
                                        final protected var idExtractor: T => Option[String], pdxIdentifiers: List[String])
  extends AbstractPDX[T](pdxIdentifiers) {

  // the string identifier of the referenced PDXScript
  protected[script] var referenceName: String = _
  private var reference: Option[T] = None

  def this(referenceCollectionSupplier: () => Iterable[T], idExtractor: T => Option[String], pdxIdentifiers: String*) = {
    this(referenceCollectionSupplier, idExtractor, pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    // Always preserve the original node.
    this.node = Some(expression)
    expression.$ match {
      case s: String =>
        referenceName = s
      case s: Int =>
        referenceName = s.toString
      case other =>
        // Log a warning, preserve the node, then throw the exception.
        LOGGER.warn(s"Expected a string or int for a reference identifier, but got ${other.getClass.getSimpleName}. Preserving original node.")
        throw new NodeValueTypeException(expression, "string | int", this.getClass)
    }
  }

  override def value: Option[T] = {
    if (reference.nonEmpty) return reference
    resolveReference()
  }

  private def resolveReference(): Option[T] = {
    val referenceCollection = referenceCollectionSupplier()
    for (ref <- referenceCollection) {
      val referenceID: Option[String] = idExtractor.apply(ref)
      if (referenceID.nonEmpty && referenceID.get.equals(referenceName)) {
        this.reference = Some(ref)
        return this.reference
      }
    }
    None
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case referencePDX: ReferencePDX[?] =>
        (referencePDX @== referenceName) &&
          this.referenceCollectionSupplier == referencePDX.referenceCollectionSupplier &&
          this.idExtractor == referencePDX.idExtractor
      case _ => false
    }
  }

  def getReferenceName: String = referenceName

  def setReferenceName(str: String): Unit = {
    referenceName = str
    node.foreach(_.setValue(str))
  }

  def getReferenceCollection: Iterable[T] = referenceCollectionSupplier()

  def getReferenceCollectionNames: Iterable[String] = referenceCollectionSupplier().flatMap(idExtractor)

  @targetName("setReference")
  def @= (str: String): Unit = {
    setReferenceName(str)
  }

  @targetName("setReference")
  def @= (other: T): Unit = {
    referenceName = idExtractor(other).orNull
    reference = Some(other)
  }

  @targetName("referenceEquals")
  def @== (other: String): Boolean = referenceName == other

  @targetName("referenceEquals")
  def @==(other: StringPDX): Boolean = referenceName == other.str

  @targetName("referenceEquals")
  def @== (other: T): Boolean = idExtractor(other).contains(referenceName)

  override def isUndefined: Boolean = {
    resolveReference()
    reference.isEmpty
  }

  override def set(obj: T): T = {
    reference = Some(obj)
    referenceName = idExtractor(obj).orNull // sure
    obj
  }

  /**
   * @inheritdoc
   */
  override def setNull(): Unit = {
    super.setNull()
    reference = None
    referenceName = null
  }

  /**
   * On-demand Node rebuilding: update the underlying node’s value to the current reference name.
   */
  override def updateNodeTree(): Unit = {
    if (node.isEmpty && referenceName != null) {
      node = Some(new Node(pdxIdentifier, "=", referenceName))
    }
    else node.foreach(_.setValue(referenceName))
  }
  
  override def toString : String = {
    super.toString
  }
}