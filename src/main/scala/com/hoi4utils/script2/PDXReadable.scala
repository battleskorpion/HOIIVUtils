package com.hoi4utils.script2

import zio.{RIO, Task, ZIO}

trait PDXReadable:
  val cleanName: String = "UnnamedPDXReadable"

  def read(): Task[Boolean]
  def clear(): Task[Unit] = ZIO.unit
