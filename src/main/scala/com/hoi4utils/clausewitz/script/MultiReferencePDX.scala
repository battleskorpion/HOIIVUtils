package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.Nullable

import scala.collection.mutable.ListBuffer

// todo uhhhhhhhhhhhhh

/**
 * (todo)
 *
 * MultiPDX's pdx script list will be the list of resolved references
 *
 * @param referenceCollectionSupplier
 * @param idExtractor
 * @param pdxIdentifiers
 * @param referencePDXIdentifiers
 * @tparam T
 */
class MultiReferencePDX[T <: AbstractPDX[?]](protected var referenceCollectionSupplier: () => Iterable[T],
                                             protected var idExtractor: T => Option[String], pdxIdentifiers: List[String],
                                             referencePDXIdentifiers: List[String])
  extends MultiPDX[T](None, None, pdxIdentifiers) {

  final protected val referenceNames = new ListBuffer[String]

  def this(referenceCollectionSupplier: () => Iterable[T], idExtractor: T => Option[String], pdxIdentifiers: String, referenceIdentifier: String) = {
    this(referenceCollectionSupplier, idExtractor, List(pdxIdentifiers), List(referenceIdentifier))
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    expression.$ match {
      case list: ListBuffer[Node] =>
        // prolly don't need this check, but it doesn't hurt right now
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

  /**
   * This is an option due to overrides. But it will always return some list.
   * @return
   */
  override def get(): Option[ListBuffer[T]] = {
    // what if name changes etc. im not sure about this
//    if (resolvedReferences.length.equals(numReferences)) resolvedReferences
//    else resolveReferences
    Some(references())
  }

  def references(): ListBuffer[T] = {
    resolveReferences()
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

  private def resolveReferences(): ListBuffer[T] = {
    val referenceCollection = referenceCollectionSupplier()
    // clear previous references (suboptimal but simple)
    resolvedReferences.clear()
    for (reference <- referenceCollection) {
      val referenceID = idExtractor.apply(reference)
      referenceID match {
        case null =>
        case referenceName =>
          this.resolvedReferences.addOne(reference)
        case _ =>
      }
    }

    resolvedReferences
  }

  @throws[UnexpectedIdentifierException]
  protected def usingReferenceIdentifier(exp: Node): Unit = {
    if(referencePDXIdentifiers.contains(exp.name)) {

    } else {
      throw new UnexpectedIdentifierException(exp)
    }
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

  def getReferenceCollectionNames: Iterable[String] = referenceCollectionSupplier().flatMap(idExtractor) //referenceCollectionSupplier().map(idExtractor).filter(_.isDefined).map(_.get)

  def addReferenceName(newValue: String): Unit = {
    referenceNames.addOne(newValue)
    resolveReferences()
  }

  /**
   * Size of actively valid references (resolved PDXScript object references)
   *
   * @return
   */
  override def size: Int = {
    return get().size
  }

  def numReferences: Int = referenceNames.size

  def resolvedReferences: ListBuffer[T] = pdxList
}