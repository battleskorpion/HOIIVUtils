package com.hoi4utils.script2

import zio.{RIO, Task, ZIO}

trait PDXReadable[-R]:
  val display: String

  def read(): RIO[R, Boolean]
  def clear(): Task[Unit] = ZIO.unit

object PDXReadable:
  type Default = PDXReadable[Any]
