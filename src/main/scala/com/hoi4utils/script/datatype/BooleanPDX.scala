package com.hoi4utils.script.datatype

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, PDXValueNode}
import com.hoi4utils.script.{AbstractPDX, PDXScript}
import com.hoi4utils.shared.BoolType

type BooleanNodeType = Boolean | String

class BooleanPDX(pdxIdentifiers: List[String], final private var defaultValue: Boolean, final private var boolType: BoolType) extends AbstractPDX[Boolean, Boolean](pdxIdentifiers):

  def this(pdxIdentifier: String, defaultValue: Boolean = false, boolType: BoolType = BoolType.YES_NO) =
    this(List(pdxIdentifier), defaultValue, boolType)

  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node[BooleanNodeType]): Unit =
    usingIdentifier(expression)
    expression.$ match
      case _: Boolean =>
      case s: String => 
        if (boolType.matches(s)) set(boolType.parse(s))
        else throw new NodeValueTypeException(expression, "String parsable as Bool matching enum + " + boolType.toString, this.getClass)
  
  override def set(value: Boolean): Boolean =
    if (this.node.nonEmpty)
      this.node.foreach(_.setValue(value))
    else
      this.node = Some(PDXValueNode(value))
    value

  override def value: Option[Boolean] =
    val v = super.value
    v.orNull match
      case b: Boolean => Some(b)
      case null => None

  def $ : Boolean =
    value.getOrElse(defaultValue)

  def objEquals(other: PDXScript[?, ?]): Boolean =
    other match
      case b: BooleanPDX => node.equals(b.node)
      case _ => false

  def invert: Boolean =
    setNode(!this.$)
    this.$

  override def toString: String = this.value.map(_.toString).getOrElse("")
