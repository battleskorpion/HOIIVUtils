package com.hoi4utils.extensions

import java.io.File

extension (folder: File)
  def validateFolder(name: String): Either[String, File] =
    Option(folder)
      .toRight(s"'$name' is null.")
      .flatMap: f =>
        if !f.exists then Left(s"'$name' does not exist.")
        else if !f.isDirectory then Left(s"'$name' is not a directory.")
        else Right(f)