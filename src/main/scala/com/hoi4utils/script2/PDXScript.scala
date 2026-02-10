package com.hoi4utils.script2

import com.hoi4utils.parser.{Node, NodeValueType}
import com.hoi4utils.script2.PDXFile
import com.typesafe.scalalogging.LazyLogging
import dotty.tools.dotc.semanticdb.TreeMessage.SealedValue.Empty.valueOption

import java.io.{File, FileNotFoundException, PrintWriter}
import java.time.LocalDateTime
import javax.swing.JOptionPane
import scala.util.Using

/**
 * TODO fix documentation
 * PDX = Paradox Interactive Clausewitz Engine Modding/Scripting Language
 * @tparam V
 */
trait PDXScript[T] { //  extends Cloneable

  /**
   * Gets the value of the PDXScript.
   *
   * @return Value of the PDXScript, or None if undefined
   */
  def apply: Option[T]

//  def toScript: String

//  /**
//   * Compare this PDXScript to another PDXScript.
//   * @param other The other PDXScript to compare to.
//   * @return True if the two PDXScripts are considered equal, false otherwise.
//   */
//  def equals(other: PDXScript[?]): Boolean

  /**
   * Get the value of the PDX script, or the given value if the PDX script is undefined or has an incompatible type.
   * @param elseValue
   * @return
   */
  infix def getOrElse(default: T): T = pdxDefinedValueOption.getOrElse(default)

  /** value is defined */
  def isDefined: Boolean = pdxDefinedValueOption.isDefined

  def pdxDefinedValueOption: Option[T]

  def pdxKey: String

//  /**
//   * A custom clone method for PDXScript.
//   *
//   * This performs a shallow clone (via super.clone) and then explicitly resets fields
//   * that should remain shared between the original and the clone (for example, childScripts).
//   */
//  override def clone(): AnyRef = {
//    val cloned = super.clone().asInstanceOf[PDXScript[Value]]
//    cloned
//  }
}

//object PDXScript {
//  def allPDXFilesInDirectory(directory: File): List[File] = {
//    if (directory.isFile) List(directory)
//    else directory.listFiles().filter(_.isFile).filter(_.getName.endsWith(".txt")).toList
//  }
//}
