package com.hoi4utils.script2

import com.hoi4utils.parser.{NodeValueType, PDXValueNode, SeqNode}
import com.hoi4utils.script.PDXFileError
import jdk.internal.net.http.common.Log.errors
import org.apache.poi.hssf.record.aggregates.SharedValueManager.createEmpty

import scala.reflect.ClassTag
import scala.reflect.Selectable.reflectiveSelectable

class PDXProperty[T](val pdxKey: String, private var _value: Option[T] = None)
                    (using override val decoder: PDXDecoder[T])
                    (using override val ct: ClassTag[T]) 
  extends PDXScript[T]:

  private var _isRequired: Boolean = false
  private var _default: Option[T] = None

  override def apply(): Option[T] = _value.orElse(_default)
  def $: T = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  override def pdxDefinedValueOption: Option[T] = _value

  def :=(newValue: T): Unit = _value = Some(newValue)

  override def set(value: T): Unit = this := value

  override def extractAndSet(nodeValue: NodeValueType): Either[String, Unit] =
    decoder.decode(nodeValue).map(v => this := v)

  infix def default(v: T): PDXProperty[T] =
    _default = Some(v)
    this
  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXProperty[T] = { _isRequired = v; this }
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXProperty[T] = { /* store validation logic */ this }

  override def load[C](node: SeqNode, context: C, 
                       loadCallback: (SeqNode, PDXEntity, C) => List[String]): Either[List[String], Unit] =
    getEmptyInstance(context) match
      case Some(child: PDXEntity) =>
        val errors: List[String] = loadCallback(node, child, context)
        set(child) 
        if errors.nonEmpty then Left(errors) else Right(())
      case _ =>
        node.rawValue.foreach {
          case cvn: PDXValueNode[?] => extractAndSet(cvn.rawValue)
          case _ => ()
        }
        Right(())
      
class PDXPropertyList[T](val pdxKey: String, private var _values: Option[List[T]] = None)
                        (using override val decoder: PDXDecoder[List[T]], val elementDecoder: PDXDecoder[T])
                        (using override val ct: ClassTag[T])
  extends PDXScript[List[T]]:

  private var _isRequired: Boolean = false
  private var _default: Option[List[T]] = None

  override def apply(): Option[List[T]] = _values.map(_.reverse).orElse(_default)
  def $: List[T] = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  override def pdxDefinedValueOption: Option[List[T]] = _values
  
  def :+(value: T): Unit = _values match
    case Some(values) => _values = Some(value :: values)
    case None => _values = Some(value :: Nil)

  /**
   * ``
   * @param value
   * @note Since apply() reverses, we must store this reversed so the final output is correct.
   */
  override def set(value: List[T]): Unit = _values = Some(value.reverse)

  override def extractAndSet(nodeValue: NodeValueType): Either[String, Unit] =
    elementDecoder.decode(nodeValue).map(v => this :+ v)

  infix def default(v: List[T]): PDXPropertyList[T] =
    _default = Some(v)
    this

  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXPropertyList[T] =
    _isRequired = v
    this
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXPropertyList[T] = { /* store validation logic */ this }

  override def load[C](node: SeqNode, context: C,
                       loadCallback: (SeqNode, PDXEntity, C) => List[String]): Either[List[String], Unit] =
    elementDecoder.createEmpty(context) match
      case Some(child: PDXEntity) =>
        val errors: List[String] = loadCallback(node, child, context)
        this :+ child
        if errors.nonEmpty then Left(errors) else Right(())
      case _ =>
        node.rawValue.foreach {
          case cvn: PDXValueNode[?] => extractAndSet(cvn.rawValue)
          case _ => ()
        }
        Right(())


