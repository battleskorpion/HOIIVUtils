package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, Parser, ParserException}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * <p>
 */
trait AbstractPDX[T](protected var pdxIdentifiers: List[String]) extends PDXScript[T] {
  private[script] var activeIdentifier = 0
  protected[script] var node: Option[Node] = None

  /**
   * Sets the active identifier to match the given expression, 
   * if it is a valid identifier. Otherwise, throws exception. 
   * @param expr the expression to check, and set the active identifier to the identifier of the expression
   * @throws UnexpectedIdentifierException if the expression is not a valid identifier
   */
  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(expr: Node): Unit = {
    if (pdxIdentifiers.isEmpty) {
      // all good? 
    }
    else if (pdxIdentifiers.indexWhere(expr.nameEquals) == -1) {
      logger.error("Unexpected identifier: " + expr.name)
      throw new UnexpectedIdentifierException(expr)
    } 
  }

  /**
   * @inheritdoc
   */
  override protected def setNode(value: T | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    if (node.isEmpty) {
      return
    }
    if (value == null) {
      setNull()
      return
    }
    value match {
      case s: String => node.get.setValue(s)
      case i: Int => node.get.setValue(i)
      case d: Double => node.get.setValue(d)
      case b: Boolean => node.get.setValue(b)
      case l: ListBuffer[Node] => node.get.setValue(l)
      case _ => throw new RuntimeException(s"Unsupported type: ${value.getClass}")
    }
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    setNode(expression.$)
  }

  /**
   * @inheritdoc
   */
  override def value: Option[T] = {
    node.getOrElse(return None).$ match {
      case value: T => Some(value)
      case _ => None
    }
  }

  /**
   * @inheritdoc
   */
  override def getNode: Option[Node] = node

  override def getNodes: List[Node] = getNode match {
    case Some(node) => List(node)
    case None => List.empty
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty && (pdxIdentifiers.nonEmpty || expression.isEmpty)) {
      logger.error("Error loading PDX script: " + expression)
      return
    }
    try {
      set(expression)
    } catch {
      case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
        logger.error("Error loading PDX script: " + e.getMessage + "\n\t" + expression)
        // Preserve the original node so that its content isn’t lost.
        node = Some(expression)
    }
  }

  /**
   * 
   * @param expressions
   * @return remaining unloaded expressions
   */
  def loadPDX(expressions: Iterable[Node]): Iterable[Node] = {
    val remaining = ListBuffer.from(expressions)
    expressions.foreach { expression =>
      if (isValidIdentifier(expression)) {
        try {
          loadPDX(expression)
          remaining -= expression
        }
        catch {
          case e: UnexpectedIdentifierException =>
            logger.error(e.getMessage)
        }
      }
    }
    remaining
  }
  
  protected def loadPDX(file: File): Unit = {
    if (!file.exists) {
      logger.error(s"Focus tree file does not exist: $file")
      return
    }

    try {
      val pdxParser = new Parser(file)
      val rootNode = pdxParser.parse
      loadPDX(rootNode)
    } catch {
      case e: ParserException =>
        logger.error(s"Error parsing focus tree file: $file\n\t${e.getMessage}")
      case e: UnexpectedIdentifierException => throw new RuntimeException(e)
    }
  }

  /**
   * @inheritdoc
   */
  override def isValidIdentifier(node: Node): Boolean = {
    isValidID(node.name)
  }
  
  override def isValidID(identifier: String): Boolean = {
    pdxIdentifiers.contains(identifier)
  }

  /**
   * @inheritdoc
   */
  override def clearNode(): Unit = {
    node = None
  }

  /**
   * @inheritdoc
   */
  override def setNull(): Unit = {
    node.foreach(_.setNull())
  }

  override def loadOrElse(exp: Node, value: T): Unit = {
    try loadPDX(exp)
    catch {
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
    if (node.get.valueIsNull) set(value)
  }

  /**
   * Rebuilds the underlying Node tree from the current state.
   * For simple leaf nodes, this is a no-op.
   * Composite types (e.g. StructuredPDX) should override this method to rebuild their Node tree.
   */
  override def updateNodeTree(): Unit = {
    // Default behavior for leaf nodes: update the node's value from the current state.
    node.foreach(n => setNode(value.orNull))
  }

  /**
   * Generates the script output.
   * Before returning the script, updateNodeTree() is called so that the underlying Node reflects any changes.
   */
  override def toScript: String = {
    updateNodeTree()
    node.map(_.toScript).getOrElse("")
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case pdx: AbstractPDX[?] =>
        if (node == null) return false
        if (pdx.node == null) return false
        node.equals(pdx.node)
      case _ => false
    }
  }

  /**
   * @inheritdoc
   */
  override def getOrElse(elseValue: T): T = {
    val value = node.getOrElse(return elseValue).value
    value match
      case Some(t: T) => t
      case _ => elseValue
  }

  override def toString: String = {
    if (node.isEmpty || node.get.isEmpty) {
      if (value.isEmpty) {
        return super.toString
      } else {
        return value.get.toString
      }
    }
    node.toString
  }

  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull

  override def isDefined: Boolean = !isUndefined

  override def pdxIdentifier: String = {
    if (pdxIdentifiers.isEmpty) return null
    else pdxIdentifiers(activeIdentifier)
  }

  /**
   * Checks whether the node’s value is an instance of the specified class.
   */
  def valueIsInstanceOf[A](implicit ct: ClassTag[A]): Boolean = {
    node.exists(n => ct.runtimeClass.isInstance(n.$))
  }

  def getPDXTypeName: String = {
    pdxIdentifier
  }

  def schema(): PDXSchema[T] = {
    var schema = new PDXSchema[T](pdxIdentifiers*)
    null.asInstanceOf[PDXSchema[T]]  // todo no
  }

}