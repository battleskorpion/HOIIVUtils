package com.hoi4utils

import java.io.File

trait PDXReadable {
  def read(testFile: File = null): Boolean
  
  
  def name: String = {
    this.getClass.getSimpleName.replaceAll("\\$", "")
  }
}