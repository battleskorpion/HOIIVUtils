package com.hoi4utils.script

import zio.{RIO, Task, ZIO}

trait PDXReadable:
  val cleanName: String = "UnnamedPDXReadable"

  def read(): Task[Boolean]
  def clear(): Task[Unit] = ZIO.unit
