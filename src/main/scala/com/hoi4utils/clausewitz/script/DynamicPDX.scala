package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz_parser.Node
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util
import java.util.List
import java.util.function.Supplier


class DynamicPDX[V, U <: StructuredPDX] extends PDXScript[V] {
  protected var simplePDX: PDXScript[V] = _
  protected var simplePDXSupplier: Supplier[PDXScript[V]] = _ 
  final protected var structuredBlock: U = null.asInstanceOf[U]
  /**
   * may be null. the structured block does not always have a single property
   * that is equivalent to the simple value. Or, the structured block has a
   * property that is equivalent to a value and the property is null.
   */
  private var structuredPDXValueIdentifiers: util.List[String] = _ 

  def this(simplePDXSupplier: Supplier[PDXScript[V]], structuredBlock: U) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = null
  }

  def this(simplePDXSupplier: Supplier[PDXScript[V]], structuredBlock: U, structuredPDXValueIdentifier: String) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = util.List.of(structuredPDXValueIdentifier)
  }

  def this(simplePDXSupplier: Supplier[PDXScript[V]], structuredBlock: U, structuredPDXValueIdentifier: util.List[String]) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier
  }

  def this(structuredBlock: U) = {
    this()
    this.simplePDXSupplier = null
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = null
  }

  def this(structuredBlock: U, structuredPDXValueIdentifier: String) = {
    this()
    this.simplePDXSupplier = null
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = util.List.of(structuredPDXValueIdentifier)
  }

  def this(structuredBlock: U, structuredPDXValueIdentifier: util.List[String]) = {
    this()
    this.simplePDXSupplier = null
    this.structuredBlock = structuredBlock
    this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier
  }

  def this(simplePDXSupplier: Supplier[PDXScript[V]]) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = null.asInstanceOf[U]
    this.structuredPDXValueIdentifiers = null
  }

  def this(simplePDXSupplier: Supplier[PDXScript[V]], structuredPDXValueIdentifier: String) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = null.asInstanceOf[U]
    this.structuredPDXValueIdentifiers = util.List.of(structuredPDXValueIdentifier)
  }

  def this(simplePDXSupplier: Supplier[PDXScript[V]], structuredPDXValueIdentifier: util.List[String]) = {
    this()
    this.simplePDXSupplier = simplePDXSupplier
    this.structuredBlock = null.asInstanceOf[U]
    this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier
  }

  def setSimplePDX(simplePDX: PDXScript[V]): Unit = {
    this.simplePDX = simplePDX
    this.setNull(structuredBlock)
  }

  private def setNull(pdx: AbstractPDX[?]): Unit = {
    if (pdx != null) pdx.setNull()
  }

  def setSimplePDX(obj: V): Unit = {
    if (obj == null) {
      simplePDX = null
      return
    }
    supplySimplePDX()
    simplePDX.set(obj)
  }

  def isBlock: Boolean = simplePDX == null

  private def isBlock(expression: Node) = expression.$.isList

  override def set(obj: V): Unit = {
    if (!isBlock) setSimplePDX(obj)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  def set(expression: Node): Unit = {
    if (!isBlock(expression)) {
      supplySimplePDX()
      simplePDX.set(expression)
    }
    else if (blockAllowed) structuredBlock.set(expression)
  }

  override def get(): V = if (!isBlock) simplePDX.get()
  else if (isBlock) {
    val valueProperty = getStructuredValueProperty
    if (valueProperty == null) null.asInstanceOf[V]
    else valueProperty.get
  }
  else null.asInstanceOf[V]

  @throws[UnexpectedIdentifierException]
  def loadPDX(expression: Node): Unit = {
    if (!isBlock(expression)) {
      supplySimplePDX()
      simplePDX.loadPDX(expression)
    }
    else if (blockAllowed) structuredBlock.loadPDX(expression)
  }

  private def supplySimplePDX(): Unit = {
    if (simplePDX == null) simplePDX = simplePDXSupplier.get
  }

  def loadPDX(expressions: util.List[Node]): Unit = {
    if (blockAllowed) structuredBlock.loadPDX(expressions)
    simplePDX.setNull()
  }

  def isValidIdentifier(node: Node): Boolean = {
    // todo i hope this works as intended
    if (!isBlock) simplePDX.isValidIdentifier(node)
    else if (blockAllowed) structuredBlock.isValidIdentifier(node)
    else false
  }

  private def blockAllowed = structuredBlock != null

  override def setNull(): Unit = {
    simplePDX = null
    setNull(structuredBlock)
  }

  def loadOrElse(exp: Node, value: V): Unit = {
    if (!isBlock) simplePDX.loadOrElse(exp, value)
    else if (blockAllowed) structuredBlock.loadOrElse(exp, null)
  }

  override def toScript: String = if (!isBlock) simplePDX.toScript
  else if (blockAllowed) structuredBlock.toScript
  else "[null DynamicPDX]"

  override def nodeEquals(other: PDXScript[?]): Boolean = if (!isBlock) simplePDX.nodeEquals(other)
  else if (blockAllowed) structuredBlock.nodeEquals(other)
  else false

  override def getOrElse(elseValue: V): V = {
    val get = get
    if (get == null) elseValue
    else get
  }

  override def isUndefined: Boolean = get() == null

  override def toString: String = "DynamicPDX{" + "simpleValue=" + simplePDX + ", structuredValue=" + structuredBlock + '}'

  /**
   * may be null. the structured block does not always have a single property
   * that is equivalent to the simple value. Or, the structured block has a
   * property that is equivalent to a value and the property is null.
   */
  private def getStructuredValueProperty = if (isBlock && blockAllowed && structuredPDXValueIdentifiers != null) structuredBlock.getPDXPropertyOfType[V](structuredPDXValueIdentifiers)
  else null

  override def getPDXIdentifier: String = if (!isBlock) simplePDX.getPDXIdentifier
  else if (blockAllowed) structuredBlock.getPDXIdentifier
  else null

  def getPDXScript: PDXScript[?] = if (!isBlock) simplePDX
  else structuredBlock
}
