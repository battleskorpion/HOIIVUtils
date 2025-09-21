package com.hoi4utils

trait PDXReadable:
  def read(): Boolean
  def name: String = this.getClass.getSimpleName.replaceAll("\\$", "")