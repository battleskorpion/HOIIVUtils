package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.targetName
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
  extends MultiPDX[ReferencePDX[T]](Some(() => new ReferencePDX(referenceCollectionSupplier, idExtractor, referencePDXIdentifiers)), None, pdxIdentifiers) with LazyLogging {

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
        if (list.isEmpty) {
          System.out.println("PDX script had empty list: " + expression)
          return
        }
        usingIdentifier(expression)

        for (child <- list) {
          try {
            add(child)
          } catch {
            case e: UnexpectedIdentifierException =>
              System.err.println("Error loading child node: " + e.getMessage + "\n\t" + child)
          }
        }
        this.node = Some(expression)
      case _ =>
        try {
          add(expression)
        } catch {
          case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
            println("Error loading PDX script: " + e.getMessage + "\n\t" + expression)
            // Preserve the node so it isn’t lost.
            node = Some(expression)
        }
    }
  }

  def validReferences: Option[ListBuffer[T]] = references() match {
    case list if list.isEmpty => None
    case list => Some(list)
  }
  
  override def iterator: Iterator[ReferencePDX[T]] = {
    resolveReferences()
    super.iterator
  }

  def references(): ListBuffer[T] = {
    resolveReferences()
  }

//  @throws[UnexpectedIdentifierException]
//  @throws[NodeValueTypeException]
//  override def set(expression: Node): Unit = {
//    referenceNames.clear()
//    expression.$ match {
//      case s: String =>
//        referenceNames.addOne(s)
//      case _ =>
//        logger.warn(s"Expected string value for pdx reference identifier, got ${expression.$}")
//        throw new NodeValueTypeException(expression, "string", this.getClass)
//    }
//  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override protected def add(expression: Node): Unit = {
    checkReferenceIdentifier(expression)
    expression.$ match {
      case str: String =>
        if (simpleSupplier.isEmpty) throw new NodeValueTypeException(expression, "string", this.getClass)
        val childScript = simpleSupplier.get.apply()
        childScript.loadPDX(expression)
        pdxList.addOne(childScript)
        referenceNames.addOne(str)
      case other =>
        logger.warn(s"Expected string value for pdx reference identifier, got ${other}. Preserving node using its string representation.")
        // Preserve the problematic node as a string.
        val preservedValue = other.toString
        if (simpleSupplier.isEmpty) throw new NodeValueTypeException(expression, "string", this.getClass)
        val childScript = simpleSupplier.get.apply()
        childScript.loadPDX(new Node(preservedValue))
        pdxList.addOne(childScript)
        referenceNames.addOne(preservedValue)
        // Then throw the exception so that callers are aware of the issue.
        throw new NodeValueTypeException(expression, "string", this.getClass)
    }
  }

  /**
   * Removes a reference (wrapper) that matches the given predicate.
   */
  override def removeIf(p: ReferencePDX[T] => Boolean): ListBuffer[ReferencePDX[T]] = {
    for (i <- pdxList.indices.reverse) {
      if (p(pdxList(i))) {
        pdxList(i).clearNode()
        pdxList.remove(i)
        referenceNames.remove(i)
      }
    }
    pdxList
  }

  /**
   * Adds a PDXScript to the list of PDXScripts. Used for when the PDXScript is not loaded from a file.
   *
   * @param referencePDX
   */
  @targetName("add")
  override def +=(referencePDX: ReferencePDX[T]): Unit = {
    pdxList += referencePDX
    referenceNames.addOne(referencePDX.referenceName)   // todo throw error instead and check this first
  }

  /**
   * Adds a new reference by providing a candidate of type T.
   * This wraps the candidate in a ReferencePDX and adds its identifier to referenceNames.
   */
  def addReferenceTo(pdxScript: T): Unit = {
    val idOpt = idExtractor(pdxScript)
    if (idOpt.isEmpty) {
      throw new NodeValueTypeException(new Node(""), "Unable to extract reference identifier", this.getClass)
    }
    // Create a new ReferencePDX[T] using the supplier.
    val wrapper: ReferencePDX[T] = simpleSupplier.get.apply()
    // Set the value of the wrapper to the candidate.
    wrapper.set(pdxScript)
    pdxList += wrapper
    referenceNames.addOne(idOpt.get)
  }

  /**
   * Removes a PDXScript from the list of PDXScripts.
   *
   * @param referencePDX
   */
  override def -=(referencePDX: ReferencePDX[T]): this.type = {
    val index = pdxList.indexOf(referencePDX)
    pdxList -= referencePDX
    referencePDX.clearNode()
    referenceNames.remove(index)
    this
  }

  /**
   * Removes a reference by candidate.
   */
  def removeReferenceTo(pdxScript: T): this.type = {
    // Find the wrapper whose extracted id matches the candidate.
    val idOpt = idExtractor(pdxScript)
    idOpt.foreach { id =>
      val idx = referenceNames.indexOf(id)
      if (idx >= 0) {
        pdxList(idx).clearNode()
        pdxList.remove(idx)
        referenceNames.remove(idx)
      }
    }
    this
  }

  /**
   * Removes a PDXScript from the list of PDXScripts.
   *
   * @param pdxScript
   * @note Java was *struggling* with 'this.type' return type. Use '-=' otherwise.
   * @return
   */
  override def remove(pdxScript: ReferencePDX[T]): Unit = {
    this -= pdxScript
  }

  override def clear(): Unit = {
    node.foreach { n =>
      n.$ match {
        case l: ListBuffer[T] => l.clear()
        case _ => // do nothing
      }
    }
    pdxList.clear()
    referenceNames.clear()
  }

  override def addNewPDX(): ReferencePDX[T] = {
    super.addNewPDX() // no override necessary.
  }

  private def resolveReferences(): ListBuffer[T] = {
//    // clear previous references (suboptimal but simple)
//    resolvedReferences.clear()
    val resolvedReferences = new ListBuffer[T]

    resolvedReferences ++= referenceCollectionSupplier().filter { reference =>
      // idExtractor(reference) is an Option[String], so .exists(...) will be true only
      // if it's Some(...) *and* that string is in referenceNames
      idExtractor(reference).exists(referenceNames.contains)
    }
    resolvedReferences
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

  private def checkReferenceIdentifier(exp: Node): Unit = {
    if (!referencePDXIdentifiers.contains(exp.name))
      throw new UnexpectedIdentifierException(exp)
  }

  /**
   * Size of actively valid references (resolved PDXScript object references)
   *
   * @return
   */
  override def length: Int = validReferences match {
    case Some(list) => list.size
    case None => 0
  }

  override def isUndefined: Boolean = referenceNames.isEmpty

  def numReferences: Int = referenceNames.size

  override def clearNode(): Unit = {
    super.clearNode()
  }

  /**
   * On-demand Node rebuilding: rebuild the underlying node from the current list of reference names.
   */
  override def updateNodeTree(): Unit = {
    super.updateNodeTree() // shouldn't need override
  }
}