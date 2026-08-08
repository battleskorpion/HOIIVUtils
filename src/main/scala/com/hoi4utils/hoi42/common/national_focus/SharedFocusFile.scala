package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.*
import com.hoi4utils.script2.*

import java.io.File
import scala.reflect.ClassTag

class SharedFocusFile(var sharedFocusFileRegistry: Registry[SharedFocusFile], var file: Option[File]) extends PDXEntity
  with FocusRegistry[SharedFocus] with RegistryMember[SharedFocusFile](sharedFocusFileRegistry) with FileReferable {

  given Registry[SharedFocus] = this

  val sharedFocuses = pdxList[SharedFocus]("shared_focus") required true

  def fileName: Option[String] = file.map(_.getName)
}

object SharedFocusFile { }

class SharedFocusFileRegistry extends Registry[SharedFocusFile] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
