package com.hoi4utils.clausewitz.script

import java.io.File

trait PDXFile {
  def getFile: Option[File]
  
  def fileName: String = getFile match {
    case Some(file) => file.getName
    case None => getClass.getSimpleName + ".txt"
  }

}
