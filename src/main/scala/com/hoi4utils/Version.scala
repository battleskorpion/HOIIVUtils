package com.hoi4utils

import java.util.Properties

case class Version(major: Int, minor: Int, patch: Int) extends Ordered[Version] {
  override def compare(that: Version): Int =
    Ordering[(Int, Int, Int)]
      .compare((major, minor, patch), (that.major, that.minor, that.patch))

  override def toString: String = s"$major.$minor.$patch"
}

object Version {
  /** Parses strings like "1.2.3" (and will throw on malformed input) */
  def apply(s: String): Version = {
    if (s == null || s.isEmpty) throw new IllegalArgumentException("Version string cannot be null or empty")
    val Array(a, b, c) = s.split("\\.", 3)
    Version(a.toInt, b.toInt, c.toInt)
  }
  val DEFAULT: Version = Version(0, 0, 0)

  def getVersion(hProperties: Properties): Version = {
    val versionString = hProperties.getProperty("version")
    if (versionString != null && versionString.nonEmpty) {
      try {
        Version(versionString)
      } catch {
        case e: Exception =>
          println(s"Failed to parse version string '$versionString': ${e.getMessage}")
          DEFAULT
      }
    } else {
      DEFAULT
    }
  }
}