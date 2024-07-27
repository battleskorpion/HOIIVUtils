package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.Nullable

import java.util.function.Function
import java.util.function.Supplier
import scala.collection.mutable
import scala.collection.mutable._

// todo uhhhhhhhhhhhhh
class MultiReferencePDX[T <: AbstractPDX[?]](protected var referenceCollectionSupplier: Supplier[mutable.AbstractIterable[T]],
                                             protected var idExtractor: Function[T, String], pdxIdentifiers: List[String],
                                             pdxReferenceIdentifiers: List[String])
  extends MultiPDX[T](null, pdxIdentifiers*) {

  final protected var referencePDXTokenIdentifiers: List[String] = _
  final protected val referenceNames = new ListBuffer[String]

  def this(referenceCollectionSupplier: Supplier[Collection[T]], idExtractor: Function[T, String], pdxIdentifiers: String, referenceIdentifier: String) = {
    this(referenceCollectionSupplier, idExtractor, List(pdxIdentifiers), List(referenceIdentifier))
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    expression.$ match {
      case list: List[Node] =>
        if (list == null) {
          System.out.println("PDX script had empty list: " + expression)
          return
        }
        usingIdentifier(expression)
        for (node <- list) {
          try add(node)
          catch {
            case e: NodeValueTypeException =>
              throw new RuntimeException(e)
          }
        }
      case _ => try add(expression)
        catch {
          case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
            throw new RuntimeException(e)
        }
    }
  }

  override def get(): List[T] = {
    if (node != null && !node.isEmpty) return null.asInstanceOf[T]//return node  // todo this made wrong
    resolveReferences
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    val value = expression.$
    referenceNames.clear()
    value match {
      case s: String => referenceNames.addOne(s)
    }
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def add(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    val value = expression.$
    value match
      case str: String => referenceNames.addOne(str)
      case _ =>
  }

  private def resolveReferences = {
    val referenceCollection = referenceCollectionSupplier.get
    for (reference <- referenceCollection) {
      for (referenceName <- referenceNames) {
//        if (idExtractor.apply(reference) == referenceName) node.addOne(reference) // todo fix
      }
    }
    node
  }

  @throws[UnexpectedIdentifierException]
  protected def usingReferenceIdentifier(exp: Node): Unit = {
    for (i <- referencePDXTokenIdentifiers.indices) {
      if (exp.nameEquals(referencePDXTokenIdentifiers(i))) {
        //                activeReferenceIdentifier = i;
        return
      }
    }
    throw new UnexpectedIdentifierException(exp)
  }

  override def toScript: String = {
    val sb = new StringBuilder
    val scripts = get()
    if (scripts == null) return null
    for (identifier <- referenceNames) {
      sb.append(getPDXIdentifier).append(" = ").append(identifier).append("\n")
    }
    sb.toString
  }

  def setReferenceName(index: Int, value: String): Unit = {
    referenceNames.update(index, value)
  }

  def getReferenceName(i: Int): String = referenceNames(i)

  def getReferenceCollectionNames: List[String] = List(referenceCollectionSupplier.get.map(idExtractor))

  def addReferenceName(newValue: String): Unit = {
    referenceNames.addOne(newValue)
    resolveReferences
  }

  /**
   * Size of actively valid references (resolved PDXScript object references)
   *
   * @return
   */
  override def size: Int = {
    val list = get()
    if (list == null) return 0
    list.size
  }

  def referenceSize: Int = referenceNames.size
}