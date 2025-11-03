package com.hoi4utils.script

trait PDXReadable:
  val cleanName: String = "UnnamedPDXReadable"
  def read(): Boolean
  def clear(): Unit = {}
