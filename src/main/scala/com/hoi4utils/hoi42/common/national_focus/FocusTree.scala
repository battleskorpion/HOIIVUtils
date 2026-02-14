package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{PDXDecoder, Registry}

import scala.reflect.ClassTag

class FocusTree extends Registry[Focus] {
  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
