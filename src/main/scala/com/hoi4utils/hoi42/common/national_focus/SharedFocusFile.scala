package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.*
import com.hoi4utils.script2.* 

import java.io.File
import scala.reflect.ClassTag

class SharedFocusFile(var sharedFocusFileRegistry: Registry[SharedFocusFile], var f: File) extends PDXEntity with Registry[SharedFocus] with RegistryMember[SharedFocusFile](sharedFocusFileRegistry) {

}
