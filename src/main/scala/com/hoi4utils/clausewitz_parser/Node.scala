package com.hoi4utils.clausewitz_parser

import com.hoi4utils.clausewitz.BoolType
import org.jetbrains.annotations.NotNull

import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

//object Node {
//  private val boolType: BoolType = null
//}

class Node(private var _identifier: String, private var _operator: String,
           protected[clausewitz_parser] var nodeValue: NodeValue, protected[clausewitz_parser] var nameToken: Token,
           protected[clausewitz_parser] var operatorToken: Token)
  extends NodeIterable[Node] {

  if (nodeValue == null) nodeValue = new NodeValue
  
  def this(identifier: String, operator: String, value: String | Int | Double | Boolean | ListBuffer[Node] | Null) = {
    this(identifier, operator, new NodeValue(value), null, null)
  }
  
  def this(value: NodeValue) = {
    this(null, null, value, null, null)
  }

  def this() = {
    this(null.asInstanceOf[NodeValue])
  }

  def this(value: ListBuffer[Node]) = {
    this(new NodeValue(value))
  }

  def name: String = identifier

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
      identifier + operator + nodeValue.asString
    else
      super.toString()
  }

  def toScript: String = {
    if (!isEmpty && !valueIsNull) {
      val sb = new StringBuilder()
      sb.append(identifier).append(' ')
      sb.append(operator).append(' ')
      $.match {
        case l: ListBuffer[Node] =>
          sb.append("{").append('\n')
          for (node <- l) {
            var sScript = node.toScript
            if (sScript != null && sScript.nonEmpty) {
              // add extra tab to any secondary lines
              sScript = sScript.replace("\n", "\n\t")
              sb.append('\t').append(sScript)
            }
          }
          sb.append("}").append('\n')
        case _ =>
          sb.append(nodeValue.asString).append('\n')
      }
      sb.toString
    } else {
      null
    }
  }

  def nameAsInteger: Int = identifier.toInt

  def nameEquals(s: String): Boolean = {
    if (identifier == null) return false
    identifier == s
  }
  
  def setNull(): Unit = nodeValue = new NodeValue
  
  def valueIsInstanceOf(clazz: Class[?]): Boolean = nodeValue.valueIsInstanceOf(clazz)

  def $ : String | Int | Double | Boolean | ListBuffer[Node] | Null = value match {
    case None => null
    case Some(v) => v
  }

  def identifier: String = _identifier

  def identifier_= (identifier: String): Unit = {
    _identifier = identifier
  }

  def operator: String = _operator

  def operator_= (operator: String): Unit = {
    _operator = operator
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
}