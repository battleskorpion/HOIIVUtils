package com.hoi4utils.script2

import zio.{RIO, Task, ZIO}

trait PDXReadable:
  val display: String

  def read(): Task[Boolean]
  def clear(): Task[Unit] = ZIO.unit
