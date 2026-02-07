package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, PDXValueNode}
import com.hoi4utils.script.datatype.StringPDX

import scala.annotation.targetName
import scala.compiletime.uninitialized
import scala.language.implicitConversions
import scala.util.boundary

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
class ReferencePDX[V, T <: Referable[V]](final protected var referenceCollectionSupplier: () => Iterable[T],
                      pdxIdentifiers: List[String])
  extends AbstractPDX[T, V](pdxIdentifiers) {

  // the string identifier of the referenced PDXScript
  protected[script] var referenceName: V = uninitialized
  private var reference: Option[T] = None
  final protected var idExtractor: T => Option[V] = (obj: T) => obj.referableID

  def this(referenceCollectionSupplier: () => Iterable[T], pdxIdentifiers: String*) = {
    this(referenceCollectionSupplier, pdxIdentifiers.toList)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: PDXValueNode[V]): Unit = {
    usingIdentifier(expression)
    // Always preserve the original node.
    this.node = Some(expression)
    referenceName = expression.$
  }

  override def value: Option[T] = {
    if (reference.nonEmpty) return reference
    resolveReference()
  }

  private def resolveReference(): Option[T] = boundary {
    val referenceCollection = referenceCollectionSupplier()
    for (ref <- referenceCollection) {
      val referenceID: Option[V] = idExtractor.apply(ref)
      if (referenceID.nonEmpty && referenceID.get.equals(referenceName)) {
        this.reference = Some(ref)
        boundary.break(this.reference)
      }
    }
    None
  }

  override def equals(other: PDXScript[?, ?]): Boolean = {
    other match {
      case referencePDX: ReferencePDX[?, ?] =>
        // TODO TODO
        false
//        (referencePDX @== referenceName) &&
//          this.referenceCollectionSupplier == referencePDX.referenceCollectionSupplier
      case _ => false
    }
  }

  def getReferenceName: V = referenceName

  def setReferenceName(name: V): Unit =
    referenceName = name
    node.foreach(_.setValue(name))

  def getReferenceCollection: Iterable[T] = referenceCollectionSupplier()

  def getReferenceCollectionNames: Iterable[V] = referenceCollectionSupplier().flatMap(idExtractor)

  @targetName("setReference")
  def @= (str: V): Unit = {
    setReferenceName(str)
  }

  @targetName("setReference")
  def @= (other: T): Unit = {
    referenceName = idExtractor(other).getOrElse(throw new Exception(s"Cannot set reference to $other"))  // todo 
    reference = Some(other)
  }

  @targetName("referenceEquals")
  def @== (other: String): Boolean = referenceName == other

  @targetName("referenceEquals")
  def @==(other: StringPDX): Boolean = referenceName == other.str

  @targetName("referenceEquals")
  def @== (other: T): Boolean = idExtractor(other).contains(referenceName)

  override def isUndefined: Boolean = {
    value.isEmpty
  }

  override def set(obj: T): T = {
    reference = Some(obj)
    referenceName = idExtractor(obj).getOrElse(throw new Exception(s"Cannot set reference to $obj"))  // todo 
    obj
  }

  /**
   * @inheritdoc
   */
  override def setNull(): Unit = {
    super.setNull()
    reference = None
//    referenceName = null
  }

  /**
   * On-demand Node rebuilding: update the underlying node’s value to the current reference name.
   */
  override def updateNodeTree(): Unit = {
    // TODO TODO 
//    if (node.isEmpty && referenceName != null) {
//      node = Some(new PDXValueNode[V](pdxIdentifier, "=", referenceName))
//    }
//    else node.foreach(_.setValue(referenceName))
  }

  override def toString : String = {
    super.toString
  }
}

trait Referable[V <: String | Int] {
  type Value = V
  def referableID: Option[V]
}
