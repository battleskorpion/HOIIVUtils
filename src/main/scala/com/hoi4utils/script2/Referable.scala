package com.hoi4utils.script2

trait Referable[K <: String | Int]:
  type KeyType = K

  def referableID: Option[KeyType]

trait IDReferable[K <: String | Int] extends Referable[K]:
  def idProperty: PDXProperty[KeyType]

  override def referableID: Option[KeyType] = idProperty()

trait NameReferable[K <: String | Int] extends Referable[K]:
  var identifier: Option[K] = None

  final def nameProperty: Option[KeyType] = identifier

  override def referableID: Option[KeyType] = identifier

  def referableID_=(value: K): Unit = identifier = Some(value)
  def clearReferableID(): Unit = identifier = None
