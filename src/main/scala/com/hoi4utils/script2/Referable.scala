package com.hoi4utils.script2

trait Referable[K <: String | Int]:
  type KeyType = K

  def idProperty: PDXProperty[KeyType]

  def referableID: Option[KeyType] = idProperty()
