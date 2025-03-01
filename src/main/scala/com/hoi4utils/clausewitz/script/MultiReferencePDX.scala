package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.{Node, NodeValue}
import org.jetbrains.annotations.Nullable

import scala.collection.mutable.ListBuffer

/**
 * (todo)
 *
 * The parent MultiPDX pdx script list will be the list of resolved references
 *
 * @param referenceCollectionSupplier the supplier for the reference collection (the reference collection being
 *                                    the collection of PDXScript objects that could be referenced)
 * @param idExtractor the function to extract the identifier from a referenced PDXScript
 * @param pdxIdentifiers the identifiers for the PDXScript
 * @param referencePDXIdentifiers  the identifiers for the reference PDXScript
 * @tparam T the PDXScript type of the reference PDXScript objects
 */
class MultiReferencePDX[T <: AbstractPDX[?]](protected var referenceCollectionSupplier: () => Iterable[T],
                                             protected var idExtractor: T => Option[String], pdxIdentifiers: List[String],
                                             referencePDXIdentifiers: List[String])
  extends MultiPDX[T](None, None, pdxIdentifiers) {

  final protected val referenceNames = new ListBuffer[String]

  def this(referenceCollectionSupplier: () => Iterable[T], idExtractor: T => Option[String], pdxIdentifiers: String, referenceIdentifier: String) = {
    this(referenceCollectionSupplier, idExtractor, List(pdxIdentifiers), List(referenceIdentifier))
  }

  /**
   * Load the PDX script from the given expression. If the expression is a list, then each element of the list will be
   * loaded as a separate PDX script, if applicable.
   * @param expression
   * @throws UnexpectedIdentifierException
   */
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
   *
   * @return
   */
  override def get(): Option[ListBuffer[T]] = references() match {
    case list if list.isEmpty => None
    case list => Some(list)
  }

  def references(): ListBuffer[T] = {
    resolveReferences()
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    referenceNames.clear()
    expression.$ match {
      case s: String => referenceNames.addOne(s)
      case _ =>
        LOGGER.warn(s"Expected string value for pdx reference identifier, got ${expression.$}")
        throw new NodeValueTypeException(expression, "string")
    }
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def add(expression: Node): Unit = {
    usingReferenceIdentifier(expression)
    expression.$ match {
      case str: String => referenceNames.addOne(str)
      case _ =>
        LOGGER.warn(s"Expected string value for pdx reference identifier, got ${expression.$}")
        throw new NodeValueTypeException(expression, "string")
    }
  }

  private def resolveReferences(): ListBuffer[T] = {
    // clear previous references (suboptimal but simple)
    resolvedReferences.clear()

    resolvedReferences ++= referenceCollectionSupplier().filter { reference =>
      // idExtractor(reference) is an Option[String], so .exists(...) will be true only
      // if it's Some(...) *and* that string is in referenceNames
      idExtractor(reference).exists(referenceNames.contains)
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
    get() match {
      case Some(scripts) =>
        for (identifier <- referenceNames) {
          sb.append(getPDXIdentifier).append(" = ").append(identifier).append("\n")
        }
      case None => return null
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

  def addReference(reference: T, index: Int): Unit = {
    val id = idExtractor(reference)
    if (id.isDefined) {
      referenceNames.insert(index, id.get)
      resolveReferences()
    }
  }

  def addReference(reference: T): Unit = {
    val id = idExtractor(reference)
    if (id.isDefined) {
      referenceNames.addOne(id.get)
      resolveReferences()
    }
  }

  def removeReference(index: Int): Unit = {
    referenceNames.remove(index)
    resolveReferences()
  }

  def removeReference(reference: T): Unit = {
    val id = idExtractor(reference)
    if (id.isDefined) {
      referenceNames.remove(referenceNames.indexOf(id.get))
      resolveReferences()
    }
  }

  def removeReference(referenceName: String): Unit = {
    val i = referenceNames.indexOf(referenceName)
    if (i >= 0) {
      referenceNames.remove(i)
      resolveReferences()
    } 
  }

  def containsReference(reference: T): Boolean = {
    val id = idExtractor(reference)
    id match {
      case Some(value) => referenceNames.contains(value)
      case None => false
    }
  }

  def containsReferenceName(referenceName: String): Boolean = {
    referenceNames.contains(referenceName)
  }

  def changeReference(oldName: String, newName: String): Unit = {
    val index = referenceNames.indexOf(oldName)
    if (index != -1) {
      referenceNames.update(index, newName)
      resolveReferences()
    }
  }

  /**
   * Size of actively valid references (resolved PDXScript object references)
   *
   * @return
   */
  override def length: Int = get() match {
    case Some(list) => list.size
    case None => 0
  }

  override def apply(idx: Int): T = get() match {
    case Some(list) => list(idx)
    case None => throw new IndexOutOfBoundsException
  }

  override def isUndefined: Boolean = referenceNames.isEmpty

  def numReferences: Int = referenceNames.size

  def resolvedReferences: ListBuffer[T] = pdxList
}