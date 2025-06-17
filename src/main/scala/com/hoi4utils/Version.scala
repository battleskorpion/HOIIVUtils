package com.hoi4utils

import java.util.Properties
import javax.swing.JOptionPane

case class Version(major: Int, minor: Int, patch: Int) extends Ordered[Version] {
  override def compare(that: Version): Int =
    Ordering[(Int, Int, Int)]
      .compare((major, minor, patch), (that.major, that.minor, that.patch))

  override def toString: String = s"$major.$minor.$patch"
}
/**
 * Parses strings like "1.2.3" (and will throw on malformed input)
 */
object Version:
  def apply(s: String): Version =
    s match {
      case s: String if s.endsWith(".version}") => throw new IllegalArgumentException("HOIIVUtils.properties Resource was compiled without maven\\n Version string is property name \\n please clean recompile with maven")
      case null => throw new IllegalArgumentException("Version string cannot be null")
      case _ if s.isEmpty => throw new IllegalArgumentException("Version string cannot be empty")
      case _ =>
    }
    val Array(a, b, c) = s.split("\\.", 3)
    Version(a.toInt, b.toInt, c.toInt)

  val DEFAULT: Version = Version(0, 0, 0)

  def getVersion(hProperties: Properties): Version =
    val versionString = hProperties.getProperty("version")
    try Version(versionString)
    catch case e: IllegalArgumentException =>
        println(s"Failed to parse version string '$versionString': ${e.getMessage}")
        JOptionPane.showMessageDialog(null, e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
        DEFAULT