package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.HOIIVUtils
import com.hoi4utils.clausewitz.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.clausewitz_parser.{Node, NodeValue, Parser, ParserException}
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.annotations.{NotNull, Nullable}

import java.io.File
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * <p>
 */
trait AbstractPDX[T](protected val pdxIdentifiers: List[String]) extends PDXScript[T] {
  val LOGGER: Logger = LogManager.getLogger(classOf[AbstractPDX[T]])
  
  private[script] var activeIdentifier = 0
  protected[script] var node: Option[Node] = None

  /**
   * Sets the active identifier to match the given expression, if applicable.
   * @param exp
   * @throws UnexpectedIdentifierException
   */
  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(exp: Node): Unit = {
    for (i <- pdxIdentifiers.indices) {
      if (exp.nameEquals(pdxIdentifiers(i))) {
        activeIdentifier = i
        return
      }
    }
    // TODO: Do something so that this doesn't clog up the console
    LOGGER.error("Unexpected identifier: " + exp)
    throw new UnexpectedIdentifierException(exp)
  }

  /**
   * @inheritdoc
   */
  override def setNode(value: T | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
  // todo?
    if (node.isEmpty) {
      return
    }
    if (value == null) {
      setNull()
      return
    }
    value.match {
      case s: String => node.get.setValue(s)
      case i: Int => node.get.setValue(i)
      case d: Double => node.get.setValue(d)
      case b: Boolean => node.get.setValue(b)
      case _ => throw new RuntimeException("Unsupported type")
    }
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
    setNode(value)
  }

  /**
   * @inheritdoc
   */
  override def get(): Option[T] = {
    node.getOrElse(return None).$ match {
      case value: T => Some(value)
      case _ => None
    }
  }

  /**
   * @inheritdoc
   */
  override def getNode: Option[Node] = node

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    if (expression.name == null) {
      System.out.println("Error loading PDX script: " + expression)
      return
    }
    try set(expression)
    catch {
      case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
        System.out.println("Error loading PDX script:" + e.getMessage + "\n\t" + expression)
    }
  }

  def loadPDX(expressions: Iterable[Node]): Unit = {
    Option(expressions).foreach { exprs =>
      exprs.find(isValidIdentifier) match {
        case Some(expression) =>
          try loadPDX(expression)
          catch {
            case e: UnexpectedIdentifierException =>
              throw new RuntimeException(e)
          }
        case None => setNull()
      }
    }
  }
  
  protected def loadPDX(file: File): Unit = {
    if (!file.exists) {
      LOGGER.error(s"Focus tree file does not exist: $file")
      return
    }

    try {
      val pdxParser = new Parser(file)
      val rootNode = pdxParser.parse
      loadPDX(rootNode)
    } catch {
      case e: ParserException =>
        LOGGER.error(s"Error parsing focus tree file: $file\n\t${e.getMessage}")
      case e: UnexpectedIdentifierException => throw new RuntimeException(e)
    }
  }

  /**
   * @inheritdoc
   */
  override def isValidIdentifier(node: Node): Boolean = {
    pdxIdentifiers.contains(node.name)
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

  override def toScript: String = {
    if (node.isEmpty || node.get.isEmpty) return null
    node.get.identifier + node.get.operator + node.get.$ + "\n"
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
    if (node.isEmpty || node.get.isEmpty) return super.toString
    node.toString
  }

  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull

  override def isDefined: Boolean = !isUndefined

  override def getPDXIdentifier: String = pdxIdentifiers(activeIdentifier)

  /**
   * Returns true if the value of the node is an instance of the specified class.
   * The implicit class tag is necessary to get around type erasure (preserve A's class at runtime).
   * @param ct
   * @tparam A
   * @return
   */
  def valueIsInstanceOf[A](implicit ct: ClassTag[A]): Boolean = {
    if (node.isEmpty) false
    else ct.runtimeClass.isInstance(node.get.$)
  }

  def schema(): PDXSchema[T] = {
    var schema = new PDXSchema[T](pdxIdentifiers*)
    null.asInstanceOf[PDXSchema[T]]  // todo no
  }

}