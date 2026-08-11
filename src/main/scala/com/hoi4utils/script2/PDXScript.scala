package com.hoi4utils.script2

import com.hoi4utils.parser.{Node, NodeValueType, SeqNode}
import com.sun.tools.javac.resources.ct

import java.io.{File, FileNotFoundException, PrintWriter}
import scala.annotation.targetName
import scala.reflect.{ClassTag, TypeTest}
import scala.util.Using

/**
 * TODO fix documentation
 * PDX = Paradox Interactive Clausewitz Engine Modding/Scripting Language
 * @tparam V
 */
trait PDXScript[T] { //  extends Cloneable

  /** The ClassTag of T (for Property) or the element T (for List) */
  def ct: ClassTag[?]
  /** Shortcut for the loader to get the runtime class */
  def runtimeClass: Class[?] = ct.runtimeClass

  /**
   * Gets the value of the PDXScript.
   *
   * @return Value of the PDXScript, or None if undefined
   */
  def apply(): Option[T]

  def $: T

  def set(value: T): T
  def extractAndSet(nodeValue: NodeValueType): Either[String, Unit]

  //  def toScript: String

//  /**
//   * Compare this PDXScript to another PDXScript.
//   * @param other The other PDXScript to compare to.
//   * @return True if the two PDXScripts are considered equal, false otherwise.
//   */
//  def equals(other: PDXScript[?]): Boolean

  /**
   * Get the value of the PDX script, or the given value if the PDX script is undefined or has an incompatible type.
   *
   * @param elseValue
   * @return
   */
  infix def getOrElse[B >: T](default: => B): B = pdxDefinedValueOption.getOrElse(default)

  /**
   * Get the value of the PDX script, or the given value if the PDX script is undefined or has an incompatible type.
   *
   * @param elseValue
   * @return
   */
  infix def getAndMapOrElse[B >: T](f: T => B, default: => B): B = pdxDefinedValueOption map f getOrElse default

  @targetName("pdxExists")
  infix def exists(p: T => Boolean): Boolean = this() exists p

  /** value is defined */
  def isDefined: Boolean = pdxDefinedValueOption.isDefined

  def pdxDefinedValueOption: Option[T]

  def pdxKey: String

  def decoder: PDXDecoder[T]

  def getEmptyInstance(context: Any): Option[T] =
    decoder.createEmpty(context)

  def load[C](node: SeqNode, context: C,
              loadCallback: (SeqNode, PDXEntity, C) => List[String]): Either[List[String], Unit]

  def display: String = this() map(_.toString) getOrElse "[undefined]"

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

object PDXScript {
//  def allPDXFilesInDirectory(directory: File): List[File] = {
//    if (directory.isFile) List(directory)
//    else directory.listFiles().filter(_.isFile).filter(_.getName.endsWith(".txt")).toList
//  }

//  given stringPDXTypeTest: TypeTest[PDXScript[?], PDXProperty[String]] with
//    override def unapply(s: PDXScript[?]): Option[s.type & PDXProperty[String]] = s match
//      case p: PDXProperty[?] if p.runtimeClass == classOf[String] =>
//        Some(p.asInstanceOf[s.type & PDXProperty[String]])
//      case _ => None

  /** Generic TypeTest for any PDXProperty[T] */
  given pdxPropertyTypeTest[T](using ct: ClassTag[T]): TypeTest[PDXScript[?], PDXProperty[T]] with
    override def unapply(s: PDXScript[?]): Option[s.type & PDXProperty[T]] = s match
      case p: PDXProperty[?] if isMatchingClass(p.runtimeClass, ct.runtimeClass) =>
        Some(p.asInstanceOf[s.type & PDXProperty[T]])
      case _ => None

  /** Normalizes primitives vs boxed wrapper classes (e.g., int.class vs Integer.class) */
  private def isMatchingClass(c1: Class[?], c2: Class[?]): Boolean =
    if c1 == c2 then true
    else
      val normalize = (c: Class[?]) =>
        if c == classOf[java.lang.Integer] then classOf[Int]
        else if c == classOf[java.lang.Double] then classOf[Double]
        else if c == classOf[java.lang.Boolean] then classOf[Boolean]
        else c
      normalize(c1) == normalize(c2)
}


