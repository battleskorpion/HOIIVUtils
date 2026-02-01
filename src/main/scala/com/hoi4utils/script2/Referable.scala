package com.hoi4utils.script2

trait Referable[V <: String | Int] {
//  type Value = V
  def referableID: Option[V]
}
