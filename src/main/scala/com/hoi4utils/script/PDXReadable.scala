package com.hoi4utils.script

trait PDXReadable:
  def read(): Boolean
  def name: String = this.getClass.getSimpleName.replaceAll("\\$", "")
  def clear(): Unit = {}