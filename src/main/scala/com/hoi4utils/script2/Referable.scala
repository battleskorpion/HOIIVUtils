package com.hoi4utils.script2

import java.io.File
import scala.reflect.{ClassTag, TypeTest}

trait Referable[K <: String | Int](using val keyTag: ClassTag[K]):
  type KeyType = K

  def referableID: Option[KeyType]

  def isIntKey: Boolean =
    keyTag.runtimeClass == classOf[Int]
  def isStringKey: Boolean =
    keyTag.runtimeClass == classOf[String]

trait IDReferable[K <: String | Int] extends Referable[K]:
  def idProperty: PDXProperty[KeyType]

  override def referableID: Option[KeyType] = idProperty()

trait NameReferable[K <: String | Int] extends Referable[K]:
  var identifier: Option[K] = None

  final def nameProperty: Option[KeyType] = identifier

  override def referableID: Option[KeyType] = identifier

  def referableID_=(value: K): Unit = identifier = Some(value)
  def clearReferableID(): Unit = identifier = None

trait FileReferable extends Referable[String]:
  def file: Option[File]

  override def referableID: Option[String] = file.map(_.getName)

object Referable:
  // Custom TypeTest for Referable[Int]
  given intReferableTypeTest: TypeTest[Any, Referable[Int]] with
    def unapply(x: Any): Option[x.type & Referable[Int]] = x match
      case r: Referable[?] if r.isIntKey => Some(r.asInstanceOf[x.type & Referable[Int]])
      case _ => None

  // Custom TypeTest for Referable[String]
  given stringReferableTypeTest: TypeTest[Any, Referable[String]] with
    def unapply(x: Any): Option[x.type & Referable[String]] = x match
      case r: Referable[?] if r.isStringKey => Some(r.asInstanceOf[x.type & Referable[String]])
      case _ => None

object NameReferable:
  // Custom TypeTest for NameReferable[Int]
  given intNameReferableTypeTest: TypeTest[Any, NameReferable[Int]] with
    def unapply(x: Any): Option[x.type & NameReferable[Int]] = x match
      case r: NameReferable[?] if r.isIntKey => Some(r.asInstanceOf[x.type & NameReferable[Int]])
      case _ => None

  // Custom TypeTest for NameReferable[String]
  given stringNameReferableTypeTest: TypeTest[Any, NameReferable[String]] with
    def unapply(x: Any): Option[x.type & NameReferable[String]] = x match
      case r: NameReferable[?] if r.isStringKey => Some(r.asInstanceOf[x.type & NameReferable[String]])
      case _ => None
