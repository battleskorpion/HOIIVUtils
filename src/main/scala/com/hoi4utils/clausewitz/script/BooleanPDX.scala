package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull

class BooleanPDX(pxdIdentifiers: List[String], final private var defaultValue: Boolean, final private var boolType: BoolType) extends AbstractPDX[Boolean | Null](pdxIdentifiers) {

  def this(pdxIdentifier: String, defaultValue: Boolean, boolType: BoolType) = {
    this(List(pdxIdentifier), defaultValue, boolType)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = expression
    this.node.$ match {
      case _: Boolean =>
//      case _: String =>
//        val v = this.node.$.asInstanceOf[String]
//        if (boolType.maches(v)) obj = boolType.parse(v)
//        else throw new NodeValueTypeException(expression, "String parsable as Bool matching enum + " + boolType.toString)
      case _ => throw new NodeValueTypeException(expression, "Boolean or String")
    }
  }

  override def get(): Boolean = {
    val v = super.get()
    v match {
      case b: Boolean => b
      case n: Null => defaultValue
      case _ => throw new RuntimeException("Unexpected type: " + v.getClass)
    }
  }

  def objEquals(other: PDXScript[?]): Boolean = {
    other match {
      case b: BooleanPDX => node.equals(b.node)
      case _ => false
    }
  }

  def invert: Boolean = {
    set(!get())
    get()
  }
}