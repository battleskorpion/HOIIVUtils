package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

//object Node {
//  private val boolType: BoolType = null
//}

class Node(@NotNull private var _identifier: Option[String], @NotNull private var _operator: Option[String],
           protected[clausewitz_parser] var nodeValue: NodeValue, protected[clausewitz_parser] var nameToken: Token,
           protected[clausewitz_parser] var operatorToken: Token)
  extends NodeIterable[Node] {

  if (nodeValue == null) nodeValue = new NodeValue
  
  def this(@NotNull identifier: String, @NotNull operator: String, value: String | Int | Double | Boolean | ListBuffer[Node] | Null) = {
    this(Some(identifier), Some(operator), new NodeValue(value), null, null)
  }
  
  def this(value: NodeValue) = {
    this(None, None, value, null, null)
  }

  def this() = {
    this(null.asInstanceOf[NodeValue])
  }

  def this(value: ListBuffer[Node]) = {
    this(new NodeValue(value))
  }

  def name: String = identifier.getOrElse("")

  def value: Option[String | Int | Double | Boolean | ListBuffer[Node]] = nodeValue.value

  //def getValue(id:String): NodeValue = find(id).nodeValue
  def getValue(id: String): NodeValue = {
    val value = find(id)
    value match {
      case Some(node) => node.nodeValue
      case None => null
    }
  }
  
  def getValueCaseInsensitive(id: String): NodeValue = {
    val value = findCaseInsensitive(id)
    value match {
      case Some(node) => node.nodeValue
      case None => null
    }
  }

  def setValue(value: String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    this.nodeValue.setValue(value)
  }

  def isParent: Boolean = nodeValue.isList

  def valueIsNull: Boolean = this.$ == null
  
  override def isEmpty: Boolean = {
    valueIsNull && identifier == null && operator == null
  }

  /**
   * Returns a string representation of the individual node.
   * @return
   */
  override def toString: String = {
    if (!isEmpty)
      (identifier, operator) match {
        case (Some(id), Some(op)) =>
          id + op + nodeValue.asString
        case (Some(id), None) =>
          id + nodeValue.asString
        case (None, Some(op)) =>
          "[null]" + op + nodeValue.asString
        case (None, None) =>
          nodeValue.asString
      }
    else super.toString()
  }

  def toScript: String = {
    val sb = new StringBuilder()

    if (!isEmpty && !valueIsNull) {
      if (identifier.nonEmpty) sb.append(identifier.get).append(' ')
      if (operator.nonEmpty) sb.append(operator.get).append(' ')
      $.match {
        case l: ListBuffer[Node] =>
          // special handling if is list of numbers such as '0.0 1.0 0.7 1.3...'
          // todo make this a setting? set number before break line? etc? 
          if (l.forall(_.identifier.isEmpty) && l.forall(_.operator.isEmpty) && l.forall(_.nodeValue.isNumber)) {
            sb.append("{").append(' ')
            sb.append(l.map(_.nodeValue.asString).mkString(" "))
            sb.append(' ').append("}").append('\n')
          }
          else {
            sb.append("{").append('\n').append('\t')
            for (node <- l) {
              var sScript = node.toScript
              if (sScript != null && sScript.nonEmpty) {
                // add extra tab to any secondary lines
                sScript = sScript.replace("\n", "\n\t")
                sb.append(sScript)
              }
            }
            // unindent, close block
            sb.deleteCharAt(sb.size - 1).append("}").append('\n')
          }
        case _ =>
          sb.append(nodeValue.asString).append('\n')
      }
    } else if (identifier != null && operator != null) {
      sb.append(identifier.get).append(' ')
      sb.append(operator.get).append(' ')
      sb.append("[null]").append('\n')
    } else {
      sb.append("[null]").append('\n')
    }
    sb.toString()
  }

  def nameAsInteger: Int = identifier match {
    case None => 0
    case Some(s) => s.toInt
  }

  def nameEquals(s: String): Boolean = {
    identifier match {
      case None => if s == null then true else false
      case Some(id) => id.equals(s)
    }
  }
  
  def setNull(): Unit = nodeValue = new NodeValue
  
  def valueIsInstanceOf(clazz: Class[?]): Boolean = nodeValue.valueIsInstanceOf(clazz)

  def $ : String | Int | Double | Boolean | ListBuffer[Node] | Null = value match {
    case None => null
    case Some(v) => v
  }

  def identifier: Option[String] = _identifier

  def identifier_= (identifier: String): Unit = {
    if (identifier == null) _identifier = None
    else _identifier = Some(identifier)
  }

  def operator: Option[String] = _operator

  def operator_= (operator: String): Unit = {
    if (operator == null) _operator = None
    else _operator = Some(operator)
  }

  override def iterator: Iterator[Node] = {
    this.$ match {
      case l: ListBuffer[Node] => l.iterator
      case _ =>List(this).iterator
    }
  }

  def $list(): Option[ListBuffer[Node]] = {
    this.$ match {
      case l: ListBuffer[Node] => Some(l)
      case _ => None
    }
  }

  def $string: Option[String] = {
    this.$ match {
      case s: String => Some(s)
      case _ => null
    }
  }

  def $stringOrElse(x: String): String = {
    this.$ match {
      case i: String => i
      case _ => x
    }
  }
  
  def $int: Option[Int] = {
    this.$ match {
      case i: Int => Some(i)
      case _ => None
    }
  }

  def $intOrElse(x: Int): Int = {
    this.$ match {
      case i: Int => i
      case _ => x
    }
  }

  def $double: Option[Double] = {
    this.$ match {
      case d: Double => Some(d)
      case _ => None
    }
  }

  def $doubleOrElse(x: Double): Double = {
    this.$ match {
      case i: Double => i
      case _ => x
    }
  }

  def $boolean: Option[Boolean] = {
    this.$ match {
      case b: Boolean => Some(b)
      case _ => None
    }
  }

  def $booleanOrElse(x: Boolean): Boolean = {
    this.$ match {
      case i: Boolean => i
      case _ => x
    }
  }

  def valueAsString: String = {
    nodeValue.asString
  }
  
  def start: Int = nameToken.start
}