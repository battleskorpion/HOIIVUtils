package com.hoi4utils

// todo this name sucks 
trait PDXReading {
  def read(): Boolean
  
  def validPropertyName: String = {
    this.getClass.getSimpleName.replaceAll("\\$", "")
  }
}
