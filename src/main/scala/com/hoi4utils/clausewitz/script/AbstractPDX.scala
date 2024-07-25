package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import com.hoi4utils.clausewitz_parser.Parser
import com.hoi4utils.clausewitz_parser.ParserException
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import scala.jdk.CollectionConverters._

import java.io.File
import java.util
import java.util.List


/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * <p>
 */
abstract class AbstractPDX[T] extends PDXScript[T] {
//  protected var obj: T = null
  final protected var pdxIdentifiers: util.List[String] = _
  private[script] var activeIdentifier = 0
  protected var node: Node = _

//  def this(pdxIdentifier: String) {
//    this()
//    this.pdxIdentifiers = util.List.of(pdxIdentifier)
//  }

  def this(pdxIdentifiers: String*) {
    this()
    this.pdxIdentifiers = util.List.of(pdxIdentifiers)
  }

  def this(pdxIdentifiers: util.List[String]) {
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

  override def set(obj: T): Unit = {
    this.obj = obj
  }

  @SuppressWarnings(Array("unchecked"))
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    val value = expression.value
    try obj = value.valueObject.asInstanceOf[T]
    catch {
      case e: ClassCastException =>
        throw new NodeValueTypeException(expression, e)
    }
  }

  override def get(): T = {
    
  }

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

  override def loadPDX(expressions: util.List[Node]): Unit = {
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
    obj = null
  }

  override def loadOrElse(exp: Node, value: T): Unit = {
    try loadPDX(exp)
    catch {
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
    if (obj == null) obj = value
  }

  override def toScript: String = {
    if (obj == null) return null
    pdxIdentifiers.get(activeIdentifier) + " = " + obj + "\n"
  }

  def objEquals(other: AbstractPDX[_]): Boolean = {
    if (obj == null) return false
    if (other.obj == null) return false
    obj == other.obj
  }

  override def getOrElse(elseValue: T): T = if (isUndefined) elseValue
  else obj

  override def toString: String = {
    if (obj == null) return super.toString
    obj.toString
  }

  override def isUndefined: Boolean = obj == null

  override def getPDXIdentifier: String = pdxIdentifiers.get(activeIdentifier)
}