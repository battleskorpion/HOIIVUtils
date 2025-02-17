package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.HOIIVUtils
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import com.hoi4utils.clausewitz_parser.Parser
import com.hoi4utils.clausewitz_parser.ParserException
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import scala.collection.mutable.ListBuffer
import java.io.File
import scala.annotation.targetName

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * <p>
 */
trait AbstractPDX[T](protected val pdxIdentifiers: List[String]) extends PDXScript[T] {
  
  private[script] var activeIdentifier = 0
  protected[script] var node: Option[Node] = None

//  def this(pdxIdentifiers: String*) = {
//    this(pdxIdentifiers*)
//  }

  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(exp: Node): Unit = {
    for (i <- pdxIdentifiers.indices) {
      if (exp.nameEquals(pdxIdentifiers(i))) {
        activeIdentifier = i
        return
      }
    }
    // TODO: Do something so that this doesn't clog up the console
    throw new UnexpectedIdentifierException(exp)
  }
  
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
    setNode(value)
  }

  override def get(): Option[T] = {
    node.getOrElse(return None).$ match {
      case value: T => Some(value)
      case _ => None
    }
  }

  override def getNode: Option[Node] = node

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    if (expression.name == null) {
      if (HOIIVUtils.getBoolean("dev_mode.enabled")) {System.out.println("Error loading PDX script: " + expression)}
      return
    }
    try set(expression)
    catch {
      case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
        if (HOIIVUtils.getBoolean("dev_mode.enabled")) {System.out.println("Error loading PDX script:" + e.getMessage + "\n\t" + expression)}
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
      System.err.println("Focus tree file does not exist: " + file)
      return
    }
    /* parser */
    val pdxParser = new Parser(file)
    //var rootNode: Node = null
    try
      val rootNode = pdxParser.parse
      loadPDX(rootNode)
    catch
      case e: ParserException =>
        System.err.println("Error parsing focus tree file: " + file + "\n\t" + e.getMessage)
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
  }

  override def isValidIdentifier(node: Node): Boolean = {
    import scala.jdk.CollectionConverters
    for (identifier <- pdxIdentifiers) {
      if (node.name == identifier) return true
    }
    false
  }

  override def clearNode(): Unit = {
    node = None
  }

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

  override def getOrElse(elseValue: T): T = {
    val value = node.getOrElse(return elseValue).getValue
    value match
      case t: T => t
      case _ => elseValue
  }

  override def toString: String = {
    if (node.isEmpty || node.get.isEmpty) return super.toString
    node.toString
  }

  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull

  override def isDefined: Boolean = !isUndefined

  override def getPDXIdentifier: String = pdxIdentifiers(activeIdentifier)

  def valueIsInstanceOf[A]: Boolean = {
    if (node.isEmpty) return false
    node.get.$ match {
      case _: A => true
      case _ => false
    }
  }

  def schema(): PDXSchema[T] = {
    var schema = new PDXSchema[T](pdxIdentifiers*)
    null.asInstanceOf[PDXSchema[T]]  // todo no
  }

}