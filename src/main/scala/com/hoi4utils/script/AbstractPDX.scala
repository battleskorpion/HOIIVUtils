//package com.hoi4utils.script
//
//import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
//import com.hoi4utils.parser.{Node, NodeSeq, NodeValueType, PDXValueNode, PDXValueType, Parser, ParserException, ParsingContext, SeqNode}
//import com.hoi4utils.script.scripter.DefaultNodeScripter
//
//import java.io.File
//import scala.collection.mutable.ListBuffer
//import scala.reflect.ClassTag
//import scala.util.boundary
//
///**
// * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
// * or event.
// * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
// * <p>
// */
//abstract class AbstractPDX[T, NodeValue <: NodeValueType](protected var pdxIdentifiers: Seq[String])
//  extends VeryAbstractPDX[T, NodeValue](pdxIdentifiers):
//
//  type NodeType = Node[NodeValue]
//  type SeqNodeType = Seq[NodeType]
//
//  protected[script] var node: Option[NodeType] = None
//
//  /**
//   * @inheritdoc
//   */
//  override protected def setNode(value: NodeValue): Unit =
//    value match
//      case value if node.isEmpty => ()
//      case v: NodeValue => node.get.setValue(v)
////      case _ => throw new RuntimeException(s"Unsupported type: ${value.getClass}")
//
//  /**
//   * @inheritdoc
//   */
//  @throws[UnexpectedIdentifierException]
//  @throws[NodeValueTypeException]
//  override def set(expression: NodeType): Unit =
//    usingIdentifier(expression)
//    setNode(expression.$)
//
//  /**
//   * @inheritdoc
//   */
//  override def value: Option[T] = node match
//    case None => None
//    case Some(n) => n.$ match
//      case null => None
//      case value => try Some(value.asInstanceOf[T])
//        catch case _: ClassCastException => None
//
//  /**
//   * @inheritdoc
//   */
//  override def getNode: Option[NodeType] = node
//
//  // TODO TOOD fix lol
//  override def getNodes: List[NodeType] = getNode match
//    case Some(node) => List(node)
//    case None => List.empty
//
//  /**
//   * @inheritdoc
//   */
//  override def loadPDX(expression: NodeType, file: Option[File]): Unit =
//    if expression.identifier.isEmpty && (pdxIdentifiers.nonEmpty || expression.isEmpty) then
//      logger.error("Error loading PDX script: " + expression)
//    else
//      try
//        set(expression)
//      catch
//        case e: Exception =>
//          handlePDXError(e, expression, file.orNull)
//          node = Some(expression)
//
//  /**
//   * @inheritdoc
//   */
//  override def isValidIdentifier(node: Node[?]): Boolean = isValidID(node.name)
//
//  override def isValidID(identifier: String): Boolean = pdxIdentifiers.contains(identifier)
//
//  /**
//   * @inheritdoc
//   */
//  override def clearNode(): Unit = node = None
//
//  override def loadOrElse(exp: NodeType, value: T): Unit =
//    loadPDX(exp, None)
//    if node.get.valueIsNull then set(value)
//
//  /**
//   * Rebuilds the underlying Node tree from the current state.
//   * For simple leaf nodes, this is a no-op.
//   * Composite types (e.g. StructuredPDX) should override this method to rebuild their Node tree.
//   */
//  override def updateNodeTree(): Unit =
//    ()
//    // TODO TODO
////    node.foreach(n => setNode(value))
////    // Default behavior for leaf nodes: update the node's value from the current state.
//
//  /**
//   * Generates the script output.
//   * Before returning the script, updateNodeTree() is called so that the underlying Node reflects any changes.
//   */
//  override def toScript: String =
//    updateNodeTree()
//    node.map(DefaultNodeScripter.toScript).getOrElse("")
//
//  /**
//   * @inheritdoc
//   */
//  override infix def getOrElse(elseValue: T): T = boundary {
//    val value = node.getOrElse(boundary.break(elseValue)).value
//    value match
//      case Some(t) => t.asInstanceOf[T]
//      case _ => elseValue
//  }
//
//  override def equals(other: PDXScript[?, ?]): Boolean =
//    other match
//      case pdx: AbstractPDX[?, ?] =>
//        if node == null then return false
//        if pdx.node == null then return false
//        node.equals(pdx.node)
//      case _ => false
//
//  override def toString: String =
//    if node.isEmpty || node.get.isEmpty then
//      if value.isEmpty then super.toString
//      else value.get.toString
//    else node.get.toString
//
//  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull
//
//  override def isDefined: Boolean = !isUndefined
//
//  override def pdxIdentifier: String = if pdxIdentifiers.isEmpty then null else pdxIdentifiers(activeIdentifier)
//
//  /**
//   * Checks whether the node's value is an instance of the specified class.
//   */
//  def valueIsInstanceOf[A](implicit ct: ClassTag[A]): Boolean = node.exists(n => ct.runtimeClass.isInstance(n.$))
//
//  def getPDXTypeName: String = pdxIdentifier
//
//  def schema(): PDXSchema[T] =
//    var schema = new PDXSchema[T](pdxIdentifiers*)
//    null.asInstanceOf[PDXSchema[T]]  // todo no
//
