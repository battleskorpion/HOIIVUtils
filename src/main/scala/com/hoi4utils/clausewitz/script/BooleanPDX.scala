package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz_parser.NodeValue
import org.jetbrains.annotations.NotNull

class BooleanPDX(pdxIdentifiers: List[String], final private var defaultValue: Boolean, final private var boolType: BoolType) extends AbstractPDX[Boolean](pdxIdentifiers) {

  def this(pdxIdentifier: String, defaultValue: Boolean, boolType: BoolType) = {
    this(List(pdxIdentifier), defaultValue, boolType)
  }

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match {
      case _: Boolean =>
//      case _: String =>
//        val v = this.node.$.asInstanceOf[String]
//        if (boolType.maches(v)) obj = boolType.parse(v)
//        else throw new NodeValueTypeException(expression, "String parsable as Bool matching enum + " + boolType.toString)
      case _ => throw new NodeValueTypeException(expression, "Boolean or String")
    }
  }
  
  override def set(value: Boolean): Unit = {
    if (this.node.nonEmpty)
      this.node.foreach(_.setValue(value))
    else
      this.node = Some(Node(NodeValue(value)))
  }

  override def get(): Option[Boolean] = {
    val v = super.get()
    v.get match {
      case b: Boolean => Some(b)
      case _ => None
    }
  }

  def $ : Boolean = {
    get().getOrElse(defaultValue)
  }

  def objEquals(other: PDXScript[?]): Boolean = {
    other match {
      case b: BooleanPDX => node.equals(b.node)
      case _ => false
    }
  }

  def invert: Boolean = {
    setNode(!this.$)
    this.$
  }
}
