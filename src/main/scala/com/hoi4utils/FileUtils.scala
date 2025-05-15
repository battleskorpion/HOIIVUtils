package com.hoi4utils

import java.io.File

object FileUtils {
  val usersHome = new File(System.getProperty("user.home"))
  val usersDocuments = new File(usersHome + File.separator + "Documents")
  val ProgramFilesX86: File = if (System.getenv("ProgramFiles(x86)") != null) new File(System.getenv("ProgramFiles(x86)"))
  else null
  val steamHOI4LocalPath: String = "Steam" + File.separator + "steamapps" + File.separator + "common" + File.separator + "Hearts of Iron IV"

  /**
   * @param data
   * @return
   */
  def usefulData(data: String): Boolean = {
    if (data.isBlank) return false
    return data.trim.charAt(0) != '#'
  }
}