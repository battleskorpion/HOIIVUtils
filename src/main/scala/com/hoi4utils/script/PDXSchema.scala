package com.hoi4utils.script

// type T is the type we expect the node value to be of. 

class PDXSchema[T](identifiers: String*) {
  if (identifiers.isEmpty) {
    throw new IllegalArgumentException("PDXSchema must have at least one identifier")
  }
  
  def this(identifiers: List[String]) = {
    this(identifiers*)
  }
  
  def this(identifiers: Array[String]) = {
    this(identifiers*)
  }
}
