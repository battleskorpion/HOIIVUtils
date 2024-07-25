package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import com.hoi4utils.clausewitz_parser.Parser
import com.hoi4utils.clausewitz_parser.ParserException
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import scala.jdk.CollectionConverters._

import java.io.File

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * <p>
 */
trait AbstractPDX[T] extends PDXScript[T] {
  final protected var pdxIdentifiers: List[String] = _
  private[script] var activeIdentifier = 0
  protected var node: Node = _

  def this(pdxIdentifiers: String*) = {
    this()
    this.pdxIdentifiers = List.of(pdxIdentifiers)
  }

  def this(pdxIdentifiers: List[String]) = {
    this()
    this.pdxIdentifiers = pdxIdentifiers
  }

  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(exp: Node): Unit = {
    for (i <- 0 until pdxIdentifiers.size) {
      if (exp.nameEquals(pdxIdentifiers.get(i))) {
        activeIdentifier = i
        return
      }
    }
    throw new UnexpectedIdentifierException(exp)
  }

  override def set(value: T): Unit = {
    // todo?
    node.setValue(value)
  }

  @SuppressWarnings(Array("unchecked"))
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.$
//    try obj = value.valueObject.asInstanceOf[T]
//    catch {
//      case e: ClassCastException =>
//        throw new NodeValueTypeException(expression, e)
//    }
    set(value)
  }

  override def get(): T = {
    node.$() match {
      case value: T => value
      case _ => null.asInstanceOf[T] // Use a default value for T if necessary
    }
  }

  def getNode: Node = node

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

  override def loadPDX(expressions: List[Node]): Unit = {
    Option(expressions).foreach { exprs =>
      val scalaExprs = exprs.asScala.toList
      scalaExprs.find(isValidIdentifier) match {
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
      System.err.println("Focus tree file does not exist: " + file)
      return
    }
    /* parser */
    val pdxParser = new Parser(file)
    var rootNode: Node = null
    try rootNode = pdxParser.parse
    catch {
      case e: ParserException =>
        System.err.println("Error parsing focus tree file: " + file)
        return
    }
    try loadPDX(rootNode)
    catch {
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
  }

  override def isValidIdentifier(node: Node): Boolean = {
    import scala.collection.JavaConversions._
    for (identifier <- pdxIdentifiers) {
      if (node.name == identifier) return true
    }
    false
  }

  override def setNull(): Unit = {
    node.setNull()
  }

  override def loadOrElse(exp: Node, value: T): Unit = {
    try loadPDX(exp)
    catch {
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
    if (node.valueIsNull) set(value)
  }

  override def toScript: String = {
    if (node == null || node.isEmpty) return null
    pdxIdentifiers.get(activeIdentifier) + " = " + node + "\n"
  }

  def nodeEquals(other: AbstractPDX[_]): Boolean = {
    if (node == null) return false
    if (other.node == null) return false
    node.equals(other.node)
  }

  override def getOrElse(elseValue: T): T = {
    if (isUndefined) return elseValue
    val value = node.getValue
    value match
      case t: T => t
      case _ => elseValue
  }

  override def toString: String = {
    if (node == null || node.isEmpty) return super.toString
    node.toString
  }

  override def isUndefined: Boolean = node == null || node.valueIsNull

  override def getPDXIdentifier: String = pdxIdentifiers.get(activeIdentifier)

  def valueIsInstanceOf(clazz: Class[_]): Boolean = {
    if (node == null) return false
    node.$ match {
      case _: clazz => true
      case _ => false
    }
  }

  def schema(): PDXSchema[T] = {
    var schema = new PDXSchema[T](pdxIdentifiers)
  }

}