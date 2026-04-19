package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.*
import com.hoi4utils.script2.* 

import java.io.File
import scala.reflect.ClassTag

class SharedFocusFile(var sharedFocusFileRegistry: Registry[SharedFocusFile], var file: Option[File]) extends PDXEntity with Registry[SharedFocus] with RegistryMember[SharedFocusFile](sharedFocusFileRegistry) {
  val sharedFocuses = pdxList[SharedFocus]("focus") required true 
  
  def fileName: Option[String] = file.map(_.getName)
}

object SharedFocusFile { }

class SharedFocusFileRegistry extends Registry[SharedFocusFile] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
