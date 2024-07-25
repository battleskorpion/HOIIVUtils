package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.Nullable
import java.util._
import java.util.function.Function
import java.util.function.Supplier


// todo uhhhhhhhhhhhhh
class MultiReferencePDX[T <: AbstractPDX[_$1]] extends MultiPDX[T](null, pdxIdentifiers) {
  final protected var referenceCollectionSupplier: Supplier[util.Collection[T]] = _
  final protected var idExtractor: Function[T, String] = _
  final protected var referencePDXTokenIdentifiers: util.List[String] = null
  final protected val referenceNames = new util.ArrayList[String]

  def this(referenceCollectionSupplier: Supplier[util.Collection[T]], idExtractor: Function[T, String], PDXIdentifiers: String, referenceIdentifier: String) {
    this()
    this.referenceCollectionSupplier = referenceCollectionSupplier
    this.idExtractor = idExtractor
    this.referencePDXTokenIdentifiers = util.List.of(referenceIdentifier)
  }

  def this(referenceCollectionSupplier: Supplier[util.Collection[T]], idExtractor: Function[T, String], PDXIdentifiers: util.List[String], pdxReferenceIdentifier: util.List[String]) {
    this()
    this.referenceCollectionSupplier = referenceCollectionSupplier
    this.idExtractor = idExtractor
    this.referencePDXTokenIdentifiers = pdxReferenceIdentifier
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    if (expression.value.isList) {
      val list = expression.value.list
      if (list == null) {
        System.out.println("PDX script had empty list: " + expression)
        return
      }
      usingIdentifier(expression)
      import scala.collection.JavaConversions._
      for (node <- list) {
        try add(node)
        catch {
          case e: NodeValueTypeException =>
            throw new RuntimeException(e)
        }
      }
    }
    else try add(expression)
    catch {
      case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
        throw new RuntimeException(e)
    }
  }

  override def get(): util.List[T] = {
    if (node != null && !node.isEmpty) return node
    resolveReferences
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    val value = expression.value
    referenceNames.clear()
    referenceNames.add(value.string)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def add(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    val value = expression.value
    referenceNames.add(value.string)
  }

  private def resolveReferences = {
    val referenceCollection = referenceCollectionSupplier.get
    import scala.collection.JavaConversions._
    for (reference <- referenceCollection) {
      import scala.collection.JavaConversions._
      for (referenceName <- referenceNames) {
        if (idExtractor.apply(reference) == referenceName) node.add(reference)
      }
    }
    node
  }

  @throws[UnexpectedIdentifierException]
  protected def usingReferenceIdentifier(exp: Node): Unit = {
    for (i <- 0 until referencePDXTokenIdentifiers.size) {
      if (exp.nameEquals(referencePDXTokenIdentifiers.get(i))) {
        //                activeReferenceIdentifier = i;
        return
      }
    }
    throw new UnexpectedIdentifierException(exp)
  }

  override def toScript: String = {
    val sb = new StringBuilder
    val scripts = get
    if (scripts == null) return null
    import scala.collection.JavaConversions._
    for (identifier <- referenceNames) {
      sb.append(getPDXIdentifier).append(" = ").append(identifier).append("\n")
    }
    sb.toString
  }

  def setReferenceName(index: Int, value: String): Unit = {
    referenceNames.set(index, value)
  }

  def getReferenceName(i: Int): String = referenceNames.get(i)

  def getReferenceCollectionNames: util.List[String] = referenceCollectionSupplier.get.stream.map(idExtractor).toList

  def addReferenceName(newValue: String): Unit = {
    referenceNames.add(newValue)
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